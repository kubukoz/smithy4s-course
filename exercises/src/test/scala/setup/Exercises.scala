package setup

import cats.effect.IO
import cats.syntax.all.*
import fs2.Chunk
import org.http4s.EntityEncoder
import org.http4s.HttpApp
import org.http4s.MediaType
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.client.Client
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.*
import smithy4s.Document
import smithy4s.Document.DArray
import smithy4s.Document.DObject
import smithy4s.Endpoint
import smithy4s.Service
import smithy4s.ShapeId
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.json.Json
import smithy4s.schema.Field
import smithy4s.schema.Schema
import smithy4s.schema.Schema.StructSchema
import specs.ep6._
import weaver.*

private[setup] object ExerciseAPI {
  extension [A](e: Either[Throwable, A]) def orThrow: A = e.fold(throw _, identity)

  type PartialImpl = PartialFunction[String, Document => Document]

  def toDynamicIO[A: Schema, B: Schema](
    f: A => B
  ): Document => Document =
    input => {
      val inputDecoded = input.decode[A].orThrow
      Document.encode(f(inputDecoded))
    }

  def toDynamicOutputOnly[B: Schema](f: Document => B): Document => Document =
    input => Document.encode(f(input))

  def forOperation[Op[_, _, _, _, _], I, O](
    op: Endpoint[Op, I, ?, O, ?, ?]
  )(
    f: Document => Document
  ): PartialImpl = {
    case n if n == op.name => f
  }

  def forStaticOperation[Op[_, _, _, _, _], I, O](op: Endpoint[Op, I, ?, O, ?, ?])(f: I => O)
    : PartialImpl =
    forOperation(op) {
      toDynamicIO(f)(
        using op.input,
        op.output,
      )
    }

  def makeExercises(impls: PartialImpl*): Int => PartialImpl =
    // exercise numbers start at 1, but indices start at 0
    impls.compose(_ - 1)

}

trait Exercises extends SimpleIOSuite {

  import ExerciseAPI.*

  case class Ctx private[Exercises] (val exerciseName: ExerciseName)

  type ExerciseName = Int
  val exercises: ExerciseName => PartialImpl

  def exercise(
    name: ExerciseName
  )(
    f: Ctx ?=> IO[Expectations]
  ): Unit =
    test(s"Exercise $name") {
      given Ctx = Ctx(name)
      f
    }

  protected def assertSuccessful(
    req: Request[IO]
  )(
    using ctx: Ctx
  ): IO[Expectations] = assert(successful(req))

  def successful(
    req: Request[IO]
  )(
    using ctx: Ctx
  ): CustomAssertion[RichResponse] = {

    val route =
      SimpleRestJsonBuilder
        .routes(
          makeFakeImpl(
            StudentService,
            exercises(ctx.exerciseName),
          )
        )
        .make
        .orThrow
        .orNotFound

    CustomAssertion.Ass(runRequestAssertOk(route, req))
  }

  def responseBody(
    using r: RichResponse
  ) = r.asDocument

  case class RichResponse(raw: Response[IO], body: String) {

    def asDocument: Document = Json.readDocument(body).orThrow
  }

  extension (doc: Document) {

    def asObject: Map[String, Document] =
      doc.match {
        case DObject(m) => m
        case _ => sys.error("expected object, got: " + Json.writeDocumentAsBlob(doc).toUTF8String)
      }

    def asArray(
      using r: RichResponse
    ): IndexedSeq[Document] =
      doc.match {
        case DArray(value) => value
        case _ => sys.error("expected array, got: " + Json.writeDocumentAsBlob(doc).toUTF8String)
      }

    def isString(
      using RichResponse
    ): CustomAssertion[RichResponse] = CustomAssertion.just {
      assert.same("String", doc.name)
    }

  }

  extension [A](seq: IndexedSeq[A])

    def sizeIs(
      value: Int
    )(
      using RichResponse
    ): CustomAssertion[RichResponse] = CustomAssertion.just(
      assert.same(value, seq.size)
    )

  extension [A](map: Map[String, A])

    def key(
      k: String
    ): A = map(k)

  extension (assert: Expect) {
    def apply(custom: CustomAssertion[?]): IO[Expectations] = custom.eval
  }

  enum CustomAssertion[Extras] {
    case Ass[O](e: IO[(Expectations, O)]) extends CustomAssertion[O]
    case And[L, R](lhs: CustomAssertion[L], rhs: L => CustomAssertion[R]) extends CustomAssertion[R]

    def &&[O](another: Extras ?=> CustomAssertion[O]): CustomAssertion[O] = And(
      this,
      another(
        using _
      ),
    )

    def eval: IO[Expectations] = evalFull.map(_._1)

    private def evalFull: IO[(Expectations, Extras)] =
      this match {
        case Ass(e) => e
        case And(first, second) =>
          first
            .evalFull
            // Important: we fail fast to avoid dealing with non-successful responses
            .flatTap(_._1.failFast[IO])
            .map(_._2)
            .flatMap {
              second(_).evalFull
            }
      }

  }

  object CustomAssertion {

    def just[E](
      check: Expectations
    )(
      using E
    ): CustomAssertion[E] = justIO(IO.pure(check))

    def justIO[E](
      check: IO[Expectations]
    )(
      using E
    ): CustomAssertion[E] = Ass(check.tupleRight(summon[E]))

  }

  def json(s: String): Document = Json.readDocument(s.trim).orThrow

  given EntityEncoder[IO, Document] =
    EntityEncoder.simple(`Content-Type`(MediaType.application.json))(doc =>
      Chunk.byteBuffer(Json.writeDocumentAsBlob(doc).asByteBuffer)
    )

  private def runRequestAssertOk(
    route: HttpApp[IO],
    request: Request[IO],
  ): IO[(Expectations, RichResponse)] = Client
    .fromHttpApp(route)
    .run(request)
    .use(_.toStrict(None))
    .flatMap { response =>
      response
        .bodyText
        .compile
        .string
        .flatMap { body =>
          response
            .status
            .match {
              case Status.Ok => IO.pure(success)
              case other     => IO.pure(failure(s"Expected 200, got $other. Body: ${body}"))
            }
            .tupleRight(RichResponse(response, body))
        }
    }

  extension [A](s: Schema[A]) {

    def structFields: Vector[Field[A, ?]] =
      s match {
        case s: StructSchema[_] => s.fields
        case other              => sys.error(s"Expected a struct, got $other instead")
      }

    def firstFieldId: ShapeId = structFields.head.schema.shapeId
  }

  private def makeFakeImpl[Alg[_[_, _, _, _, _]]](
    service: Service[Alg],
    output: PartialFunction[String, Document => Document],
  ): service.Impl[IO] = service
    .impl(
      new service.FunctorEndpointCompiler[IO] {
        override def apply[I, E, O, SI, SO](endpoint: service.Endpoint[I, E, O, SI, SO])
          : I => IO[O] = {
          val inputEncoder = Document.Encoder.fromSchema(endpoint.input)
          val docDecoder = Document.Decoder.fromSchema(endpoint.output)

          input => {
            val expectedOutputDoc = output
              .applyOrElse(
                endpoint.name,
                _ =>
                  sys.error(
                    "This endpoint should not be called in this exercise! This might be a bug in your solution or the exercise itself."
                  ),
              )
              .apply(inputEncoder.encode(input))

            docDecoder.decode(expectedOutputDoc).liftTo[IO]
          }
        }
      }
    )

}

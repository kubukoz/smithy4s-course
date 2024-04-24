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
import smithy4s.Document.DString
import smithy4s.Service
import smithy4s.ShapeId
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.json.Json
import smithy4s.schema.Field
import smithy4s.schema.Schema
import smithy4s.schema.Schema.StructSchema
import specs.ep6._
import weaver.*

import scala.util.Random

trait Ep6Exercises extends Exercises {

  def makeDetails(exerciseName: String): PartialImpl =
    exerciseName match {
      case "Exercise 1" => { case StudentServiceOperation.GetStudent.schema.id.name =>
        input => {
          val paramValue =
            input match {
              case DObject(value) if value.sizeIs == 1 =>
                value.head._2 match {
                  case DString(value) => value
                  case other =>
                    sys.error(
                      s"Expected a string, but got $other (${GetStudentInput.schema.firstFieldId})"
                    )
                }

              case DObject(v) =>
                sys.error(
                  s"The operation had ${v.size} inputs, but it should have had 1! Did you cheat? ;)"
                )

              case _ => sys.error("shouldn't be possible: operation inputs are structs")
            }

          Document.encode(GetStudentOutput(name = "Output for input " + paramValue))
        }
      }

      case "Exercise 2" => { case StudentServiceOperation.CreateStudent.schema.id.name =>
        input =>
          Document.encode(
            CreateStudentOutput(
              id = Random.nextInt().show,
              name = input.decode[CreateStudentInput].orThrow.name,
            )
          )
      }

      case "Exercise 3" => { case StudentServiceOperation.ListStudents.schema.id.name =>
        input =>
          val realLimit = input
            .decode[ListStudentsInput]
            .toTry
            .get
            .maxStudents
            .getOrElse(20)

          val students = ListStudentsOutput(
            List.fill(realLimit)(Student(name = s"Student ${Random.nextInt()}"))
          )

          Document.encode(students)
      }

      case "Exercise 4" => { case StudentServiceOperation.ListClasses.schema.id.name =>
        _ =>
          Document.encode(
            ListClassesOutput(
              List(Class("Chemistry", "Maya"), Class("Music", "Eve"))
            )
          )
      }
    }

}

trait Exercises extends SimpleIOSuite {

  type PartialImpl = PartialFunction[String, Document => Document]
  case class Ctx private[Exercises] (val exerciseName: String)

  def makeDetails(exerciseName: String): PartialImpl

  def exercise(
    name: TestName
  )(
    f: Ctx ?=> IO[Expectations]
  ): Unit =
    test(name) {
      given Ctx = Ctx(name.name)
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
            makeDetails(ctx.exerciseName),
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

    def asObject(
      using r: RichResponse
    ): Map[String, Document] =
      doc.match {
        case DObject(m) => m
        case _          => sys.error("expected object, got: " + r.body)
      }

    def asArray(
      using r: RichResponse
    ): IndexedSeq[Document] =
      doc.match {
        case DArray(value) => value
        case _             => sys.error("expected array, got: " + r.body)
      }

  }

  extension [A](seq: IndexedSeq[A])

    def sizeIs(
      value: Int
    )(
      using RichResponse
    ): CustomAssertion[RichResponse] = CustomAssertion.Ass(
      IO(
        (assert.same(seq.size, value), summon[RichResponse])
      )
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

  extension [A](e: Either[Throwable, A]) def orThrow: A = e.fold(throw _, identity)

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

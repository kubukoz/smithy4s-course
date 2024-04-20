package smithy4sdemo

import cats.data.EitherT
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.syntax.all.*
import hello.City
import hello.CityId
import hello.CityNotFoundError
import hello.CreateCityOutput
import hello.GetWeatherOutput
import hello.InternalServerError
import hello.WeatherService
import hello.WeatherServiceGen.GetWeatherError
import hello.WeatherServiceOperation.CreateCityError
import hello.WeatherServiceOperation.GetWeather
import org.http4s.HttpRoutes
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import smithy4s.Transformation
import smithy4s.http.MetadataError
import smithy4s.http4s.SimpleRestJsonBuilder

object ServerMain extends IOApp.Simple {

  type Result[+E, +A] = IO[Either[E, A]]

  val impl: WeatherService[IO] =
    new WeatherService.ErrorAware[Result] {

      def getWeather(
        cityId: CityId,
        region: String,
      ): Result[GetWeatherError, GetWeatherOutput] =
        if cityId.value == "London" then
          IO.println(s"getWeather($cityId)") *>
            IO.pure(
              Right {
                GetWeatherOutput(
                  weather = "Good weather",
                  city = Some(
                    City(
                      name = "London",
                      id = cityId,
                    )
                  ),
                )
              }
            )
        // else IO.raiseError(new java.lang.IllegalArgumentException("whatever"))
        else IO.pure(Left(GetWeatherError.cityNotFoundError(CityNotFoundError())))

      def createCity(
        city: String,
        country: String,
      ): Result[Nothing, CreateCityOutput] =
        IO.println(s"createCity($city, $country)") *>
          IO.pure(Right(CreateCityOutput(CityId("123"))))

    }.transform(
      new Transformation.AbsorbError[Result, IO] {
        def apply[E, A](fa: Result[E, A], injectError: E => Throwable): IO[A] = fa.flatMap {
          case Left(e)  => IO.raiseError(injectError(e))
          case Right(a) => IO.pure(a)
        }
      }
    )

  def run: IO[Unit] =
    SimpleRestJsonBuilder
      .routes(impl)
      .flatMapErrors {
        case e if !e.isInstanceOf[MetadataError] =>
          // logger.error(e) *>
          IO.pure(InternalServerError(Some("hidden message")))
      }
      .resource
      .flatMap { routes =>
        EmberServerBuilder
          .default[IO]
          .withHttpApp(routes.orNotFound)
          .build
      }
      .evalMap(srv => IO.println(srv.addressIp4s))
      .useForever

}

object ClientMain extends IOApp.Simple {

  type Result[+E, +A] = IO[Either[E, A]]

  val surface =
    new Transformation.SurfaceError[IO, Result] {
      def apply[E, A](fa: IO[A], projectError: Throwable => Option[E]): Result[E, A] = fa
        .map(Right(_))
        .recoverWith {
          case e if projectError(e).isDefined => IO.pure(Left(projectError(e).get))
          case e                              => IO.raiseError(e)
        }
    }

  def run: IO[Unit] = EmberClientBuilder.default[IO].build.use { c =>
    SimpleRestJsonBuilder(WeatherService)
      .client(c)
      .resource
      .map(_.transform(surface))
      .use { client =>
        EitherT(client.createCity("123", "UK"))
          .flatMap { output =>
            EitherT(
              client
                .getWeather(output.cityId, "region")
            )
              .recoverWith {
                case GetWeatherError.CityNotFoundErrorCase(CityNotFoundError(reason)) =>
                  EitherT.liftF(IO.println(s"CNFE: $reason"))
              }
          }
          .value
          .flatMap(IO.println(_))
      }
  }

}

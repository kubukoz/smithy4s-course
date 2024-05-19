package smithy4sdemo

import cats.effect.IO
import cats.effect.IOApp
import org.http4s.ember.client.EmberClientBuilder

object Main extends IOApp.Simple {

  def run: IO[Unit] = EmberClientBuilder.default[IO].build.use { c =>
    ???
  }

}

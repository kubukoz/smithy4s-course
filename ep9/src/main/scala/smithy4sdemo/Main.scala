package smithy4sdemo

import cats.effect.IO
import cats.effect.IOApp
import com.amazonaws.translate.BoundedLengthString
import com.amazonaws.translate.LanguageCodeString
import com.amazonaws.translate.ResourceName
import com.amazonaws.translate.Translate
import com.amazonaws.translate.TranslateOperation.TranslateTextError
import com.amazonaws.translate.TranslateTextRequest
import com.amazonaws.translate.TranslateTextResponse
import com.amazonaws.translate.TranslationSettings
import org.http4s.ember.client.EmberClientBuilder
import smithy4s.aws.AwsClient
import smithy4s.aws.AwsEnvironment
import smithy4s.aws.kernel.AwsRegion
import smithy4s.kinds.stubs.Kind1

object Main extends IOApp.Simple {

  def run: IO[Unit] = EmberClientBuilder.default[IO].build.use { c =>
    AwsEnvironment.default(c, AwsRegion.US_EAST_1).use { env =>
      //
      AwsClient(Translate, env).use { translateService =>
        translateService
          .translateText(
            text = BoundedLengthString("I love my subscribers!"),
            sourceLanguageCode = LanguageCodeString("en"),
            targetLanguageCode = LanguageCodeString("pl"),
          )
          .flatMap { response =>
            IO.println(response.translatedText.value)
          }
      }

    }
  }

}

import sjsonnew.BasicJsonProtocol.StringJsonFormat
import sjsonnew.BasicJsonProtocol.listFormat

val fetchAwsSpecsTarget = settingKey[File]("The target directory to fetch AWS spec files to")
val fetchAwsSpecs = taskKey[Seq[File]]("Fetch AWS spec files")

val smithyModels = project
  .settings(
    scalaVersion := "3.4.2",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-aws-kernel" % smithy4sVersion.value
    ),
    fetchAwsSpecsTarget := (Compile / sourceManaged).value / "smithy",
    fetchAwsSpecs := {
      val s = streams.value
      val inputs = List(
        "https://raw.githubusercontent.com/aws/aws-sdk-js-v3/b06a8b7d9d2a0ca2fc73acdbf37220575b0f1945/codegen/sdk-codegen/aws-models/location.json"
      )

      val cached =
        Tracked.inputChanged[List[String], Seq[File]](
          s.cacheStoreFactory.make("input")
        ) {
          Function.untupled {
            Tracked.lastOutput[(Boolean, List[String]), Seq[File]](
              s.cacheStoreFactory.make("output")
            ) { case ((inputChanged, args), outputs) =>
              if (inputChanged || outputs.isEmpty) {
                fetchAwsSpecsTarget.value.mkdirs()
                import scala.sys.process._

                args.foreach { p =>
                  (url(p) #> (fetchAwsSpecsTarget.value / p.split("/").last)).!;
                }

                // hack
                Nil
              } else {
                outputs.getOrElse(Seq.empty)
              }
            }
          }
        }

      cached(inputs)
    },
    (Compile / sourceGenerators) := (fetchAwsSpecs.taskValue +: (Compile / sourceGenerators).value),
    libraryDependencies ++= Seq(
      "smithy-aws-traits",
      "smithy-aws-cloudformation-traits",
      "smithy-aws-iam-traits",
      "smithy-waiters",
      "smithy-rules-engine",
      "smithy-aws-endpoints",
    ).map { art =>
      smithy4s.codegen.BuildInfo.smithyOrg % art % smithy4s
        .codegen
        .BuildInfo
        .smithyVersion % Smithy4s,
    },
  )
  .enablePlugins(Smithy4sCodegenPlugin)

val root = project
  .in(file("."))
  .dependsOn(smithyModels)
  .aggregate(smithyModels)
  .settings(
    scalaVersion := "3.4.2",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % "0.23.27",
      "com.disneystreaming.smithy4s" %% "smithy4s-aws-http4s" % smithy4sVersion.value,
    ),
    fork := true,
    scalacOptions += "-Wunused:imports",
  )

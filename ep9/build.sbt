val smithyModels = project
  .settings(
    scalaVersion := "3.4.2",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-aws-kernel" % smithy4sVersion.value
    ),
    smithy4sAwsSpecs += AWS.translate,
  )
  .enablePlugins(Smithy4sCodegenPlugin)

val root = project
  .in(file("."))
  .dependsOn(smithyModels)
  .settings(
    scalaVersion := "3.4.2",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % "0.23.27",
      "com.disneystreaming.smithy4s" %% "smithy4s-aws-http4s" % smithy4sVersion.value,
    ),
    fork := true,
    scalacOptions += "-Wunused:imports",
  )

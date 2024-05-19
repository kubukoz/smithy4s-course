val root = project
  .in(file("."))
  .settings(
    scalaVersion := "3.4.2",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % "0.23.27",
      "org.http4s" %% "http4s-ember-client" % "0.23.27",
    ),
    fork := true,
    scalacOptions += "-Wunused:imports",
  )
  .enablePlugins(Smithy4sCodegenPlugin)

import complete.DefaultParsers._

lazy val check = inputKey[Unit]("Check task")

val root = project
  .in(file("."))
  .settings(
    scalaVersion := "3.3.3",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "org.http4s" %% "http4s-ember-server" % "0.23.26",
      "org.http4s" %% "http4s-ember-client" % "0.23.26",
      "org.http4s" %% "http4s-dsl" % "0.23.26",
    ),
    fork := true,
    libraryDependencies += "com.disneystreaming" %% "weaver-cats" % "0.8.3" % Test,
    Test / fork := true,
    scalacOptions -= "-Xfatal-warnings",
    check := {
      val args: Seq[String] = spaceDelimited("<arg>").parsed
      require(args.size < 2, "Too many arguments! Only zero or one are valid.")

      args.headOption match {
        case None => (Test / test).value
        case Some(tn) if tn.startsWith("ep") =>
          val suiteName = s"Ep${tn.drop(2)}Tests"
          (Test / testOnly).inputTaskValue.fullInput(suiteName)
      }
    },
  )
  .enablePlugins(Smithy4sCodegenPlugin)

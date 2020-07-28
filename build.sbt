name := "YFC"

version := "1.0"

lazy val `ymc` = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  ehcache , ws , specs2 % Test , guice,
  "org.webjars" % "swagger-ui" % "3.24.3",
  "org.postgresql" % "postgresql" % "42.2.14",
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "org.flywaydb" % "flyway-core" % "6.1.4",
  "org.flywaydb" %% "flyway-play" % "6.0.0",
  "com.typesafe.play" %% "play-mailer" % "8.0.0",
  "com.typesafe.play" %% "play-mailer-guice" % "8.0.0",
  "io.monix" %% "monix" % "3.2.2",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.1",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "org.scalamock" %% "scalamock" % "4.4.0" % Test,
  "org.reactivemongo" %% "play2-reactivemongo" % "1.0.0-play27-rc.2"
)

swaggerDomainNameSpaces := Seq("model")

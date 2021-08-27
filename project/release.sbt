addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % "1.0.1")

val mdocVersion = "2.2.23"
addSbtPlugin("org.scalameta" % "sbt-mdoc" % mdocVersion)
libraryDependencies ++= Seq(
  "org.scalameta" %% "mdoc" % mdocVersion,
)

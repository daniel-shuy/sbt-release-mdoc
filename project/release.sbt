addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % "0.2.0")

val mdocVersion = "2.0.0"
addSbtPlugin("org.scalameta" % "sbt-mdoc" % mdocVersion)
libraryDependencies ++= Seq(
  "org.scalameta" %% "mdoc" % mdocVersion,
)

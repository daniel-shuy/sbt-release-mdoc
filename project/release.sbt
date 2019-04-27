addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % "0.0.1")

val mdocVersion = "1.2.10"
addSbtPlugin("org.scalameta" % "sbt-mdoc" % mdocVersion)
libraryDependencies ++= Seq(
  "org.scalameta" %% "mdoc" % mdocVersion,
)

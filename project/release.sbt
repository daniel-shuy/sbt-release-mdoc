addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % "0.1.1")

val mdocVersion = "1.3.1"
addSbtPlugin("org.scalameta" % "sbt-mdoc" % mdocVersion)
libraryDependencies ++= Seq(
  "org.scalameta" %% "mdoc" % mdocVersion,
)

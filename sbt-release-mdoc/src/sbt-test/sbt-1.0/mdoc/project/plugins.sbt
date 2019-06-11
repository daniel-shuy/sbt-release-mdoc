sys.props.get("plugin.version") match {
  case Some(pluginVersion) =>
    addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % pluginVersion)
  case _ =>
    sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

addSbtPlugin("com.github.daniel-shuy" % "sbt-scripted-scalatest" % "1.1.1")
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.7"

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "5.3.0.201903130848-r",
  "com.github.pathikrit" %% "better-files" % "3.8.0",
)

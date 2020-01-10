sys.props.get("plugin.version") match {
  case Some(pluginVersion) =>
    addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % pluginVersion)
  case _ =>
    sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

useCoursier := false

addSbtPlugin("com.github.daniel-shuy" % "sbt-scripted-scalatest" % "2.0.0")
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0"

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "5.6.0.201912101111-r",
  "com.github.pathikrit" %% "better-files" % "3.8.0",
)

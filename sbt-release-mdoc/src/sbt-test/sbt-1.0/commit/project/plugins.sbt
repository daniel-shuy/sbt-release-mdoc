sys.props.get("plugin.version") match {
  case Some(pluginVersion) =>
    addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % pluginVersion)
  case _ =>
    sys.error("""|The system property 'plugin.version' is not defined.
                 |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

addSbtPlugin("com.github.daniel-shuy" % "sbt-scripted-scalatest" % "2.0.0")
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11"

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "5.13.0.202109080827-r",
  "com.github.pathikrit" %% "better-files" % "3.9.1",
)

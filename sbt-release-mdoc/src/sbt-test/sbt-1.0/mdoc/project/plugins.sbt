sys.props.get("plugin.version") match {
  case Some(pluginVersion) =>
    addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % pluginVersion)
  case _ =>
    sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}

addSbtPlugin("com.github.daniel-shuy" % "sbt-scripted-scalatest" % "2.0.0")
libraryDependencies += "org.scalatest" %% "scalatest" % "3.3.0-SNAP2"

libraryDependencies ++= Seq(
  "org.eclipse.jgit" % "org.eclipse.jgit" % "5.7.0.202003110725-r",
  "com.github.pathikrit" %% "better-files" % "3.8.0",
)

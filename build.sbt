lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    organization := "com.github.daniel-shuy",

    name := "sbt-release-mdoc",

    licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),

    homepage := Some(url("https://github.com/daniel-shuy/sbt-release-mdoc")),

    crossSbtVersions := Seq(
      "1.2.8"
    ),
  )

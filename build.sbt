import ReleaseTransformations._
import com.github.daniel.shuy.sbt.release.mdoc.ReleaseMdocStateTransformations

val sbtReleaseVersion = "1.0.11"
val mdocVersion = "1.0.0"

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin, MdocPlugin)
  .settings(
    organization := "com.github.daniel-shuy",
    name := "sbt-release-mdoc",
    licenses := Seq(
      "Apache License, Version 2.0" -> url(
        "http://www.apache.org/licenses/LICENSE-2.0.txt",
      ),
    ),
    homepage := Some(url("https://github.com/daniel-shuy/sbt-release-mdoc")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/daniel-shuy/sbt-release-mdoc"),
        "git@github.com:daniel-shuy/sbt-release-mdoc.git",
      ),
    ),
    developers := List(
      Developer(
        "daniel-shuy",
        "Daniel Shuy",
        "daniel_shuy@hotmail.com",
        url("https://github.com/daniel-shuy"),
      ),
    ),
    crossSbtVersions := Seq("1.2.8"),
    addSbtPlugin("com.github.gseitz" % "sbt-release" % sbtReleaseVersion),
    addSbtPlugin("org.scalameta" % "sbt-mdoc" % mdocVersion),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "mdoc" % mdocVersion,
    ),
    // scripted test settings
    scriptedLaunchOpts := scriptedLaunchOpts.value ++ Seq(
      "-Xmx1024M",
      "-Dplugin.version=" + version.value,
    ),
    scriptedBufferLog := false,
    // sbt-mdoc settings
    mdocOut := baseDirectory.in(ThisBuild).value,
    mdocVariables := Map(
      "ORGANIZATION" -> organization.value,
      "ARTIFACT_NAME" -> name.value,
      "VERSION" -> version.value,
      "SBT_RELEASE_VERSION" -> sbtReleaseVersion,
      "MDOC_VERSION" -> mdocVersion,
    ),
    // sbt-bintray settings
    publishMavenStyle := false,
    bintrayRepository := "sbt-plugins",
    bintrayPackageLabels := Seq(
      "sbt-plugin",
      "sbt-release",
      "mdoc",
      "sbt-mdoc",
    ),
    bintrayReleaseOnPublish := false,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("^ test"),
      // When running scripted tests targeting multiple SBT versions, we must first publish locally for all SBT versions
      releaseStepCommandAndRemaining("^ publishLocal"),
      releaseStepCommandAndRemaining("^ scripted"),
      setReleaseVersion,
      commitReleaseVersion,
      ReleasePlugin.autoImport.releaseStepInputTask(MdocPlugin.autoImport.mdoc),
      ReleaseMdocStateTransformations.commitMdoc,
      // don't tag, leave it to git flow
      // tagRelease,
      releaseStepCommandAndRemaining("^ publish"),
      releaseStepTask(bintrayRelease),
      setNextVersion,
      commitNextVersion,
      pushChanges,
    ),
    // skip Travis CI build
    releaseMdocCommitMessage := s"[ci skip] ${releaseMdocCommitMessage.value}",
    releaseCommitMessage := s"[ci skip] ${releaseCommitMessage.value}",
  )

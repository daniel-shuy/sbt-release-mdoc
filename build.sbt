import ReleaseTransformations._
import com.github.daniel.shuy.sbt.release.mdoc.ReleaseMdocStateTransformations

val sbtReleaseVersion = "1.0.13"
val mdocVersion = "2.5.2"

ThisBuild / organization := "com.github.daniel-shuy"
ThisBuild / name := "sbt-release-mdoc"
ThisBuild / licenses := Seq(
  "Apache License, Version 2.0" -> url(
    "http://www.apache.org/licenses/LICENSE-2.0.txt",
  ),
)
ThisBuild / homepage := Some(
  url("https://github.com/daniel-shuy/sbt-release-mdoc"),
)
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/daniel-shuy/sbt-release-mdoc"),
    "git@github.com:daniel-shuy/sbt-release-mdoc.git",
  ),
)
ThisBuild / developers := List(
  Developer(
    "daniel-shuy",
    "Daniel Shuy",
    "daniel_shuy@hotmail.com",
    url("https://github.com/daniel-shuy"),
  ),
)

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true,
    // sbt-bintray settings
    bintrayPackage := "sbt-release-mdoc",
    bintrayRepository := "sbt-plugins",
    bintrayPackageLabels := Seq(
      "sbt-plugin",
      "sbt-release",
      "mdoc",
      "sbt-mdoc",
    ),
    releaseIgnoreUntrackedFiles := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
    ),
    releaseProcess ++= releaseStepScopedReleaseAndRemaining(
      sbtReleaseMdoc,
    ).toSeq,
    releaseProcess ++= Seq[ReleaseStep](
      /*
      // execute scalafmt in Travis CI build instead
      releaseStepTask(scalafmtSbtCheck),
      releaseStepTask(scalafmtCheckAll),
       */
      setReleaseVersion,
      commitReleaseVersion,
    ),
    releaseProcess ++= releaseStepScopedReleaseAndRemaining(docs).toSeq,
    releaseProcess ++= Seq[ReleaseStep](
      // don't tag, leave it to git flow
      // tagRelease,
      releaseStepCommandAndRemaining("^ publish"),
      releaseStepTask(bintrayRelease),
      setNextVersion,
      commitNextVersion,
      pushChanges,
    ),
    // skip Travis CI build
    releaseCommitMessage := s"[ci skip] ${releaseCommitMessage.value}",
  )
  .aggregate(sbtReleaseMdoc, docs)

lazy val sbtReleaseMdoc = project
  .in(file("sbt-release-mdoc"))
  .settings(
    moduleName := (ThisBuild / name).value,
    crossSbtVersions := Seq("1.2.8"),
    // mdoc must be declared before sbt-mdoc due to package/class name conflict (mdoc.Main)
    // The compiler looks up classes in the build classpath order
    libraryDependencies ++= Seq(
      "org.scalameta" %% "mdoc" % mdocVersion,
    ),
    addSbtPlugin("org.scalameta" % "sbt-mdoc" % mdocVersion),
    addSbtPlugin("com.github.gseitz" % "sbt-release" % sbtReleaseVersion),
    // scripted test settings
    scriptedLaunchOpts := scriptedLaunchOpts.value ++ Seq(
      "-Xmx1024M",
      "-Dplugin.version=" + version.value,
    ),
    scriptedBufferLog := false,
    // sbt-bintray settings
    publishMavenStyle := false,
    bintrayReleaseOnPublish := false,
    releaseProcess := Seq[ReleaseStep](
      /*
      // run tests in Travis CI build instead
      runClean,
      releaseStepCommandAndRemaining("^ test"),
      // When running scripted tests targeting multiple SBT versions, we must first publish locally for all SBT versions
      releaseStepCommandAndRemaining("^ publishLocal"),
      releaseStepInputTask(scripted),
       */
    ),
  )
  .enablePlugins(SbtPlugin)

lazy val docs = project
  .in(file("mdoc"))
  .settings(
    skip in publish := true,
    mdocOut := (ThisBuild / baseDirectory).value,
    mdocVariables := Map(
      "ORGANIZATION" -> organization.value,
      "ARTIFACT_NAME" -> (ThisBuild / name).value,
      "VERSION" -> version.value,
      "MDOC_VERSION" -> mdocVersion,
    ),
    libraryDependencies ++= Seq(
      "org.scala-sbt" % "sbt" % sbtVersion.value,
    ),
    releaseProcess := Seq[ReleaseStep](
      ReleasePlugin.autoImport.releaseStepInputTask(MdocPlugin.autoImport.mdoc),
      ReleaseMdocStateTransformations.commitMdoc,
    ),
    // skip Travis CI build
    releaseMdocCommitMessage := s"[ci skip] ${releaseMdocCommitMessage.value}",
  )
  .dependsOn(sbtReleaseMdoc)
  .enablePlugins(MdocPlugin)

def scopedCommand(
    project: Project,
    command: Command,
): Option[State => String] = {
  command.nameOption.map(commandName => {
    state: State => {
      val extracted = Project.extract(state)
      val projectName = extracted.get(project / name)
      s";project $projectName; $commandName; project ${extracted.currentProject.id}"
    },
  })
}

def releaseStepScopedReleaseAndRemaining(
    project: Project,
): Option[State => State] = {
  scopedCommand(
    project,
    ReleaseKeys.releaseCommand,
  ).map(stateToCommand => {
    state: State => {
      val command = stateToCommand.apply(state)
      releaseStepCommandAndRemaining(command)
        .apply(state)
    },
  })
}

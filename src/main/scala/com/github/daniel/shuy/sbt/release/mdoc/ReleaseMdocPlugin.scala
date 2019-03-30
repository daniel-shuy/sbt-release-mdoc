package com.github.daniel.shuy.sbt.release.mdoc

import mdoc.MdocPlugin
import sbt.{AutoPlugin, Def, taskKey}
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseProcess}
import sbtrelease.ReleaseStateTransformations._

object ReleaseMdocPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = ReleasePlugin && MdocPlugin

  object autoImport {
    val releaseMdocCommitMessage =
      taskKey[String]("The commit message to use when committing mdoc")
  }
  import autoImport._

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    releaseMdocCommitMessage := "Update documentation",
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      ReleasePlugin.autoImport.releaseStepInputTask(MdocPlugin.autoImport.mdoc),
      ReleaseMdocStateTransformations.commitMdoc,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    ),
  )
}

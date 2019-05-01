package com.github.daniel.shuy.sbt.release.mdoc

import com.github.daniel.shuy.sbt.release
import mdoc.MdocPlugin
import sbt.{AutoPlugin, Def, settingKey}
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseProcess}
import sbtrelease.ReleaseStateTransformations._

object ReleaseMdocPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = ReleasePlugin && MdocPlugin

  object autoImport {
    val releaseMdocCommitMessage = settingKey[String](
      "The commit message to use when committing mdoc output files",
    )

    lazy val ReleaseMdocStateTransformations
        : release.mdoc.ReleaseMdocStateTransformations.type =
      release.mdoc.ReleaseMdocStateTransformations
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
      autoImport.ReleaseMdocStateTransformations.commitMdoc,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges,
    ),
  )
}

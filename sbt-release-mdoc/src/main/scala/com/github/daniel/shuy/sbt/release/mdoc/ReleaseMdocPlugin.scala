package com.github.daniel.shuy.sbt.release.mdoc

import com.github.daniel.shuy.sbt.release
import mdoc.MdocPlugin
import sbt.Keys.name
import sbt.{AutoPlugin, Command, Def, Project, State, settingKey}
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.{
  ReleaseKeys,
  ReleaseStep,
  releaseProcess,
  releaseStepCommandAndRemaining,
}
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

    /**
      * Obtain a Command String scoped to the given SBT project.
      *
      * @param project The SBT project.
      * @param command The SBT command.
      * @return A scoped Command String.
      */
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

    /**
      * Convert the release Command for the given SBT project to a release step action,
      * preserving and invoking remaining commands
      *
      * @param project The SBT project.
      * @return A release step action.
      */
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

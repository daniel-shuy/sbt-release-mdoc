package com.github.daniel.shuy.sbt.release.mdoc

import mdoc.MdocPlugin
import sbt.{IO, State}
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseVcs}
import sbtrelease.Utilities._
import sbtrelease.{ReleasePlugin, ReleaseStateTransformations, Vcs}

import scala.language.postfixOps
import scala.sys.process.ProcessLogger

object ReleaseMdocStateTransformations {

  /**
    * [[ReleaseStep]] to commit mdoc changes.
    * @see [[ReleaseStateTransformations.commitNextVersion]]
    */
  lazy val commitMdoc = ReleaseStep(st => {
    val log = toProcessLogger(st)
    val directory =
      st.extract.get(MdocPlugin.autoImport.mdocIn).getCanonicalFile
    val base = vcs(st).baseDir.getCanonicalFile
    val sign = st.extract.get(ReleasePlugin.autoImport.releaseVcsSign)
    val signOff = st.extract.get(ReleasePlugin.autoImport.releaseVcsSignOff)
    val relativePath = IO
      .relativize(base, directory)
      .getOrElse(
        "mdoc input directory [%s] is outside of this VCS repository with base directory [%s]!" format (directory, base))

    vcs(st).add(relativePath) !! log
    val status = (vcs(st).status !!) trim

    val newState = if (status.nonEmpty) {
      val (state, msg) =
        st.extract
          .runTask(ReleaseMdocPlugin.autoImport.releaseMdocCommitMessage, st)
      vcs(state).commit(msg, sign, signOff) ! log
      state
    } else {
      // nothing to commit. this happens if the mdoc input directory hasn't changed.
      st
    }
    newState
  })

  /**
    * @see [[ReleaseStateTransformations]]`.toProcessLogger(State)`
    */
  protected def toProcessLogger(st: State): ProcessLogger = new ProcessLogger {
    override def err(s: => String): Unit = st.log.info(s)
    override def out(s: => String): Unit = st.log.info(s)
    override def buffer[T](f: => T): T = st.log.buffer(f)
  }

  /**
    * @see [[ReleaseStateTransformations]]`.vcs(State)`
    */
  protected def vcs(st: State): Vcs = {
    st.extract
      .get(releaseVcs)
      .getOrElse(sys.error(
        "Aborting release. Working directory is not a repository of a recognized VCS."))
  }
}

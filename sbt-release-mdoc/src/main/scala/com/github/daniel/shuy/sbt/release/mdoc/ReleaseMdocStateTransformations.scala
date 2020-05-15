package com.github.daniel.shuy.sbt.release.mdoc

import mdoc.MdocPlugin
import mdoc.internal.cli.{MainOps, MdocProperties, Settings}
import sbt.Keys.{classDirectory, dependencyClasspath, scalacOptions}
import sbt.{Compile, File, Project, State}
import sbtrelease.ReleasePlugin.autoImport.{ReleaseStep, releaseVcs}
import sbtrelease.Utilities._
import sbtrelease.{ReleasePlugin, ReleaseStateTransformations, Vcs}

import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.meta.internal.io.PathIO
import scala.meta.io.AbsolutePath
import scala.sys.process.ProcessLogger

object ReleaseMdocStateTransformations {

  /**
    * [[ReleaseStep]] to commit mdoc changes.
    * @see [[ReleaseStateTransformations.commitNextVersion]]
    * @see [[MainOps.generateCompleteSite()]]
    * @see [[MdocPlugin.projectSettings]]
    * @see [[MdocProperties.fromProps()]]
    * @see [[Settings.default()]]
    */
  lazy val commitMdoc = ReleaseStep(action = st => {
    val classpath = ListBuffer.empty[File]
    val compileDependencyClasspath =
      Project.runTask(dependencyClasspath.in(Compile), st)
    compileDependencyClasspath
      .map(_._2.toEither)
      .map(_.map(_.iterator.map(_.data)))
      .foreach(_.foreach(classpath ++= _))
    classpath += st.extract.get(classDirectory.in(Compile))

    val propsDefaultScalacOptions = MdocProperties(
      classpath = classpath.mkString(java.io.File.pathSeparator),
      site = st.extract.get(MdocPlugin.autoImport.mdocVariables),
      in = Option(AbsolutePath(st.extract.get(MdocPlugin.autoImport.mdocIn))),
      out = Option(AbsolutePath(st.extract.get(MdocPlugin.autoImport.mdocOut))),
    )
    val compileScalacOptions = Project.runTask(scalacOptions.in(Compile), st)
    val props = compileScalacOptions
      .map(_._2.toEither)
      .map(_.map(_.mkString(" ")))
      .map(
        _.map(options =>
          propsDefaultScalacOptions.copy(scalacOptions = options),
        ),
      )
      .map(_.getOrElse(propsDefaultScalacOptions))
      .getOrElse(propsDefaultScalacOptions)
    val settings = Settings
      .baseDefault(AbsolutePath(PathIO.workingDirectory.toNIO))
      .withProperties(props)
    val inputFiles = mdoc.internal.io.IO.inputFiles(settings)
    if (inputFiles.nonEmpty) {
      val log = toProcessLogger(st)
      val sign = st.extract.get(ReleasePlugin.autoImport.releaseVcsSign)
      val signOff = st.extract.get(ReleasePlugin.autoImport.releaseVcsSignOff)

      val vcsInstance = vcs(st)
      val base = vcsInstance.baseDir.getCanonicalFile
      val outputFiles = inputFiles.toStream
        .map(_.out)
        .map(outputFile => sbt.IO.relativize(base, outputFile.toFile))
        .filter(_.nonEmpty)
        .map(_.get)
      outputFiles.foreach(vcsInstance.add(_) !! log)

      val status = (vcsInstance.status !!) trim

      val newState = if (status.nonEmpty) {
        val msg =
          st.extract.get(ReleaseMdocPlugin.autoImport.releaseMdocCommitMessage)
        vcs(st).commit(msg, sign, signOff) ! log
        st
      } else {
        // nothing to commit. this happens if the mdoc output files haven't changed.
        st
      }
      newState
    } else {
      st
    }
  })

  /**
    * @see [[ReleaseStateTransformations]]`.toProcessLogger(State)`
    */
  protected def toProcessLogger(st: State): ProcessLogger =
    new ProcessLogger {
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
      .getOrElse(
        sys.error(
          "Aborting release. Working directory is not a repository of a recognized VCS.",
        ),
      )
  }
}

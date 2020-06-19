import better.files.{File, FileExtensions}
import com.github.daniel.shuy.sbt.release.mdoc.ReleaseMdocStateTransformations
import com.github.daniel.shuy.sbt.scripted.scalatest.ScriptedScalaTestSuiteMixin
import org.eclipse.jgit.api.{Git => JGit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import sbtrelease.Git

import scala.collection.JavaConverters._

lazy val testCommit = (project in file("."))
  .enablePlugins(MdocPlugin)
  .settings(
    name := "test/sbt-1.0/commit",

    mdocIn := baseDirectory.in(ThisBuild).value / "sbt-release-mdoc-in",
    mdocOut := target.in(Compile).value / "sbt-release-mdoc-out",

    releaseIgnoreUntrackedFiles := true,
    releaseVcs := Option(Git.mkVcs(baseDirectory.value)),
    releaseProcess := Seq[ReleaseStep](
      ReleasePlugin.autoImport.releaseStepInputTask(MdocPlugin.autoImport.mdoc),
      ReleaseMdocStateTransformations.commitMdoc,
    ),

    scriptedScalaTestStacks := SbtScriptedScalaTest.FullStacks,
    scriptedScalaTestSpec := Some(new AnyWordSpec with ScriptedScalaTestSuiteMixin with BeforeAndAfterAll {
      override val sbtState: State = state.value

      override def beforeAll(): Unit = JGit.init().call().close()

      "release" should {
        "commit generated files in output directory" in {
          Command.process(ReleaseKeys.releaseCommand.nameOption.get, sbtState)

          val diffEntries = JGit.open(baseDirectory.value).diff().call().asScala
            .toStream
            .map(_.getNewPath)
            .map(File(_))
            .filter(_.isChildOf(mdocOut.value.toScala))
          assert(diffEntries.isEmpty)
        }
      }
    })
  )

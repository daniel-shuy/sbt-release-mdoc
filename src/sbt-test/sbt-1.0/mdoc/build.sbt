import better.files.FileOps
import com.github.daniel.shuy.sbt.release.mdoc.ReleaseMdocStateTransformations
import com.github.daniel.shuy.sbt.scripted.scalatest.ScriptedScalaTestSuiteMixin
import org.eclipse.jgit.api.{Git => JGit}
import org.scalatest.{BeforeAndAfterAll, WordSpec}
import sbtrelease.Git

lazy val testMdoc = (project in file("."))
  .enablePlugins(MdocPlugin)
  .settings(
    name := "test/sbt-1.0/mdoc",

    mdocIn := baseDirectory.in(ThisBuild).value / "sbt-release-mdoc-in",
    mdocOut := target.in(Compile).value / "sbt-release-mdoc-out",
    mdocVariables := Map(
      "KEY" -> "VALUE"
    ),

    releaseIgnoreUntrackedFiles := true,
    releaseVcs := Option(Git.mkVcs(baseDirectory.value)),
    releaseProcess := Seq[ReleaseStep](
      ReleasePlugin.autoImport.releaseStepInputTask(MdocPlugin.autoImport.mdoc),
      ReleaseMdocStateTransformations.commitMdoc,
    ),

    scriptedScalaTestStacks := SbtScriptedScalaTest.FullStacks,
    scriptedScalaTestSpec := Some(new WordSpec with ScriptedScalaTestSuiteMixin with BeforeAndAfterAll {
      override val sbtState: State = state.value

      override def beforeAll(): Unit = JGit.init().call().close()

      "release" should {
        "copy non-markdown files from input directory to output directory" in {
          val outputFile = mdocOut.value.toScala / "testa.txt"

          Command.process(ReleaseKeys.releaseCommand.nameOption.get, sbtState)

          assert(outputFile.exists(), s"$outputFile not found")
        }

        "substitute variables in generated markdown files" in {
          val generatedMarkdownFile = mdocOut.value.toScala / "test.md"

          Command.process(ReleaseKeys.releaseCommand.nameOption.get, sbtState)

          assert(generatedMarkdownFile.exists(), s"$generatedMarkdownFile not found")
          assert(generatedMarkdownFile.contentAsString == "KEY : VALUE\n")
        }
      }
    })
  )

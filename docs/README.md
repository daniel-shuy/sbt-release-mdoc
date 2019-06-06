# sbt-release-mdoc

[![Download](https://api.bintray.com/packages/daniel-shuy/sbt-plugins/sbt-release-mdoc/images/download.svg)](https://bintray.com/daniel-shuy/sbt-plugins/sbt-release-mdoc/_latestVersion)

| Branch  | Travis CI                                                                                                                                    | CodeFactor                                                                                                                                                                                         | Codacy                                                                                                                                                                                                                                                                             | Better Code Hub                                                                                                                   |
| ------- | -------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------- |
| Master  | [![Build Status](https://travis-ci.org/daniel-shuy/sbt-release-mdoc.svg?branch=master)](https://travis-ci.org/daniel-shuy/sbt-release-mdoc)  | [![CodeFactor](https://www.codefactor.io/repository/github/daniel-shuy/sbt-release-mdoc/badge/master)](https://www.codefactor.io/repository/github/daniel-shuy/sbt-release-mdoc/overview/master)   | [![Codacy Badge](https://api.codacy.com/project/badge/Grade/7200688e08804c60bbf9cd9107811aaa?branch=master)](https://www.codacy.com/app/daniel-shuy/sbt-release-mdoc?utm_source=github.com&utm_medium=referral&utm_content=daniel-shuy/sbt-release-mdoc&utm_campaign=Badge_Grade)  | [![BCH compliance](https://bettercodehub.com/edge/badge/daniel-shuy/sbt-release-mdoc?branch=master)](https://bettercodehub.com/)  |
| Develop | [![Build Status](https://travis-ci.org/daniel-shuy/sbt-release-mdoc.svg?branch=develop)](https://travis-ci.org/daniel-shuy/sbt-release-mdoc) | [![CodeFactor](https://www.codefactor.io/repository/github/daniel-shuy/sbt-release-mdoc/badge/develop)](https://www.codefactor.io/repository/github/daniel-shuy/sbt-release-mdoc/overview/develop) | [![Codacy Badge](https://api.codacy.com/project/badge/Grade/7200688e08804c60bbf9cd9107811aaa?branch=develop)](https://www.codacy.com/app/daniel-shuy/sbt-release-mdoc?utm_source=github.com&utm_medium=referral&utm_content=daniel-shuy/sbt-release-mdoc&utm_campaign=Badge_Grade) | [![BCH compliance](https://bettercodehub.com/edge/badge/daniel-shuy/sbt-release-mdoc?branch=develop)](https://bettercodehub.com/) |

| Plugin Version | SBT Version | sbt-release Version   | sbt-mdoc Version |
| -------------- | ----------- | --------------------- | ---------------- |
| 0.x.x          | 1.x.x       | @SBT_RELEASE_VERSION@ | 1.x.x            |

This plugin modifies [sbt-release](https://github.com/sbt/sbt-release)'s `releaseProcess` `SettingKey` to execute [sbt-mdoc](https://scalameta.org/mdoc/docs/installation.html#sbt)'s `mdoc` `InputTask` and commit the changes during release.

The new release process is based on `sbt-release`'s default Release Process (<https://github.com/sbt/sbt-release#user-content-release-process>, <https://github.com/sbt/sbt-release#user-content-the-default-release-process>), with 2 new steps:

1.  Check that the working directory is a git repository and the repository has no outstanding changes. Also prints the hash of the last commit to the console.
2.  If there are any snapshot dependencies, ask the user whether to continue or not (default: no).
3.  Ask the user for the `release version` and the `next development version`. Sensible defaults are provided.
4.  run `clean`
5.  Run `test:test`, if any test fails, the release process is aborted.
6.  Write `version in ThisBuild := "$releaseVersion"` to the file `version.sbt` and also apply this setting to the current [build state](http://www.scala-sbt.org/release/docs/Build-State.html).
7.  Commit the changes in `version.sbt`.
8.  **Run `mdoc`.**
9.  **Commit the `mdoc` output files.**
10. Tag the previous commit with `v$version` (eg. `v1.2`, `v1.2.3`).
11. Run `publish`.
12. Write `version in ThisBuild := "nextVersion"` to the file `version.sbt` and also apply this setting to the current build state.
13. Commit the changes in `version.sbt`.

specifically:

```scala mdoc:invisible:reset-class
import com.github.daniel.shuy.sbt.release.mdoc.ReleaseMdocStateTransformations
import mdoc.MdocPlugin
import sbtrelease.{ReleasePlugin, ReleaseStateTransformations}
import sbtrelease.ReleasePlugin.autoImport._
```

```scala mdoc:silent
import ReleaseStateTransformations._

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
)
```

## Usage

### Step 1: Include the plugin in your build

Add the following to your `project/plugins.sbt`:

```scala mdoc:invisible:reset-class
import sbt._
```

```scala mdoc:silent
addSbtPlugin("@ORGANIZATION@" % "@ARTIFACT_NAME@" % "@VERSION@")
```

Override the `sbt-mdoc` and `mdoc` dependency versions with the version of mdoc you wish to use:

```scala mdoc:invisible:reset-class
import sbt._
import sbt.Keys._
```

```scala mdoc:silent
addSbtPlugin("@ORGANIZATION@" % "@ARTIFACT_NAME@" % "@VERSION@")

val mdocVersion = "@MDOC_VERSION@"
addSbtPlugin("org.scalameta" % "sbt-mdoc" % mdocVersion)
libraryDependencies ++= Seq(
  "org.scalameta" %% "mdoc" % mdocVersion
)
```

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.scalameta/mdoc_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.scalameta/mdoc_2.12)

### Step 2: Enable `sbt-mdoc`

```scala mdoc:invisible:reset-class
import mdoc.MdocPlugin
import sbt._
```

```scala mdoc:silent
// build.sbt
lazy val root = (project in file("."))
  .enablePlugins(MdocPlugin)
  .settings(
    // ...
  )
```

### Step 3: Configure `sbt-mdoc`

See <https://scalameta.org/mdoc/docs/installation.html#reference> for the full documentation on `sbt-mdoc` settings.

Minimal Example to substitute `@@VERSION@` placeholders with project `version` and generate markdown files in project base directory:

```markdown
// docs/README.md
version = @@VERSION@
```

```scala mdoc:invisible:reset-class
import mdoc.MdocPlugin
import mdoc.MdocPlugin.autoImport.{mdocOut, mdocVariables}
import sbt._
import sbt.Keys._
```

```scala mdoc:silent
// build.sbt
lazy val root = (project in file("."))
  .enablePlugins(MdocPlugin)
  .settings(
    mdocOut := baseDirectory.in(ThisBuild).value,

    mdocVariables := Map(
      "VERSION" -> version.value
    ),
  )
```

### Step 4: Run `sbt release`

## Settings

| Setting                  | Type   | Description                                                                |
| ------------------------ | ------ | -------------------------------------------------------------------------- |
| releaseMdocCommitMessage | String | **Optional**. The commit message to use when committing mdoc output files. |

## Notes

- This project now uses itself to generate this `README`!

### Custom Release Process

If you need to customize `sbt-release`'s `releaseProcess`, use `ReleasePlugin.autoImport.releaseStepInputTask(MdocPlugin.autoImport.mdoc)` and `ReleaseMdocStateTransformations.commitMdoc` to add the `ReleaseStep`s to your build.

### Continuous Integration

When releasing in a CI tool, set `releaseMdocCommitMessage` to modify the commit message to skip building (else your build may recurse infinitely).

Example for Travis CI:

```scala mdoc:invisible:reset-class
import com.github.daniel.shuy.sbt.release.mdoc.ReleaseMdocPlugin.autoImport.releaseMdocCommitMessage
import mdoc.MdocPlugin
import sbt._
```

```scala mdoc:silent
// build.sbt
lazy val root = (project in file("."))
  .enablePlugins(MdocPlugin)
  .settings(
    releaseMdocCommitMessage := s"[ci skip] ${releaseMdocCommitMessage.value}"
  )
```

### Extra Mdoc Dependencies

Because Mdoc can only import from dependencies that are available at runtime, if you need to import a `provided`/`test` dependency or a dependency that your project doesn't already depend on, separate the Mdoc project and add them to `libraryDependencies`.

```scala mdoc:invisible:reset-class
import com.github.daniel.shuy.sbt.release.mdoc.ReleaseMdocPlugin.autoImport._
import mdoc.MdocPlugin
import sbt._
import sbt.Keys._
import sbtrelease.{ReleasePlugin, ReleaseStateTransformations}
import sbtrelease.ReleasePlugin.autoImport._
```

```scala mdoc:silent
import ReleaseTransformations._

lazy val root = project
  .in(file("."))
  .settings(
    skip in publish := true,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
    ),
    releaseProcess ++= releaseStepScopedReleaseAndRemaining(myproject).toSeq,
    releaseProcess ++= Seq[ReleaseStep](
      setReleaseVersion,
      commitReleaseVersion,
    ),
    releaseProcess ++= releaseStepScopedReleaseAndRemaining(docs).toSeq,
    releaseProcess ++= Seq[ReleaseStep](
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges,
    ),
  )
  .aggregate(myproject, docs)

lazy val myproject = project  // your existing library
  .settings(
    // ...
    releaseProcess := Seq[ReleaseStep](
      runClean,
      runTest,
    ),
    // ...
  )

lazy val docs = project
  .in(file("myproject-docs"))
  .settings(
    skip in publish := true,
    libraryDependencies ++= Seq(
      // declare additional dependencies here
    ),
    releaseProcess := Seq[ReleaseStep](
      ReleasePlugin.autoImport.releaseStepInputTask(MdocPlugin.autoImport.mdoc),
      ReleaseMdocStateTransformations.commitMdoc,
    ),
  )
  .dependsOn(myproject)
  .enablePlugins(MdocPlugin)
```

## Licence

Copyright 2019 Daniel Shuy

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

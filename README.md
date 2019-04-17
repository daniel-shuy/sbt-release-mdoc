# sbt-release-mdoc

[![Download](https://api.bintray.com/packages/daniel-shuy/sbt-plugins/sbt-release-mdoc/images/download.svg)](https://bintray.com/daniel-shuy/sbt-plugins/sbt-release-mdoc/_latestVersion)

| Branch | Travis CI | Codacy |
| ------ | --------- | ------ |
| Master | [![Build Status](https://travis-ci.org/daniel-shuy/sbt-release-mdoc.svg?branch=master)](https://travis-ci.org/daniel-shuy/sbt-release-mdoc) | [![Codacy Badge](https://api.codacy.com/project/badge/Grade/7200688e08804c60bbf9cd9107811aaa?branch=master)](https://www.codacy.com/app/daniel-shuy/sbt-release-mdoc?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=daniel-shuy/sbt-release-mdoc&amp;utm_campaign=Badge_Grade) |
| Develop | [![Build Status](https://travis-ci.org/daniel-shuy/sbt-release-mdoc.svg?branch=develop)](https://travis-ci.org/daniel-shuy/sbt-release-mdoc) | [![Codacy Badge](https://api.codacy.com/project/badge/Grade/7200688e08804c60bbf9cd9107811aaa?branch=develop)](https://www.codacy.com/app/daniel-shuy/sbt-release-mdoc?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=daniel-shuy/sbt-release-mdoc&amp;utm_campaign=Badge_Grade) |

| Plugin Version | SBT Version   | sbt-release Version | sbt-mdoc Version |
| -------------- | ------------- | ------------------- | ---------------- |
| 0.x.x          | 1.x.x         | 1.0.11              | 1.x.x            |

This plugin modifies [sbt-release](https://github.com/sbt/sbt-release)'s `releaseProcess` `SettingKey` to execute [sbt-mdoc](https://scalameta.org/mdoc/docs/installation.html#sbt)'s `mdoc` `InputTask` and commit the changes during release.

The new release process is based on `sbt-release`'s default Release Process (https://github.com/sbt/sbt-release#user-content-release-process, https://github.com/sbt/sbt-release#user-content-the-default-release-process), with 2 new steps:

 1. Check that the working directory is a git repository and the repository has no outstanding changes. Also prints the hash of the last commit to the console.
 1. If there are any snapshot dependencies, ask the user whether to continue or not (default: no).
 1. Ask the user for the `release version` and the `next development version`. Sensible defaults are provided.
 1. run `clean`
 1. Run `test:test`, if any test fails, the release process is aborted.
 1. Write `version in ThisBuild := "$releaseVersion"` to the file `version.sbt` and also apply this setting to the current [build state](http://www.scala-sbt.org/release/docs/Build-State.html).
 1. Commit the changes in `version.sbt`.
 1. **Run `mdoc`.**
 1. **Commit the `mdoc` output files.**
 1. Tag the previous commit with `v$version` (eg. `v1.2`, `v1.2.3`).
 1. Run `publish`.
 1. Write `version in ThisBuild := "nextVersion"` to the file `version.sbt` and also apply this setting to the current build state.
 1. Commit the changes in `version.sbt`.
 
specifically:
```scala
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
```scala
addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % "0.0.1")
```

Override the `sbt-mdoc` and `mdoc` dependency versions with the version of mdoc you wish to use:
```scala
addSbtPlugin("com.github.daniel-shuy" % "sbt-release-mdoc" % "0.0.1")

val mdocVersion = "1.0.0"
addSbtPlugin("org.scalameta" % "sbt-mdoc" % mdocVersion)
libraryDependencies ++= Seq(
  "org.scalameta" %% "mdoc" % mdocVersion
)
```

### Step 2: Enable `sbt-mdoc`

```scala
// build.sbt
lazy val root = (project in file("."))
  .enablePlugins(MdocPlugin)
  .settings(
    // ...
  )
```

### Step 3: Run `sbt release`

## Settings

| Setting                                     | Type                         | Description                                                                                                                                                                                                                         |
| ------------------------------------------- | ---------------------------- | -------------------------------------------------------------------------- |
| releaseMdocCommitMessage                    | String                       | __Optional__. The commit message to use when committing mdoc output files. |

## Notes

### Custom Release Process

If you need to customize `sbt-release`'s `releaseProcess`, use `ReleasePlugin.autoImport.releaseStepInputTask(MdocPlugin.autoImport.mdoc)` and `ReleaseMdocStateTransformations.commitMdoc` to add the `ReleaseStep`s to your build.

### Continuous Integration

When releasing in a CI tool (eg. Travis CI), modify `releaseMdocCommitMessage` to skip building (else your build may recurse infinitely), eg.
```scala
// build.sbt
lazy val root = (project in file("."))
  .enablePlugins(MdocPlugin)
  .settings(
    releaseMdocCommitMessage := s"[ci skip] ${releaseMdocCommitMessage.value}"
  )
```

## Licence

Copyright 2019 Daniel Shuy

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

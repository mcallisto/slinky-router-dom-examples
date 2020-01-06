import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import org.scalajs.core.tools.linker.ModuleKind

/**
  * Custom task to start demo with webpack-dev-server, use as `<project>/start`.
  * Just `start` also works, and starts all frontend demos
  *
  * After that, the incantation is this to watch and compile on change:
  * `~<project>/fastOptJS::webpack`
  */
lazy val start = TaskKey[Unit]("start")

/** Say just `dist` or `<project>/dist` to make a production bundle in
  * `docs` for github publishing
  */
lazy val dist = TaskKey[File]("dist")

lazy val `basic` =
  project
    .enablePlugins(ScalablyTypedConverterPlugin)
    .configure(baseSettings, scalajsBundler, browserProject, slinkyReact)
    .settings(
      webpackDevServerPort := 8007,
      Compile / stIgnore += "csstype",
      Compile / stMinimize := Selection.All,
      Compile / npmDependencies ++= Seq(
        "react-router-dom" -> "5.1.2",
        "@types/react-router-dom" -> "5.1.2"
      )
    )


lazy val baseSettings: Project => Project =
  _.enablePlugins(ScalaJSPlugin)
    .settings(
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.13.1",
      scalacOptions ++= ScalacOptions.flags,
      scalaJSUseMainModuleInitializer := true,
      /* disabled because it somehow triggers many warnings */
      emitSourceMaps := false,
      scalaJSModuleKind := ModuleKind.CommonJSModule,
      /* in preparation for scala.js 1.0 */
      scalacOptions += "-P:scalajs:sjsDefinedByDefault",
      /* for slinky*/
      libraryDependencies ++= Seq(
        "me.shadaj" %%% "slinky-web" % "0.6.3",
        "me.shadaj" %%% "slinky-hot" % "0.6.3"
      ),
      scalacOptions += "-Ymacro-annotations"
    )

lazy val slinkyReact: Project => Project =
  _.settings(
    Compile / stFlavour := Flavour.Slinky,
    Compile / npmDependencies ++= Seq(
      "react" -> "16.9",
      "react-dom" -> "16.9",
      "@types/react" -> "16.9.5"
    )
  )

lazy val scalajsBundler: Project => Project =
  _.enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      /* Specify current versions and modes */
      startWebpackDevServer / version := "3.1.10",
      webpack / version := "4.26.1",
      Compile / fastOptJS / webpackExtraArgs += "--mode=development",
      Compile / fullOptJS / webpackExtraArgs += "--mode=production",
      Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
      Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production",
      useYarn := true
    )

lazy val withCssLoading: Project => Project =
  _.settings(
    /* custom webpack file to include css */
    webpackConfigFile := Some((ThisBuild / baseDirectory).value / "custom.webpack.config.js"),
    Compile / npmDevDependencies ++= Seq(
      "webpack-merge" -> "4.1",
      "css-loader" -> "2.1.0",
      "style-loader" -> "0.23.1",
      "file-loader" -> "3.0.1",
      "url-loader" -> "1.1.2"
    )
  )

/**
  * Implement the `start` and `dist` tasks defined above.
  * Most of this is really just to copy the index.html file around.
  */
lazy val browserProject: Project => Project =
  _.settings(
    start := {
      (Compile / fastOptJS / startWebpackDevServer).value
      val indexFrom = baseDirectory.value / "assets/index.html"
      val indexTo   = (Compile / fastOptJS / crossTarget).value / "index.html"
      Files.copy(indexFrom.toPath, indexTo.toPath, REPLACE_EXISTING)
    },
    dist := {
      val artifacts      = (Compile / fullOptJS / webpack).value
      val artifactFolder = (Compile / fullOptJS / crossTarget).value
      val distFolder     = (ThisBuild / baseDirectory).value / "docs" / moduleName.value

      distFolder.mkdirs()
      artifacts.foreach { artifact =>
        val target = artifact.data.relativeTo(artifactFolder) match {
          case None          => distFolder / artifact.data.name
          case Some(relFile) => distFolder / relFile.toString
        }

        Files.copy(artifact.data.toPath, target.toPath, REPLACE_EXISTING)
      }

      val indexFrom = baseDirectory.value / "assets/index.html"
      val indexTo   = distFolder / "index.html"

      val indexPatchedContent = {
        import collection.JavaConverters._
        Files
          .readAllLines(indexFrom.toPath, IO.utf8)
          .asScala
          .map(_.replaceAllLiterally("-fastopt-", "-opt-"))
          .mkString("\n")
      }

      Files.write(indexTo.toPath, indexPatchedContent.getBytes(IO.utf8))
      distFolder
    }
  )

import cats.uri.sbt.{Versions => V}
import scala.collection.immutable.SortedSet

val Scala212 = "2.12.15"
val Scala213 = "2.13.8"
val Scala3   = "3.0.2"

lazy val supportedScalaVersions: SortedSet[String] = SortedSet(Scala212, Scala213, Scala3)

ThisBuild / scalaVersion  := Scala213
ThisBuild / tlBaseVersion := "0.0"

// Utility

lazy val wildcardImport: SettingKey[Char] =
  settingKey[Char]("Character to use for wildcard imports.")
ThisBuild / wildcardImport := {
  if (tlIsScala3.value) {
    '*'
  } else {
    '_'
  }
}

// Common Settings
//
// We don't use these on every module, but most.

lazy val commonSettings = Seq(
  crossScalaVersions := supportedScalaVersions.toSeq
)

// Projects

lazy val root = tlCrossRootProject
  .aggregate(
    core,
    laws,
    scalacheck,
    testing
  )
  .settings(name := "cats-uri", crossScalaVersions := List(Scala213))

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "case-insensitive" % V.caseInsensitiveV,
      "org.typelevel" %%% "cats-core"        % V.catsV,
      "org.typelevel" %%% "cats-parse"       % V.catsParseV,
      "org.typelevel" %%% "literally"        % V.literallyV
    ),
    libraryDependencies ++= {
      // Needed for macros
      if (tlIsScala3.value) {
        Nil
      } else {
        List("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)
      }
    },
    console / initialCommands := {
      List("cats.", "cats.syntax.all.", "cats.uri.", "cats.uri.syntax.all.")
        .map(value => s"import ${value}${wildcardImport.value}")
        .mkString("\n")
    },
    consoleQuick / initialCommands := ""
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )

lazy val laws = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("laws"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= List(
      "org.typelevel" %%% "discipline-core" % V.disciplineV
    ),
    console / initialCommands := {
      List(
        "cats.",
        "cats.syntax.all.",
        "cats.uri.",
        "cats.uri.syntax.all."
      ).map(value => s"import ${value}${wildcardImport.value}").mkString("\n")
    },
    consoleQuick / initialCommands := ""
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .dependsOn(core)

lazy val scalacheck = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("scalacheck"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % V.scalacheckV
    ),
    console / initialCommands := {
      List(
        "cats.",
        "cats.syntax.all.",
        "cats.uri.",
        "cats.uri.syntax.all.",
        "org.scalacheck.",
        "cats.uri.scalacheck.all.")
        .map(value => s"import ${value}${wildcardImport.value}")
        .mkString("\n")
    },
    consoleQuick / initialCommands := ""
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .dependsOn(core)

lazy val testing = crossProject(JVMPlatform, JSPlatform)
  .in(file("testing"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit-scalacheck" % V.munitV,
      "org.typelevel" %%% "cats-kernel-laws" % V.catsV,
      "org.typelevel" %%% "discipline-munit" % V.disciplineMunitV
    ).map(_ % Test),
    Test / console / initialCommands := {
      List(
        "cats.",
        "cats.syntax.all.",
        "cats.uri.",
        "cats.uri.syntax.all.",
        "org.scalacheck.",
        "cats.uri.scalacheck.all.")
        .map(value => s"import ${value}${wildcardImport.value}")
        .mkString("\n")
    },
    Test / consoleQuick / initialCommands := ""
  )
  .jvmSettings(
    libraryDependencies ++= List(
      "com.google.guava" % "guava" % V.guavaV
    ).map(_ % Test)
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )
  .enablePlugins(NoPublishPlugin)
  .dependsOn(scalacheck % "test -> compile", laws % "test -> compile")

lazy val benchmarks = project
  .in(file("benchmarks"))
  .settings(
    libraryDependencies ++= List(
      "com.google.guava" % "guava"       % V.guavaV,
      "org.http4s"      %% "http4s-core" % V.http4sV,
      "org.scalacheck"  %% "scalacheck"  % V.scalacheckV,
      "org.typelevel"   %% "cats-core"   % V.catsV,
      "org.typelevel"   %% "cats-kernel" % V.catsV
    ),
    console / initialCommands := {
      List(
        "cats.",
        "cats.syntax.all.",
        "cats.uri.",
        "cats.uri.syntax.all.",
        "org.scalacheck."
      ).map(value => s"import ${value}${wildcardImport.value}").mkString("\n")
    },
    consoleQuick / initialCommands := "",
    // http4s forces us to us 3.1.x here.
    scalaVersion       := Scala213,
    crossScalaVersions := ((supportedScalaVersions - Scala3) + "3.1.1").toSeq
  )
  .dependsOn(core.jvm)
  .enablePlugins(NoPublishPlugin, JmhPlugin)

lazy val docs = project.in(file("site")).dependsOn(core.jvm).enablePlugins(TypelevelSitePlugin)

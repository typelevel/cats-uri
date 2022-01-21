import cats.uri.sbt.{Versions => V}

val Scala212 = "2.12.15"
val Scala213 = "2.13.8"
val Scala3   = "3.0.2"

ThisBuild / crossScalaVersions := Seq(Scala212, Scala213, Scala3)
ThisBuild / scalaVersion       := Scala213
ThisBuild / tlBaseVersion      := "0.0"

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

// Projects

lazy val root = tlCrossRootProject
  .settings(
    inThisBuild(
      List(
        scalafixScalaBinaryVersion := scalaBinaryVersion.value,
        semanticdbEnabled          := true,
        semanticdbVersion          := scalafixSemanticdb.revision
      )
    )
  )
  .aggregate(
    benchmarks,
    core,
    laws,
    scalacheck,
    testing
  )
  .settings(name := "cats-uri")

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
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
      "org.scalacheck" %%% "scalacheck"  % V.scalacheckV,
      "org.http4s"     %%% "http4s-core" % V.http4sV
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
    consoleQuick / initialCommands := ""
  )
  .dependsOn(core.jvm)
  .enablePlugins(NoPublishPlugin, JmhPlugin)

lazy val docs = project.in(file("site")).dependsOn(core.jvm).enablePlugins(TypelevelSitePlugin)

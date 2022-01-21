import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import cats.uri.sbt.{Versions => V}

val Scala212 = "2.12.15"
val Scala213 = "2.13.8"
val Scala3 = "3.0.2"

ThisBuild / crossScalaVersions := Seq(Scala212, Scala213, Scala3)
ThisBuild / scalaVersion := Scala213

// Projects
lazy val `cats-uri` = project
  .in(file("."))
  .disablePlugins(MimaPlugin)
  .enablePlugins(NoPublishPlugin)
  .aggregate(core.jvm, core.js)

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "cats-uri",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % V.catsV
    )
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
  )

lazy val site = project.in(file("site")).disablePlugins(MimaPlugin).dependsOn(core.jvm)

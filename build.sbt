import cats.uri.sbt.{Versions => V}

val Scala212 = "2.12.15"
val Scala213 = "2.13.8"
val Scala3 = "3.0.2"

ThisBuild / crossScalaVersions := Seq(Scala212, Scala213, Scala3)
ThisBuild / scalaVersion := Scala213
ThisBuild / tlBaseVersion := "0.0"

// Projects
lazy val root = tlCrossRootProject.aggregate(core)

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

lazy val docs = project.in(file("site")).dependsOn(core.jvm).enablePlugins(TypelevelSitePlugin)

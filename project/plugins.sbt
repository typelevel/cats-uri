val sbtTypelevelV: String = "0.4.1"

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.28")
addSbtPlugin("com.github.cb372" % "sbt-explicit-dependencies" % "0.2.16")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.3")
addSbtPlugin("org.typelevel" % "sbt-typelevel" % sbtTypelevelV)
addSbtPlugin("org.typelevel" % "sbt-typelevel-site" % sbtTypelevelV)

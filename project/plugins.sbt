val sbtTypelevelV: String = "0.4.1"

addSbtPlugin("ch.epfl.scala"      % "sbt-scalafix"              % "0.9.34")
addSbtPlugin("com.github.cb372"   % "sbt-explicit-dependencies" % "0.2.16")
addSbtPlugin("com.timushev.sbt"   % "sbt-updates"               % "0.5.3")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"               % "1.8.0")
addSbtPlugin("org.typelevel"      % "sbt-typelevel"             % sbtTypelevelV)
addSbtPlugin("org.typelevel"      % "sbt-typelevel-site"        % sbtTypelevelV)
addSbtPlugin("pl.project13.scala" % "sbt-jmh"                   % "0.4.3")

name := "baggage-jwt"
organization := "nl.gn0s1s"
startYear := Some(2017)
homepage := Some(url("https://github.com/philippus/baggage-jwt"))
licenses += ("BSD 3-Clause", url("http://opensource.org/licenses/BSD-3-Clause"))

developers := List(
  Developer(
    id = "philippus",
    name = "Philippus Baalman",
    email = "",
    url = url("https://github.com/philippus")
  )
)

crossScalaVersions := List("2.13.6")
scalaVersion := crossScalaVersions.value.last

useJCenter := true

libraryDependencies ++= Seq(
  "co.blocke" %% "scalajack" % "6.2.0",
  "org.scalacheck" %% "scalacheck" % "1.15.4" % Test
)

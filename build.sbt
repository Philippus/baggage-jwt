name := "baggage-jwt"
organization := "nl.gn0s1s"
version := "0.2.0"
startYear := Some(2017)
homepage := Some(url("https://github.com/philippus/baggage-jwt"))
licenses += ("BSD 3-Clause", url("http://opensource.org/licenses/BSD-3-Clause"))

crossScalaVersions := List("2.11.12", "2.12.4")
scalaVersion := crossScalaVersions.value.last

bintrayOrganization := Some("gn0s1s")
bintrayRepository := "releases"

useJCenter := true

libraryDependencies ++= Seq(
  "co.blocke" %% "scalajack" % "5.0.8",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
)

pomExtra :=
  <scm>
    <url>git@github.com:Philippus/baggage-jwt.git</url>
    <connection>scm:git@github.com:Philippus/baggage-jwt.git</connection>
  </scm>
  <developers>
    <developer>
      <id>philippus</id>
      <name>Philippus Baalman</name>
      <url>https://github.com/philippus</url>
    </developer>
  </developers>

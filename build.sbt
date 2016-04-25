organization := "no.met.data"
name := """util"""
version := "0.2-SNAPSHOT"
description := "Utility code used by the metapi."
homepage :=  Some(url(s"https://github.com/metno"))
licenses += "GPL-2.0" -> url("https://www.gnu.org/licenses/gpl-2.0.html")

// Scala settings
// ----------------------------------------------------------------------
scalaVersion := "2.11.8"
scalacOptions ++= Seq("-deprecation", "-feature")
lazy val root = (project in file("."))

// Dependencies
// ----------------------------------------------------------------------
libraryDependencies ++= Seq(
 "com.typesafe.play" %% "play-json" % "2.4.1",
 "org.json4s" %% "json4s-native" % "3.2.11",
 "com.github.scopt" %% "scopt" % "3.3.0",
 "com.github.nscala-time" %% "nscala-time" % "2.0.0",
 "org.apache.commons" % "commons-email" % "1.1",
 "org.slf4j" % "slf4j-log4j12" % "1.7.12",
 "commons-logging" % "commons-logging" % "1.2",
 "org.specs2" %% "specs2-core" % "3.4" % "test",
 "org.specs2" %% "specs2-junit" % "3.4" % "test",
 "org.subethamail" % "subethasmtp" % "3.1.7" % "test",
 "junit" % "junit" % "4.12" % "test",
 "com.typesafe.play" %% "play" % "2.4.1",
 "com.typesafe.play" %% "play-test" % "2.4.1"
)

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "sonatype-releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "sonatype-central" at "https://repo1.maven.org/maven2"
)

// Publish Settings
// ----------------------------------------------------------------------
publishTo := {
  val jfrog = "https://oss.jfrog.org/artifactory/"
  if (isSnapshot.value)
    Some("Artifactory Realm" at jfrog + "oss-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
  else
    Some("Artifactory Realm" at jfrog + "oss-release-local")
}
pomExtra := (
  <scm>
    <url>https://github.com/metno/metapi-{name.value}.git</url>
    <connection>scm:git:git@github.com:metno/metapi-{name.value}.git</connection>
  </scm>
  <developers>
    <developer>
      <id>metno</id>
      <name>Meteorological Institute, Norway</name>
      <url>http://www.github.com/metno</url>
    </developer>
  </developers>)
bintrayReleaseOnPublish := false
publishArtifact in Test := false


// Testing
// ----------------------------------------------------------------------
javaOptions += "-Djunit.outdir=target/test-report"
scalacOptions in Test ++= Seq("-Yrangepos")
coverageHighlighting := true
coverageMinimum := 95
coverageFailOnMinimum := true
coverageExcludedPackages := """
  <empty>;
"""

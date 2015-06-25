name := """metapi-util"""

version := "0.1-SNAPSHOT"

organization := "no.met"

licenses += "GPLv2" -> url("https://www.gnu.org/licenses/gpl-2.0.html")

description := "Utilities used by the metapi."

publishTo := {
  val nexus = "http://maven.met.no/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

lazy val root = (project in file("."))

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

scalaVersion := "2.11.6"

javaOptions += "-Djunit.outdir=target/test-report"

ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := true

ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 80

ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := false

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := """
    <empty>;
"""

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.3.9",
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
  "com.typesafe.play" %% "play" % "2.3.9",
  "com.typesafe.play" %% "play-test" % "2.3.9"
)

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
  "sonatype-releases" at "http://oss.sonatype.org/content/repositories/releases/",
  "sonatype-central" at "https://repo1.maven.org/maven2"
)

//"metno repo" at "http://maven.met.no/content/groups/public"

parallelExecution in Test := false

scalacOptions in Test ++= Seq("-Yrangepos")

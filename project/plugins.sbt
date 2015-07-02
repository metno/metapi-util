resolvers ++= Seq(
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

// Git plugin
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.4")

// Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.1")

//Test plugins
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.1.0")

//Scalastyle Plugin
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

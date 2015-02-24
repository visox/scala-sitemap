import bintray.Keys._

organization := "com.banno"

name := "scala-sitemap"

version := "0.10.2"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.10.4", scalaVersion.value)

val dependencies = Seq(
  "org.specs2" %% "specs2-core" % "2.4.15" % "test",
  "com.github.nscala-time" %% "nscala-time" % "1.8.0",
  "com.netaporter" %% "scala-uri" % "0.4.4"
)

libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value ++ dependencies ++ Seq(
        "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
        "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"
      )
    case _ => libraryDependencies.value ++ dependencies
  }
}

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

seq(bintrayPublishSettings:_*)

bintrayOrganization in bintray := Some("banno")

repository in bintray := "oss"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

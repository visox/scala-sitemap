import bintray.Keys._

organization := "com.banno"

name := "scala-sitemap"

version := "0.10.1"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "org.specs2" % "specs2_2.11.0-SNAPSHOT" % "1.13.1-SNAPSHOT" % "test",
  "com.github.nscala-time" % "nscala-time_2.10" % "0.8.0",
  "com.netaporter" % "scala-uri_2.10" % "0.4.1",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.1",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
)

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

seq(bintrayPublishSettings:_*)

bintrayOrganization in bintray := Some("banno")

repository in bintray := "oss"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

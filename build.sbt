import bintray.Keys._

organization := "com.banno"

name := "scala-sitemap"

version := "0.10.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.3.10" % "test",
  "com.github.nscala-time" %% "nscala-time" % "0.8.0",
  "com.netaporter" %% "scala-uri" % "0.4.1"
)

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

seq(bintrayPublishSettings:_*)

bintrayOrganization in bintray := Some("banno")

repository in bintray := "oss"

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

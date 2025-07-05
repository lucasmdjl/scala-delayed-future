ThisBuild / organization := "dev.lucasmdjl"
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / version := "0.3.0"
ThisBuild / description := "A Scala 3 micro-library for ergonomically creating delayed Futures."
ThisBuild / scalaVersion := "3.7.1"
ThisBuild / sonatypeCredentialHost := xerial.sbt.Sonatype.sonatypeCentralHost

lazy val root = (project in file("."))
  .settings(
    name := "scala-delayed-future",
    idePackagePrefix := Some("dev.lucasmdjl.delayedfuture"),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "org.scalacheck" %% "scalacheck" % "1.18.1" % "test",
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % "test"
    ),
    publishTo := sonatypePublishToBundle.value,
  )

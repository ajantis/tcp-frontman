import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._

import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

import org.scalastyle.sbt.ScalastylePlugin

object TcpFrontmanBuild extends Build {
   import ProjectSettings._

  /**
   * Main module
   */ 
  lazy val tcpFrontman = Project("tcp-frontman", file("."))
    .settings(defaultSettings: _*)
    .settings(tcpFrontmanAssemblySettings: _*)
    .settings(libraryDependencies ++= Dependencies.tcpFrontman)

  override lazy val settings = {
    super.settings ++
    buildSettings ++
    Seq(
      shellPrompt := {
        s => Project.extract(s).currentProject.id + " > "
      }
    )
  }
}

object ProjectSettings {
  val ProjectVersion = "0.1"
  val ScalaVersion = "2.11.7"

  lazy val buildSettings = Seq(
    organization := "io.dmitryivanov.tcpfrontman",
    version := ProjectVersion,
    scalaVersion := ScalaVersion
  )

  lazy val defaultSettings = Defaults.defaultSettings ++
      ScalastylePlugin.Settings ++
      formatSettings ++
  Seq(
      scalacOptions in Compile := Seq(
        "-encoding", "utf8", "-target:jvm-1.8", "-feature", "-language:implicitConversions", "-language:postfixOps", "-unchecked", "-deprecation",
        "-Ywarn-adapted-args", "-Xlog-reflective-calls"
      ))

  lazy val tcpFrontmanAssemblySettings = Seq(
    mainClass in assembly := Some("io.dmitryivanov.tcpfrontman.Boot"),
    jarName   in assembly := "tcp-frontman.jar")

  lazy val formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences in Compile := formattingPreferences,
    ScalariformKeys.preferences in Test    := formattingPreferences
  )

  lazy val formattingPreferences = {
    import scalariform.formatter.preferences._
    FormattingPreferences()
      .setPreference(AlignParameters, true)
      .setPreference(AlignSingleLineCaseStatements, true)
  }
}

object Dependencies {
  val nettyV      = "4.1.0.Beta7"
  val scalaTestV  = "2.2.4"

  object Compile {
    val netty = "io.netty"          % "netty-all"   % nettyV  % "compile"
    val scopt = "com.github.scopt"  %% "scopt"      % "3.3.0"
  }

  object Test {
    val scalatest = "org.scalatest"     %% "scalatest"      % scalaTestV   % "test"
  }

  val tcpFrontman = Seq(
    Compile.netty,
    Compile.scopt,
    Test.scalatest)
}

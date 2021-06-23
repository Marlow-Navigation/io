import ProjectLibraries.RuntimeLibraries._
import ProjectLibraries.TestLibraries._
import ProjectLibraryVersions._
import sbt._

object ProjectLibraries {

  object ModuleLibraries {
    lazy val testkit: Seq[sbt.ModuleID] = Seq(
      specs2Core(false),
      scalaTest(false),
      specs2Mock(false)
    )

    lazy val io: Seq[ModuleID] = Seq(
      ScalaLogging,
      JodaTime,
      TypeSafeConfig
    ) ++ testkit
  }

  object RuntimeLibraries {
    val ScalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion
    val JodaTime = "joda-time" % "joda-time" % JodaTimeVersion
    val TypeSafeConfig = "com.typesafe" % "config" % TypeSafeConfigVersion
  }

  object TestLibraries {
    def srcDependency(testDependency: Boolean, dependency: ModuleID): sbt.ModuleID =
      testDependency match {
        case true  => dependency % Test
        case false => dependency
      }
    def specs2Core(testDependency: Boolean = true): ModuleID =
      srcDependency(testDependency, "org.specs2" %% "specs2-core" % Specs2Version)
    def specs2Mock(testDependency: Boolean = true): ModuleID =
      srcDependency(testDependency, "org.specs2" %% "specs2-mock" % Specs2Version)
    def scalaTest(testDependency: Boolean = true): ModuleID =
      srcDependency(testDependency, "org.scalatest" %% "scalatest" % ScalaTestVersion)
  }
}

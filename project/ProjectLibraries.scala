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
      TypeSafeConfig,
      ItextpdfKernel,
      ItextpdfIo,
      ItextpdfLayout,
      ItextpdfHtml2pdf,
      ItextpdfForms,
      ItextpdfSvg,
      JacksonCore,
      JacksonAnnotations,
      JacksonDatabind,
      Slf4jApi,
      Slf4jSimple,
      BcpkixJdk15on,
      BcprovJdk15on,
      PureConfig
    ) ++ testkit
  }

  object RuntimeLibraries {
    val ScalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion
    val JodaTime = "joda-time" % "joda-time" % JodaTimeVersion
    val TypeSafeConfig = "com.typesafe" % "config" % TypeSafeConfigVersion
    val ItextpdfKernel = "com.itextpdf" % "kernel" % ItextpdfKernelVersion
    val ItextpdfIo = "com.itextpdf" % "io" % ItextpdfIoVersion
    val ItextpdfLayout = "com.itextpdf" % "layout" % ItextpdfLayoutVersion
    val ItextpdfHtml2pdf = "com.itextpdf" % "html2pdf" % ItextpdfHtml2pdfVersion
    val ItextpdfForms = "com.itextpdf" % "forms" % ItextpdfFormsVersion
    val ItextpdfSvg = "com.itextpdf" % "svg" % ItextpdfSvgVersion
    val JacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % JacksonCoreVersion
    val JacksonAnnotations =
      "com.fasterxml.jackson.core" % "jackson-annotations" % JacksonAnnotationsVersion
    val JacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % JacksonDatabindVersion
    val Slf4jApi = "org.slf4j" % "slf4j-api" % Slf4jApiVersion
    val Slf4jSimple = "org.slf4j" % "slf4j-simple" % Slf4jSimpleVersion
    val BcpkixJdk15on = "org.bouncycastle" % "bcpkix-jdk15on" % BcpkixJdk15onVersion
    val BcprovJdk15on = "org.bouncycastle" % "bcprov-jdk15on" % BcprovJdk15onVersion
    val PureConfig = "com.github.pureconfig" %% "pureconfig" % PureConfigVersion
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

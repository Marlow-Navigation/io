import ProjectLibraries.ModuleLibraries
import sbt.Keys._
import sbt.{Def, _}

object CommonSettingsDefinition {
  val organisationString = "com.marlow"
  val scalaVersionString = "2.13.6"
  val ioVersion = "1.0.0"

  def commonSettings(projectName: String): Seq[Def.SettingsDefinition] = {
    val nexusUrl = sys.env
      .getOrElse(
        "NEXUS_REPO",
        ""
      )
    lazy val allResolvers = Seq(
      ("MarlowGroup Nexus Repo" at s"https://$nexusUrl/repository/io/")
        .withAllowInsecureProtocol(true)
    )

    Seq(
      libraryDependencies ++= ModuleLibraries.io,
      homepage := Some(url("https://github.com/Marlow-Navigation/io")),
      scmInfo := Some(
        ScmInfo(
          url("https://github.com/Marlow-Navigation/io"),
          "git@github.com:Marlow-Navigation/io.git"
        )
      ),
      licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
      publishTo := {
        if (isSnapshot.value)
          Some("maven-snapshots" at s"https://$nexusUrl/repository/io/")
        else
          Some("maven-releases" at s"https://$nexusUrl/repository/io/")
      },
      credentials += Credentials(
        "Sonatype Nexus Repository Manager",
        nexusUrl,
        sys.env.getOrElse("NEXUS_USER", ""),
        sys.env.getOrElse("NEXUS_PASS", "")
      ),
      updateOptions := updateOptions.value.withCachedResolution(true),
      updateOptions := updateOptions.value.withGigahorse(false),
      publishConfiguration := publishConfiguration.value.withOverwrite(true),
      publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true),
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in (Test, packageDoc) := false,
      Test / publishArtifact := false,
      skip in (Test, publish) := true,
      organization := organisationString,
      scalaVersion := scalaVersionString,
      crossPaths := false,
      name := projectName,
      version := sys.env.getOrElse("IO_VERSION", ioVersion),
      resolvers ++= allResolvers,
      Compile / doc / sources := Seq.empty,
      scalacOptions := Seq(
        "-encoding",
        "UTF-8",
        "-language:higherKinds",
        "-language:postfixOps",
        "-feature",
        "-Ywarn-unused:imports",
        "-language:implicitConversions"
      ),
      scalacOptions in Test ~= { (options: Seq[String]) =>
        options filterNot (_ == "-Ywarn-dead-code") // Allow dead code in tests (to support using mockito).
      }
    )
  }
}

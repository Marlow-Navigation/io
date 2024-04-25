object ProjectUtils {
  def setupProject(project: sbt.Project): sbt.Project =
    project
      .settings(CommonSettingsDefinition.commonSettings(s"${project.id}-reports"): _*)
}

object ProjectUtils {
  def setupProject(project: sbt.Project): sbt.Project =
    project
      .settings(CommonSettingsDefinition.commonSettings(project.id): _*)
}

lazy val `io` = (project in file("."))
  .settings(
    publishArtifact := true
  )
  .configure(ProjectUtils.setupProject)

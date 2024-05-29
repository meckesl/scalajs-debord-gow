lazy val shared = project.in(file("gow-shared"))

lazy val server = project.in(file("gow-server"))
  .dependsOn(shared)

lazy val client = project.in(file("gow-client"))
  .dependsOn(shared)
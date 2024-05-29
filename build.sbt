lazy val shared = project.in(file("gow-shared"))

lazy val server = project.in(file("gow-server"))
  .dependsOn(shared)

lazy val client = project.in(file("gow-client"))
  .dependsOn(shared)

/*jsDependencies ++= Seq(
  "org.webjars.npm" % "atmosphere.js" % "3.1.3" / "atmosphere.js" % "compile"
)
Test / jsDependencies := Nil
packageJSDependencies / skip := false
Test / packageJSDependencies / skip := true*/
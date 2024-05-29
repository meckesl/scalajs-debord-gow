lazy val shared = project.in(file("gow-shared"))

lazy val client = project.in(file("gow-client"))
  .dependsOn(shared)

lazy val server = project.in(file("gow-server"))
  .dependsOn(shared)
  .settings(
    inConfig(Compile)(Seq(
      resourceGenerators += Def.task {
        val outputFolder = resourceManaged.value / "web" / "target" / "scala-2.13"
        val jsBinary = (client / fullOptJS).value.data
        val jsSourceMap = jsBinary.getParentFile / (jsBinary.getName + ".map")
        IO.copy(Seq((jsBinary, outputFolder / jsBinary.getName), (jsSourceMap, outputFolder / jsSourceMap.getName)))
        val c = ((client / resourceDirectory).value ** "*").get()
          .pair(Path.rebase((client / resourceDirectory).value, outputFolder / "classes")).map {
            case (in, out) => IO.copyDirectory(in, out)
              out
          }
        Seq(
          outputFolder / jsBinary.getName,
          outputFolder / jsSourceMap.getName
        ) ++ c
      }
    ))
  )


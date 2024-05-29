lazy val shared = project.in(file("gow-shared"))

lazy val client = project.in(file("gow-client"))
  .dependsOn(shared)

lazy val server = project.in(file("gow-server"))
  .dependsOn(shared)
  .settings(
    inConfig(Compile)(Seq(
      resourceGenerators += Def.task {
        val outputFolder = resourceManaged.value / "web"
        val jsOutputFolder = outputFolder / "target" / "scala-2.13"
        val jsBinary = (client / fullOptJS).value.data
        val jsSourceMap = jsBinary.getParentFile / (jsBinary.getName + ".map")
        IO.copy(Seq((jsBinary, jsOutputFolder / jsBinary.getName), (jsSourceMap, jsOutputFolder / jsSourceMap.getName)))
        val clientResources = (client / resourceDirectory).value ** "*"
        val c = clientResources.get().filterNot(_.isDirectory).map{ r =>
          IO.copy(Seq((r, outputFolder / r.getName)))
          outputFolder / r.getName
        }
        Seq(jsOutputFolder / jsBinary.getName, jsOutputFolder / jsSourceMap.getName) ++ c
      }
    ))
  )


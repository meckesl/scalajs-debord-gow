lazy val shared = project.in(file("gow-shared"))

lazy val client = project.in(file("gow-client"))
  .dependsOn(shared)

lazy val server = project.in(file("gow-server"))
  .dependsOn(shared)
  .settings(
    inConfig(Compile)(Seq(
      resourceGenerators += Def.task {
        // Client resources
        val indexFile = (client / baseDirectory).value / "index.html"
        val staticFiles = (client / resourceDirectory).value ** "*"
        val jsBinary = (client / fullOptJS).value.data
        val jsSourceMap = jsBinary.getParentFile / (jsBinary.getName + ".map")
        // Replicate folder structure
        val webFolder = resourceManaged.value / "web"
        val outputFolder = webFolder / "target" / "scala-2.13"
        IO.copy(Seq(
          (jsBinary, outputFolder / jsBinary.getName),
          (jsSourceMap, outputFolder / jsSourceMap.getName),
          (indexFile, webFolder / indexFile.getName))
        )
        val staticResources = staticFiles.get()
          .pair(Path.rebase((client / resourceDirectory).value, outputFolder / "classes")).map {
            case (in, out) => IO.copyDirectory(in, out)
              out
          }
        // Outputs resources items
        Seq(
          webFolder / indexFile.getName,
          outputFolder / jsBinary.getName,
          outputFolder / jsSourceMap.getName
        ) ++ staticResources
      }
    ))
  )


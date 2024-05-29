lazy val shared = project.in(file("gow-shared"))

lazy val client = project.in(file("gow-client"))
  .dependsOn(shared)

lazy val server = project.in(file("gow-server"))
  .dependsOn(shared)
  .settings(
    inConfig(Compile)(Seq(
      resourceGenerators += Def.task {
        val webFolder = resourceManaged.value / "web"
        val jsBinary = (client / fullOptJS).value.data
        val jsSourceMap = jsBinary.getParentFile / (jsBinary.getName + ".map")
        IO.move(Seq((jsBinary, webFolder / jsBinary.getName), (jsSourceMap, webFolder / jsSourceMap.getName)))
        Seq(webFolder / jsBinary.getName, webFolder / jsSourceMap.getName) //, f2, f3)
      }
    ))
  )


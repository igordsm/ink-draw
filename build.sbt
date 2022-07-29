enablePlugins(ScalaJSPlugin)

name := "Ink Draw"
scalaVersion := "3.1.0"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.1.0"
scalaJSUseMainModuleInitializer := true

lazy val release = taskKey[Unit]("Copy all resources to docs")

Compile / release := {
    val outputDir = baseDirectory.value / "docs"
    val clientCodeDir = baseDirectory.value / "client"
    IO.delete(outputDir)
    IO.createDirectory(outputDir)

    val allSvg = clientCodeDir * "*.svg"
    allSvg.get().foreach((f) => {
        IO.copyFile(f, outputDir / f.name)
    })

    IO.copyFile(clientCodeDir / "main.css", outputDir / "main.css")

    (Compile / fastOptJS).value

    IO.copyFile((Compile / fastOptJS / artifactPath).value, outputDir / "main.js")
    IO.copyFile(clientCodeDir / "index.html", outputDir / "index.html")
}

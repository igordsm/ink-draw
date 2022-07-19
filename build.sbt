enablePlugins(ScalaJSPlugin)

name := "Scala.js Tutorial"
scalaVersion := "3.1.0"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.1.0"

// This is an application with a main method
scalaJSUseMainModuleInitializer := true

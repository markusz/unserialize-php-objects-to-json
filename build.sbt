name := "php-unserialize-to-json"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.+" % "compile",
  "com.sandinh" %% "php-unserializer" % "1.0.3",
  "io.spray" %%  "spray-json" % "1.3.2"
)
package main.scala

case class SQLConfig(url: String, username: String, password: String)
case class MigrationConfig(dataColumnName: String, jsonTargetColumnName: String)
package main.scala

case class SQLConfig(url: String, username: String, password: String, db: String, table: String)
case class MigrationConfig(dataColumnName: String, jsonTargetColumnName: String, simulation: Boolean = true, replaceKeys: Seq[(String, String)] = Seq())
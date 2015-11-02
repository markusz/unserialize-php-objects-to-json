package main.scala

object Main {
  def main(args: Array[String]) {

    val query: String = "SELECT * FROM adextern.psd_adextern_brands"

    val sqlConfig = new SQLConfig(
      url = "jdbc:mysql://:3306",
      username = "root",
      password = "root"
    )

    val migrationConfig = new MigrationConfig(
      dataColumnName = "data",
      jsonTargetColumnName = "dataJSON"
    )
    SQL.unserializeAndStoreInAdditionalColumn(sqlConfig, query, migrationConfig)
  }
}
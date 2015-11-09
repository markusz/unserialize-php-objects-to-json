package main.scala

object Main {
  def main(args: Array[String]) {

    val db = "ivw_prd"
    val table = "piranha_ivw_defaults"
    val qualifiedTableName = db + "." + table
    val selectQuery: String = "SELECT * FROM " + qualifiedTableName

    val sqlConfig = new SQLConfig(
      url = "jdbc:mysql://:3306",
      username = "root",
      password = "root",
      db = db,
      table = table
    )

    val migrationConfig = new MigrationConfig(
      dataColumnName = "data",
      jsonTargetColumnName = "dataJSON",
      replaceKeys = Seq(
        (" \\* ", ""),
        ("PSD\\\\AdExternBundle\\\\Entity\\\\", ""),
        ("Mineus\\\\NewAgofCodeSystemBundle\\\\Entity\\\\", "")
      ),
      simulation = false
    )
    SQL.unserializeAndStoreInAdditionalColumn(sqlConfig, selectQuery, migrationConfig)
  }
}
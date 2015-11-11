package main.scala

object Main {
  val DO_SIMULATION = false

  def main(args: Array[String]) {

    val sqlConfig1 = new SQLConfig(
      db = "schema_goes_here",
      table = "table_goes_here"
    )

    val sqlConfig2 = sqlConfig1.copy(db = "another_schema")

    val migrationConfig1 = new MigrationConfig(
      replaceKeys = Seq(
        (" \\* ", "")
      ),
      simulation = DO_SIMULATION
    )

    val migrationConfig2 = migrationConfig1.copy()

    SQL.unserializeAndStoreInAdditionalColumn(sqlConfig1, migrationConfig1)
    SQL.unserializeAndStoreInAdditionalColumn(sqlConfig2, migrationConfig2)
  }
}
package main.scala

case class SQLConfig(
                      url: String = "jdbc:mysql://:3306",
                      username: String = "root",
                      password: String = "root",
                      db: String,
                      table: String
                    ) {

  def toQualifiedTable = {
    this.db + "." + this.table
  }

  def makeSelectAll = {
    "SELECT * FROM " + this.db + "." + this.table
  }
}

case class MigrationConfig(
                            dataColumnName: String = "data",
                            jsonTargetColumnName: String = "dataJSON",
                            simulation: Boolean = true,
                            replaceKeys: Seq[(String, String)] = Seq()
                          )

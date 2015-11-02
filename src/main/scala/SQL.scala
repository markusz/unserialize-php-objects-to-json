package main.scala

import java.sql.{Connection, DriverManager}

import spray.json.JsValue


object SQL {
  def unserializeAndStoreInAdditionalColumn(sqlConfig: SQLConfig, selectQuery: String, migrationConfig: MigrationConfig) {
    val driver = "com.mysql.jdbc.Driver"

    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null

    try {
      Class.forName(driver)
      connection = DriverManager.getConnection(sqlConfig.url, sqlConfig.username, sqlConfig.password)
      connection2 = DriverManager.getConnection(sqlConfig.url, sqlConfig.username, sqlConfig.password)

      // create the statement, and run the select query
      val statement = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
      statement.setFetchSize(Integer.MIN_VALUE)
      val resultSet = statement.executeQuery(selectQuery)
      var i = 0
      var e = 0
      while (resultSet.next()) {
        i += 1
        val configRaw: String = resultSet.getString(migrationConfig.dataColumnName)
        val id = resultSet.getInt("id")

        //We deal with references that can not be parsed properly by setting their type to "i"
        def canonicalizeSerializedObject(s: String) = s.replaceAll("\u0000", " ").replaceAll("r:", "i:")

        val nodeConfigAsJSON: Option[JsValue] = configRaw match {
          case s: String => Parsing.parseSerializedPHPObjectToJSONString(canonicalizeSerializedObject(s)) match {
            case null =>
              e += 1
              None
            case ss: JsValue => Some(ss)
          }
          case _ => None
        }

        val query = "UPDATE adextern.psd_adextern_brands SET " + migrationConfig.jsonTargetColumnName + "=? WHERE id=?;"
        val preparedStmt = connection2.prepareStatement(query)
        preparedStmt.setString(1, nodeConfigAsJSON.get.compactPrint)
        preparedStmt.setInt(2, id)
        preparedStmt.executeUpdate()
      }
      println(s"Unserialized $i entries, Errors: $e (${e.toDouble / i.toDouble}) -----")
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    connection.close()
    connection2.close()
  }
}

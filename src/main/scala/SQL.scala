package main.scala

import java.sql.{Connection, DriverManager}

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json.JsValue


object SQL {
  def unserializeAndStoreInAdditionalColumn(sqlConfig: SQLConfig, migrationConfig: MigrationConfig) {
    val logger = Logger(LoggerFactory.getLogger("SQL"))

    if (migrationConfig.simulation) {
      logger.info("Running in simulation mode (Read-Only). No changes will be written to DB")
    }

    val driver = "com.mysql.jdbc.Driver"

    var connection: Connection = null
    var connection2: Connection = null

    try {
      Class.forName(driver)
      connection = DriverManager.getConnection(sqlConfig.url, sqlConfig.username, sqlConfig.password)
      connection2 = DriverManager.getConnection(sqlConfig.url, sqlConfig.username, sqlConfig.password)

      val statement = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY, java.sql.ResultSet.CONCUR_READ_ONLY)
      statement.setFetchSize(Integer.MIN_VALUE)
      val resultSet = statement.executeQuery(sqlConfig.makeSelectAll)
      var i = 0
      var e = 0
      while (resultSet.next()) {
        i += 1
        val configRaw: String = resultSet.getString(migrationConfig.dataColumnName)

        /**
          * - We deal with references that can not be parsed properly by setting their type from "r:" to "i:"
          * - We escape UTF8 chars that can break the parsing
          */
        def canonicalizeSerializedObject(s: String) = s.replaceAll("\u0000", " ").replaceAll("r:", "i:")

        val nodeConfigAsJSON: Option[JsValue] = configRaw match {
          case s: String => Parsing.parseSerializedPHPObjectToJSONString(canonicalizeSerializedObject(s), migrationConfig.replaceKeys) match {
            case null =>
              e += 1
              None
            case ss: JsValue => Some(ss)
          }
          case _ => None
        }

        val compactPrint: String = nodeConfigAsJSON.get.compactPrint
        logger.debug("OK: " + compactPrint)

        //ALTER TABLE `ivw_prd`.`piranha_ivw_defaults` ADD COLUMN `dataJSON` LONGTEXT NULL COMMENT '' AFTER `data`;

        if (!migrationConfig.simulation) {
          val id = resultSet.getInt("id")
          val query = "UPDATE " + sqlConfig.toQualifiedTable + " SET " + migrationConfig.jsonTargetColumnName + "=? WHERE id=?;"
          val preparedStmt = connection2.prepareStatement(query)
          preparedStmt.setString(1, compactPrint)
          preparedStmt.setInt(2, id)
          preparedStmt.executeUpdate()
        }
      }
      logger.info(s"Unserialized $i entries, Errors: $e (${e.toDouble / i.toDouble}) -----")
    } catch {
      case e: Throwable => logger.error("Error ", e)
    }
    connection.close()
    connection2.close()
  }
}


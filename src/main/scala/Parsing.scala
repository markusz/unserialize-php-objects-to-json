package main.scala

import com.sandinh.phpparser.PhpUnserializer
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import spray.json._
import JSON._


object Parsing {
  val logger = Logger(LoggerFactory.getLogger("Parser"))

  private def replaceStrings(value: String, replacementSets: Seq[(String, String)]) = {
    var replacedString = value

    for(replacement <- replacementSets) {
      replacedString = replacedString.replaceAll(replacement._1, replacement._2)
    }

    replacedString
  }

  private def cropResultMap(map: Map[String, Any], replacementSets: Seq[(String, String)]): Map[String, Any] = {
    val cleanedMap = map.map({
      case (key, value) =>
        val cleanedKey: String = replaceStrings(key, replacementSets)

        value match {
          case s: Map[String, Any] =>
            val m: Map[String, Any] = value.asInstanceOf[Map[String, Any]]
            cleanedKey -> cropResultMap(m, replacementSets)
          case s: Tuple2[String, Map[String, Any]] =>
            val tuple: (String, Map[String, Any]) = value.asInstanceOf[Tuple2[String, Map[String, Any]]]
            val string: String = tuple._1
            val resultMap: Map[String, Any] = tuple._2

            cleanedKey ->(replaceStrings(string, replacementSets), cropResultMap(resultMap, replacementSets))
          case s: Boolean => cleanedKey -> value
          case s: Int => cleanedKey -> value
          case _ => cleanedKey -> value.toString
        }
    })
    cleanedMap
  }

  def parseSerializedPHPObjectToJSONString(phpObject: String, replaceSets: Seq[(String, String)]): JsValue = {
    try {
      val parsedResultAsMap = PhpUnserializer.parse(phpObject).asInstanceOf[Map[String, Any]]
      val croppedResultAsMap = cropResultMap(parsedResultAsMap, replaceSets)
      croppedResultAsMap.toJson
    }
    catch {
      case e: Throwable =>
        println("Cant parse " + phpObject)
        e.printStackTrace()
        null
    }

  }
}

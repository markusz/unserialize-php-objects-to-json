package main.scala

import com.sandinh.phpparser.PhpUnserializer
import spray.json._
import JSON._


object Parsing {
  private def replaceStrings(value: String) = value.replaceAll(" \\* ", "").replaceAll("PSD\\\\AdExternBundle\\\\Entity\\\\", "")

  private def cropResultMap(map: Map[String, Any]): Map[String, Any] = {
    val cleanedMap = map.map({
      case (key, value) =>
        val cleanedKey: String = replaceStrings(key)

        value match {
          case s: Map[String, Any] =>
            val m: Map[String, Any] = value.asInstanceOf[Map[String, Any]]
            cleanedKey -> cropResultMap(m)
          case s: Tuple2[String, Map[String, Any]] =>
            val tuple: (String, Map[String, Any]) = value.asInstanceOf[Tuple2[String, Map[String, Any]]]
            val string: String = tuple._1
            val resultMap: Map[String, Any] = tuple._2

            cleanedKey ->(replaceStrings(string), cropResultMap(resultMap))
          case s: Boolean => cleanedKey -> value
          case s: Int => cleanedKey -> value
          case _ => cleanedKey -> value.toString
        }
    })
    cleanedMap
  }

  def parseSerializedPHPObjectToJSONString(phpObject: String): JsValue = {
    try {
      val parsedResultAsMap = PhpUnserializer.parse(phpObject).asInstanceOf[Map[String, Any]]
      val croppedResultAsMap = cropResultMap(parsedResultAsMap)
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

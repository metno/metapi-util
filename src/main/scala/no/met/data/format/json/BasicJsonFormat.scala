/*
    MET-API

    Copyright (C) 2014 met.no
    Contact information:
    Norwegian Meteorological Institute
    Box 43 Blindern
    0313 OSLO
    NORWAY
    E-mail: met-api@met.no

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
    MA 02110-1301, USA
*/

package no.met.data.format.json

import scala.language.postfixOps
import play.api.libs.json._
import no.met.data._
import com.github.nscala_time.time.Imports._
import org.joda.time.format.{ DateTimeFormatter, DateTimeFormatterBuilder }
import java.net.URL

/**
 * Various json formatting methods that are useful for all data.met.no services.
 */
class BasicJsonFormat {

  private val dateTimeZFormatter = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
    .appendTimeZoneOffset("Z", false, 2, 2)
    .toFormatter()

  def urlWrites: Writes[URL] = new Writes[URL] {
    def writes(u: URL): JsValue = JsString(u.toString)
  }

  /**
   * Formatting datetime, optionally with a format string
   */
  def dateTimeWrites(pattern: String = ""): Writes[DateTime] = pattern match {
    case s if !s.isEmpty() => dateTimeWrites(Some(new DateTimeFormatterBuilder()
      .appendPattern(s)
      .toFormatter()))
    case _ => dateTimeWrites(Some(dateTimeZFormatter))
  }

  /**
   * Formatting datetime, with a custom formatter
   */
  def dateTimeWrites(formatter: Option[DateTimeFormatter]): Writes[DateTime] = new Writes[DateTime] {
    def writes(dt: DateTime): JsValue = formatter match {
      case Some(f) => JsString(f.print(dt))
      case None => JsString(dateTimeZFormatter.print(dt))
    }
  }

  /**
   * Formatting durations
   */
  def durationWrites: Writes[Duration] = new Writes[Duration] {
    def writes(d: Duration): JsValue = JsNumber(d.getMillis.toDouble / 1000)
  }

  implicit val dateWrite = dateTimeWrites(Some(dateTimeZFormatter))
  implicit val durationWrite = durationWrites

  /**
   * Get json formatted header elements
   */
  def header(responseData: BasicResponseData): JsObject = {
    JsObject(headerElements(responseData))
  }

  private def headerElements(responseData: BasicResponseData): Seq[(String, JsValue)] = {
    val data = scala.collection.mutable.MutableList[(String, JsValue)](
      "@context" -> JsString(responseData.context.toString),
      "@type" -> JsString(responseData.contextType),
      "@id" -> JsString(responseData.id),
      "apiVersion" -> JsString(responseData.apiVersion),
      "license" -> JsString(responseData.license.toString),
      "createdAt" -> dateTimeWrites().writes(responseData.createdAt),
      "queryTime" -> durationWrites.writes(responseData.queryTime))

    if (responseData.itemsPerPage < responseData.totalItemCount) {
      data ++= Seq(
        "currentItemCount" -> JsNumber(responseData.currentItemCount),
        "itemsPerPage" -> JsNumber(responseData.itemsPerPage),
        "startOffset" -> JsNumber(responseData.startOffset))
    }

    data += Tuple2("totalItemCount", JsNumber(responseData.totalItemCount))

    if (responseData.itemsPerPage < responseData.totalItemCount) {
      def unpack(value: Option[URL]): JsValue = {
        value match {
          case Some(s) => JsString(s.toString)
          case None => JsNull
        }
      }
      data ++= Seq(
        "nextLink" -> unpack(responseData.nextLink),
        "previousLink" -> unpack(responseData.previousLink))
    }

    data += Tuple2("currentLink", JsString(responseData.currentLink.toString))

    data toList
  }
}

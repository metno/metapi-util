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

package no.met.json

import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import java.net.URL
import org.joda.time.format.{ DateTimeFormatter, DateTimeFormatterBuilder }
import scala.language.postfixOps
import no.met.data.ObsValue
import no.met.geometry._

//$COVERAGE-OFF$ Not sure how to test writers

/**
 * Various json formatting methods that are useful for all data.met.no services.
 */
class BasicJsonFormat {

  implicit val geometryPropertyWrites = Json.writes[GeometryProperty]

  implicit val pointWrites: Writes[Point] = (
    (JsPath \ "@type").write[String] and
    (JsPath \ "coordinates").write[Seq[Double]] and
    (JsPath \ "properties").writeNullable[GeometryProperty]
  )(unlift(Point.unapply))

  implicit val urlWrites: Writes[URL] = new Writes[URL] {
    def writes(u: URL): JsValue = JsString(u.toString)
  }

  private val dateTimeZFormatter = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
    .appendTimeZoneOffset("Z", false, 2, 2)
    .toFormatter()

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

  implicit val dateWrite = dateTimeWrites(Some(dateTimeZFormatter))

  /**
   * Formatting durations
   */
  def durationWrites: Writes[Duration] = new Writes[Duration] {
    def writes(d: Duration): JsValue = JsNumber(d.getMillis.toDouble / 1000)
  }

  implicit val durationWrite = durationWrites

  /**
    * Formatting ObsValue
    */
  def obsValueWrites: Writes[ObsValue] = new Writes[ObsValue] {
    def writes(oval: ObsValue): JsValue = ObsValue.toJsValue(oval.value)
  }

  implicit val obsValueWrite = obsValueWrites

}

//$COVERAGE-ON$

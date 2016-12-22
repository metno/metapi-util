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

package no.met.data

import java.sql.Timestamp
import scala.util.{Left, Right}
import play.Logger
import play.api.libs.json.{JsValue, JsNumber, JsString}
import anorm.{TypeDoesNotMatch, ColumnName}

/**
  * Generic type of an observation value.
  */
case class ObsValue(value: Any)


/**
  * Companion object.
  */
object ObsValue {

  // Converts supported type to ObsValue.
  def toObsValue(value: Any, qualified: ColumnName): Either[TypeDoesNotMatch, ObsValue] = value match {
    case v: Float => Right(ObsValue(v))
    case v: Double => Right(ObsValue(v))
    case v: Int => Right(ObsValue(v))
    case v: Long => Right(ObsValue(v))
    case v: java.math.BigDecimal => Right(ObsValue(v))
    case v: Timestamp => Right(ObsValue(v))
    case _ => {
      val msg = s"Cannot convert $value: ${value.asInstanceOf[Any].getClass} to ObsValue for column $qualified"
      Logger.warn(msg)
      Left(TypeDoesNotMatch(msg))
    }
  }

  // Converts supported type to JsValue.
  def toJsValue(value: Any): JsValue = value match {
    case v: Float => JsNumber(v)
    case v: Double => JsNumber(v)
    case v: Int => JsNumber(v)
    case v: Long => JsNumber(v)
    case v: java.math.BigDecimal => JsNumber(v)
    case v: Timestamp => JsString(v.asInstanceOf[Timestamp].toString())
    case _ => JsString(s"<unsupported type: ${value.asInstanceOf[Any].getClass}>")
  }
}

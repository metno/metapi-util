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

package test

import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import no.met.data._
import play.api.libs.json.{JsBoolean, JsNumber, JsString}
import anorm.ColumnName

// scalastyle:off magic.number

/* Tests for no.met.data utility classes */

@RunWith(classOf[JUnitRunner])
class DataSpec extends Specification {

  "A Bad Request Exception" should {

    "have error code 400" in {
      val ex = new BadRequestException("Default")
      ex.code must equalTo(400)
    }

  }

  "PostgresUtil" should {

    "accept valid text strings when sanitizing" in {
      PostgresUtil.sanitize(List("TA", "air_temp", "rr_24")) must not(throwA[Exception])
    }

    "throw Exception on invalid strings when sanitizing" in {
      PostgresUtil.sanitize(List("\"")) must throwA[Exception]
    }

    "throw Exception on invalid strings when sanitizing" in {
      PostgresUtil.sanitize(List("\'flf")) must throwA[Exception]
    }

    "throw Exception on invalid strings when sanitizing" in {
      PostgresUtil.sanitize(List("a-v")) must throwA[Exception]
    }

    "throw Exception on invalid strings when sanitizing" in {
      PostgresUtil.sanitize(List("a ")) must throwA[Exception]
    }

    "throw Exception on invalid strings when sanitizing" in {
      PostgresUtil.sanitize(List("-ad")) must throwA[Exception]
    }

    "throw Exception on invalid strings when sanitizing" in {
      PostgresUtil.sanitize(List("RR 12")) must throwA[Exception]
    }

    "throw Exception on invalid strings when sanitizing" in {
      PostgresUtil.sanitize(List(" TA")) must throwA[Exception]
    }

  }

  class Unsupported

  "ObsValue.toObsValue" should {
    "support Float" in {
      val v: Float = 5
      ObsValue.toObsValue(v, ColumnName("dummy", None)) must be equalTo(Right(ObsValue(v)))
    }

    "support Double" in {
      val v: Double = 5
      ObsValue.toObsValue(v, ColumnName("dummy", None)) must be equalTo(Right(ObsValue(v)))
    }

    "support Int" in {
      val v: Int = 5
      ObsValue.toObsValue(v, ColumnName("dummy", None)) must be equalTo(Right(ObsValue(v)))
    }

    "support Long" in {
      val v: Long = 5
      ObsValue.toObsValue(v, ColumnName("dummy", None)) must be equalTo(Right(ObsValue(v)))
    }

    "support java.math.BigDecimal" in {
      val v: java.math.BigDecimal = new java.math.BigDecimal(5)
      ObsValue.toObsValue(v, ColumnName("dummy", None)) must be equalTo(Right(ObsValue(v)))
    }

    "support java.sql.Timestamp" in {
      val v: java.sql.Timestamp = java.sql.Timestamp.valueOf("2016-01-01 00:00:00")
      ObsValue.toObsValue(v, ColumnName("dummy", None)) must be equalTo(Right(ObsValue(v)))
    }

    "not support Unsupported" in {
      val v: Unsupported = new Unsupported
      ObsValue.toObsValue(v, ColumnName("dummy", None)) must not be equalTo(Right(ObsValue(v)))
    }
  }

  "ObsValue.toJsValue" should {
    "support Float" in {
      val v: Float = 5
      ObsValue.toJsValue(v) must be equalTo(JsNumber(v))
    }

    "support Double" in {
      val v: Double = 5
      ObsValue.toJsValue(v) must be equalTo(JsNumber(v))
    }

    "support Int" in {
      val v: Int = 5
      ObsValue.toJsValue(v) must be equalTo(JsNumber(v))
    }

    "support Long" in {
      val v: Long = 5
      ObsValue.toJsValue(v) must be equalTo(JsNumber(v))
    }

    "support java.math.BigDecimal" in {
      val v: java.math.BigDecimal = new java.math.BigDecimal(5)
      ObsValue.toJsValue(v) must be equalTo(JsNumber(v))
    }

    "support java.sql.Timestamp" in {
      val v: java.sql.Timestamp = java.sql.Timestamp.valueOf("2016-01-01 00:00:00")
      ObsValue.toJsValue(v) must be equalTo(JsString(v.asInstanceOf[java.sql.Timestamp].toString()))
    }

    "not support Unsupported" in {
      val v: Unsupported = new Unsupported
      ObsValue.toJsValue(v) match {
        case s: JsString => s.value.contains("unsupported")
        case _ => false
      }
    }
  }

}

// scalastyle:on

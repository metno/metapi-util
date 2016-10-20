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
import scala.util._
import no.met.data.SourceSpecification

// scalastyle:off magic.number

@RunWith(classOf[JUnitRunner])
class SourceSpecificationSpec extends Specification {

  "SourceSpecification" should {

    "parse single source" in {
      val s = Seq("1234")
      SourceSpecification.parse(Some("SN1234")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234 ")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234 ")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234,")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234,,")) must equalTo(s)
    }

    "parse multiple sources" in {
      val s = Seq("1234", "5678")
      SourceSpecification.parse(Some("SN1234,SN5678")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234, SN5678")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234 ,SN5678")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234 , SN5678")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234,SN5678 ")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234 , SN5678 ")) must equalTo(s)
    }

    "parse empty" in {
      SourceSpecification.parse(None) must equalTo(Seq())
      SourceSpecification.parse(Some(",")) must equalTo(Seq())
      SourceSpecification.parse(Some(",,")) must equalTo(Seq())
    }

    "throw exception" in {
      SourceSpecification.parse(Some(", ,")) must throwA[Exception]
      SourceSpecification.parse(Some(",SN1234")) must throwA[Exception]
      SourceSpecification.parse(Some(",SN1234 ,")) must throwA[Exception]
      SourceSpecification.parse(Some(",,SN1234 ,")) must throwA[Exception]
      SourceSpecification.parse(Some(",SN1234 ,  SN5678")) must throwA[Exception]
      SourceSpecification.parse(Some(",SN1234 ,  SN5678,,")) must throwA[Exception]
      SourceSpecification.parse(Some("XX1234")) must throwA[Exception]
      SourceSpecification.parse(Some("SN 1234")) must throwA[Exception]
      SourceSpecification.parse(Some("SN")) must throwA[Exception]
      SourceSpecification.parse(Some(",SN1234")) must throwA[Exception]
      SourceSpecification.parse(Some("")) must throwA[Exception]
    }

    "parse source with sensor 0" in {
      val s = Seq("1234:0")
      SourceSpecification.parse(Some("SN1234:0")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234:0 ")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234:0")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234:0 ")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234:0,")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234:0,,")) must equalTo(s)
    }

    "parse source with sensor ALL" in {
      val s = Seq("1234:ALL")
      SourceSpecification.parse(Some("SN1234:ALL")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234:ALL ")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234:ALL")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234:ALL ")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234:ALL,")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234:ALL,,")) must equalTo(s)
    }

    "parse source with sensor ALL in different cases" in {
      val s = Seq("1234:ALL")
      SourceSpecification.parse(Some("SN1234:aLL")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234:AlL ")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234:All")) must equalTo(s)
      SourceSpecification.parse(Some(" SN1234:ALl ")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234:all,")) must equalTo(s)
      SourceSpecification.parse(Some("SN1234:all,,")) must equalTo(s)
    }

   "fail to parse source with sensor that is not ALL or digits" in {
      SourceSpecification.parse(Some("SN1234:A")) must throwA[Exception]
      SourceSpecification.parse(Some("SN1234:AL")) must throwA[Exception]
      SourceSpecification.parse(Some("SN1234:xyz")) must throwA[Exception]
      SourceSpecification.parse(Some("SN1234:xal")) must throwA[Exception]
      SourceSpecification.parse(Some("SN1234:al1")) must throwA[Exception]
      SourceSpecification.parse(Some("SN1234:al")) must throwA[Exception]
    }

    "convert list of source and sensor numbers to SQL" in {
      val s = "(stationId = SN12 AND sensorNr = 0) OR (stationId = SN34 AND sensorNr = 1) OR (stationId = SN56 AND sensorNr = 2)"
      SourceSpecification.sql(Seq("SN12:0", "SN34:1", "SN56:2"), "stationId", Some("sensorNr")) must equalTo(s)
    }

    "convert list of source with ALL to SQL" in {
      val s = "(stationId = SN12) OR (stationId = SN34) OR (stationId = SN56)"
      SourceSpecification.sql(Seq("SN12:ALL", "SN34:ALL", "SN56:ALL"), "stationId", Some("sensorNr")) must equalTo(s)
    }

    "convert list of source without sensor numbers to SQL" in {
      val s = "(stationId = SN12 AND sensorNr = 0) OR (stationId = SN34 AND sensorNr = 0) OR (stationId = SN56 AND sensorNr = 0)"
      SourceSpecification.sql(Seq("SN12", "SN34", "SN56"), "stationId", Some("sensorNr")) must equalTo(s)
    }

    "convert list of mixed source Ids with and without sensor numbers to SQL" in {
      val s = "(stationId = SN12 AND sensorNr = 0) OR (stationId = SN34) OR (stationId = SN56 AND sensorNr = 0)"
      SourceSpecification.sql(Seq("SN12:0", "SN34:ALL", "SN56"), "stationId", Some("sensorNr")) must equalTo(s)
    }

    "convert list of source and sensor numbers to SQL without a SensorNumber attribute" in {
      val s = "(stationId = SN12) OR (stationId = SN34) OR (stationId = SN56)"
      SourceSpecification.sql(Seq("SN12:0", "SN34:1", "SN56:2"), "stationId", None) must equalTo(s)
    }

  }
}

// scalastyle:on

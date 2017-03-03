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
import no.met.data._


// scalastyle:off magic.number

@RunWith(classOf[JUnitRunner])
class SourceSpecificationSpec extends Specification {

  "SourceSpecification" should {

    "parse single source" in {
      val s = Seq("1234")
      SourceSpecification(Some("SN1234")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234 ")).stationNumbers must equalTo(s)
      SourceSpecification(Some(" SN1234")).stationNumbers must equalTo(s)
      SourceSpecification(Some(" SN1234 ")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234,")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234,,")).stationNumbers must equalTo(s)
    }

    "parse multiple sources" in {
      val s = Seq("1234", "5678")
      SourceSpecification(Some("SN1234,SN5678")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234, SN5678")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234 ,SN5678")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234 , SN5678")).stationNumbers must equalTo(s)
      SourceSpecification(Some(" SN1234,SN5678 ")).stationNumbers must equalTo(s)
      SourceSpecification(Some(" SN1234 , SN5678 ")).stationNumbers must equalTo(s)
    }

    "parse empty" in {
      val emptySeq = Seq()
      SourceSpecification(None).stationNumbers must equalTo(emptySeq)
      SourceSpecification(Some("")).stationNumbers must equalTo(emptySeq)
      SourceSpecification(Some(",")).stationNumbers must equalTo(emptySeq)
      SourceSpecification(Some(",,")).stationNumbers must equalTo(emptySeq)
      SourceSpecification(Some(",, ")).stationNumbers must equalTo(emptySeq)
      SourceSpecification(Some(", ,")).stationNumbers must equalTo(emptySeq)
      SourceSpecification(Some(" , ,")).stationNumbers must equalTo(emptySeq)
    }

    "parse extra space and commas" in {
      val s1 = Seq("1234")
      val s2 = Seq("1234", "5678")
      SourceSpecification(Some(",SN1234")).stationNumbers must equalTo(s1)
      SourceSpecification(Some(",SN1234 ,")).stationNumbers must equalTo(s1)
      SourceSpecification(Some(",,SN1234 ,")).stationNumbers must equalTo(s1)
      SourceSpecification(Some(",SN1234 ,  SN5678")).stationNumbers must equalTo(s2)
      SourceSpecification(Some(",SN1234 ,  SN5678,,")).stationNumbers must equalTo(s2)
    }

    "parse malformed names" in {
      SourceSpecification(Some("XX1234")).stationNumbers must throwA[Exception]
      SourceSpecification(Some("SN 1234")).stationNumbers must throwA[Exception]
      SourceSpecification(Some("SN")).stationNumbers must throwA[Exception]
    }

    "parse source with sensor 0" in {
      val s = Seq("1234:0")
      SourceSpecification(Some("SN1234:0")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234:0 ")).stationNumbers must equalTo(s)
      SourceSpecification(Some(" SN1234:0")).stationNumbers must equalTo(s)
      SourceSpecification(Some(" SN1234:0 ")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234:0,")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234:0,,")).stationNumbers must equalTo(s)
    }

    "parse source with sensor ALL" in {
      val s = Seq("1234:ALL")
      SourceSpecification(Some("SN1234:ALL")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234:ALL ")).stationNumbers must equalTo(s)
      SourceSpecification(Some(" SN1234:ALL")).stationNumbers must equalTo(s)
      SourceSpecification(Some(" SN1234:ALL ")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234:ALL,")).stationNumbers must equalTo(s)
      SourceSpecification(Some("SN1234:ALL,,")).stationNumbers must equalTo(s)
    }

    "parse source with sensor ALL in different cases" in {
      SourceSpecification(Some("SN1234:all")).stationNumbers must equalTo(Seq("1234:all"))
      SourceSpecification(Some("SN1234:aLL")).stationNumbers must equalTo(Seq("1234:aLL"))
      SourceSpecification(Some("SN1234:All")).stationNumbers must equalTo(Seq("1234:All"))
    }

   "fail to parse source with sensor that is not ALL or digits" in {
      SourceSpecification(Some("SN1234:A")).stationNumbers must throwA[Exception]
      SourceSpecification(Some("SN1234:AL")).stationNumbers must throwA[Exception]
      SourceSpecification(Some("SN1234:xyz")).stationNumbers must throwA[Exception]
      SourceSpecification(Some("SN1234:xal")).stationNumbers must throwA[Exception]
      SourceSpecification(Some("SN1234:al1")).stationNumbers must throwA[Exception]
      SourceSpecification(Some("SN1234:al")).stationNumbers must throwA[Exception]
    }

    "convert list of source and sensor numbers to SQL" in {
      val s = "(stationId = SN12 AND sensorNr = 0) OR (stationId = SN34 AND sensorNr = 1) OR (stationId = SN56 AND sensorNr = 2)"
      SourceSpecification.stationWhereClause(Seq("SN12:0", "SN34:1", "SN56:2"), "stationId", Some("sensorNr")) must equalTo(s)
    }

    "convert list of source with ALL to SQL" in {
      val s = "(stationId = SN12) OR (stationId = SN34) OR (stationId = SN56)"
      SourceSpecification.stationWhereClause(Seq("SN12:ALL", "SN34:ALL", "SN56:ALL"), "stationId", Some("sensorNr")) must equalTo(s)
    }

    "convert list of source without sensor numbers to SQL" in {
      val s = "(stationId = SN12 AND sensorNr = 0) OR (stationId = SN34 AND sensorNr = 0) OR (stationId = SN56 AND sensorNr = 0)"
      SourceSpecification.stationWhereClause(Seq("SN12", "SN34", "SN56"), "stationId", Some("sensorNr")) must equalTo(s)
    }

    "convert list of mixed source Ids with and without sensor numbers to SQL" in {
      val s = "(stationId = SN12 AND sensorNr = 0) OR (stationId = SN34) OR (stationId = SN56 AND sensorNr = 0)"
      SourceSpecification.stationWhereClause(Seq("SN12:0", "SN34:ALL", "SN56"), "stationId", Some("sensorNr")) must equalTo(s)
    }

    "convert list of source and sensor numbers to SQL without a SensorNumber attribute" in {
      val s = "(stationId = SN12) OR (stationId = SN34) OR (stationId = SN56)"
      SourceSpecification.stationWhereClause(Seq("SN12:0", "SN34:1", "SN56:2"), "stationId", None) must equalTo(s)
    }

    "parse station source name" in {
      val s = Seq("SN1234")
      SourceSpecification(Some(s.head)).stationNames must equalTo(s)
    }

    "parse existing IDF grid name" in {
      val s = Seq(IDFGridConfig.name)
      SourceSpecification(Some(s.head)).idfGridNames must equalTo(s)
    }

    "parse supported types" in {
      val sInput = "SN1234,SN5678"
      val sOutputNames: Seq[String] = sInput split "," map(s => s.trim)

      val iInput = IDFGridConfig.name
      val iOutputNames: Seq[String] = iInput split "," map(s => s.trim)

      val ukInput = "unknown"

      val emptyOutput = Seq[String]()

      val siInput = s"$sInput,$iInput"
      val isInput = s"$sInput,$iInput"

      val sTag = StationConfig.typeName
      val iTag = IDFGridConfig.typeName // a.k.a. IDF gridded dataset type
      val siTag = s"$sTag,$iTag"
      val isTag = s"$iTag,$sTag"

      // specify station type only
      SourceSpecification(Some( sInput), Some(sTag)).stationNames must equalTo(sOutputNames)
      SourceSpecification(Some( iInput), Some(sTag)) must throwA[Exception]
      SourceSpecification(Some(siInput), Some(sTag)) must throwA[Exception]
      SourceSpecification(Some(isInput), Some(sTag)) must throwA[Exception] // test commutativity on first parameter
      SourceSpecification(Some(ukInput), Some(sTag)) must throwA[Exception]

      // specify IDF gridded dataset type only
      SourceSpecification(Some( iInput), Some(iTag)).idfGridNames must equalTo(iOutputNames)
      SourceSpecification(Some( sInput), Some(iTag)) must throwA[Exception]
      SourceSpecification(Some(siInput), Some(iTag)) must throwA[Exception]
      SourceSpecification(Some(ukInput), Some(iTag)) must throwA[Exception]

      // specify any type (variant 1: default types)
      SourceSpecification(Some( sInput)             ).stationNames must equalTo(sOutputNames)
      SourceSpecification(Some( iInput)             ).idfGridNames must equalTo(iOutputNames)
      SourceSpecification(Some(siInput)             ).stationNames must equalTo(sOutputNames)
      SourceSpecification(Some(siInput)             ).idfGridNames must equalTo(iOutputNames)
      SourceSpecification(Some(ukInput)             )              must throwA[Exception]

      // specify any type (variant 2: explicitly specifying all types)
      SourceSpecification(Some( sInput), Some(siTag)).stationNames must equalTo(sOutputNames)
      SourceSpecification(Some( sInput), Some(isTag)).stationNames must equalTo(sOutputNames) // test commutativity on second parameter
      SourceSpecification(Some( iInput), Some(siTag)).idfGridNames must equalTo(iOutputNames)
      SourceSpecification(Some(siInput), Some(siTag)).stationNames must equalTo(sOutputNames)
      SourceSpecification(Some(siInput), Some(siTag)).idfGridNames must equalTo(iOutputNames)
      SourceSpecification(Some(ukInput), Some(siTag))              must throwA[Exception]
    }

    "parse unsupported types" in {
      SourceSpecification(Some("SN1234"), Some("foobar")) must throwA[Exception]
    }

    "check type inclusion" in {
      val sInput = "SN1234"
      val iInput = IDFGridConfig.name
      val siInput = s"$sInput,$iInput"
      val sTag = StationConfig.typeName
      val iTag = IDFGridConfig.typeName
      val siTag = s"$sTag,$iTag"
      val isTag = s"$iTag,$sTag"

      SourceSpecification(Some(sInput), Some(sTag)).typeAllowed(sTag) must equalTo(true)
      SourceSpecification(Some(siInput), Some(siTag)).typeAllowed(sTag) must equalTo(true)
      SourceSpecification(Some(siInput), Some(isTag)).typeAllowed(sTag) must equalTo(true)
      SourceSpecification(Some(siInput), Some("")).typeAllowed(sTag) must equalTo(true)
      SourceSpecification(Some(siInput), None).typeAllowed(sTag) must equalTo(true)
      SourceSpecification(Some(iInput), Some(iTag)).typeAllowed(sTag) must equalTo(false)

      SourceSpecification(Some(iInput), Some(iTag)).typeAllowed(iTag) must equalTo(true)
      SourceSpecification(Some(siInput), Some(siTag)).typeAllowed(iTag) must equalTo(true)
      SourceSpecification(Some(siInput), Some(isTag)).typeAllowed(iTag) must equalTo(true)
      SourceSpecification(Some(siInput), Some("")).typeAllowed(iTag) must equalTo(true)
      SourceSpecification(Some(siInput), None).typeAllowed(iTag) must equalTo(true)
      SourceSpecification(Some(sInput), Some(sTag)).typeAllowed(iTag) must equalTo(false)
    }

    "check isEmpty" in {
      SourceSpecification(Some("SN1234")).isEmpty must equalTo(false)
      SourceSpecification(Some(IDFGridConfig.name)).isEmpty must equalTo(false)
      SourceSpecification(Some("")).isEmpty must equalTo(true)
    }

    "check includeSources" in {
      val sInput = "SN1234"
      val iInput = IDFGridConfig.name
      val sTag = StationConfig.typeName
      val iTag = IDFGridConfig.typeName

      SourceSpecification(Some(sInput)        ).includeStationSources must equalTo(true)
      SourceSpecification(Some("")            ).includeStationSources must equalTo(true)
      SourceSpecification(Some(""), Some(sTag)).includeStationSources must equalTo(true)
      SourceSpecification(Some(""), Some(iTag)).includeStationSources must equalTo(false)

      SourceSpecification(Some(iInput)        ).includeIdfGridSources must equalTo(true)
      SourceSpecification(Some("")            ).includeIdfGridSources must equalTo(true)
      SourceSpecification(Some(""), Some(iTag)).includeIdfGridSources must equalTo(true)
      SourceSpecification(Some(""), Some(sTag)).includeIdfGridSources must equalTo(false)
    }

  }
}

// scalastyle:on

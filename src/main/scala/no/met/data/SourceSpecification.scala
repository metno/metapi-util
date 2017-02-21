/*
    MET-API.

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

import scala.language.postfixOps
import scala.util._
import IDFGridConfig._

import play.api.Logger


protected class SourceSpecification(srcstr: Option[String], typestr: Option[String]) {

  private var stNames: Seq[String] = Seq[String]() // complete station names, including "SN" prefix
  private var stNumbers: Seq[String] = Seq[String]() // station numbers, i.e. station names without "SN" prefix
  private var igNames: Seq[String] = Seq[String]() // IDF gridded dataset names

  // Extracts sources from srcstr into sequences for their respective types. Considers typestr as an extra restriction.
  // Throws BadRequestException upon error.
  private def init() = {
    // extract sources
    var sources: Seq[String] = srcstr.getOrElse("") split "," map(_.trim) filter(_.nonEmpty)

    // extract and validate types
    val stag = StationConfig.typeName
    val stagLC = stag.toLowerCase
    val itag = IDFGridConfig.typeName // a.k.a. IDF gridded dataset
    val itagLC = itag.toLowerCase
    val supTypes = Set(stag, itag)
    var reqTypes: Set[String] = typestr.getOrElse("").toLowerCase split "," map(_.trim) filter(_.nonEmpty) toSet
    val unsupTypes = reqTypes -- supTypes.map(_.toLowerCase)
    if (unsupTypes.nonEmpty) {
      throw new BadRequestException(
        s"Unsupported type${if (unsupTypes.size == 1) "" else "s"}: ${unsupTypes.mkString(",")}",
        Some(s"Supported types: ${supTypes.mkString(", ")}"))
    }

    val sTypeOnly = !reqTypes.contains(itagLC) && reqTypes.contains(stagLC)
    val iTypeOnly = !reqTypes.contains(stagLC) && reqTypes.contains(itagLC)

    val supIdfGridNames = Set(IDFGridConfig.name) // only one name for now

    if (!sTypeOnly) {
      // IDF gridded dataset sources may be specified, so move any of those into igNames
      igNames = sources.filter(s => supIdfGridNames.contains(s))
      sources = sources.filter(s => !igNames.contains(s))
    }

    if (iTypeOnly && sources.nonEmpty) {
      // only IDF gridded dataset sources were allowed, but other types were found
      throw new BadRequestException(
        s"Unsupported IDF gridded dataset source${if (sources.size == 1) "" else "s"}: ${sources.mkString(",")}",
        Some(s"Supported sources: ${supIdfGridNames.mkString(", ")}"))
    }

    // at this point, any remaining sources are either station sources or misspelled IDF gridded dataset names
    stNumbers = sources map(s => SourceSpecification.extractStationNumber(s) match {
      case Some(x) => x
      case None => throw new BadRequestException(
        s"Source misspelled or not found: $s",
        Some({
          val prefix = SourceSpecification.stationPrefix
          s"A station name must have the form $prefix<int>[:<int>|all] (e.g. ${prefix}18700, ${prefix}18700:0, " +
            s"or ${prefix}18700:all, where the content behind a colon specifies the sensor channel(s)). " +
            s"An IDF gridded dataset name must be one of: ${supIdfGridNames.mkString(", ")}."
        })
      )
    })

    stNames = sources
  }

  // Returns any station names found.
  def stationNames: Seq[String] = stNames

  // Returns any station numbers found.
  def stationNumbers: Seq[String] = stNumbers

  // Returns any IDF gridded dataset names found.
  def idfGridNames: Seq[String] = igNames

  init()
}


/**
 * Parsing of sources.
 */
object SourceSpecification {

  def apply(srcstr: Option[String], typestr: Option[String] = None) = new SourceSpecification(srcstr, typestr)

  private def stationPrefix = "SN"

  // Validates and returns a station number, i.e. the part of the station name after the "SN" prefix.
  private def extractStationNumber(stationName: String): Option[String] = {
    val pattern = s"""(?i)$stationPrefix(\\d+([:](([\\d+])|(?i)all))?)""".r
    stationName match {
      case pattern(x,_,_,_) => Some(x)
      case _ => None
    }
  }


  def sql(sources: Seq[String], stNr: String, snNr: Option[String]):String = {

    def querySourceAndSensor(source: String, stNr: String, snNr: Option[String]) : String = {
      val s = source.split(":")
      val sourceId = s(0)
      val sensorId = if (s.length > 1) s(1) else "0"
      if (snNr.isEmpty) {
        s"""($stNr = $sourceId)"""
      } else {
        sensorId.toUpperCase match {
          case "ALL" => s"""($stNr = $sourceId)"""
          case _ => s"""($stNr = $sourceId AND ${snNr.get} = $sensorId)"""
        }
      }
    }

    sources.map(querySourceAndSensor(_, stNr, snNr)).mkString(" OR ")

  }

}

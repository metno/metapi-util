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


class SourceSpecification(srcstr: Option[String], typestr: Option[String] = None) {

  // Type 1: Stations
  private var stNames: Seq[String] = Seq[String]() // complete station names, including "SN" prefix
  private var stNumbers: Seq[String] = Seq[String]() // station numbers, i.e. station names without "SN" prefix

  // Type 2: IDF gridded datasets
  private var igNames: Seq[String] = Seq[String]() // IDF gridded dataset names

  // Type 3: ...
  // ...


  private var reqTypes: Set[String] = Set[String]() // requested types
  private var supTypes = Set[String]() // supported types


  // Initializes object by extracting sources from srcstr into sequences for their respective types. Considers typestr as an extra restriction.
  // Throws BadRequestException upon error.
  private def init() = {
    // extract sources
    var sources: Seq[String] = srcstr.getOrElse("") split "," map(_.trim) filter(_.nonEmpty)

    // define supported types
    supTypes = supTypes ++ Set(StationConfig.typeName) // type 1
    supTypes = supTypes ++ Set(IDFGridConfig.typeName) // type 2
    // ... // type 3

    // extract and validate types
    reqTypes = typestr.getOrElse("").toLowerCase split "," map(_.trim) filter(_.nonEmpty) toSet
    val unsupTypes = reqTypes -- supTypes.map(_.toLowerCase)
    if (unsupTypes.nonEmpty) {
      throw new BadRequestException(
        s"Unsupported type${if (unsupTypes.size == 1) "" else "s"}: ${unsupTypes.mkString(",")}",
        Some(s"Supported types: ${supTypes.mkString(", ")}"))
    }

    val supIdfGridNames = Set(IDFGridConfig.name) // supported IDF grid names (only one name for now!)

    // move IDF gridded dataset sources into igNames if possible
    if (typeAllowed(IDFGridConfig.typeName)) {
      igNames = sources.filter(s => supIdfGridNames.contains(s))
      sources = sources.filter(s => !igNames.contains(s))

      // flag an error if other types were found when only IDF gridded dataset sources were allowed
      if (!typeAllowed(StationConfig.typeName) && sources.nonEmpty) {
        throw new BadRequestException(
          s"Unsupported IDF gridded dataset source${if (sources.size == 1) "" else "s"}: ${sources.mkString(",")}",
          Some(s"Supported sources: ${supIdfGridNames.mkString(", ")}"))
      }
    }

    // at this point, any remaining sources are either station sources or misspelled IDF gridded dataset names
    if (typeAllowed(StationConfig.typeName)) {
      stNumbers = sources map (s => SourceSpecification.extractStationNumber(s) match {
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
  }


  // Type 1

  // Returns any station names found.
  def stationNames: Seq[String] = stNames

  // Returns any station numbers found.
  def stationNumbers: Seq[String] = stNumbers


  // Type 2

  // Returns any IDF gridded dataset names found.
  def idfGridNames: Seq[String] = igNames


  // Type 3

  // ...


  // Returns true iff the given type is included (implicitly or explicitly) in the typestr passed when instantiating the object.
  def typeAllowed(typeName: String): Boolean = {
    val typeNameLC = typeName.toLowerCase
    supTypes.map(_.toLowerCase).contains(typeNameLC) && (reqTypes.isEmpty || reqTypes.contains(typeNameLC))
  }

  // Returns true iff no supported and allowed sources were found.
  def isEmpty: Boolean = stNames.isEmpty && igNames.isEmpty


  init() // initialize
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

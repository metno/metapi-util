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

/**
 * Parsing of sources.
 */
object SourceSpecification {

  /**
   * Attempts to extract a list of climate station numbers from a string.
   * @param sources A list of one or more climate station numbers prefixed with "SN", e.g. "SN1234, SN4567".
   */
  def parse(sources: Option[String]): Seq[String] = {

    /** Returns the integer resulting from removing a prefix from a string.
     * @param s Input string, expected to include the prefix.
     * @param prefix Prefix, expected to be a combination of characters from [a-z] and [A-Z].
     *   Special characters are not guaranteed to work (in particular not '(' and ')').
     */
    def stripPrefixFromInt(s: String, prefix: String): String = {
      val pattern = s"""$prefix(\\d+([:](([\\d+])|ALL))?)""".r
      s match {
        case pattern(x,_,_,_) => x
        case _ => throw new BadRequestException(
            s"Invalid source name: $s (expected $prefix<int>)",
            Some(s"Currently, all sources must have the prefix $prefix, like this: ${prefix}18700, and may optionally contain a specification of the sensor channel; e.g., SN18700:0, SN18700:1 or SN18700:all.")
          )
      }
    }

    sources match {
      case Some(x) => x split "," map (s => stripPrefixFromInt(s.toUpperCase.trim().toString, "SN")) toSeq
      case _ => Seq()
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
        sensorId match {
          case "ALL" => s"""($stNr = $sourceId)"""
          case _ => s"""($stNr = $sourceId AND ${snNr.get} = $sensorId)"""
        }
      }
    }
    
    sources.map(querySourceAndSensor(_, stNr, snNr)).mkString(" OR ")
    
  }

}

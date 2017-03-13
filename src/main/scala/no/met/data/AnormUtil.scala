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

import anorm._

//$COVERAGE-OFF$ Postponed

object AnormUtil {

  // Converts a SQL query to a version where placeholder base tags have been replaced with comma-separated, indexed tags
  // to support prepared statements (which in turn serves to prevent SQL injection).
  def insertPlaceholders(query: String, items: List[(String, Int)]): String = {
    var result = query
    items.foreach( item => {
      val tag = item._1
      val size = item._2
      val toBeReplaced = "{%s}" format tag
      val tags = (1 to size) map ("%s%d" format (tag, _))
      val tagsWithBraces = tags map ("{%s}" format _)
      val replacement = "(%s)" format (tagsWithBraces mkString ",")
      result = result replace (toBeReplaced, replacement)
    })
    result
  }

  // Generates the argument to pass to the on() function for a query that has been converted using insertPlaceholders().
  def onArg(items: List[(String, List[String])]): Seq[NamedParameter] = {
    var result = Seq[NamedParameter]()
    items.foreach( item => {
      val tag = item._1
      val values = item._2
      val tags = (1 to values.length) map ("%s%d" format (tag, _))
      val paramValues: List[ParameterValue] = values map (ParameterValue.toParameterValue(_))
      result = result ++ (tags zip paramValues map { (x) => new NamedParameter(x._1, x._2) })
    })
    result
  }

}

//$COVERAGE-ON$

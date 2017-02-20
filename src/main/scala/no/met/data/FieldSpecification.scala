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

import scala.language.postfixOps
import scala.util._

class FieldSpecification(fields: Option[Set[String]]) {

  /**
   * If the wanted keyword exists in contructor's fields, or fields is None,
   * execute function and return result. Else return None.
   */
  def apply[T](wanted: String)(t: () => T): Option[T] = {
    fields match {
      case Some(spec) if (!spec.contains(wanted)) => None
      case _ => Some(t())
    }
  }
}


/**
 * Parsing of fields.
 */
object FieldSpecification {

  /**
   * Create a FieldSpecification object, using the parse function.
   */
  def apply(fields: Option[String]): FieldSpecification = {
    val fieldSet = parse(fields)
    if (fieldSet isEmpty) {
      new FieldSpecification(None)
    } else {
      new FieldSpecification(Some(fieldSet))
    }
  }


  /** Create a set of field entries from the string. Returns an empty set if None.
   * @param fields A comma-delimited list of field strings.
   */
  def parse(fields: Option[String]): Set[String] = {
    fields match {
      case Some(x) => x.toLowerCase.split(",").map(_.trim).toSet
      case _ => Set()
    }
  }

}

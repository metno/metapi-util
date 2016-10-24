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

object QueryStringUtil {

  /** Throws BadRequestException iff a set of actual query string fields is not a subset of the supported fields.
    *
    * @param supportedFields The set of field names allowed in the query string.
    * @param actualFields The set of field names actually found in the query string.
    */
  def ensureSubset(supportedFields: Set[String], actualFields: Set[String]): Unit = {
    val unsupportedFields = actualFields -- supportedFields
    if (unsupportedFields.nonEmpty) {
      throw new BadRequestException(
        unsupportedFields.mkString(s"Unsupported field${if (unsupportedFields.size == 1) "" else "s"} in query string: ", ", ", ""),
        Some(supportedFields.mkString(s"Supported field${if (supportedFields.size == 1) "" else "s"}: ", ", ", "")))
    }
  }
}

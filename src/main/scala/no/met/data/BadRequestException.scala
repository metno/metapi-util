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


import com.github.nscala_time.time.Imports._

/**
 * Throwing this exception is a signal to a controller that it should return
 * Bad Request. Any other exceptions should return Internal Server Error.
 * @param start  The start time of this session
 * @param reason The reason for the error that this user has encountered.
 * @param help Extra information regarding the error, that may be useful to a user
 * @param headers Extra headers that should be included in a response with this error
 */
class BadRequestException(
  reason: String,
  val help: Option[String] = None,
  val headers: Seq[(String, String)] = Seq.empty[(String, String)]) extends Exception(reason) {
  /**
   * The error code to use. Subclasses may redefine this to return other exit
   * codes.
   */
  val code: Int = 400 // scalastyle:ignore
}

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

import play.api.Configuration
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results.Status
import com.github.nscala_time.time.Imports._
import no.met.json.ErrorJsonFormat

object Error {
  def error(code: Int, msg: Option[String], help: Option[String], start: DateTime = DateTime.now(DateTimeZone.UTC))(implicit configuration: Configuration, request: RequestHeader): Result = {
    Status(code)(new ErrorJsonFormat().error(code, msg, help, start)(configuration, request))
  }
}

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

package no.met.data.format.json

import no.met.data._
import play.api.libs.json._
import play.api.mvc._
import com.github.nscala_time.time.Imports._

/**
 * Functionality to provide error messages as json output
 */
class ErrorJsonFormat extends BasicJsonFormat {

  /**
   * Basic error information
   */
  private case class ErrorData(code: Int, message: String, help: Option[String]) {
    def reason: String = {
      val reasons = Map[Int, String](400 -> "Bad Request", 401 -> "Unauthorized", 404 -> "Not found", 500 -> "Internal Server Error")
      reasons.getOrElse(code, code.toString)
    }
  }

  /**
   * Error information with header data
   */
  private case class CompleteErrorData(header: BasicResponseData, errorData: ErrorData)

  private implicit val errorWrites: Writes[ErrorData] = new Writes[ErrorData] {
    def writes(error: ErrorData): JsObject = Json.obj(
      "errorCode" -> error.code,
      "errorReason" -> error.reason,
      "errorMessage" -> error.message,
      "errorHelp" -> error.help)
  }

  private implicit val completeErrorWrites: Writes[CompleteErrorData] = new Writes[CompleteErrorData] {
    def writes(response: CompleteErrorData): JsObject = {
      header(response.header) + ("error", Json.toJson(response.errorData))
    }
  }

  /**
   * Write a json string, specifying the given error
   */
  def error(code: Int, message: String, help: Option[String] = None)(implicit request: RequestHeader): String = {
    val duration = new Duration(0)
    val header = BasicResponseData("Response", "Observations", "v0", duration, 1, 1, 1, 0, None, None)
    val errorData = ErrorData(code, message, help)

    val response = CompleteErrorData(header, errorData)
    Json.prettyPrint(Json.toJson(response))
  }

}

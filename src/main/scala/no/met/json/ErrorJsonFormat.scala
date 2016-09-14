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

package no.met.json

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import com.github.nscala_time.time.Imports._
import io.swagger.annotations._
import java.net.URL
import scala.annotation.meta.field
import no.met.data._

// scalastyle:off line.size.limit

/**
 * Functionality to provide error messages as json output
 */
class ErrorJsonFormat extends BasicJsonFormat {

  @ApiModel(description="MET API error response.")
  case class ErrorResponse(
    @(ApiModelProperty @field)(name=ApiConstants.CONTEXT_NAME, value=ApiConstants.CONTEXT, example=ApiConstants.METAPI_CONTEXT) context: URL,
    @(ApiModelProperty @field)(name=ApiConstants.OBJECT_TYPE_NAME, value=ApiConstants.OBJECT_TYPE, example="ErrorResponse") responseType: String,
    @(ApiModelProperty @field)(value=ApiConstants.API_VERSION, example=ApiConstants.API_VERSION_EXAMPLE) apiVersion: String,
    @(ApiModelProperty @field)(value=ApiConstants.LICENSE, example=ApiConstants.METAPI_LICENSE) license: URL,
    @(ApiModelProperty @field)(value=ApiConstants.CREATED_AT, dataType="String", example=ApiConstants.CREATED_AT_EXAMPLE) createdAt: DateTime,
    @(ApiModelProperty @field)(value=ApiConstants.QUERY_TIME, dataType="String", example=ApiConstants.QUERY_TIME_EXAMPLE) queryTime: Duration,
    @(ApiModelProperty @field)(value=ApiConstants.CURRENT_LINK, example=ApiConstants.CURRENT_LINK_EXAMPLE) currentLink: URL,
    @(ApiModelProperty @field)(value=ApiConstants.DATA) error: ErrorReport
  )

  @ApiModel(description="MET API Error report.")
  case class ErrorReport(
    @(ApiModelProperty @field)(value="The HTTP Error code.", example="404") code: Int,
    @(ApiModelProperty @field)(value="A human readable message describing the error.", example="Data not found.") message: String,
    @(ApiModelProperty @field)(value="Optional text describing the reason for the error.", example="No data sources exist with the given source IDs.") reason: Option[String],
    @(ApiModelProperty @field)(value="Optional help text that may provide additional help.", example="Use /observations/timeSeries to determine which data sources exist with the given parameters.") help: Option[String]
  )

  implicit val errorReportWrites = Json.writes[ErrorReport]

  implicit val errorResponseWrites : Writes[ErrorResponse] = (
    (JsPath \ ApiConstants.CONTEXT_NAME).write[URL] and
    (JsPath \ ApiConstants.OBJECT_TYPE_NAME).write[String] and
    (JsPath \ ApiConstants.API_VERSION_NAME).write[String] and
    (JsPath \ ApiConstants.LICENSE_NAME).write[URL] and
    (JsPath \ ApiConstants.CREATED_AT_NAME).write[DateTime] and
    (JsPath \ ApiConstants.QUERY_TIME_NAME).write[Duration] and
    (JsPath \ ApiConstants.CURRENT_LINK_NAME).write[URL] and
    (JsPath \ "error").write[ErrorReport]
  )(unlift(ErrorResponse.unapply))

  def message(code: Int): String = {
    val messages = Map[Int, String](
        400 -> "Bad Request",
        401 -> "Unauthorized",
        404 -> "Not found",
        500 -> "Internal Server Error")
    messages.getOrElse(code, code.toString)
  }

  /**
   * Write a json string, specifying the given error
   */
  def error(start: DateTime, code: Int, reason: Option[String] = None, help: Option[String] = None)(implicit request: RequestHeader): String = {
    val duration = new Duration(DateTime.now.getMillis() - start.getMillis())
    val response = new ErrorResponse( new URL(ApiConstants.METAPI_CONTEXT),
                                      "ErrorResponse",
                                      "v0",
                                      new URL(ApiConstants.METAPI_LICENSE),
                                      start,
                                      duration,
                                      new URL(ConfigUtil.urlStart + request.uri),
                                      new ErrorReport(code, message(code), reason, help))
    Json.prettyPrint(Json.toJson(response))
  }

}

// scalastyle:on

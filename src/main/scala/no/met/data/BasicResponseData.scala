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

import play.api.mvc._
import com.github.nscala_time.time.Imports._
import java.net.URL

/**
 * All data that could go into all possible response documents from data.met.no
 */
class BasicResponseData(
  val context: URL,
  val contextType: String,
  val id: String,
  val apiVersion: String,
  val license: URL,
  val createdAt: DateTime,
  val queryTime: Duration,
  val currentItemCount: Long,
  val itemsPerPage: Long,
  val startOffset: Long,
  val totalItemCount: Long,
  val nextLink: Option[URL],
  val previousLink: Option[URL],
  val currentLink: URL)

object BasicResponseData {

  /**
   * Simplest construction of BasicResponseData structure
   */
  def apply[A]( // scalastyle:ignore
    contextType: String,
    id: String,
    apiVersion: String,
    queryTime: Duration,
    totalItemCount: Long = 1,
    currentItemCount: Long = 1,
    itemsPerPage: Long = 1,
    startOffset: Long = 0,
    nextLink: Option[URL] = None,
    previousLink: Option[URL] = None)(implicit request: Request[A]): BasicResponseData = {
    new BasicResponseData(
      new URL("https://data.met.no/schema/"),
      contextType,
      id,
      apiVersion,
      new URL("http://met.no/English/Data_Policy_and_Data_Services/"),
      new DateTime(),
      queryTime,
      currentItemCount,
      itemsPerPage,
      startOffset,
      totalItemCount,
      nextLink,
      previousLink,
      new URL(ConfigUtil.urlStart + request.uri))
  }
}

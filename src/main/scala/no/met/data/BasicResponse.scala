/*
    MET-API

    Copyright (C) 2016 met.no
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
import java.net.URL

/**Basic Response Template
 * We set the the individual case classes to inherit from BasicResponse in order
 * to enforce consistency (i.e., if the BasicResponse is modified, then the underlying
 * case classes will also require modification.
 * 
 * To drop the consistency requirement, simply ignore the base class. 
 */
abstract class BasicResponse(
  context: URL,
  responseType: String,
  apiVersion: String,
  license: URL,
  createdAt: DateTime,
  queryTime: Duration,
  currentItemCount: Long,
  itemsPerPage: Long,
  offset: Long,
  totalItemCount: Long,
  nextLink: Option[URL],
  previousLink: Option[URL],
  currentLink: URL
)

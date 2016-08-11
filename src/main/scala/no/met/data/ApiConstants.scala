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

object ApiConstants {
  final val METAPI_CONTEXT="https://data.met.no/schema/"
  final val METAPI_LICENSE="http://met.no/English/Data_Policy_and_Data_Services/"
  final val CONTEXT_NAME="@context"
  final val CONTEXT="The Json-LD context."
  final val OBJECT_TYPE_NAME="@type"
  final val OBJECT_TYPE="The object type."
  final val API_VERSION_NAME="apiVersion"
  final val API_VERSION="The version of the API that generated this response."
  final val API_VERSION_EXAMPLE="v0"
  final val LICENSE_NAME="license"
  final val LICENSE="The license that applies to this content."
  final val CREATED_AT_NAME="createdAt"
  final val CREATED_AT="The time at which this document was created (RFC 3339)."
  final val CREATED_AT_EXAMPLE="2007-11-06T16:34:41.000Z"
  final val QUERY_TIME_NAME="queryTime"
  final val QUERY_TIME="The time, in seconds, that this document took to generate."
  final val QUERY_TIME_EXAMPLE="0.025"
  final val CURRENT_ITEM_COUNT_NAME="currentItemCount"
  final val CURRENT_ITEM_COUNT="The current number of items in this result set."
  final val CURRENT_ITEM_COUNT_EXAMPLE="3456"
  final val ITEMS_PER_PAGE_NAME="itemsPerPage"
  final val ITEMS_PER_PAGE="The maximum number of items in a result set."
  final val ITEMS_PER_PAGE_EXAMPLE="1000"
  final val OFFSET_NAME="offset"
  final val OFFSET="The offset of the first item in the result set. The MET API uses a zero-base index."
  final val OFFSET_EXAMPLE="2000"
  final val TOTAL_ITEM_COUNT_NAME="totalItemCount"
  final val TOTAL_ITEM_COUNT="The total number of items in this specific result set."
  final val TOTAL_ITEM_COUNT_EXAMPLE="1000"
  final val NEXT_LINK_NAME="nextLink"
  final val NEXT_LINK="The next link indicates how more data can be retrieved. It points to the URI to load the next set of data."
  final val NEXT_LINK_EXAMPLE="https://data.met.no/resource/v0.jsonld?param=example_param&offset=3000"
  final val PREVIOUS_LINK_NAME="previousLink"
  final val PREVIOUS_LINK="The previous link indicates how more data can be retrieved. It points to the URI to load the previous set of data."
  final val PREVIOUS_LINK_EXAMPLE="https://data.met.no/resource/v0.jsonld?param=example_param&offset=1000"
  final val CURRENT_LINK_NAME="currentLink"
  final val CURRENT_LINK="The current link indicates the URI that was used to generate the current API response"
  final val CURRENT_LINK_EXAMPLE="https://data.met.no/resource/v0.jsonld?param=example_param&offset=2000"
  final val DATA_NAME="data"
  final val DATA="Container for all the data from the response."
}

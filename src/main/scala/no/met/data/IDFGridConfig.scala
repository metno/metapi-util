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

object IDFGridConfig {
  def typeName: String = "InterpolatedDataset"
  // WARNING: Hard-coded values may need to be updated upon changes to source data.
  def name: String = "idf_bma1km_v1"
  def diskName: String = "idf_grid_interpolated_1km" // TBD: use name for diskName once directory has been renamed to the former
  def validFrom: String = "1957-01-01T00:00:00Z"
  def validTo: String = "2016-01-01T00:00:00Z"
  // scalastyle:off magic.number
  def numberOfSeasons: Int = 59
  // scalastyle:on magic.number
}

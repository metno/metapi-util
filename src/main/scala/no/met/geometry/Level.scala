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

package no.met.geometry

import io.swagger.annotations._
import scala.annotation.meta.field

/**
 * Support for Level objects in the data model.
 */

// scalastyle:off line.size.limit

case class Level(
  @(ApiModelProperty @field)(value="The level type or parameter. This defines the reference for the level value.", example="height_above_ground") levelType: Option[String],
  @(ApiModelProperty @field)(value="The value of the level data.", example="42") value: Option[Double],
  @(ApiModelProperty @field)(value="The unit of measure of the level data. *code* if the unit is described using a code table.", example="m") unit: Option[String],
  @(ApiModelProperty @field)(value="If the unit is a *code*, the codetable that describes the codes used.", example="beaufort_scale") codeTable: Option[String]
)

// scalastyle:on

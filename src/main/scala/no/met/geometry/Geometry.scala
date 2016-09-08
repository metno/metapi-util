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
import scala.util.matching._
import java.util.regex.{Pattern, Matcher}
import no.met.data.BadRequestException

/* Simple regular expressions to parse a WKT geometry with interpolation.
 */

case class GeometryProperty(
  interpolation: Option[String]
)

sealed abstract class Geometry(properties: Option[GeometryProperty]) {
  /** Return the geometry as a WKT string. ex POINT (9 62) */
  def asWkt:String
  override def toString():String = asWkt
}

object Geometry {
  val value = "[+-]?([0-9]*[.])?[0-9]+(E[-+]?[0-9]+)?"
  val valuePattern = value.r
  val coordinatePair = (value + "\\s+" + value)
  val coordinatePairPattern = coordinatePair.r
  val point = "POINT\\s*\\(\\s*" + coordinatePair  + "\\s*\\)"
  val pointPattern = point.r
  val nearest = "NEAREST\\s*\\(\\s*" + point + "\\s*\\)"
  val nearestPattern = nearest.r
  val innerRing = "\\(\\s*" + coordinatePair + "\\s*,\\s*" +  coordinatePair +  "\\s*,\\s*" +  coordinatePair + "\\s*(,\\s*" +  coordinatePair + ")*\\s*\\)"
  val innerRingPattern = innerRing.r
  val polygon = "POLYGON\\s*\\(\\s*" + innerRing + "\\s*(,\\s*" + innerRing + ")*\\s*\\)"
  val polygonPattern = polygon.r
  
  def decode(geometry : String) : Geometry = {
    val geom = geometry.toUpperCase
    if (nearestPattern.pattern.matcher(geom).matches)
      Point(coordinates=getPointCoordinates(geom), properties=Some(GeometryProperty(Some("nearest"))))
    else if (pointPattern.pattern.matcher(geom).matches)
      Point(coordinates=getPointCoordinates(geom))
    else if (polygonPattern.pattern.matcher(geom).matches)
      Polygon(coordinates=getPolygonCoordinates(geom))
    else
      throw new BadRequestException(geometry + " is not a valid data.met.no MET API geometry specification")
  }

  private def getPointCoordinates(coord: String) : Seq[Double] = {
    decodeCoordinatePair(coord)
  }

  
  private def getPolygonCoordinates(polygon: String) : Seq[Seq[Seq[Double]]] = {
    innerRingPattern.findAllIn(polygon).toSeq map( x => decodeRing(x) )
  }
  
  private def decodeRing(ring : String) : Seq[Seq[Double]] = { 
    coordinatePairPattern.findAllIn(ring).toSeq map( x => decodeCoordinatePair(x) )
  }
  
  private def decodeCoordinatePair(valuePair : String) : Seq[Double] = { 
    valuePattern.findAllIn(valuePair).toSeq map( x => x.toDouble )
  }
  
}

case class Point(
  @(ApiModelProperty @field)(name="@type", value="The type of the geometry object", example="Point") geomType: String = "Point",
  @(ApiModelProperty @field)(value="Coordinates of the geometry object", example="59.9423, 10.72") coordinates: Seq[Double],
  @(ApiModelProperty @field)(hidden=true) properties: Option[GeometryProperty] = None
)  extends Geometry(properties) {
  def asWkt:String = s"POINT(${coordinates(0)} ${coordinates(1)})"
}


case class Polygon(
  @(ApiModelProperty @field)(name="@type", value="The type of the geometry object", example="Polygon") geomType: String = "Polygon",
  @(ApiModelProperty @field)(value="Coordinates of the geometry object", example="[[100.0, 0.0],[101.0, 0.0],[101.0, 1.0],[100.0, 1.0],[100.0, 0.0]]") coordinates: Seq[Seq[Seq[Double]]],
  @(ApiModelProperty @field)(hidden=true) properties: Option[GeometryProperty] = None
)  extends Geometry(properties) {
  // Has to be a better way to do this in Scala, but this does the job. @michaeloa
  def asWkt:String = {
    var text = "POLYGON("
    for (polygons <- coordinates) {
      text += "("
      for (i <- 0 to polygons.size-1) {
        if (i != 0)
          text +=","
        text += s"${polygons(i)(0)} ${polygons(i)(1)}"
      }
      text += ")"
    }
    text + ")"
  }
}

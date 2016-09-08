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

package test

import org.junit.runner._
import org.specs2.execute._
import org.specs2.mutable._
import org.specs2.runner._
import no.met.data.BadRequestException
import no.met.geometry._

@RunWith(classOf[JUnitRunner])
class GeometrySpec extends Specification {

  "Point object" should {

    "return valid WKT from toString" in {

      Point(coordinates=Seq(30,10)).toString mustEqual("POINT(30.0 10.0)")
      
      Point(coordinates=Seq(54.5,10.2)).toString mustEqual("POINT(54.5 10.2)")
  
    }

  }

  "Polygon object" should {

    "return valid WKT from toString" in {

      Polygon(coordinates=Seq(Seq(Seq(30,10), Seq(40,40),Seq(20,40),Seq(10,20),Seq(30,10)))).toString mustEqual("POLYGON((30.0 10.0,40.0 40.0,20.0 40.0,10.0 20.0,30.0 10.0))")
  
    }

  }

  "Geometry class" should {

    "decode the valid WKT of POINT(30 10) to a Point" in {
      
      val geom = Geometry.decode("POINT(30 10)")
      geom match {
        case x:Point => x.coordinates mustEqual(Seq(30.0,10.0))
        case _ => geom must haveClass[Point]
      }
      
    }

    "decode the valid WKT of POINT(30.0 10.0) to a Point" in {
      
      val geom = Geometry.decode("POINT(30.0 10.0)")
      geom match {
        case x:Point => x.coordinates mustEqual(Seq(30.0,10.0))
        case _ => geom must haveClass[Point]
      }
      
    }

    "fail to decode the invalid WKT POINT(30.0 10.0 5)" in {
      
      Geometry.decode("POINT(30.0 10.0 5)") must throwA[BadRequestException]
      
    }

    "decode a valid WKT of a Point and return valid WKT from object" in {
      
      val point = "POINT(10.54 59.98)"
      val geom = Geometry.decode(point)
      geom match {
        case x:Point => x.asWkt mustEqual(point)
        case _ => geom must haveClass[Point]
      }
      
    }
    
    "decode a valid WKT of a Polygon" in {
      
      val geom = Geometry.decode("POLYGON((30.0 10.0,40.0 40.0,20.0 40.0,10.0 20.0,30.0 10.0))")
      geom must haveClass[Polygon]
      
    }

    "fail to decode the invalid WKT POLYGON((30.0 10.0, 5 10))" in {
      
      Geometry.decode("POLYGON((30.0 10.0, 5 10))") must throwA[BadRequestException]
      
    }

    "decode a valid WKT of a Polygon and return correct WKT from object" in {
      
      val polygon = "POLYGON((30.0 10.0,40.0 40.0,20.0 40.0,10.0 20.0,30.0 10.0))"
      val geom = Geometry.decode(polygon)
      geom match {
        case x:Polygon => x.asWkt mustEqual(polygon)
        case _ => geom must haveClass[Polygon]
      }
    }
    
    "decode the nearest interpolation of POINT(30.1 10.2) to a Point with interpolation" in {
      
      val geom = Geometry.decode("nearest(POINT(30.1 10.2))")
      geom match {
        case x:Point => {
          x.coordinates mustEqual(Seq(30.1,10.2))
          x.properties.get.interpolation.getOrElse("") mustEqual("nearest")
        }
        case _ => geom must haveClass[Point]
      }
      
    }
    
  }
  /*
  
  POINT (30 10)
  
  POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))
  
  POLYGON ((35 10, 45 45, 15 40, 10 20, 35 10),(20 30, 35 35, 30 20, 20 30))
  
  "WKT Geometry object" should {
    
    "return valid WKT from toString" in {
      new Point(coordinates=Seq(54.5,10.2)).toString mustEqual("POINT(54.5 10.2)")
    }
    
  }
  */
  
}

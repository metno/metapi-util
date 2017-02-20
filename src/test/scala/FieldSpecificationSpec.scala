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
import org.specs2.mutable._
import org.specs2.runner._
import scala.util._
import no.met.data.FieldSpecification

// scalastyle:off magic.number

@RunWith(classOf[JUnitRunner])
class FieldSpecificationSpec extends Specification {

  "FieldSpecification's parse function" should {

    "parse single field" in {
      val s = Set("description")
      FieldSpecification.parse(Some("description")) must equalTo(s)
      FieldSpecification.parse(Some("description ")) must equalTo(s)
      FieldSpecification.parse(Some(" description")) must equalTo(s)
      FieldSpecification.parse(Some(" description ")) must equalTo(s)
      FieldSpecification.parse(Some("description,")) must equalTo(s)
      FieldSpecification.parse(Some("description,,")) must equalTo(s)
    }

    "parse multiple fields" in {
      val s = Set("name", "description")
      FieldSpecification.parse(Some("name,description")) must equalTo(s)
      FieldSpecification.parse(Some("name, description")) must equalTo(s)
      FieldSpecification.parse(Some("name ,description")) must equalTo(s)
      FieldSpecification.parse(Some("name , description")) must equalTo(s)
      FieldSpecification.parse(Some(" name,description ")) must equalTo(s)
      FieldSpecification.parse(Some(" name , description ")) must equalTo(s)
    }

    "parse duplicate fields" in {
      val s = Set("name", "description")
      FieldSpecification.parse(Some("name,description,name")) must equalTo(s)
      FieldSpecification.parse(Some("name,   name, description")) must equalTo(s)
      FieldSpecification.parse(Some("name,name ,description")) must equalTo(s)
      FieldSpecification.parse(Some("name , description, name ")) must equalTo(s)
      FieldSpecification.parse(Some(" description, name,description ")) must equalTo(s)
      FieldSpecification.parse(Some(" name , description , name")) must equalTo(s)
    }

    "parse empty fields" in {
      FieldSpecification.parse(None) must equalTo(Set())
      FieldSpecification.parse(Some(",")) must equalTo(Set())
      FieldSpecification.parse(Some(",,")) must equalTo(Set())
    }

    /*
    "throw exception" in {
      FieldSpecification.parse(", ,") must throwA[Exception]
      FieldSpecification.parse(",SN1234") must throwA[Exception]
      FieldSpecification.parse(",SN1234 ,") must throwA[Exception]
      FieldSpecification.parse(",,SN1234 ,") must throwA[Exception]
      FieldSpecification.parse(",SN1234 ,  SN5678") must throwA[Exception]
      FieldSpecification.parse(",SN1234 ,  SN5678,,") must throwA[Exception]
      FieldSpecification.parse("XX1234") must throwA[Exception]
      FieldSpecification.parse("SN 1234") must throwA[Exception]
      FieldSpecification.parse("SN") must throwA[Exception]
      FieldSpecification.parse(",SN1234") must throwA[Exception]
      FieldSpecification.parse("") must throwA[Exception]
    }
    */
  }

  "FieldSpecification objects" should {
    "handle None construction" in {
      FieldSpecification(None)("a"){()=>3} must_== Some(3)
    }

    "handle simple spec - give data" in {
      FieldSpecification(Some("a"))("a"){()=>3} must_== Some(3)
    }

    "handle simple spec - hide data" in {
      FieldSpecification(Some("a"))("b"){()=>3} must_== None
    }

    "handle complex spec - give data" in {
      FieldSpecification(Some("aba,gigi"))("gigi"){()=>2} must_== Some(2)
    }

    "handle complex spec - hide data" in {
      FieldSpecification(Some("aba,gigi"))("flopp"){()=>67} must_== None
    }

  }
}

// scalastyle:on

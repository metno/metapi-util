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
import org.specs2._
import org.specs2.runner._
import org.junit.runner._
import no.met.json._
import play.api.libs.json._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class JsonSpec extends mutable.Specification {

  "JsonHelper" should {

    "'/' should parse to empty Path" in {
      val p = JsonHelper.Path.parse("/")
      p must beSome.which(_.isEmpty)
    }

    "'/a' should parse to Path '/a'" in {
      val p = JsonHelper.Path.parse("/a")
      p must beSome.which(_.toString == "/a")
    }

    "'/a/@' should parse to Path '/a/@'" in {
      val p = JsonHelper.Path.parse("/a/@")
      p must beSome.which(_.toString == "/a/@")
    }

    "'/a/@3' should parse to Path '/a/@3'" in {
      val p = JsonHelper.Path.parse("/a/@3")
      p must beSome.which(_.toString == "/a/@3")
    }

    "'/a/@3/@' should parse to Path '/a/@3/@'" in {
      val p = JsonHelper.Path.parse("/a/@3/@")
      p must beSome.which(_.toString == "/a/@3/@")
    }

    "'/features/@' should process all elements in the array." in {
      val path = JsonHelper.Path.parse("/features/@")
      val v = new TestJson.JsonAcc

      JsonHelper.Parser.parse(TestJson.fileName, path.get, v.populate _)

      val js: JsArray = (TestJson.testJson \ "features").as[JsValue] match {
        case a: JsArray => a
        case _ => new JsArray
      }

      v.acc must beEqualTo(js.value)
    }

    "'/features/@1/geometry/coordinates' should process one element in the array." in {
      val path = JsonHelper.Path.parse("/features/@1/geometry/coordinates")
      val v = new TestJson.JsonAcc

      JsonHelper.Parser.parse(TestJson.fileName, path.get, v.populate _)
      val js = (TestJson.testJson \ "features")(1) \ "geometry" \ "coordinates"
      v.acc(0) must beEqualTo(js.as[JsValue])
    }

    "'/' should give the the complete json." in {
      val path = JsonHelper.Path.parse("/")
      val v = new TestJson.JsonAcc

      JsonHelper.Parser.parse(TestJson.fileName, path.get, v.populate _)
      v.acc(0) must beEqualTo(TestJson.testJson)
    }

    "'/' should give the the complete json from a zip file." in {
      import TestJson._
      val path = JsonHelper.Path.parse("/")
      val res = zipFile(zipFileName, zipEntry) match {
        case Some(in) =>
          val v = new JsonAcc
          JsonHelper.Parser.parse(in, path.get, v.populate _)
          Some(v)
        case _ => None
      }

      res must beSome.which(_.acc(0) == testJson)
    }

    "create object context with string should return a valid json object." in {
      import PlayJson.PlayFacade._
      val jsonExpect = """
           | { "key1":"val1", "key2":"val2" }
          """.stripMargin('|')
      val obj = objectContext
      obj.add("key1")
      obj.add("val1")
      obj.add("key2")
      obj.add("val2")

      val json = obj.finish
      val jsExpect = Json.parse(jsonExpect)
      json must beEqualTo(jsExpect)
      obj.isObj must beTrue
    }

    "create array context should return a valid json array." in {
      import PlayJson.PlayFacade._
      val jsonExpect = """
           | [ "val1", "val2", { "key3":"val3"}]
          """.stripMargin('|')
      val arr = arrayContext
      val obj = objectContext
      arr.add("val1")
      arr.add("val2")
      obj.add("key3")
      obj.add("val3")

      val jsObj = obj.finish
      arr.add( jsObj )

      val json = arr.finish
      val jsExpect = Json.parse(jsonExpect)
      json must beEqualTo(jsExpect)
      arr.isObj must beFalse
    }

  }
}

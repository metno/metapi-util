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
import com.github.nscala_time.time.Imports._
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import no.met.json._

// scalastyle:off magic.number

@RunWith(classOf[JUnitRunner])
class JsonSpec extends Specification {

  "Error json formatter" should {

    "return valid Json from error call" in new WithApplication() {
      val json = Json.parse(new ErrorJsonFormat().error(
          DateTime.now(DateTimeZone.UTC), 404, Some("This is a test."), Some("You do not need any help."))(FakeRequest(GET, "index.html")))
      (json \ "error" \ "code").as[Int] must equalTo(404)
      (json \ "error" \ "reason").as[String] must equalTo("This is a test.")
      (json \ "error" \ "help").as[String] must equalTo("You do not need any help.")
    }

    "return code as message for unknown error code" in new WithApplication() {
      val json = Json.parse(new ErrorJsonFormat().error(
          DateTime.now(DateTimeZone.UTC), 420, Some("This is a test."), Some("You do not need any help."))(FakeRequest(GET, "index.html")))
      (json \ "error" \ "code").as[Int] must equalTo(420)
      (json \ "error" \ "message").as[String] must equalTo("420") // Enhance your Calm
    }

  }
}

// scalastyle:on

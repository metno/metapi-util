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

import scala.collection.mutable
import play.api.libs.json._

object PlayJson {

  object PlayFacade extends Facade[JsValue] {

    def jnull(): JsValue = JsNull
    def jbool(v: Boolean): JsValue = JsBoolean(v)
    def jnum(v: BigDecimal): JsValue = JsNumber(v)
    def jstring(v: String): JsValue = JsString(v)

    def singleContext(): FContext[JsValue] =
      new FContext[JsValue] {
        var value: Option[JsValue] = None
        def add(s: String): Unit = { value = Some(jstring(s)) }
        def add(v: JsValue): Unit = { value = Some(v) }
        def finish: JsValue = value getOrElse (JsNull)
        def isObj: Boolean = false
      }

    def arrayContext(): FContext[JsValue] =
      new FContext[JsValue] {
        val vs = mutable.ListBuffer.empty[JsValue]
        def add(s: String): Unit = { vs += jstring(s) }
        def add(v: JsValue): Unit = { vs += v }
        def finish: JsValue = JsArray(vs.toList)
        def isObj: Boolean = false
      }

    def objectContext(): FContext[JsValue] =
      new FContext[JsValue] {
        var key: Option[String] = None
        var vs = List.empty[(String, JsValue)]
        def add(s: String): Unit =
          key = key match {
            case Some(k) =>
              vs = (k, jstring(s)) :: vs; None
            case _ => Some(s)
          }
        def add(v: JsValue): Unit =
          key = key flatMap { k => vs = (k, v) :: vs; None }
        def finish: JsValue = JsObject(vs.reverse)
        def isObj: Boolean = true
      }
  }
}

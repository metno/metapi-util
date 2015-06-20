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

/**
 * Facade is a type class that describes how Jawn should construct
 * JSON AST elements of type J.
 *
 * Facade[J] also uses FContext[J] instances, so implementors will
 * usually want to define both.
 */
trait Facade[J] {
  def singleContext(): FContext[J]
  def arrayContext(): FContext[J]
  def objectContext(): FContext[J]

  def jnull(): J
  def jbool(v: Boolean): J
  def jnum(s: BigDecimal): J
  def jstring(s: String): J
}

/**
 * FContext is used to construct nested JSON values.
 *
 * The most common cases are to build objects and arrays. However,
 * this type is also used to build a single top-level JSON element, in
 * cases where the entire JSON document consists of one value, ex "123".
 */
trait FContext[J] {
  def add(s: String): Unit
  def add(v: J): Unit
  def finish: J
  def isObj: Boolean
}

class FContextStack[J] extends Stack[FContext[J]] {
  import scala.collection.mutable.Stack
  private val stack = new Stack[FContext[J]]()
  def push(e: FContext[J]): Unit = stack.push(e)
  def pop(): Option[FContext[J]] = if (stack.isEmpty) None else Some(stack.pop)
  def peek(): Option[FContext[J]] = stack.headOption
  def colapseTop: Boolean =
    if (size > 1) {
      val v = pop.get.finish
      peek.get.add(v)
      true
    } else {
      false
    }
  override def name(n: String): Unit = stack map { _.add(n) }
  def finish: Option[J] = if (stack.size == 1) Some(stack.pop.finish) else None
  def isEmpty: Boolean = stack.isEmpty
  def size: Int = stack.size

}

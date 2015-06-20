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


/**
 * Use json4s https://github.com/json4s/json4s to create a push parser.
 */

package no.met.json
import play.api.libs.json._
import org.json4s.native._
import java.nio.file._
import java.nio.charset.Charset
import scala.util.{ Try, Success, Failure }
import scala.annotation.tailrec

//scalastyle:off cyclomatic.complexity

object JsonHelper {
  trait PathElement {
    def isEqual(other: PathElement): Boolean
  }

  case class ArrayPath(var n: Int = -1) extends PathElement {
    override def isEqual(other: PathElement): Boolean = {
      other match {
        case ArrayPath(o) if o >= 0 && (n < 0 || n == o) => true
        case _ => false
      }
    }
    def inc: Unit = n += 1
  }

  case class ObjPath(var field: String = "") extends PathElement {
    override def isEqual(other: PathElement): Boolean = {
      other match {
        case o: ObjPath => field == o.field
        case _ => false
      }
    }
  }

  class Path extends Stack[PathElement] {
    import scala.collection.mutable.Stack
    private val stack = new Stack[PathElement]()
    override def push(element: PathElement): Unit = stack.push(element)
    override def pop(): Option[PathElement] = if (stack.isEmpty) None else Some(stack.pop)
    override def peek(): Option[PathElement] = stack.headOption
    override def inc: Unit = peek map {
      _ match {
        case arr: ArrayPath => arr.inc
        case _ => ()
      }
    }

    def isEmpty: Boolean = stack.isEmpty

    override def name(n: String): Unit = stack.head match {
      case obj: ObjPath =>
        obj.field = n; ()
      case _ => ()
    }

    override def toString(): String = {
      stack reverseMap {
        _ match {
          case ArrayPath(n) => if (n < 0) "@" else s"@${n}"
          case ObjPath(f) => f
        }
      } mkString ("/", "/", "")
    }

    def isEqual(other: Path): Boolean = {
      if (stack.size != other.stack.size) {
        false
      } else {
        val r = stack zip other.stack takeWhile { case (t, o) => t isEqual o }
        r.size == stack.size
      }
    }

    def size: Int = stack.size
  }

  object Path {
    def parse(path: String): Option[Path] = {
      val p = new Path
      try {
        path split ("/") foreach { pathElement =>
          val s = pathElement.trim
          if (s.startsWith("@")) {
            val ss = s.substring(1).trim()
            if (ss.isEmpty()) {
              p.push(ArrayPath())
            } else { p.push(ArrayPath(ss.toInt)) }
          } else if (!s.isEmpty()) {
            p.push(ObjPath(s))
          }
        }

        if (!p.isEmpty && path.endsWith("/")) {
          p.push(ObjPath())
        }

        Some(p)
      } catch {
        case _: Throwable => None
      }
    }
  }

  object Parser {

    private class Parser(path: Path, f: (JsValue, String) => Unit) {
      private def doValue[V](ctx: FContextStack[JsValue], v: V): Option[Boolean] = {
        import PlayJson.PlayFacade._
        def value(js: JsValue): Option[Boolean] = {
          if (ctx.isEmpty) {
            ctx.push(singleContext())
            ctx.peek.map { _.add(js) }
            Some(false)
          } else {
            ctx.peek.map { _.add(js) }
            Some(true)
          }
        }

        v match {
          case JsonParser.StringVal(v: String) => value(JsString(v))
          case JsonParser.IntVal(v: BigInt) => value(JsNumber(BigDecimal(v)))
          case JsonParser.DoubleVal(v: Double) => value(JsNumber(v))
          case JsonParser.BigDecimalVal(v: BigDecimal) => value(JsNumber(v))
          case JsonParser.BoolVal(v: Boolean) => value(JsBoolean(v))
          case JsonParser.NullVal => value(JsNull)
          case _ => None
        }
      }

      def parsePath(p: JsonParser.Parser, token: JsonParser.Token): Option[JsValue] = {
        val ctx = new FContextStack[JsValue]

        @tailrec
        def doParsePath(t: JsonParser.Token): Boolean = {
          import PlayJson.PlayFacade._
          t match {
            case JsonParser.OpenObj =>
              ctx.push(objectContext()); doParsePath(p.nextToken)
            case JsonParser.CloseObj =>
              if (ctx.colapseTop) doParsePath(p.nextToken) else true
            case JsonParser.FieldStart(name: String) =>
              ctx.peek.map(_.add(name)); doParsePath(p.nextToken)
            case JsonParser.OpenArr =>
              ctx.push(arrayContext()); doParsePath(p.nextToken)
            case JsonParser.CloseArr =>
              if (ctx.colapseTop) doParsePath(p.nextToken) else true
            case JsonParser.End => true
            case jsValue => doValue(ctx, jsValue) match {
              case Some(true) => doParsePath(p.nextToken)
              case Some(false) => true
              case None => false
            }
          }
        }

        if (doParsePath(token)) ctx.finish else None
      }

      def parse(p: JsonParser.Parser): Try[Unit] = Try {
        val cp = new Path

        @tailrec
        def doParse(ctx: Path): Unit = {
          val t = p.nextToken
          t match {
            case JsonParser.OpenObj =>
              ctx.inc
              if (path isEqual ctx) {
                parsePath(p, t) map { f(_, ctx.toString()) }
              } else {
                ctx.push(ObjPath())
              }
              doParse(ctx)
            case JsonParser.CloseObj =>
              ctx.pop; doParse(ctx)
            case JsonParser.FieldStart(name: String) =>
              ctx.name(name); doParse(ctx)
            case JsonParser.OpenArr =>
              if (path isEqual ctx) {
                parsePath(p, t) map { f(_, ctx.toString()) }
              } else {
                ctx.push(ArrayPath())
              }
              doParse(ctx)
            case JsonParser.CloseArr =>
              ctx.pop; doParse(ctx)
            case JsonParser.End => ()
            case value =>
              ctx.inc
              if (path isEqual ctx) {
                parsePath(p, t) map { f(_, ctx.toString()) }
              }
              doParse(ctx)
          }
        }

        doParse(cp)
      }
    }

    /**
     * Incremental parsing of a json file.
     *
     * The json file is split into values, objects or arrays in compliance to a
     * path.
     *
     * The path is on the form
     *  - / - Parse the complete document and return it as a JsValue
     *  - /@ - If the document is an array return every single element as a JsValue
     *  - /@2 -  If the document is an array return the second element as a JsValue
     *  - /features - If the document is an object return the value of the property 'features'
     *          as an JsValue.
     *  - /features/@ - If the value to the property is an array. Return every element of the
     *          array as a JsValue
     */
    def parse(file: java.nio.file.Path, jsonPath: Path,
      f: (JsValue, String) => Unit): Try[Unit] = {
      parse(Files.newBufferedReader(file, Charset.forName("UTF-8")), jsonPath, f)
    }

    def parse(in: java.io.InputStream, jsonPath: Path,
      f: (JsValue, String) => Unit): Try[Unit] = {
      import java.io._
      val reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")))
      parse(reader, jsonPath, f)
    }

    def parse(in: java.io.Reader, jsonPath: Path,
      f: (JsValue, String) => Unit): Try[Unit] = {
      val p = new Parser(jsonPath, f)
      JsonParser.parse(in, p.parse _)
    }

  }
}

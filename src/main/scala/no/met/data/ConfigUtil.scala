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

import play.api.Play.current

//$COVERAGE-OFF$ Requires active Play application

/**
 * Various utilities for non-trivial reading of application configuration
 */
object ConfigUtil {

  private trait ServiceSpec {
    def scheme: String
    def host: String
    def port: String
  }
  private class ProductionServiceSpec extends ServiceSpec {
    override def scheme: String = current.configuration.getString("service.scheme") getOrElse "https"
    override def host: String = current.configuration.getString("service.host").get // fail and propagate internal server error (500) if absent
    override def port: String = current.configuration.getString("service.port").getOrElse("")
  }
  private class DevServiceSpec extends ServiceSpec {
    override def scheme: String = current.configuration.getString("service.scheme") getOrElse "http"
    override def host: String = current.configuration.getString("service.host") getOrElse "localhost"
    override def port: String = current.configuration.getString("service.port").getOrElse("9000")
  }

  private lazy val serviceSpec: ServiceSpec = {
    if (play.api.Play.isProd(play.api.Play.current)) {
      new ProductionServiceSpec()
    } else {
      new DevServiceSpec()
    }
  }

  /**
   * access scheme, such as http or https
   */
  def scheme: String = serviceSpec.scheme

  /**
   * host name
   */
  def host: String = serviceSpec.host

  /**
   * port number. Errors will not be detected here
   */
  def port: String = serviceSpec.port

  /**
   * Prefix to path
   */
  def pathPrefix: String = current.configuration.getString("service.pathPrefix") getOrElse ""

  /**
   * Returns a map that contains scheme, server, and pathPrefix derived from the service.* values in the configuration as follows:
   *
   *   - scheme: service.scheme, defaulting to https in PROD mode and to http in DEV/TEST mode.
   *   - server: service.host:service.port if service.port is a valid port number, otherwise service.host.
   *             If service.host is missing in PROD mode, an exception is thrown. In DEV/TEST mode, service.host defaults to localhost.
   *   - pathPrefix: service.pathPrefix, defaulting to "".
   */
  def serviceConf: Map[String, String] = {
    def validPort(s: String): Boolean = { // returns true iff s can be converted to a valid port number
      scala.util.Try(s.toInt) match {
        case scala.util.Success(x) => (x >= 0) && (x <= 65535)
        case _ => false
      }
    }
    Map(
      "scheme" -> scheme,
      "server" -> {
        port match {
          case port if (validPort(port)) => s"$host:$port"
          case _ => host
        }
      },
      "pathPrefix" -> pathPrefix)
  }

  /**
   * The starting part of any URLs this service provides
   */
  lazy val urlStart: String = {

    var ret = s"$scheme://$host"
    val p = port
    if (!p.isEmpty) {
      ret += s":$port"
    }
    val prefix = pathPrefix
    if (!prefix.isEmpty) {
      ret += s"/$pathPrefix"
    }
    ret + "/"
  }

}

//$COVERAGE-ON$
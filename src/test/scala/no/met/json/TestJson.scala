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
import play.api.libs.json._
import java.nio.file._
//import java.nio.file.Path
import java.nio.charset.Charset
import scala.collection.mutable._
import java.io.InputStream
object TestJson {

  val json = """
{  "type":"FeatureCollection",
   "features":[
        { "type":"Feature",
          "properties": { "skr_snskrstat":"G",
                          "enh_ssr_id":244,
                          "for_kartid":"",
                          "for_regdato":20000816,
                          "skr_sndato":19581101,
                          "enh_snmynd":"SK",
                          "for_sist_endret_dt":20100325,
                          "enh_snspraak":"NO",
                          "nty_gruppenr":1,
                          "enh_snavn":"Hestheia",
                          "enh_komm":1004,
                          "enh_ssrobj_id":245,
                          "enh_sntystat":"H",
                          "enh_navntype":6,
                          "for_snavn":"Hestheia",
                          "kom_fylkesnr":10,
                          "kpr_tekst":"Riksgrenseliste"
                        },
          "geometry": { "type":"Point",
                        "coordinates":[6.723450,58.246000 ]
                      }
        },
        { "type":"Feature",
          "properties": { "skr_snskrstat":"G",
                          "enh_ssr_id":805,
                          "for_kartid":"",
                          "for_regdato":20020618,
                          "skr_sndato":19600101,
                          "enh_snmynd":"SK",
                          "for_sist_endret_dt":20140305,
                          "enh_snspraak":"NO",
                          "nty_gruppenr":5,
                          "enh_snavn":"Moi",
                          "enh_komm":1032,
                          "enh_ssrobj_id":806,
                          "enh_sntystat":"H",
                          "enh_navntype":103,
                          "for_snavn":"Moi",
                          "kom_fylkesnr":10,
                          "kpr_tekst":"N50 Kartdata"
                        },
          "geometry": { "type":"Point",
                        "coordinates":[7.184372,58.245103 ]
                      }
        },
        { "type":"Feature",
          "properties": { "skr_snskrstat":"G",
                           "enh_ssr_id":1619,
                           "for_kartid":"1411-2",
                           "for_regdato":19870707,
                           "skr_sndato":19590101,
                           "enh_snmynd":"SK",
                           "for_sist_endret_dt":20060714,
                           "enh_snspraak":"NO",
                           "nty_gruppenr":5,
                           "enh_snavn":"Nome",
                           "enh_komm":1021,
                           "enh_ssrobj_id":1622,
                           "enh_sntystat":"H",
                           "enh_navntype":108,
                           "for_snavn":"Nome",
                           "kom_fylkesnr":10,
                           "kpr_tekst":"Norge 1:50 000"
                        },
          "geometry": { "type":"Point",
                        "coordinates":[7.568300,58.149633 ]
                      }
        }
    ]
}
"""
  lazy val testJson = Json.parse(json)
  lazy val fileName: Path = FileSystems.getDefault().getPath("src/test/scala/no/met/json/test.json");
  lazy val zipFileName: Path = FileSystems.getDefault().getPath("src/test/scala/no/met/json/test.zip");
  lazy val zipEntry = "test.json"

  class JsonAcc(var acc: ListBuffer[JsValue] = new ListBuffer[JsValue]()) {

    def populate(v: JsValue, path: String): Unit = {
      acc = acc :+ v
    }
  }

  def zipFile(f: Path, zipEntry: String): Option[InputStream] = {
    import java.util.zip._
    try {
      val zipFile = new ZipFile(f.toFile())
      val entry = Option(zipFile.getEntry(zipEntry))
      entry flatMap { e =>
        if (e.isDirectory()) {
          None
        } else {
          Some(zipFile.getInputStream(e))
        }
      }
    } catch {
      case e: Exception =>
        None
    }
  }
}

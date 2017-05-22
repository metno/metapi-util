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

import scala.language.postfixOps
import play.api.Play.current
import play.api.db._
import anorm._

import play.Logger


//$COVERAGE-OFF$ Not testing database queries

/**
  * Simple LRU caching of DB queries. The cache key is an SQL query. The cached value is the list of rows resulting from executing the query.
  *
  * @param maxItems Maximum number of items in the cache.
  * @param expireSecs Expiration limit in seconds. An item cannot be cached longer than this limit. A negative value (the default) means that an item
  *                   never expires.
  */
class DBCache(maxItems: Int, expireSecs: Int = -1) {

  assert(maxItems > 0)

  // Returns the current number of milliseconds since 1970
  private def nowMillis: Long = System.currentTimeMillis

  private var cache: collection.mutable.Map[String, List[Any]] = collection.mutable.Map[String, List[Any]]()
  private var cached: collection.mutable.Map[String, Long] = collection.mutable.Map[String, Long]() // time (millisecs since 1970) at which the item was cached
  private var lastAccessed: collection.mutable.Map[String, Long] = collection.mutable.Map[String, Long]() // time (ditto) at which the item was last accessed

  // Ensures that the cache has room for at least one more item.
  private def makeRoom() = {
    assert(cache.size <= maxItems)
    if (cache.size == maxItems) {
    val lruQuery = lastAccessed.minBy(_._2)._1
      lastAccessed.remove(lruQuery)
      cached.remove(lruQuery)
      cache.remove(lruQuery)
    }
  }

  /**
    * Retrieves the (possibly cached) result of executing a query.
    *
    * @param T The type resulting from parsing a row.
    * @param database The database name.
    * @param query: The SQL query.
    * @param rowParser: The row parser.
    */
  def get[T](database: String, query: String, rowParser: RowParser[T]): List[T] = {

    assert(cache.size <= maxItems)

    val origSize = cache.size
    val hit = cache.contains(query)
    val missed = !hit
    val now = nowMillis
    val expireMillis = expireSecs * 1000
    val expired = hit && (expireSecs >= 0) && ((now - cached(query)) > expireMillis)

    val value: List[T] = {
      if (missed || expired) { // return new or refreshed value
        // compute value from database, cache and return it
        val v = DB.withConnection(database) { implicit connection => SQL(query).as(rowParser *) }
        makeRoom() // if necessary, remove an item in the cache to make room for the new one
        assert(cache.size < maxItems)
        cache(query) = v // cache value
        cached(query) = now // record time at which the value was cached
        v
      } else { // return unexpired value
        cache(query)
      }
    }.asInstanceOf[List[T]]

    lastAccessed(query) = now // update access time
    //Logger.debug(s"hit: ${if (hit) 1 else 0}; expired: ${if (expired) 1 else 0}; items in cache: ${cache.size}; rows in value: ${value.size}")
    value // returned cached value
  }

}

//$COVERAGE-ON$

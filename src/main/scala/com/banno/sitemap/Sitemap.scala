package com.banno.sitemap

import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._
import scala.xml._

trait ISitemap {
  def xml: NodeSeq
  def add(entry: SitemapEntry): Unit
}

class Sitemap(val baseUrl: Uri) extends ISitemap
    with SitemapEntryUtil
{
  require(baseUrl.scheme != None, "Base Url requires protocol")
  require(baseUrl.host   != None, "Base Url requires host")

  val maxEntries = 50000
  var entries: Seq[SitemapEntry] = Seq()
  val xmlns = "http://www.sitemaps.org/schemas/sitemap/0.9"

  def xml = {
    <urlset xmlns={xmlns}>
      { sortedEntries.map(entryXml(_)) }
    </urlset>
  }

  private def sortedEntries = {
    entries.sortWith((e1: SitemapEntry, e2: SitemapEntry) =>
      e1.loc.pathParts.length < e2.loc.pathParts.length &&
      e1.loc.toString < e2.loc.toString)
  }

  private def ifOkToAdd(entry: SitemapEntry) = {
    if (entries.length >= maxEntries) {
      throw new RuntimeException(s"Maximum $maxEntries entries per sitemap")
    }
    if (entries.map(_.loc).contains(entry.loc)) {
      throw new IllegalArgumentException("Duplicate loc added")
    }
    entry
  }

  def add(entry: SitemapEntry): Unit = {
    entries +:= (validateEntry _ andThen ifOkToAdd _)(entry)
  }

  def add(url: Uri): Unit = add(SitemapEntry(url))
}

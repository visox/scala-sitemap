package com.banno.sitemap

import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._
import scala.xml._

trait ISitemap {
  def xml: NodeSeq
  def add(entry: SitemapEntry): Sitemap
}

object Sitemap {
  def apply(baseUrl: Uri, entries: Seq[SitemapEntry] = Seq()) = {
    new Sitemap(baseUrl, entries)
  }
}

class Sitemap(val baseUrl: Uri, val entries: Seq[SitemapEntry] = Seq())
    extends ISitemap
    with SitemapEntryUtil
{
  val maxEntries = 50000
  require(entries.length <= maxEntries,
    s"Maximum $maxEntries entries per sitemap")
  require(baseUrl.scheme != None,
    "Base Url requires protocol")
  require(baseUrl.host   != None,
    "Base Url requires host")

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

  def add(entry: SitemapEntry): Sitemap = {
    val newEntry = validateEntry(entry)
    if (entries.map(_.loc).contains(newEntry.loc))
      throw new IllegalArgumentException("Duplicate loc added")
    else
      Sitemap(baseUrl, newEntry +: entries)
  }

  def add(url: Uri): Sitemap = add(SitemapEntry(url))
}

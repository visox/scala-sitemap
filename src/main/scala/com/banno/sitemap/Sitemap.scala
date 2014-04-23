package com.banno.sitemap

import com.github.nscala_time.time.Imports._
import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._
import scala.xml._

trait ISitemap {
  def xml: NodeSeq
  def add(entry: SitemapEntry): Sitemap
}

object Sitemap {
  def apply(
    baseUrl: String, initEntries: Seq[SitemapEntry] = Seq()): Sitemap =
  {
    new Sitemap(baseUrl, initEntries)
  }
}

class Sitemap(val baseUrl: Uri, initEntries: Seq[SitemapEntry] = Seq())
    extends ISitemap
    with SitemapEntryUtil
    with SitemapEntryXml
{
  val maxEntries = 50000
  require(initEntries.length <= maxEntries,
    s"Maximum $maxEntries entries per sitemap")
  require(baseUrl.scheme != None,
    "Base Url requires protocol")
  require(baseUrl.host   != None,
    "Base Url requires host")

  val entries = initEntries.map(validateEntry)
  val xmlns = "http://www.sitemaps.org/schemas/sitemap/0.9"

  def xml = {
    <urlset xmlns={xmlns}>
      { sortedEntries.map(entryXml) }
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
      new Sitemap(baseUrl, newEntry +: entries)
  }

  def add(
    loc:        String,
    lastmod:    Option[DateTime] = None,
    changefreq: Option[ChangeFreq] = None,
    priority:   Option[Double]   = None): Sitemap =
  {
    add(SitemapEntry(loc, lastmod, changefreq, priority))
  }
}

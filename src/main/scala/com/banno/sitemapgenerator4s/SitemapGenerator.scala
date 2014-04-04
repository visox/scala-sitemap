package com.banno.sitemapgenerator4s

import com.github.nscala_time.time.Imports._
import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._
import scala.xml._

case class SitemapEntry(
  val loc:        Uri,
  val lastmod:    Option[DateTime] = None,
  val changefreq: Option[String]   = None,
  val priority:   Option[Double]   = None)

class SitemapGenerator(baseUrl: Uri) {

  require(baseUrl.scheme != None, "Base Url requires protocol")
  require(baseUrl.host   != None, "Base Url requires host")

  var entries: Seq[SitemapEntry] = Seq()
  val xmlns = "http://www.sitemaps.org/schemas/sitemap/0.9"

  def xml = {
    <urlset xmlns={xmlns}>
      { entries.map(entryXml(_)) }
    </urlset>
  }

  private def entryXml(entry: SitemapEntry) =
    <url>
      <loc>{entry.loc}</loc>
      {optionalNode("lastmod", entry.lastmod)}
      {optionalNode("changefreq", entry.changefreq)}
      {optionalNode("priority", entry.priority)}
    </url>

  private def optionalNode(label: String, optValue: Option[Any]) = {
    optValue match {
      case Some(value) => <ignore>{value}</ignore>.copy(label=label)
      case None => NodeSeq.Empty
    }
  }

  private def fillInMissingDomains(entry: SitemapEntry) = {
    (entry.loc.scheme, entry.loc.host) match {
      case (None, None) => {
        entry.copy(loc = entry.loc.copy(
          scheme = baseUrl.scheme,
          host   = baseUrl.host))
      }
      case (None, _) => {
        entry.copy(loc = entry.loc.copy(scheme = baseUrl.scheme))
      }
      case (_, None) => {
        entry.copy(loc = entry.loc.copy(host = baseUrl.host))
      }
      case (_, _) => entry
    }
  }

  private def checkEntryForErrors(entry: SitemapEntry) = {
    if (entry.loc.host   != baseUrl.host ||
        entry.loc.scheme != baseUrl.scheme)
    {
      throw new IllegalArgumentException(
        s"Sitemap entries must all be for same site ($baseUrl)")
    }
    entry
  }

  def validateEntry(entry: SitemapEntry) = {
    checkEntryForErrors(fillInMissingDomains(entry))
  }

  def add(entry: SitemapEntry): Unit = {
    entries +:= validateEntry(entry)
  }

  def add(url: Uri): Unit = add(SitemapEntry(url))
}

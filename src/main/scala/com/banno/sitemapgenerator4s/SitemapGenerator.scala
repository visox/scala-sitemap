package com.banno.sitemapgenerator4s

import com.github.nscala_time.time.Imports._
import com.netaporter.uri.Uri
import scala.xml._
//import com.netaporter.uri.dsl._

case class SitemapEntry(
  val loc:        Uri,
  val lastmod:    Option[DateTime] = None,
  val changefreq: Option[String]   = None,
  val priority:   Option[Double]   = None)

object SitemapEntry {
  def apply(
    loc:        String,
    lastmod:    Option[DateTime],
    changefreq: Option[String],
    priority:   Option[Double]) =
  {
    new SitemapEntry(Uri.parse(loc), lastmod, changefreq, priority)
  }

  def apply(loc: Uri):    SitemapEntry = this(loc, None, None, None)
  def apply(loc: String): SitemapEntry = this(Uri.parse(loc))
}

class SitemapGenerator(baseUrl: String) {

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

  def add(entry: SitemapEntry) { entries +:= entry }
  def add(url: String) { entries +:= SitemapEntry(url) }
  def add(url: Uri)    { entries +:= SitemapEntry(url) }
}

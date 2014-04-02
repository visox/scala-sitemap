package com.banno.sitemapgenerator4s

import com.github.nscala_time.time.Imports._
import com.netaporter.uri.Uri
//import com.netaporter.uri.dsl._

class SitemapEntry(
  val loc:        Uri,
  val lastmod:    Option[DateTime] = None,
  val changefreq: Option[String]   = None,
  val priority:   Option[Double]   = None)
{
  def xml = <url><loc>{loc}</loc></url>
}

object SitemapEntry {
  def apply(
    loc:        Uri,
    lastmod:    Option[DateTime],
    changefreq: Option[String],
    priority: Option[Double]) =
  { new SitemapEntry(loc, lastmod, changefreq, priority) }

  def apply(
    loc:        String,
    lastmod:    Option[DateTime],
    changefreq: Option[String],
    priority: Option[Double]) =
  { new SitemapEntry(Uri.parse(loc), lastmod, changefreq, priority) }
}

class SitemapGenerator(baseUrl: String) {

  var urls: Seq[SitemapEntry] = Seq()
  val xmlns = "http://www.sitemaps.org/schemas/sitemap/0.9"

  def xml = {
    <urlset xmlns={xmlns}>
      { urls.map(_.xml) }
    </urlset>
  }

  def add(entry: SitemapEntry) { urls +:= entry }

  def add(url: String) {
    urls +:= SitemapEntry(url, None, None, None)
  }

  def add(url: Uri) {
    urls +:= SitemapEntry(url, None, None, None)
  }

}

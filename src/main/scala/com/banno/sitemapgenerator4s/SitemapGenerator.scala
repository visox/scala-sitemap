package com.banno.sitemapgenerator4s

import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._
import scala.xml._

trait ISitemapGenerator {
  def xml: NodeSeq
  def add(entry: SitemapEntry): Unit
}

class SitemapGenerator(val baseUrl: Uri) extends ISitemapGenerator
    with SitemapEntryUtil
{
  require(baseUrl.scheme != None, "Base Url requires protocol")
  require(baseUrl.host   != None, "Base Url requires host")

  var entries: Seq[SitemapEntry] = Seq()
  val xmlns = "http://www.sitemaps.org/schemas/sitemap/0.9"

  def xml = {
    <urlset xmlns={xmlns}>
      { entries.map(entryXml(_)) }
    </urlset>
  }

  def add(entry: SitemapEntry): Unit = {
    entries +:= validateEntry(entry)
  }

  def add(url: Uri): Unit = add(SitemapEntry(url))
}

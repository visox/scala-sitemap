package com.banno.sitemap

import com.github.nscala_time.time.Imports._
import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._
import scala.xml._

object ChangeFreq extends Enumeration {
  val Always  = Value("always")
  val Hourly  = Value("hourly")
  val Daily   = Value("daily")
  val Weekly  = Value("weekly")
  val Monthly = Value("monthly")
  val Yearly  = Value("yearly")
  val Never   = Value("never")
}

object SitemapEntry {
  import ChangeFreq._
  def apply(
    loc:        String,
    lastmod:    Option[DateTime]   = None,
    changefreq: Option[ChangeFreq.Value] = None,
    priority:   Option[Double]     = None): SitemapEntry =
  {
    SitemapEntry(Uri.parse(loc), lastmod, changefreq, priority)
  }
}

case class SitemapEntry(
  loc:        Uri,
  lastmod:    Option[DateTime],
  changefreq: Option[ChangeFreq.Value],
  priority:   Option[Double])
{
  require(priority match {
    case None    => true
    case Some(p) => p <= 1.0 && p >= 0.0
  }, "Priority must be between 0.0 and 1.0 inclusive")
}

trait SitemapEntryXml {
  def entryXml(entry: SitemapEntry) =
    <url>
      <loc>{entry.loc}</loc>
      {optionalNode("lastmod", entry.lastmod)}
      {optionalNode("changefreq", entry.changefreq)}
      {optionalNode("priority", entry.priority)}
    </url>

  protected def optionalNode(label: String, optValue: Option[Any]) = {
    optValue match {
      case Some(value) => <ignore>{value}</ignore>.copy(label=label)
      case None => NodeSeq.Empty
    }
  }
}

trait SitemapEntryUtil {
  val baseUrl: Uri

  protected def fillInMissingDomains(entry: SitemapEntry) = {
    (entry.loc.scheme, entry.loc.host) match {
      case (None, None) =>
        entry.copy(loc = entry.loc.copy(
          scheme = baseUrl.scheme,
          host   = baseUrl.host))
      case (None, _) =>
        entry.copy(loc = entry.loc.copy(scheme = baseUrl.scheme))
      case (_, None) =>
        entry.copy(loc = entry.loc.copy(host = baseUrl.host))
      case (_, _) => entry
    }
  }

  protected def checkEntryForErrors(entry: SitemapEntry) = {
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
}

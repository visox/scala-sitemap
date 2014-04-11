package com.banno.sitemap

import com.github.nscala_time.time.Imports._
import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._
import scala.xml._

sealed trait ChangeFreq { val name: String; override def toString = name }
case object Always  extends ChangeFreq { val name = "always"  }
case object Hourly  extends ChangeFreq { val name = "hourly"  }
case object Daily   extends ChangeFreq { val name = "daily"   }
case object Weekly  extends ChangeFreq { val name = "weekly"  }
case object Monthly extends ChangeFreq { val name = "monthly" }
case object Yearly  extends ChangeFreq { val name = "yearly"  }
case object Never   extends ChangeFreq { val name = "never"   }

case class SitemapEntry(
  val loc:        Uri,
  val lastmod:    Option[DateTime] = None,
  val changefreq: Option[ChangeFreq] = None,
  val priority:   Option[Double]   = None)
{
  require(priority match {
    case None    => true
    case Some(p) => p <= 1.0 && p >= 0.0
  }, "Priority must be between 0.0 and 1.0 inclusive")

  def apply(loc: Uri): SitemapEntry = SitemapEntry(loc, None, None, None)
}

trait SitemapEntryUtil {
  val baseUrl: Uri

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

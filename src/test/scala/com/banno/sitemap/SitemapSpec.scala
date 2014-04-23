package com.banno.sitemap

import org.specs2.mutable.Specification
import org.specs2.specification.{After, BeforeAfterEach, Scope}
import scala.xml._
import com.github.nscala_time.time.Imports._
import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._

class SitemapSpec extends Specification {

  trait context extends Scope {
    val baseUrl = "http://www.example.com"
    lazy val sitemap = new Sitemap(baseUrl)
  }
  trait sitemapEntryContext extends Scope {
    val entry = SitemapEntry("http://www.example.com/blog")
  }
  trait addingEntriesContext extends context with sitemapEntryContext

  "A sitemap" should {

    "make sure that the base url protocol and domain are given/valid" in {
      new Sitemap("//folder")     must throwA[IllegalArgumentException]
      new Sitemap("http://")      must throwA[IllegalArgumentException]
      new Sitemap("www.ebay.com") must throwA[IllegalArgumentException]
    }
  }

  "A Sitemap Entry" should {

    "not accept invalid urls" in  {
      SitemapEntry("derp?!") must throwA[RuntimeException]
      // ParseError comes from scala-uri when implicitly converting
      // a string to a Uri whilst instantiating a SitemapEntry. I'm
      // not sure how to catch it and convert it to another exception
      // type, or even if that's what I should do.
    }

    "check for invalid priority values" in new sitemapEntryContext {
      entry.copy(priority = Some(5.0))   must(throwA[IllegalArgumentException])
      entry.copy(priority = Some(-0.05)) must(throwA[IllegalArgumentException])
      entry.copy(priority = Some(1)) must not(throwA[IllegalArgumentException])
      entry.copy(priority = Some(0)) must not(throwA[IllegalArgumentException])
    }
  }

  "Adding entries to a sitemap" should {

    "accept a SitemapEntry" in new addingEntriesContext {
      (Sitemap(baseUrl, Seq(entry)).xml \\ "loc")(0).text mustEqual(
        "http://www.example.com/blog")
    }

    "not accept pages from another domain" in new context {
      sitemap.add("http://twitter.com/hoff2dev") must(
        throwA[IllegalArgumentException])
    }

    "not accept duplicate urls" in new context {
      sitemap
        .add("/page.html")
        .add("/page.html") must(throwA[IllegalArgumentException])
    }

    "error when it grows beyond 50,000 entries" in new addingEntriesContext {
      val entries = (1 to 50000).map(i => SitemapEntry(s"/page$i.html"))
      Sitemap(baseUrl, entries)
        .add(entry.copy(loc = "/page_too_many.html")) must(
        throwA[IllegalArgumentException])
    }
  }

  "The XML produced by a sitemap" should {

    "be an urlset element" in new context {
      sitemap.xml.label mustEqual "urlset"
      sitemap.xml.namespace mustEqual(
        "http://www.sitemaps.org/schemas/sitemap/0.9")
    }

    "start out with no url elements (no pages)" in new context {
      sitemap.xml \\ "url" must be empty
    }

    "contain one url element for each url added" in new context {
      (sitemap
        .add("http://www.example.com/")
        .xml \ "url" length) mustEqual(1)
      (sitemap
        .add("http://www.example.com/blog")
        .add("http://www.example.com/")
        .xml \ "url" length) mustEqual(2)
    }

    "place each url in a loc element within its url element" in new context {
      val newSitemap = sitemap.add("http://www.example.com/blog")
      val urlElement = (newSitemap.xml \ "url")(0)
      val locElements = (urlElement \ "loc")
      locElements.length mustEqual(1)
      locElements(0).text mustEqual("http://www.example.com/blog")
    }

    "append the domain/baseUrl when given just paths" in new context {
      (sitemap
        .add("/blog.html")
        .add("//www.example.com/section/page") // a protocol-relative uri
        .xml \\ "loc").map(_.text) must contain(exactly(
          "http://www.example.com/blog.html",
          "http://www.example.com/section/page"))
    }

    "include each entry's lastmod" in new addingEntriesContext {
      val justNow = DateTime.now
      new DateTime((sitemap
        .add(entry.copy(lastmod = Some(justNow)))
        .xml \\ "lastmod")(0).text) mustEqual justNow
    }

    "and changefreq" in new addingEntriesContext {
      (sitemap
        .add(entry.copy(changefreq = Some(ChangeFreq.Monthly)))
        .xml \\ "changefreq")(0).text mustEqual "monthly"
    }

    "and priority" in new addingEntriesContext {
      (sitemap
        .add(entry.copy(priority = Some(0.8)))
        .xml \\ "priority")(0).text mustEqual "0.8"
    }

    "order entries according to loc" in new context {
      // to try to keep pages in the same section together
      (sitemap
        .add("http://www.example.com/section/something.html")
        .add("http://www.example.com/page44.html")
        .add("http://www.example.com/section/page1.html")
        .xml \\ "loc").map(_.text) mustEqual(Seq(
          "http://www.example.com/page44.html",
          "http://www.example.com/section/page1.html",
          "http://www.example.com/section/something.html"))
    }

    "format lastmod in W3C Datetime format" in new context {
      val lastmod = new DateTime(2014, 2, 13, 12, 0)
        .withZoneRetainFields(DateTimeZone.forID("-06:00"))
      val entry = SitemapEntry("/index.html", Some(lastmod))
      (Sitemap(baseUrl, Seq(entry))
        .xml \\ "lastmod")(0).text mustEqual("2014-02-13T12:00:00.000-06:00")
      // possible TODO: support for leaving off optional parts of the
      // datetime format. Currently Scala seems to be giving back the
      // above format by default (probably since this is XML) without
      // any effort on our part specifying a format, so this will do
      // for now.
      // for more info: http://www.w3.org/TR/NOTE-datetime
    }

    // acceptance test
    "work as used typically" in new context {
      val pages = Seq(
        ("/section/something.html", new DateTime(2014, 2, 13, 12, 0)),
        ("/page44.html",            new DateTime(2014, 2, 28, 14, 15)),
        ("/section/page1.html",     new DateTime(2014, 4, 23, 10, 0)))
      val xml = Sitemap("http://www.example.com",
        pages.map( page =>
          SitemapEntry(
            page._1, Some(page._2),
            Some(ChangeFreq.Weekly), Some(0.7)))).xml
      (xml \\ "lastmod").map(_.text) mustEqual Seq(
        "2014-02-28T14:15:00.000-06:00",
        "2014-02-13T12:00:00.000-06:00",
        "2014-04-23T10:00:00.000-05:00")
      (xml \\ "loc").map(_.text) mustEqual Seq(
        "http://www.example.com/page44.html",
        "http://www.example.com/section/something.html",
        "http://www.example.com/section/page1.html")
      (xml \\ "changefreq").map(_.text) mustEqual(
        Seq("weekly", "weekly", "weekly"))
      (xml \\ "priority")map(_.text) mustEqual(
        Seq("0.7", "0.7", "0.7"))
    }
  }
}

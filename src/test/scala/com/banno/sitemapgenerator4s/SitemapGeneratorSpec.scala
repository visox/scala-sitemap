package com.banno.sitemapgenerator4s

import org.specs2.mutable.Specification
import org.specs2.specification.{After, BeforeAfterEach, Scope}
import scala.xml._
import com.github.nscala_time.time.Imports._
import com.netaporter.uri.Uri
import com.netaporter.uri.dsl._

class SitemapGeneratorSpec extends Specification {

  trait context extends Scope {
    lazy val generator = new SitemapGenerator("http://www.example.com")
  }

  "A sitemap generator" should {

    "make sure that the base url protocol and domain are given/valid" in {
      new SitemapGenerator("//folder")     must throwA[IllegalArgumentException]
      new SitemapGenerator("http://")      must throwA[IllegalArgumentException]
      new SitemapGenerator("www.ebay.com") must throwA[IllegalArgumentException]
    }
  }

  trait sitemapEntryContext extends Scope {
    val entry = SitemapEntry("http://www.example.com/blog")
    // here to make sure this doesn't error:
    val entry2 = SitemapEntry("http://www.example.com/blog", None, None, None)
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

    "accept lastmod as string" in new sitemapEntryContext {
      pending
    }
  }

  trait addingEntriesContext extends context with sitemapEntryContext

  "Adding entries to a sitemap" should {

    "accept a SitemapEntry" in new addingEntriesContext {
      generator.add(entry)
      (generator.xml \\ "loc")(0).text mustEqual "http://www.example.com/blog"
    }

    "not accept pages from another domain" in new context {
      generator.add("http://twitter.com/hoff2dev") must(
        throwA[IllegalArgumentException])
    }

    "error when sitemap grows beyond 50,000 entries" in new context {
      pending
    }

    "not accept duplicate urls" in new context {
      pending
    }
  }

  "The XML produced by a sitemap" should {

    "be an urlset element" in new context {
      generator.xml.label mustEqual "urlset"
      generator.xml.namespace mustEqual(
        "http://www.sitemaps.org/schemas/sitemap/0.9")
    }

    "start out with no url elements (no pages)" in new context {
      generator.xml \\ "url" must be empty
    }

    "contain one url element for each url added" in new context {
      generator.add("http://www.example.com/")
      (generator.xml \ "url" length) mustEqual(1)
      generator.add("http://www.example.com/blog")
      (generator.xml \ "url" length) mustEqual(2)
    }

    "place each url in a loc element within its url element" in new context {
      generator.add("http://www.example.com/blog")
      val urlElement = (generator.xml \ "url")(0)
      val locElements = (urlElement \ "loc")
      locElements.length mustEqual(1)
      locElements(0).text mustEqual("http://www.example.com/blog")
    }

    "append the domain/baseUrl when given just paths" in new context {
      generator.add("/blog.html")
      generator.add("//www.example.com/section/page") // a protocol-relative uri
      (generator.xml \\ "loc").map(_.text) must contain(exactly(
        "http://www.example.com/blog.html",
        "http://www.example.com/section/page"))
    }

    "include each entry's lastmod" in new addingEntriesContext {
      val justNow = DateTime.now
      generator.add(entry.copy(lastmod = Some(justNow)))
      (generator.xml \\ "lastmod") must not be empty
      new DateTime((generator.xml \\ "lastmod")(0).text) mustEqual justNow
    }

    "and changefreq" in new addingEntriesContext {
      generator.add(entry.copy(changefreq = Some(Monthly)))
      (generator.xml \\ "changefreq") must not be empty
      (generator.xml \\ "changefreq")(0).text mustEqual "monthly"
    }

    "and priority" in new addingEntriesContext {
      generator.add(entry.copy(priority = Some(0.8)))
      (generator.xml \\ "priority") must not be empty
      (generator.xml \\ "priority")(0).text mustEqual "0.8"
    }

    "order entries according to loc" in new context {
      pending
    }

    "format lastmod in W3C Datetime format" in new context {
      pending
    }
  }
}

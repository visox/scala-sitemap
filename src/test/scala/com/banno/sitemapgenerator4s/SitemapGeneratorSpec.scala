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

  "the sitemap generator" should {

    "make sure that the protocol and domain are given/valid" in {
      new SitemapGenerator("//folder")     must throwA[IllegalArgumentException]
      new SitemapGenerator("http://")      must throwA[IllegalArgumentException]
      new SitemapGenerator("www.ebay.com") must throwA[IllegalArgumentException]
    }

    "generate xml containing an urlset element" in new context {
      generator.xml.label mustEqual "urlset"
      generator.xml.namespace mustEqual(
        "http://www.sitemaps.org/schemas/sitemap/0.9")
    }

    "start out with no url elements (no pages)" in new context {
      generator.xml \\ "url" must be empty
    }

    "Have one url element for each url added" in new context {
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

    "will append the domain/baseUrl given just paths" in new context {
      generator.add("/blog.html")
      generator.add("//www.example.com/section/page") // a protocol-relative uri
      (generator.xml \\ "loc").map(_.text) must contain(exactly(
        "http://www.example.com/blog.html",
        "http://www.example.com/section/page"))
    }

    "will not accept pages from another domain" in new context {
      generator.add("http://twitter.com/hoff2dev") must(
        throwA[IllegalArgumentException])
    }

    "will not accept invalid urls" in new context {
      generator.add("derp?!") must throwA[RuntimeException]
      // ParseError comes from scala-uri when implicitly converting
      // a string to a Uri whilst instantiating a SitemapEntry. I'm
      // not sure how to catch it and convert it to another exception
      // type, or even if that's what I should do.
    }

    trait entryObjectContext extends context {
      val entry = SitemapEntry("http://www.example.com/blog")
      // here to make sure this doesn't error:
      val entry2 = SitemapEntry("http://www.example.com/blog", None, None, None)
    }

    "uses a case object for sitemap entry info" in new entryObjectContext {
      generator.add(entry)
      (generator.xml \\ "loc")(0).text mustEqual "http://www.example.com/blog"
    }

    "including lastmod" in new entryObjectContext {
      val justNow = DateTime.now
      generator.add(entry.copy(lastmod = Some(justNow)))
      (generator.xml \\ "lastmod") must not be empty
      new DateTime((generator.xml \\ "lastmod")(0).text) mustEqual justNow
    }

    "and changefreq" in new entryObjectContext {
      generator.add(entry.copy(changefreq = Some(Monthly)))
      (generator.xml \\ "changefreq") must not be empty
      (generator.xml \\ "changefreq")(0).text mustEqual "monthly"
    }

    "and priority" in new entryObjectContext {
      generator.add(entry.copy(priority = Some(0.8)))
      (generator.xml \\ "priority") must not be empty
      (generator.xml \\ "priority")(0).text mustEqual "0.8"
    }

    "will check for invalid priority values" in new context {
      pending
    }

    "will accept lastmod as string and convert to DateTime" in new context {
      pending
    }

    "error when sitemap grows beyond 50,000 entries" in new context {
      pending
    }

    "ignore duplicate urls" in new context {
      pending
    }

    "sort entries by url in the xml output" in new context {
      pending
    }

    "format lastmod" in new context {
      pending
    }
  }
}

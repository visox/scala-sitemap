package com.banno.sitemapgenerator4s

import org.specs2.mutable.Specification
import org.specs2.specification.{After, BeforeAfterEach, Scope}
import scala.xml._
import com.github.nscala_time.time.Imports._
import com.netaporter.uri.Uri

class SitemapGeneratorSpec extends Specification {

  trait context extends Scope {
    lazy val generator = new SitemapGenerator("http://www.example.com")
  }

  "the sitemap generator" should {

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

    trait entryObjectContext extends context {
      val entry = SitemapEntry(Uri.parse("http://www.example.com/blog"))
    }

    "uses an object for other sitemap entry info" in new entryObjectContext {
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
      generator.add(entry.copy(changefreq = Some("monthly")))
      (generator.xml \\ "changefreq") must not be empty
      (generator.xml \\ "changefreq")(0).text mustEqual "monthly"
    }

    "and priority" in new entryObjectContext {
      generator.add(entry.copy(priority   = Some(0.8)))
      (generator.xml \\ "priority") must not be empty
      (generator.xml \\ "priority")(0).text mustEqual "0.8"
    }

    // take other url info (lastmod, changefreq, priority)
    // take paths as well as full urls or URL objects (scala-uri)
    // error on invalid data (url, lastmod, changefreq, priority)
    // take URL objects as well as strings
    // check when list of urls gets too big (max 50k)
    // sort urls in output
    // catch or ignore duplicate urls
    // date formatting options
  }
}

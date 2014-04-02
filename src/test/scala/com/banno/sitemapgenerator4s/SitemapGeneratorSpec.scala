package com.banno.sitemapgenerator4s

import org.specs2.mutable.Specification
import org.specs2.specification.{After, BeforeAfterEach, Scope}
import scala.xml._

class SitemapGeneratorSpec extends Specification {
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
      generator.addUrl("http://www.example.com/")
      (generator.xml \\ "url" length) mustEqual(1)
      generator.addUrl("http://www.example.com/blog")
      (generator.xml \\ "url" length) mustEqual(2)
    }

    "place each url in a loc element within its url element" in new context {
      generator.addUrl("http://www.example.com/blog")
      val urlElement = (generator.xml \ "url").head
      val locElements = (urlElement \ "loc")
      locElements.length mustEqual(1)
      locElements.head.text mustEqual("http://www.example.com/blog")
    }

    // take paths as well as full urls
    // error on invalid urls
    // take URL objects as well as strings
    // take other url info (lastmod, changefreq, priority)
    // validate the above, error
    // check when list of urls gets too big (max 50k)
    // sort urls in output
    // catch or ignore duplicate urls
  }
}

trait context extends Scope {
  lazy val generator = new SitemapGenerator("http://www.example.com")
}

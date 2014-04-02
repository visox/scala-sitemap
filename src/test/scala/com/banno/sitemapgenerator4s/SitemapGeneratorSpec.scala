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


  }
}

trait context extends Scope {
  lazy val generator = new SitemapGenerator("http://www.example.com")
}

package com.banno.sitemapgenerator4s

class SitemapGenerator(baseUrl: String) {

  var urls: Seq[String] = Seq()

  def xml = {
    <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
      { urls.map(u => <url><loc>{u}</loc></url>) }
    </urlset>
  }

  def addUrl(url: String) { urls +:= url }


}

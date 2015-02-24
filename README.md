# scala-sitemap

This makes XML for sitemaps. I wrote it because sitemapgen4j doesn't
give you any good way to do anything with the sitemaps it generates
other than save to a file, and the sitemap plugin for Play works by
adding annotations to controller actions, which won't work for CMS.
This can just be used in a controller responding to a route for
"/sitemap.xml", and you can iterate through a site's pages from the
database and return the xml from there.

It doesn't do sitemap indexes yet, so it's limited to listing 50,000
pages. Hopefully we can live with that for now.

More info about sitemaps in general: http://www.sitemaps.org/protocol.html

## Usage

### Start a new sitemap:

```scala
val mySitemap = Sitemap("http://example.com")
```

### Adding pages:

Adding pages to a sitemap produces a new sitemap. The only required
data is the page's URL. All these work:
```scala
mySitemap.add("/section/page")
mySitemap.add("/section/page/")
mySitemap.add("http://example.com/section/page", Some(modified))
mySitemap.add("//example.com/section/page", None, Some(ChangeFreq.Monthly))
```

But `add` is just a convenient shortcut to the SitemapEntry case
class, whose attributes are named like the XML tags' labels in a
sitemap:

 - `loc` for the URL (`Uri` but scala-uri will implicitly
   convert from String)
 - `lastmod` for last-modified (`Option[DateTime]`)
 - `changefreq` for change frequency (using an enumeration called
   `ChangeFreq`) for the valid values of this field: `Always`,
   `Hourly`, `Daily`, `Weekly`, `Monthly`, `Yearly`, `Never`)
 - `priority` (`Option[Double]` between 0.0 and 1.0 inclusive)

So in practice, you'll likely make sitemap something like this:
```scala
val sitemap = Sitemap(
  "http://example.com",
  allMyPages.map(page =>
    SitemapEntry(page.path, Some(page.modified), Some(ChangeFreq.Weekly))))
```

And then of course, just ask it for its XML.
```scala
sitemap.xml
```

## Install

Hosted in Banno's Bintray:  
`"com.banno" %% "scala-sitemap" % "0.10.1"` Scala 2.10

`"com.banno" %% "scala-sitemap" % "0.10.2"` Scala 2.11

## License

Apache 2.0

## Contributing

Go for it. (with tests.)
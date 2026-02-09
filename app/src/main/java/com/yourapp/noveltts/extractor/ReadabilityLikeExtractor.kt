package com.yourapp.noveltts.extractor

import com.yourapp.noveltts.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URL
import kotlin.math.max

class ReadabilityLikeExtractor : ChapterExtractor {
    override suspend fun extract(url: String): Result<ExtractedChapter> = withContext(Dispatchers.IO) {
        runCatching {
            val html = fetch(url)
            val doc = Jsoup.parse(html, url)
            cleanNoise(doc)
            val title = extractTitle(doc)
            val container = findBestContainer(doc)
            val paragraphs = extractParagraphs(container)
            require(paragraphs.isNotEmpty()) { "No readable text found" }
            val (nextUrl, prevUrl) = extractNavLinks(doc, url)
            ExtractedChapter(url, title, paragraphs, nextUrl, prevUrl)
        }
    }

    private fun fetch(url: String): String {
        val request = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0 NovelTTS").build()
        HttpClient.client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code}")
            return response.body?.string().orEmpty()
        }
    }

    private fun cleanNoise(doc: Document) {
        doc.select("script,style,nav,header,footer,aside,noscript,form,iframe,advertisement,.ads,.ad,.sidebar,.menu").remove()
    }

    private fun extractTitle(doc: Document): String {
        return doc.selectFirst("h1")?.text()?.takeIf { it.length > 3 }
            ?: doc.title().ifBlank { "Untitled Chapter" }
    }

    private fun findBestContainer(doc: Document): Element {
        val candidates = doc.select("article,main,section,div,body")
        var best: Element = doc.body()
        var bestScore = 0
        for (el in candidates) {
            val pLen = el.select("p,li").sumOf { it.text().length }
            val headingBonus = el.select("h1,h2,h3").sumOf { it.text().length / 3 }
            val penalty = el.select("a").size * 12
            val score = max(0, pLen + headingBonus - penalty)
            if (score > bestScore) {
                bestScore = score
                best = el
            }
        }
        return best
    }

    private fun extractParagraphs(container: Element): List<String> {
        val raw = container.select("p,h1,h2,h3,li")
            .map { it.text().trim() }
            .filter { it.length > 25 }
            .distinct()

        if (raw.isNotEmpty()) return raw

        return container.text().split(". ").map { it.trim() }.filter { it.length > 25 }.map { "$it." }
    }

    private fun extractNavLinks(doc: Document, baseUrl: String): Pair<String?, String?> {
        var next: String? = doc.selectFirst("link[rel=next]")?.attr("abs:href")
        var prev: String? = doc.selectFirst("link[rel=prev]")?.attr("abs:href")
        val anchors = doc.select("a[href]")
        for (a in anchors) {
            val text = a.text().lowercase()
            val href = a.attr("href")
            if (next == null && ("next" in text || text.contains("chapter") || text.contains(">"))) {
                next = toAbsolute(baseUrl, href)
            }
            if (prev == null && ("prev" in text || "previous" in text || text.contains("<"))) {
                prev = toAbsolute(baseUrl, href)
            }
        }
        return next to prev
    }

    private fun toAbsolute(baseUrl: String, href: String): String {
        return URL(URL(baseUrl), href).toExternalForm()
    }
}

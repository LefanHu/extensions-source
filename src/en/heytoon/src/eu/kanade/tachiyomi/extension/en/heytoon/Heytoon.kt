package eu.kanade.tachiyomi.extension.en.heytoon

import eu.kanade.tachiyomi.multisrc.hotcomics.HotComics
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class Heytoon : HotComics(
    "Heytoon.net",
    "en",
    "https://heytoon.net",
) {
    override val browseList = listOf(
        Pair("Home", "en"),
        Pair("Weekly", "en/weekly"),
        Pair("New", "en/new"),
        Pair("Genre: All", "en/genres"),
        Pair("Genre: Romance", "en/genres/Romance"),
        Pair("Genre: Office", "en/genres/Office"),
        Pair("Genre: College", "en/genres/College"),
        Pair("Genre: Drama", "en/genres/Drama"),
        Pair("Genre: Isekai", "en/genres/Isekai"),
        Pair("Genre: UNCENSORED", "en/genres/UNCENSORED"),
        Pair("Genre: Action", "en/genres/Action"),
        Pair("Genre: BL", "en/genres/BL"),
        Pair("Genre: New", "en/genres/New"),
        Pair("Genre: Slice of Life", "en/genres/Slice_of_Life"),
        Pair("Genre: Supernatural", "en/genres/Supernatural"),
        Pair("Genre: Historical", "en/genres/Historical"),
        Pair("Genre: School Life", "en/genres/School_Life"),
        Pair("Genre: Horror Thriller", "en/genres/Horror_Thriller"),
    )

    override fun searchMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()
        val entries = document.select("div.grid > div.comicItemCon > a").map { element ->
            SManga.create().apply {
                setUrlWithoutDomain(element.absUrl("href"))
                thumbnail_url = element.selectFirst("div img")!!.attr("abs:data-src")
                title = element.selectFirst("div.comicInfo > div")!!.text()
//                Log.e("toon", thumbnail_url.toString())
            }
        }.distinctBy { it.url }
        val hasNextPage = false

        return MangasPage(entries, hasNextPage)
    }

    override fun mangaDetailsParse(response: Response) = SManga.create().apply {
        val document = response.asJsoup()

        title = document.selectFirst("h1.titCon")!!.text()
//        with(document.selectFirst("p.type_box")!!) {
//            author = selectFirst("span.writer")?.text()
//                ?.substringAfter("ⓒ")?.trim()
//            genre = selectFirst("span.type")?.text()
//                ?.split("/")?.joinToString { it.trim() }
//            status = when (selectFirst("span.date")?.text()) {
//                "End", "Ende" -> SManga.COMPLETED
//                null -> SManga.UNKNOWN
//                else -> SManga.ONGOING
//            }
//        }
        description = buildString {
            document.selectFirst("div.episode-contents header")
                ?.text()?.let {
                    append(it)
                    append("\n\n")
                }
            document.selectFirst("div.title_content > h2:not(.episode-title)")
                ?.text()?.let { append(it) }
        }.trim()
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        return response.asJsoup().select("div.episodeListConMO > a.flex").map { element ->
            SChapter.create().apply {
                setUrlWithoutDomain(element.attr("abs:href"))
                name = element.selectFirst("p")!!.text()
                date_upload = parseDate(element.selectFirst("p.episodeDate")?.text())
            }
        }.reversed()
    }

    private fun parseDate(date: String?): Long {
        date ?: return 0L

        return try {
            dateFormat.parse(date)!!.time
        } catch (_: ParseException) {
            0L
        }
    }

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
}

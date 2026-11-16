package com.example.data.islamic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class DistrictGeo(
    val bnName: String,
    val enName: String,
    val division: String,
    val lat: Double,
    val lon: Double
)

data class PrayerSchedule(
    val district: DistrictGeo,
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime,
    val qiblaDegrees: Double,
    val hijriDisplayBn: String
)

data class QuranSurahMeta(
    val number: Int,
    val bnName: String,
    val arabicName: String,
    val ayahCount: Int,
    val revelationType: String
)

data class QuranVerse(
    val numberInSurah: Int,
    val arabic: String,
    val bengali: String
)

data class HadithItem(
    val book: String,
    val narrators: String,
    val textBn: String,
    val reference: String
)

object BangladeshDistricts {
    val ALL: List<DistrictGeo> = listOf(
        DistrictGeo("ঢাকা", "Dhaka", "ঢাকা", 23.8103, 90.4125),
        DistrictGeo("গাজীপুর", "Gazipur", "ঢাকা", 24.0023, 90.4264),
        DistrictGeo("নারায়ণগঞ্জ", "Narayanganj", "ঢাকা", 23.6238, 90.5000),
        DistrictGeo("নরসিংদী", "Narsingdi", "ঢাকা", 23.9322, 90.7154),
        DistrictGeo("মানিকগঞ্জ", "Manikganj", "ঢাকা", 23.8644, 90.0047),
        DistrictGeo("মুন্সিগঞ্জ", "Munshiganj", "ঢাকা", 23.5422, 90.5305),
        DistrictGeo("টাঙ্গাইল", "Tangail", "ঢাকা", 24.2513, 89.9167),
        DistrictGeo("কিশোরগঞ্জ", "Kishoreganj", "ঢাকা", 24.4449, 90.7766),
        DistrictGeo("ফরিদপুর", "Faridpur", "ঢাকা", 23.6071, 89.8429),
        DistrictGeo("মাদারীপুর", "Madaripur", "ঢাকা", 23.1641, 90.1897),
        DistrictGeo("শরীয়তপুর", "Shariatpur", "ঢাকা", 23.2423, 90.4348),
        DistrictGeo("গোপালগঞ্জ", "Gopalganj", "ঢাকা", 23.0051, 89.8266),
        DistrictGeo("রাজবাড়ী", "Rajbari", "ঢাকা", 23.7574, 89.6445),
        DistrictGeo("চট্টগ্রাম", "Chattogram", "চট্টগ্রাম", 22.3569, 91.7832),
        DistrictGeo("কক্সবাজার", "Cox's Bazar", "চট্টগ্রাম", 21.4272, 92.0058),
        DistrictGeo("কুমিল্লা", "Cumilla", "চট্টগ্রাম", 23.4607, 91.1809),
        DistrictGeo("ব্রাহ্মণবাড়িয়া", "Brahmanbaria", "চট্টগ্রাম", 23.9608, 91.1115),
        DistrictGeo("চাঁদপুর", "Chandpur", "চট্টগ্রাম", 23.2333, 90.6713),
        DistrictGeo("নোয়াখালী", "Noakhali", "চট্টগ্রাম", 22.8696, 91.0994),
        DistrictGeo("ফেনী", "Feni", "চট্টগ্রাম", 23.0159, 91.3976),
        DistrictGeo("লক্ষ্মীপুর", "Lakshmipur", "চট্টগ্রাম", 22.9425, 90.8412),
        DistrictGeo("রাঙ্গামাটি", "Rangamati", "চট্টগ্রাম", 22.7324, 92.2985),
        DistrictGeo("খাগড়াছড়ি", "Khagrachhari", "চট্টগ্রাম", 23.1193, 91.9847),
        DistrictGeo("বান্দরবান", "Bandarban", "চট্টগ্রাম", 22.1953, 92.2184),
        DistrictGeo("সিলেট", "Sylhet", "সিলেট", 24.8949, 91.8687),
        DistrictGeo("মৌলভীবাজার", "Moulvibazar", "সিলেট", 24.4829, 91.7774),
        DistrictGeo("হবিগঞ্জ", "Habiganj", "সিলেট", 24.3749, 91.4155),
        DistrictGeo("সুনামগঞ্জ", "Sunamganj", "সিলেট", 25.0658, 91.3950),
        DistrictGeo("রাজশাহী", "Rajshahi", "রাজশাহী", 24.3745, 88.6042),
        DistrictGeo("বগুড়া", "Bogura", "রাজশাহী", 24.8465, 89.3773),
        DistrictGeo("পাবনা", "Pabna", "রাজশাহী", 24.0064, 89.2372),
        DistrictGeo("সিরাজগঞ্জ", "Sirajganj", "রাজশাহী", 24.4534, 89.7007),
        DistrictGeo("নওগাঁ", "Naogaon", "রাজশাহী", 24.7936, 88.9318),
        DistrictGeo("নাটোর", "Natore", "রাজশাহী", 24.4206, 89.0003),
        DistrictGeo("চাঁপাইনবাবগঞ্জ", "Chapainawabganj", "রাজশাহী", 24.5965, 88.2775),
        DistrictGeo("জয়পুরহাট", "Joypurhat", "রাজশাহী", 25.0968, 89.0227),
        DistrictGeo("খুলনা", "Khulna", "খুলনা", 22.8456, 89.5403),
        DistrictGeo("যশোর", "Jashore", "খুলনা", 23.1634, 89.2182),
        DistrictGeo("সাতক্ষীরা", "Satkhira", "খুলনা", 22.7185, 89.0705),
        DistrictGeo("বাগেরহাট", "Bagerhat", "খুলনা", 22.6516, 89.7859),
        DistrictGeo("কুষ্টিয়া", "Kushtia", "খুলনা", 23.9013, 89.1205),
        DistrictGeo("ঝিনাইদহ", "Jhenaidah", "খুলনা", 23.5448, 89.1539),
        DistrictGeo("চুয়াডাঙ্গা", "Chuadanga", "খুলনা", 23.6402, 88.8418),
        DistrictGeo("মেহেরপুর", "Meherpur", "খুলনা", 23.7622, 88.6318),
        DistrictGeo("মাগুরা", "Magura", "খুলনা", 23.4873, 89.4199),
        DistrictGeo("নড়াইল", "Narail", "খুলনা", 23.1725, 89.5127),
        DistrictGeo("বরিশাল", "Barishal", "বরিশাল", 22.7010, 90.3535),
        DistrictGeo("পটুয়াখালী", "Patuakhali", "বরিশাল", 22.3596, 90.3299),
        DistrictGeo("ভোলা", "Bhola", "বরিশাল", 22.6859, 90.6482),
        DistrictGeo("পিরোজপুর", "Pirojpur", "বরিশাল", 22.5841, 89.9720),
        DistrictGeo("ঝালকাঠি", "Jhalokati", "বরিশাল", 22.6406, 90.1987),
        DistrictGeo("বরগুনা", "Barguna", "বরিশাল", 22.1592, 90.1256),
        DistrictGeo("রংপুর", "Rangpur", "রংপুর", 25.7439, 89.2752),
        DistrictGeo("দিনাজপুর", "Dinajpur", "রংপুর", 25.6217, 88.6355),
        DistrictGeo("কুড়িগ্রাম", "Kurigram", "রংপুর", 25.8054, 89.6362),
        DistrictGeo("গাইবান্ধা", "Gaibandha", "রংপুর", 25.3288, 89.5281),
        DistrictGeo("নীলফামারী", "Nilphamari", "রংপুর", 25.9318, 88.8560),
        DistrictGeo("লালমনিরহাট", "Lalmonirhat", "রংপুর", 25.9923, 89.2847),
        DistrictGeo("ঠাকুরগাঁও", "Thakurgaon", "রংপুর", 26.0337, 88.4617),
        DistrictGeo("পঞ্চগড়", "Panchagarh", "রংপুর", 26.3411, 88.5542),
        DistrictGeo("ময়মনসিংহ", "Mymensingh", "ময়মনসিংহ", 24.7471, 90.4203),
        DistrictGeo("জামালপুর", "Jamalpur", "ময়মনসিংহ", 24.9375, 89.9378),
        DistrictGeo("নেত্রকোণা", "Netrokona", "ময়মনসিংহ", 24.8710, 90.7279),
        DistrictGeo("শেরপুর", "Sherpur", "ময়মনসিংহ", 25.0205, 90.0153)
    )

    fun findByName(name: String): DistrictGeo =
        ALL.find { it.bnName == name || it.enName.equals(name, ignoreCase = true) } ?: ALL.first()
}

object PrayerCalculator {
    private const val TZ_HOURS = 6.0 // Asia/Dhaka UTC+6
    private const val KAABA_LAT = 21.4225
    private const val KAABA_LON = 39.8262

    private val HIJRI_MONTHS_BN = listOf(
        "মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ্জ"
    )

    fun calculate(district: DistrictGeo, date: LocalDate = LocalDate.now(), hanafiAsr: Boolean = true): PrayerSchedule {
        val jd = julianDay(date.year, date.monthValue, date.dayOfMonth)
        val n = jd - 2451545.0
        val g = Math.toRadians((357.529 + 0.98560028 * n) % 360.0)
        val q = (280.459 + 0.98564736 * n) % 360.0
        val l = Math.toRadians((q + 1.915 * sin(g) + 0.020 * sin(2 * g)) % 360.0)
        val e = Math.toRadians(23.439 - 0.00000036 * n)

        val raRad = atan2(cos(e) * sin(l), cos(l))
        var raHours = Math.toDegrees(raRad) / 15.0
        raHours = (raHours % 24.0 + 24.0) % 24.0

        val declRad = asin(sin(e) * sin(l))
        val eqTime = (q / 15.0) - raHours
        val eqTimeFixed = when {
            eqTime > 12 -> eqTime - 24
            eqTime < -12 -> eqTime + 24
            else -> eqTime
        }

        val solarNoon = 12.0 + TZ_HOURS - (district.lon / 15.0) - eqTimeFixed
        val latRad = Math.toRadians(district.lat)

        fun sunAngleOffset(angleDeg: Double): Double {
            val cosHour = (-sin(Math.toRadians(angleDeg)) - sin(latRad) * sin(declRad)) /
                (cos(latRad) * cos(declRad))
            val clamped = cosHour.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(clamped)) / 15.0
        }

        fun asrOffset(factor: Double): Double {
            val tanAngle = 1.0 / (factor + tan(kotlin.math.abs(latRad - declRad)))
            val altRad = atan(tanAngle)
            val cosHour = (sin(altRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
            val clamped = cosHour.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(clamped)) / 15.0
        }

        val fajrH = solarNoon - sunAngleOffset(18.0)
        val sunriseH = solarNoon - sunAngleOffset(0.833)
        val dhuhrH = solarNoon + (2.0 / 60.0)
        val asrH = solarNoon + asrOffset(if (hanafiAsr) 2.0 else 1.0)
        val maghribH = solarNoon + sunAngleOffset(0.833) + (2.0 / 60.0)
        val ishaH = solarNoon + sunAngleOffset(18.0)

        val hijri = HijrahDate.from(date)
        val hDay = hijri.get(ChronoField.DAY_OF_MONTH)
        val hMonthIdx = (hijri.get(ChronoField.MONTH_OF_YEAR) - 1).coerceIn(0, 11)
        val hYear = hijri.get(ChronoField.YEAR)
        val hijriBn = "${toBnDigits(hDay)} ${HIJRI_MONTHS_BN[hMonthIdx]} ${toBnDigits(hYear)} হিজরি"

        return PrayerSchedule(
            district = district,
            fajr = hoursToLocalTime(fajrH),
            sunrise = hoursToLocalTime(sunriseH),
            dhuhr = hoursToLocalTime(dhuhrH),
            asr = hoursToLocalTime(asrH),
            maghrib = hoursToLocalTime(maghribH),
            isha = hoursToLocalTime(ishaH),
            qiblaDegrees = qiblaBearing(district.lat, district.lon),
            hijriDisplayBn = hijriBn
        )
    }

    fun qiblaBearing(latDeg: Double, lonDeg: Double): Double {
        val phi1 = Math.toRadians(latDeg)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dLon = Math.toRadians(KAABA_LON - lonDeg)
        val y = sin(dLon) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun hoursToLocalTime(hours: Double): LocalTime {
        val wrapped = ((hours % 24.0) + 24.0) % 24.0
        val totalMin = (wrapped * 60.0).toInt().coerceIn(0, 23 * 60 + 59)
        return LocalTime.of(totalMin / 60, totalMin % 60)
    }

    fun toBnDigits(num: Int): String {
        val bn = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return num.toString().map { c -> if (c in '0'..'9') bn[c - '0'] else c }.joinToString("")
    }
}

object IslamicLibrary {
    val FEATURED_SURAHS = listOf(
        QuranSurahMeta(1, "আল-ফাতিহা", "الفاتحة", 7, "মাক্কী"),
        QuranSurahMeta(18, "আল-কাহফ", "الكهف", 110, "মাক্কী"),
        QuranSurahMeta(36, "ইয়াসীন", "يس", 83, "মাক্কী"),
        QuranSurahMeta(55, "আর-রাহমান", "الرحمن", 78, "মাদানী"),
        QuranSurahMeta(67, "আল-মুলক", "الملك", 30, "মাক্কী"),
        QuranSurahMeta(93, "আদ-দুহা", "الضحى", 11, "মাক্কী"),
        QuranSurahMeta(94, "আল-ইনশিরাহ", "الشرح", 8, "মাক্কী"),
        QuranSurahMeta(97, "আল-কদর", "القدر", 5, "মাক্কী"),
        QuranSurahMeta(103, "আল-আসর", "العصر", 3, "মাক্কী"),
        QuranSurahMeta(108, "আল-কাউসার", "الكوثر", 3, "মাক্কী"),
        QuranSurahMeta(109, "আল-কাফিরুন", "الكافرون", 6, "মাক্কী"),
        QuranSurahMeta(112, "আল-ইখলাস", "الإخلاص", 4, "মাক্কী"),
        QuranSurahMeta(113, "আল-ফালাক", "الفلق", 5, "মাক্কী"),
        QuranSurahMeta(114, "আন-নাস", "الناس", 6, "মাক্কী")
    )

    val CURATED_HADITH = listOf(
        HadithItem("সহীহ বুখারী", "উমর ইবনুল খাত্তাব (রা.)", "সকল কাজের ফলাফল নিয়তের ওপর নির্ভরশীল এবং প্রত্যেক ব্যক্তি তাই পাবে যা সে নিয়ত করেছে।", "বুখারী: ১"),
        HadithItem("সহীহ মুসলিম", "আবু হুরায়রা (রা.)", "যে ব্যক্তি কোনো মুমিনের দুনিয়ার কোনো কষ্ট দূর করে দেয়, আল্লাহ কিয়ামতের দিন তার কষ্ট দূর করে দেবেন।", "মুসলিম: ২৬৯৯"),
        HadithItem("সহীহ বুখারী", "আব্দুল্লাহ ইবনে আমর (রা.)", "প্রকৃত মুসলিম সেই ব্যক্তি, যার জিহ্বা ও হাত থেকে অন্য মুসলিমরা নিরাপদ থাকে।", "বুখারী: ১০"),
        HadithItem("জামে তিরমিযী", "আনাস ইবনে মালিক (রা.)", "দান-সদকা বিপদ-আপদ দূর করে এবং আল্লাহর ক্রোধ প্রশমিত করে।", "তিরমিযী: ৬৬৪"),
        HadithItem("সহীহ মুসলিম", "আবু হুরায়রা (রা.)", "দান করলে কখনো সম্পদ কমে না, আর ক্ষমা করলে আল্লাহ সম্মান বাড়িয়ে দেন।", "মুসলিম: ২৫৮৮"),
        HadithItem("সহীহ বুখারী", "জারীর ইবনে আব্দুল্লাহ (রা.)", "যে মানুষের প্রতি দয়া করে না, আল্লাহও তার প্রতি দয়া করেন না।", "বুখারী: ৭৩৭৬"),
        HadithItem("সহীহ বুখারী", "আবু হুরায়রা (রা.)", "কথায় ও কাজে সত্যবাদী এবং প্রতিশ্রুতি রক্ষাকারী প্রকৃত মুমিন।", "বুখারী: ৩৩")
    )

    private val surahCache = mutableMapOf<Int, List<QuranVerse>>()

    suspend fun fetchSurahVerses(surahNumber: Int): Result<List<QuranVerse>> = withContext(Dispatchers.IO) {
        surahCache[surahNumber]?.let { return@withContext Result.success(it) }
        try {
            val url = URL("https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,bn.bengali")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 12_000
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val dataArr = json.getJSONArray("data")
            val arAyahs = dataArr.getJSONObject(0).getJSONArray("ayahs")
            val bnAyahs = dataArr.getJSONObject(1).getJSONArray("ayahs")
            val verses = (0 until arAyahs.length()).map { idx ->
                val ar = arAyahs.getJSONObject(idx)
                val bn = bnAyahs.getJSONObject(idx)
                QuranVerse(
                    numberInSurah = ar.getInt("numberInSurah"),
                    arabic = ar.getString("text"),
                    bengali = bn.getString("text")
                )
            }
            surahCache[surahNumber] = verses
            Result.success(verses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

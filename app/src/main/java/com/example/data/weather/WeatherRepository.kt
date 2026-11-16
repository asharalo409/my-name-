package com.example.data.weather

import com.example.data.islamic.DistrictGeo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

data class HourlyTempPoint(
    val hourLabel: String,
    val tempC: Double,
    val rainProb: Int
)

data class DailyForecastPoint(
    val dateLabel: String,
    val weatherCode: Int,
    val maxTempC: Double,
    val minTempC: Double,
    val rainMm: Double,
    val sunrise: String,
    val sunset: String
)

data class WeatherSnapshot(
    val district: DistrictGeo,
    val tempC: Double,
    val feelsLikeC: Double,
    val humidityPct: Int,
    val windKmh: Double,
    val windDirDeg: Int,
    val uvIndex: Double,
    val aqi: Int,
    val pm25: Double,
    val weatherCode: Int,
    val conditionBn: String,
    val fieldWorkerScore: Int,
    val fieldWorkerAdviceBn: String,
    val hourly: List<HourlyTempPoint>,
    val daily: List<DailyForecastPoint>
)

class WeatherRepository {

    suspend fun fetchWeather(district: DistrictGeo): Result<WeatherSnapshot> = withContext(Dispatchers.IO) {
        try {
            val forecastUrl = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=${district.lat}&longitude=${district.lon}" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m,uv_index" +
                    "&hourly=temperature_2m,precipitation_probability&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_sum" +
                    "&timezone=Asia%2FDhaka&forecast_days=7"
            )
            val aqiUrl = URL(
                "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=${district.lat}&longitude=${district.lon}&current=us_aqi,pm2_5&timezone=Asia%2FDhaka"
            )

            val fConn = (forecastUrl.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 12_000
            }
            val forecastJson = JSONObject(fConn.inputStream.bufferedReader().use { it.readText() })

            var aqiVal = 55
            var pm25Val = 15.0
            try {
                val aConn = (aqiUrl.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8_000
                    readTimeout = 10_000
                }
                val aJson = JSONObject(aConn.inputStream.bufferedReader().use { it.readText() }).getJSONObject("current")
                aqiVal = aJson.optInt("us_aqi", 55)
                pm25Val = aJson.optDouble("pm2_5", 15.0)
            } catch (_: Exception) {
                // Keep reasonable fallback if AQI sub-API is briefly unreachable
            }

            val cur = forecastJson.getJSONObject("current")
            val tempC = cur.optDouble("temperature_2m", 28.0)
            val feelsLikeC = cur.optDouble("apparent_temperature", tempC)
            val humidity = cur.optInt("relative_humidity_2m", 70)
            val wind = cur.optDouble("wind_speed_10m", 8.0)
            val windDir = cur.optInt("wind_direction_10m", 180)
            val uv = cur.optDouble("uv_index", 4.0)
            val code = cur.optInt("weather_code", 0)
            val precip = cur.optDouble("precipitation", 0.0)

            val hourlyObj = forecastJson.getJSONObject("hourly")
            val hTimes = hourlyObj.getJSONArray("time")
            val hTemps = hourlyObj.getJSONArray("temperature_2m")
            val hRain = hourlyObj.getJSONArray("precipitation_probability")
            val hourlyList = (0 until minOf(24, hTimes.length())).map { idx ->
                val rawTime = hTimes.getString(idx)
                HourlyTempPoint(
                    hourLabel = rawTime.substringAfter("T", rawTime).take(5),
                    tempC = hTemps.optDouble(idx, tempC),
                    rainProb = hRain.optInt(idx, 0)
                )
            }

            val dailyObj = forecastJson.getJSONObject("daily")
            val dDates = dailyObj.getJSONArray("time")
            val dCodes = dailyObj.getJSONArray("weather_code")
            val dMax = dailyObj.getJSONArray("temperature_2m_max")
            val dMin = dailyObj.getJSONArray("temperature_2m_min")
            val dRain = dailyObj.getJSONArray("precipitation_sum")
            val dSunrise = dailyObj.getJSONArray("sunrise")
            val dSunset = dailyObj.getJSONArray("sunset")
            val dailyList = (0 until dDates.length()).map { idx ->
                DailyForecastPoint(
                    dateLabel = dDates.getString(idx).substring(5),
                    weatherCode = dCodes.optInt(idx, 0),
                    maxTempC = dMax.optDouble(idx, tempC),
                    minTempC = dMin.optDouble(idx, tempC - 6),
                    rainMm = dRain.optDouble(idx, 0.0),
                    sunrise = dSunrise.optString(idx, "06:00").substringAfter("T").take(5),
                    sunset = dSunset.optString(idx, "17:20").substringAfter("T").take(5)
                )
            }

            val (score, advice) = computeFieldWorkerIndex(tempC, precip, aqiVal, uv)
            Result.success(
                WeatherSnapshot(
                    district = district,
                    tempC = tempC,
                    feelsLikeC = feelsLikeC,
                    humidityPct = humidity,
                    windKmh = wind,
                    windDirDeg = windDir,
                    uvIndex = uv,
                    aqi = aqiVal,
                    pm25 = pm25Val,
                    weatherCode = code,
                    conditionBn = describeWeatherCodeBn(code),
                    fieldWorkerScore = score,
                    fieldWorkerAdviceBn = advice,
                    hourly = hourlyList,
                    daily = dailyList
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun computeFieldWorkerIndex(temp: Double, precip: Double, aqi: Int, uv: Double): Pair<Int, String> {
        var penalty = 0
        if (temp > 34) penalty += ((temp - 34) * 5).roundToInt()
        if (temp < 13) penalty += ((13 - temp) * 4).roundToInt()
        if (precip > 1.0) penalty += (precip * 6).roundToInt().coerceAtMost(35)
        if (aqi > 100) penalty += ((aqi - 100) / 5).coerceAtMost(25)
        if (uv > 7) penalty += ((uv - 7) * 3).roundToInt()

        val score = (100 - penalty).coerceIn(10, 100)
        val advice = when {
            score >= 80 -> "মাঠ পর্যায়ের ত্রাণ ও রক্তদান কর্মসূচির জন্য চমৎকার আবহাওয়া।"
            score >= 55 -> "মাঠের কাজে পর্যাপ্ত পানি ও স্যালাইন সাথে রাখুন।"
            else -> "তীব্র আবহাওয়া/বৃষ্টি: জরুরি প্রয়োজনে নিরাপদ আশ্রয় ও ছাতা ব্যবহার করুন।"
        }
        return score to advice
    }

    fun describeWeatherCodeBn(code: Int): String = when (code) {
        0 -> "পরিষ্কার আকাশ"
        1, 2 -> "আংশিক মেঘলা"
        3 -> "মেঘলা আকাশ"
        45, 48 -> "কুয়াশাচ্ছন্ন"
        51, 53, 55, 61, 63, 65 -> "বৃষ্টিপাত"
        80, 81, 82 -> "দমকা বৃষ্টি"
        95, 96, 99 -> "বজ্রসহ বৃষ্টি"
        else -> "স্বাভাবিক আবহাওয়া"
    }
}

package com.example.data.islamic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PrayerCalculatorTest {

    @Test
    fun districts_containsAll64DistrictsOfBangladesh() {
        assertEquals(64, BangladeshDistricts.ALL.size)
        val uniqueBnNames = BangladeshDistricts.ALL.map { it.bnName }.toSet()
        assertEquals(64, uniqueBnNames.size)
    }

    @Test
    fun findByName_resolvesBengaliAndEnglishNames() {
        assertEquals("ঢাকা", BangladeshDistricts.findByName("Dhaka").bnName)
        assertEquals("Chattogram", BangladeshDistricts.findByName("চট্টগ্রাম").enName)
        assertEquals("Sylhet", BangladeshDistricts.findByName("সিলেট").enName)
    }

    @Test
    fun dhakaPrayerSchedule_hasMonotonicOrderAndValidQibla() {
        val dhaka = BangladeshDistricts.findByName("ঢাকা")
        val schedule = PrayerCalculator.calculate(dhaka, LocalDate.of(2026, 11, 16), hanafiAsr = true)

        assertTrue(schedule.fajr < schedule.sunrise)
        assertTrue(schedule.sunrise < schedule.dhuhr)
        assertTrue(schedule.dhuhr < schedule.asr)
        assertTrue(schedule.asr < schedule.maghrib)
        assertTrue(schedule.maghrib < schedule.isha)
        assertTrue("Qibla bearing for Dhaka should be ~277 deg", schedule.qiblaDegrees in 270.0..285.0)
        assertTrue(schedule.hijriDisplayBn.contains("হিজরি"))
    }

    @Test
    fun toBnDigits_convertsAsciiDigitsToBengaliNumerals() {
        assertEquals("১২৩৪৫৬৭৮৯০", PrayerCalculator.toBnDigits(1234567890))
        assertEquals("০", PrayerCalculator.toBnDigits(0))
    }
}

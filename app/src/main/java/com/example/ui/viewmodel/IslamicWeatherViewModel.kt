package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.islamic.BangladeshDistricts
import com.example.data.islamic.DistrictGeo
import com.example.data.islamic.IslamicLibrary
import com.example.data.islamic.PrayerCalculator
import com.example.data.islamic.PrayerSchedule
import com.example.data.islamic.QuranVerse
import com.example.data.weather.WeatherRepository
import com.example.data.weather.WeatherSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val snapshot: WeatherSnapshot) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

sealed interface QuranReaderState {
    data object Idle : QuranReaderState
    data object Loading : QuranReaderState
    data class Loaded(val surahNumber: Int, val verses: List<QuranVerse>) : QuranReaderState
    data class Error(val message: String) : QuranReaderState
}

class IslamicWeatherViewModel(
    private val weatherRepo: WeatherRepository = WeatherRepository()
) : ViewModel() {

    private val _selectedDistrict = MutableStateFlow(BangladeshDistricts.ALL.first())
    val selectedDistrict: StateFlow<DistrictGeo> = _selectedDistrict.asStateFlow()

    private val _hanafiAsr = MutableStateFlow(true)
    val hanafiAsr: StateFlow<Boolean> = _hanafiAsr.asStateFlow()

    private val _prayerSchedule = MutableStateFlow(PrayerCalculator.calculate(BangladeshDistricts.ALL.first()))
    val prayerSchedule: StateFlow<PrayerSchedule> = _prayerSchedule.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _quranState = MutableStateFlow<QuranReaderState>(QuranReaderState.Idle)
    val quranState: StateFlow<QuranReaderState> = _quranState.asStateFlow()

    private val _tasbihCount = MutableStateFlow(0)
    val tasbihCount: StateFlow<Int> = _tasbihCount.asStateFlow()

    private val _tasbihTarget = MutableStateFlow(33)
    val tasbihTarget: StateFlow<Int> = _tasbihTarget.asStateFlow()

    init {
        refreshWeather()
    }

    fun selectDistrict(district: DistrictGeo) {
        if (_selectedDistrict.value.bnName == district.bnName) return
        _selectedDistrict.value = district
        _prayerSchedule.value = PrayerCalculator.calculate(district, hanafiAsr = _hanafiAsr.value)
        refreshWeather()
    }

    fun syncDistrictByName(name: String) {
        val match = BangladeshDistricts.findByName(name)
        selectDistrict(match)
    }

    fun toggleAsrMethod() {
        _hanafiAsr.value = !_hanafiAsr.value
        _prayerSchedule.value = PrayerCalculator.calculate(_selectedDistrict.value, hanafiAsr = _hanafiAsr.value)
    }

    fun refreshWeather() {
        val district = _selectedDistrict.value
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            weatherRepo.fetchWeather(district)
                .onSuccess { _weatherState.value = WeatherUiState.Success(it) }
                .onFailure { _weatherState.value = WeatherUiState.Error(it.localizedMessage ?: "আবহাওয়া লোড হয়নি।") }
        }
    }

    fun loadSurah(surahNumber: Int) {
        viewModelScope.launch {
            _quranState.value = QuranReaderState.Loading
            IslamicLibrary.fetchSurahVerses(surahNumber)
                .onSuccess { _quranState.value = QuranReaderState.Loaded(surahNumber, it) }
                .onFailure { _quranState.value = QuranReaderState.Error(it.localizedMessage ?: "কুরআনের সূরা লোড করতে ইন্টারনেট সংযোগ প্রয়োজন।") }
        }
    }

    fun closeSurah() {
        _quranState.value = QuranReaderState.Idle
    }

    fun incrementTasbih() {
        _tasbihCount.value += 1
    }

    fun resetTasbih() {
        _tasbihCount.value = 0
    }

    fun setTasbihTarget(target: Int) {
        _tasbihTarget.value = target
    }
}

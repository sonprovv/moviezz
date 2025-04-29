package com.client.moviezz.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.client.moviezz.models.Category
import com.client.moviezz.models.Film
import com.client.moviezz.models.FilmDetail
import com.client.moviezz.models.Movie
import com.client.moviezz.repository.MovieRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MovieViewModel : ViewModel() {
    private var searchJob: Job? = null
    private val repository = MovieRepository()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _categorys = MutableStateFlow<List<Category>>(emptyList())
    val categorys: StateFlow<List<Category>> = _categorys

    private val _filmOfCategoryMap = MutableStateFlow<Map<Int, List<Film>>>(emptyMap())

    private val _filmDetail = MutableStateFlow<FilmDetail?>(null)
    val filmDetail: StateFlow<FilmDetail?> = _filmDetail

    private val _relatedFilms = MutableStateFlow<List<Film>>(emptyList())
    val relatedFilms: StateFlow<List<Film>> = _relatedFilms

    private val _searchFilms = MutableStateFlow<List<Film>>(emptyList())
    val searchFilms: StateFlow<List<Film>> = _searchFilms


    fun fetchMovies(msisdn: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.getMovieHome(msisdn)

                if (response.errorCode == "0") {
                    // Get movies with isSlide = 1 for the viewpager
                    val slideMovies = response.data.filter { it.isSlide == 1 }
                    _movies.value = slideMovies
                    Log.d("MovieViewModel", "Loaded ${slideMovies.size} slide movies")
                } else {
                    _error.value = response.message
                    Log.e("MovieViewModel", "API error bander: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                Log.e("MovieViewModel", "Exception: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchCategoryList(msisdn: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.getCategoryList(msisdn)
                if (response.errorCode == "0") {
                    _categorys.value = response.data
                    Log.d("MovieViewModel", "Loaded ${response.data.size} category list")
                } else {
                    _error.value = response.message
                    Log.e("MovieViewModel", "API error category: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
                Log.e("MovieViewModel", "Exception: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }

    }

    fun fetchFilmOfCategory(
        categoryId: Int,
        msisdn: String,
        language: String = "",
        page: Int = 0,
        size: Int = 100
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null
                val response =
                    repository.getFilmOfCategory(categoryId, msisdn, language, page, size)
                if (response.errorCode == "0") {
                    val updatedMap = _filmOfCategoryMap.value.toMutableMap()
                    updatedMap[categoryId] = response.data ?: emptyList()
                    _filmOfCategoryMap.value = updatedMap
                    Log.d(
                        "MovieViewModel",
                        "Loaded ${response.data.size ?: 0} films for category $categoryId"
                    )

                    // Update categoryList with films
                    val updatedCategories = _categorys.value.map { category ->
                        if (category.id == categoryId) {
                            category.copy(films = response.data ?: emptyList())
                        } else {
                            category
                        }
                    }
                    _categorys.value = updatedCategories
                } else {
                    _error.value = response.message
                    Log.e(
                        "MovieViewModel",
                        "API error for category $categoryId: ${response.message}"
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load films for category"
                Log.e("MovieViewModel", "Exception for category $categoryId: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchFilmDetail(filmId: Int, msisdn: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val response = repository.getFilmDetail(filmId, msisdn)
                if (response.errorCode == "0") {
                    _filmDetail.value = response.data
                    Log.d("MovieViewModel", "Loaded film detail for film $filmId")
                } else {
                    _error.value = response.message
                    Log.e("MovieViewModel", "API error for film $filmId: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load film detail"
                Log.e("MovieViewModel", "Exception for film $filmId: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchRelatedFilms(
        categoryId: Int,
        msisdn: String,
        page: Int = 0,
        size: Int = 100,
        language: String = ""
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = repository.getRelatedFilms(categoryId, msisdn, page, size, language)

                if (response.errorCode == "0") {
                    _relatedFilms.value = response.data ?: emptyList()
                    Log.d("MovieViewModel", "Loaded ${_relatedFilms.value.size} related films for category $categoryId")
                } else {
                    _error.value = response.message
                    Log.e("MovieViewModel", "API error for related films: ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load related films"
                Log.e("MovieViewModel", "Exception: ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }


    fun fetchSearchFilms(keyword: String, msisdn: String, page: Int = 1, size: Int = 100) {
        searchJob?.cancel() // Huỷ job cũ nếu còn đang chạy

        searchJob = viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val response = repository.getMovieSearch(keyword, msisdn, page, size)
                Log.e("hj", response.message)

                if (response.errorCode == "0") {
                    _searchFilms.value = response.data ?: emptyList()
                    Log.d(
                        "MovieViewModel",
                        "Loaded ${response.data.size ?: 0} films for keyword \"$keyword\""
                    )
                } else {
                    _error.value = response.message
                    Log.e("MovieViewModel", "API error for keyword \"$keyword\": ${response.message}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load films for keyword"
                Log.e("MovieViewModel", "Exception for keyword \"$keyword\": ${e.message}", e)
            } finally {
                _loading.value = false
            }
        }
    }

}
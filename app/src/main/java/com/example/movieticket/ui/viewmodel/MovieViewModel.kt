package com.example.movieticket.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieticket.data.model.Movie
import com.example.movieticket.data.repository.MovieRepository
import com.example.movieticket.di.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {
    private val TAG = "MovieViewModel"
    private val movieApi = NetworkModule.movieApi
    private val apiKey = NetworkModule.API_KEY

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies.asStateFlow()

    private val _upcomingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val upcomingMovies: StateFlow<List<Movie>> = _upcomingMovies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults: StateFlow<List<Movie>> = _searchResults.asStateFlow()

    init {
        Log.d(TAG, "Initializing MovieViewModel")
        loadMovies()
        loadUpcomingMovies()
    }

    private fun loadMovies() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading movies...")
                _isLoading.value = true
                _error.value = null
                val result = repository.getMovies()
                _movies.value = result
                Log.d(TAG, "Successfully loaded all movies")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading movies: ${e.message}", e)
                _error.value = "Đã có lỗi xảy ra: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUpcomingMovies() {
        viewModelScope.launch {
            try {
                val result = repository.getUpcomingMovies()
                _upcomingMovies.value = result
            } catch (e: Exception) {
                // Log error but don't show to user since this is not critical
                Log.e("MovieViewModel", "Error loading upcoming movies: ${e.message}")
            }
        }
    }

    fun searchMovies(query: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Searching movies with query: $query")
                _isSearching.value = true
                _searchQuery.value = query
                _error.value = null
                
                if (query.isBlank()) {
                    _searchResults.value = emptyList()
                    return@launch
                }

                val response = movieApi.searchMovies(apiKey, query)
                Log.d(TAG, "Got ${response.results.size} search results")
                
                _searchResults.value = response.results.mapNotNull { result ->
                    try {
                        Movie(
                            id = result.id,
                            title = result.title,
                            posterPath = result.poster_path ?: "",
                            backdropPath = result.backdrop_path ?: "",
                            overview = result.overview ?: "",
                            releaseDate = result.release_date ?: "",
                            voteAverage = result.vote_average ?: 0.0,
                            genres = emptyList()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error mapping search result: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error searching movies: ${e.message}", e)
                _error.value = "Đã có lỗi xảy ra khi tìm kiếm: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun getMovie(id: Int): Movie? {
        return movies.value.find { it.id == id }
            ?: upcomingMovies.value.find { it.id == id }
            ?: searchResults.value.find { it.id == id }
    }

    suspend fun getMovieDetails(id: Int): Movie? {
        return try {
            val response = movieApi.getMovieDetails(id, apiKey)
            Movie(
                id = response.id,
                title = response.title,
                posterPath = response.poster_path ?: "",
                backdropPath = response.backdrop_path ?: "",
                overview = response.overview ?: "",
                releaseDate = response.release_date ?: "",
                voteAverage = response.vote_average ?: 0.0,
                genres = response.genres.map { it.name }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting movie details: ${e.message}")
            null
        }
    }

    fun refreshMovies() {
        loadMovies()
    }
} 
package com.example.movieticket.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.movieticket.R
import com.example.movieticket.data.model.Movie
import com.example.movieticket.ui.components.MovieCard
import com.example.movieticket.ui.components.MovieCategories
import com.example.movieticket.ui.viewmodel.MovieViewModel
import com.google.accompanist.pager.*
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val movies = viewModel.movies.collectAsState().value
    val upcomingMovies = viewModel.upcomingMovies.collectAsState().value
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    val allGenres = remember(movies) {
        movies.flatMap { it.genres }.distinct().sorted()
    }

    val filteredMovies = remember(selectedCategory, movies) {
        when (selectedCategory) {
            "All" -> movies
            else -> movies.filter { movie ->
                movie.genres.any { genre ->
                    genre.equals(selectedCategory, ignoreCase = true)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Upcoming movies size: ${upcomingMovies.size}")
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E1E1E).copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Column {
                    // Top section with app name and profile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "phim gì cũng có",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Search Icon
                            IconButton(onClick = onSearchClick) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Tìm kiếm",
                                    tint = Color(0xFF2196F3)
                                )
                            }

                            // Profile Picture
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onProfileClick)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(user?.photoUrl ?: R.drawable.default_avatar)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.background_home),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                // Main Content with ScrollableColumn
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                ) {
                    // Upcoming Movies Slider
                    if (upcomingMovies.isNotEmpty()) {
                        Text(
                            text = "Phim sắp chiếu",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TrendingMoviesSlider(
                            movies = upcomingMovies.take(5),
                            onMovieClick = onMovieClick
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Now Playing Section
                    Text(
                        text = "Phim đang chiếu",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    // Categories
                    MovieCategories(
                        selectedCategory = selectedCategory,
                        categories = listOf("All") + allGenres,
                        onCategorySelected = { category ->
                            selectedCategory = category
                        }
                    )

                    // Movie Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((filteredMovies.size * 160).dp)
                            .nestedScroll(rememberNestedScrollInteropConnection())
                    ) {
                        items(filteredMovies) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = { onMovieClick(movie) }
                            )
                        }
                    }

                    // Add some bottom padding
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TrendingMoviesSlider(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    val pagerState = rememberPagerState()
    
    // Auto-scroll effect
    LaunchedEffect(Unit) {
        while(true) {
            delay(5000) // Wait for 5 seconds
            val nextPage = (pagerState.currentPage + 1) % movies.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp)
    ) {
        HorizontalPager(
            count = movies.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val movie = movies[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onMovieClick(movie) }
            ) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w500${movie.posterPath}",
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = movie.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier,
                activeColor = Color(0xFF2196F3),
                inactiveColor = Color.White.copy(alpha = 0.6f),
                indicatorWidth = 5.dp,
                indicatorHeight = 5.dp,
                spacing = 5.dp
            )
        }
    }
} 
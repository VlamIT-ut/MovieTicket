package com.example.movieticket.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movieticket.data.model.Movie
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.movieticket.ui.viewmodel.MovieViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    movie: Movie,
    onBuyTicketClick: () -> Unit,
    onBackClick: () -> Unit,
    movieViewModel: MovieViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val nowShowingMovies = movieViewModel.movies.collectAsState().value
    val isNowShowing = nowShowingMovies.any { it.id == movie.id }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết phim", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B2B4A)
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1B2B4A),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onBuyTicketClick,
                    enabled = isNowShowing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNowShowing) Color(0xFF00B4D8) else Color.Gray
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(if (isNowShowing) "Đặt vé ngay" else "Chưa mở bán")
                }
            }
        },
        containerColor = Color(0xFF0A1832) // Màu nền chính
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // Ảnh phim
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${movie.backdropPath}",
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // Thông tin phim
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Đánh giá và năm
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐ ${String.format("%.1f", movie.voteAverage)}",
                        color = Color.White
                    )
                    Text(
                        text = " | ${movie.releaseDate}",
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thể loại
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(movie.genres) { genre ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(genre) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF1B2B4A)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mô tả
                Text(
                    text = "Nội dung phim",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val rows = mutableListOf<List<androidx.compose.ui.layout.Measurable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Measurable>()
        var currentRowWidth = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(measurable)
            currentRowWidth += placeable.width
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = rows.sumOf { row ->
            row.maxOf { it.measure(constraints).height }
        }

        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                row.forEach { measurable ->
                    val placeable = measurable.measure(constraints)
                    placeable.place(x, y)
                    x += placeable.width
                }
                y += row.maxOf { it.measure(constraints).height }
            }
        }
    }
}

@Composable
fun ExpandableDescription(
    title: String,
    description: String,
    initiallyExpanded: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    
    Column(
        modifier = Modifier.animateContentSize()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis
        )
        
        if (description.length > 150) {
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Ẩn bớt" else "Xem thêm"
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Ẩn bớt" else "Xem thêm"
                    )
                }
            }
        }
    }
} 
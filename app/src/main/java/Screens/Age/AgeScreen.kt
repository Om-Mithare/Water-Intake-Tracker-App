package com.waterintaketracker.screens.age
import androidx.compose.foundation.border

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun AgeScreen(
    viewModel: AgeViewModel = viewModel(),
    onNextClick: () -> Unit = {}
) {
    val age by viewModel.age.collectAsState()
    val ageRange = (1..100).map { it.toString() }
    val listState: LazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll to selected item
    LaunchedEffect(age) {
        val index = ageRange.indexOf(age)
        if (index >= 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(index - 3)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 24.dp)
    ) {
        // Header Section
        AnimatedVisibility(
            visible = true,
            enter = fadeIn()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How old are you?",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap on your age to select",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.7f)
                    )
                )
            }
        }

        // Age Picker List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 160.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(ageRange) { item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    viewModel.onAgeChange(item)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = if (age == item) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = if (age == item) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .then(
                                        if (age == item) Modifier
                                            .border(
                                                width = 2.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                        else Modifier
                                    )
                            )
                        }
                    }

                }
            }
        }

        // Next Button
        AnimatedVisibility(
            visible = age.isNotBlank(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = {
                    onNextClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text("Next")
            }
        }
    }
}

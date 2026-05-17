package com.example.hebrewassistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hebrewassistant.data.LlmSettings
import com.example.hebrewassistant.data.SettingsRepository
import com.example.hebrewassistant.llm.LlmRepository
import com.example.hebrewassistant.llm.LlmResponse
import kotlinx.coroutines.launch

@Composable
fun MainScreen(repository: LlmRepository, settingsRepository: SettingsRepository) {
    val coroutineScope = rememberCoroutineScope()
    val topicState = remember { mutableStateOf("Составь урок по базовой грамматике иврита") }
    val lessonState = remember { mutableStateOf<LlmResponse?>(null) }
    val progressState = remember { mutableStateOf(listOf<String>()) }
    val statusState = remember { mutableStateOf("Готов к работе") }
    val loadingState = remember { mutableStateOf(false) }
    val showVoicePractice = remember { mutableStateOf(false) }
    val showSettings = remember { mutableStateOf(false) }
    val menuExpanded = remember { mutableStateOf(false) }
    val recognizedSpeech = remember { mutableStateOf("") }
    val settings by settingsRepository.settingsFlow.collectAsState(initial = LlmSettings())

    LaunchedEffect(Unit) {
        progressState.value = repository.loadProgress().map { it.summary }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (showVoicePractice.value) {
            VoicePracticeScreen(
                onBack = { showVoicePractice.value = false },
                onSubmit = { text -> recognizedSpeech.value = text }
            )
        } else if (showSettings.value) {
            SettingsScreen(
                settingsRepository = settingsRepository,
                onBack = { showSettings.value = false }
            )
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                @OptIn(ExperimentalMaterial3Api::class)
                TopAppBar(
                    title = { Text(text = "Помощник по изучению иврита", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = { menuExpanded.value = true }) {
                            Icon(imageVector = Icons.Filled.Settings, contentDescription = "Настройки")
                        }

                        DropdownMenu(
                            expanded = menuExpanded.value,
                            onDismissRequest = { menuExpanded.value = false }
                        ) {
                            DropdownMenuItem(text = { Text("Настройка LLM") }, onClick = {
                                menuExpanded.value = false
                                showSettings.value = true
                            })
                        }
                    }
                )

                Text(text = "Текущий провайдер: ${settings.provider.name}")

                OutlinedTextField(
                    value = topicState.value,
                    onValueChange = { topicState.value = it },
                    label = { Text("Тема урока") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            loadingState.value = true
                            statusState.value = "Генерация урока..."
                            lessonState.value = repository.generateLesson(topicState.value)
                            progressState.value = repository.loadProgress().map { it.summary }
                            statusState.value = "Урок готов"
                            loadingState.value = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сгенерировать задание")
                }

                TextButton(onClick = { showVoicePractice.value = true }) {
                    Text("Перейти к голосовой практике")
                }

                if (loadingState.value) {
                    CircularProgressIndicator()
                }

                Text(text = "Статус: ${statusState.value}")
                lessonState.value?.let { lesson ->
                    Text(text = lesson.output, style = MaterialTheme.typography.bodyLarge)
                }

                Text(text = "Распознанная речь:")
                Text(text = recognizedSpeech.value, style = MaterialTheme.typography.bodyMedium)

                Text(text = "Последние сохраненные задания:")
                progressState.value.take(3).forEach { summary ->
                    Text(text = summary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

package com.example.hebrewassistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.hebrewassistant.data.LlmSettings
import com.example.hebrewassistant.data.SettingsRepository
import com.example.hebrewassistant.llm.LlmProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val settingsState by settingsRepository.settingsFlow.collectAsState(initial = LlmSettings())
    var provider by remember { mutableStateOf(settingsState.provider) }
    var apiKey by remember { mutableStateOf(settingsState.apiKey) }

    LaunchedEffect(settingsState) {
        provider = settingsState.provider
        apiKey = settingsState.apiKey
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Настройки LLM")
                TextButton(onClick = onBack) {
                    Text(text = "Назад")
                }
            }

            Text(text = "Выберите провайдера")

            LlmProvider.values().forEach { option ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option == provider,
                        onClick = { provider = option }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = option.name)
                }
            }

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "API ключ") },
                placeholder = { Text(text = "Введите ключ для выбранного провайдера") }
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        settingsRepository.updateSettings(provider, apiKey)
                        snackbarHostState.showSnackbar("Настройки сохранены")
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Сохранить")
            }

            Text(text = "Gemini: используйте ваш Google API ключ. OpenAI: используйте ваш ключ OpenAI.")
        }
    }
}

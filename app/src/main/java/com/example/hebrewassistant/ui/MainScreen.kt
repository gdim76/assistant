package com.example.hebrewassistant.ui

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.hebrewassistant.data.ChatMessage
import com.example.hebrewassistant.data.SettingsRepository
import com.example.hebrewassistant.llm.LlmRepository
import com.example.hebrewassistant.voice.VoiceInputManager
import com.example.hebrewassistant.voice.VoiceRecognitionListener
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(repository: LlmRepository, settingsRepository: SettingsRepository) {
    val coroutineScope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val inputText = remember { mutableStateOf("") }
    val loadingState = remember { mutableStateOf(value = false) }
    val showSettings = remember { mutableStateOf(value = false) }
    val menuExpanded = remember { mutableStateOf(value = false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val context = LocalContext.current
    val activity = context as? Activity
    val voiceManager = remember { activity?.let { VoiceInputManager(it) } }
    val listening = remember { mutableStateOf(value = false) }

    val listState = rememberLazyListState()

    DisposableEffect(voiceManager) {
        onDispose {
            voiceManager?.destroy()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        messages.add(ChatMessage(text, isUser = true))
        inputText.value = ""
        coroutineScope.launch {
            loadingState.value = true
            try {
                val response = repository.chat(text)
                messages.add(ChatMessage(response.output, isUser = false))
            } catch (e: Exception) {
                messages.add(ChatMessage("Ошибка: ${e.message}", isUser = false))
            } finally {
                loadingState.value = false
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (showSettings.value) {
            SettingsScreen(
                settingsRepository = settingsRepository,
                onBack = { showSettings.value = false }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(text = "Иврит Ассистент", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = { menuExpanded.value = true }) {
                            Icon(imageVector = Icons.Filled.Settings, contentDescription = "Настройки")
                        }

                        DropdownMenu(
                            expanded = menuExpanded.value,
                            onDismissRequest = { menuExpanded.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Настройка LLM") },
                                onClick = {
                                    menuExpanded.value = false
                                    showSettings.value = true
                                }
                            )
                        }
                    }
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        ChatBubble(message)
                    }
                    if (loadingState.value) {
                        item {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (listening.value) {
                                voiceManager?.stopListening()
                                listening.value = false
                            } else {
                                voiceManager?.startListening(object : VoiceRecognitionListener {
                                    override fun onReady() {
                                        listening.value = true
                                    }

                                    override fun onEndOfSpeech() {
                                        listening.value = false
                                    }

                                    override fun onResult(text: String) {
                                        inputText.value = text
                                        sendMessage(text)
                                    }

                                    override fun onError(errorCode: Int) {
                                        listening.value = false
                                    }
                                })
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (listening.value) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = "Голосовой ввод",
                            tint = if (listening.value) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = inputText.value,
                        onValueChange = { inputText.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .clickable {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            },
                        placeholder = { Text("Введите сообщение...") },
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage(inputText.value) })
                    )

                    IconButton(
                        onClick = { sendMessage(inputText.value) },
                        enabled = inputText.value.isNotBlank() && !loadingState.value
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val shape = if (message.isUser) {
        RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
    } else {
        RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = color,
            shape = shape,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }
}

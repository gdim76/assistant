package com.example.hebrewassistant.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hebrewassistant.voice.VoiceInputManager
import com.example.hebrewassistant.voice.VoiceRecognitionListener

@Composable
fun VoicePracticeScreen(
    onBack: () -> Unit,
    onSubmit: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val voiceManager = remember { activity?.let { VoiceInputManager(it) } }
    val recognizedText = remember { mutableStateOf("") }
    val errorText = remember { mutableStateOf<String?>(null) }
    val listening = remember { mutableStateOf(false) }

    DisposableEffect(voiceManager) {
        onDispose {
            voiceManager?.destroy()
        }
    }

    Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Голосовая практика", style = MaterialTheme.typography.titleLarge)

            Text(text = "Нажмите кнопку, говорите на иврите, и система распознает вашу речь.", style = MaterialTheme.typography.bodyLarge)

            Button(
                onClick = {
                    errorText.value = null
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
                                recognizedText.value = text
                                onSubmit(text)
                            }

                            override fun onError(errorCode: Int) {
                                listening.value = false
                                errorText.value = "Ошибка распознавания: $errorCode"
                            }
                        })
                    }
                }
            ) {
                Text(if (listening.value) "Остановить" else "Начать" )
            }

            Text(text = "Распознанная речь:")
            Text(text = recognizedText.value, style = MaterialTheme.typography.bodyLarge)
            errorText.value?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium) }

            TextButton(onClick = onBack) {
                Text("Назад к заданиям")
            }
        }
    }
}

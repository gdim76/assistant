package com.example.hebrewassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.hebrewassistant.data.ProgressDatabase
import com.example.hebrewassistant.data.SettingsRepository
import com.example.hebrewassistant.data.StudentProfileRepository
import com.example.hebrewassistant.llm.LlmRepository
import com.example.hebrewassistant.llm.LlmServiceFactory
import com.example.hebrewassistant.ui.MainScreen
import com.example.hebrewassistant.ui.theme.HebrewAssistantTheme

class MainActivity : ComponentActivity() {
    private val progressDao by lazy {
        ProgressDatabase.getDatabase(applicationContext).lessonProgressDao()
    }

    private val studentProfileRepository by lazy {
        StudentProfileRepository(ProgressDatabase.getDatabase(applicationContext).studentProfileDao())
    }

    private val settingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    private val llmRepository by lazy {
        LlmRepository(LlmServiceFactory(), settingsRepository, progressDao, studentProfileRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HebrewAssistantTheme {
                MainScreen(repository = llmRepository, settingsRepository = settingsRepository)
            }
        }
    }
}

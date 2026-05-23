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
    private val database by lazy {
        ProgressDatabase.getDatabase(applicationContext)
    }

    private val progressDao by lazy {
        database.lessonProgressDao()
    }

    private val studentProfileRepository by lazy {
        StudentProfileRepository(database.studentProfileDao())
    }

    private val learningMemoryDao by lazy {
        database.learningMemoryDao()
    }

    private val settingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    private val llmRepository by lazy {
        LlmRepository(
            LlmServiceFactory(),
            settingsRepository,
            progressDao,
            studentProfileRepository,
            learningMemoryDao
        )
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

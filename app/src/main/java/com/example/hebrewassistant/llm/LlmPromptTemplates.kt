package com.example.hebrewassistant.llm

import com.example.hebrewassistant.data.ChatMessage
import com.example.hebrewassistant.data.LessonProgress
import com.example.hebrewassistant.data.LessonSummary
import com.example.hebrewassistant.data.StudentProfile

object LlmPromptTemplates {
    fun buildLessonPrompt(topic: String): String {
        return buildLessonPrompt(topic, profile = null)
    }

    fun buildLessonPrompt(topic: String, profile: StudentProfile?): String {
        return buildTeacherSystemPrompt(profile) + "\n\n" +
            "Составь адаптивный урок на тему: '$topic'. " +
            "Включи краткое объяснение грамматики, полезные фразы и упражнения с переводом."
    }

    fun buildReviewPrompt(progress: List<LessonProgress>): String {
        if (progress.isEmpty()) {
            return "Пользователь только начинает. Сформируй вводное упражнение по базовой лексике иврита."
        }

        val recentTopics = progress.take(3).joinToString(separator = ", ") { it.topic }
        return "У студента были уроки по темам: $recentTopics. " +
            "Предложи новое задание, которое повторит предыдущие темы и добавит одно новое слово."
    }

    fun buildChatPrompt(message: String): String {
        return buildTeacherSystemPrompt(profile = null) + "\n\n" +
            "Ответь пользователю на сообщение: '$message'. " +
            "Если пользователь говорит на иврите, исправь его ошибки, если они есть, и ответь на иврите с переводом на русский. " +
            "Если пользователь спрашивает на русском, ответь на русском и предложи полезные фразы на иврите."
    }

    fun buildChatPrompt(message: String, profile: StudentProfile?): String {
        return buildChatPrompt(message, profile, conversation = emptyList())
    }

    fun buildChatPrompt(message: String, profile: StudentProfile?, conversation: List<ChatMessage>): String {
        return buildChatPrompt(message, profile, conversation, lessonSummaries = emptyList())
    }

    fun buildChatPrompt(
        message: String,
        profile: StudentProfile?,
        conversation: List<ChatMessage>,
        lessonSummaries: List<LessonSummary>
    ): String {
        return buildTeacherSystemPrompt(profile) + "\n\n" +
            buildLessonSummaryContext(lessonSummaries) + "\n\n" +
            buildConversationContext(conversation, maxMessages = CHAT_WINDOW_SIZE) + "\n\n" +
            "Ответь пользователю на сообщение: '$message'. " +
            "Если пользователь говорит на иврите, исправь его ошибки, если они есть, и ответь на иврите с переводом на русский. " +
            "Если пользователь спрашивает на русском, ответь на русском и предложи полезные фразы на иврите. " +
            "Используй профиль ученика, сводки прошлых уроков и текущий контекст для выбора примеров, темпа и сложности."
    }

    fun shouldStartStarterLesson(message: String): Boolean {
        val normalized = message.lowercase()
        return listOf(
            "начнем",
            "начать",
            "урок",
            "продолж",
            "готов",
            "давай",
            "practice",
            "lesson",
            "start"
        ).any { normalized.contains(it) }
    }

    fun nextStarterLessonNumber(completedLessonCount: Int): Int {
        return (completedLessonCount + 1).coerceIn(1, STARTER_LESSON_COUNT)
    }

    fun starterLessonTopic(lessonNumber: Int): String {
        return starterLessonPlan(lessonNumber).topic
    }

    fun buildStarterLessonPrompt(
        lessonNumber: Int,
        profile: StudentProfile?,
        conversation: List<ChatMessage> = emptyList()
    ): String {
        return buildStarterLessonPrompt(lessonNumber, profile, conversation, lessonSummaries = emptyList())
    }

    fun buildStarterLessonPrompt(
        lessonNumber: Int,
        profile: StudentProfile?,
        conversation: List<ChatMessage>,
        lessonSummaries: List<LessonSummary>
    ): String {
        val plan = starterLessonPlan(lessonNumber)
        return buildTeacherSystemPrompt(profile) + "\n\n" +
            buildLessonSummaryContext(lessonSummaries) + "\n\n" +
            buildConversationContext(conversation, maxMessages = CHAT_WINDOW_SIZE) + "\n\n" +
            "Проведи стартовый урок $lessonNumber из $STARTER_LESSON_COUNT: '${plan.topic}'. " +
            "Это не лекция целиком, а живой чат-урок: дай короткий блок объяснения, 3-5 ключевых фраз на иврите с переводом, " +
            "один микродиалог и одно упражнение, после которого жди ответ ученика. " +
            "Учитывай профиль ученика и подставляй близкие ему темы, но не спрашивай заново то, что уже есть в профиле. " +
            "Цель урока: ${plan.goal}. " +
            "Языковой материал: ${plan.languageMaterial}. " +
            "Практика: ${plan.practice}. " +
            "В конце задай только одно задание или вопрос для ответа ученика."
    }

    fun buildLessonSummaryPrompt(
        profile: StudentProfile?,
        lessonMessages: List<ChatMessage>,
        lessonSummaries: List<LessonSummary>
    ): String {
        return buildTeacherSystemPrompt(profile) + "\n\n" +
            buildLessonSummaryContext(lessonSummaries) + "\n\n" +
            buildConversationContext(lessonMessages, maxMessages = null) + "\n\n" +
            "Ты — аналитик данных обучения. Проанализируй полный лог текущего урока и верни строго один JSON-блок внутри маркеров <lesson_summary>...</lesson_summary>. " +
            "JSON должен иметь поля: topic, newWords, practicedPhrases, repeatedErrors, correctedErrors, progressScore, nextLessonRecommendation, profilePatch. " +
            "profilePatch должен содержать currentLevel, weakPointsToAdd, weakPointsToRemove, masteredTopicsToAdd, teacherNotes. " +
            "Не добавляй markdown и пояснения вне JSON, кроме короткой видимой фразы перед служебным блоком. " +
            "Если данных мало, верни честную короткую сводку и progressScore от 1 до 10."
    }

    fun buildInitialOnboardingMessage(): String {
        return "שלום! Я буду твоим преподавателем иврита. Давай начнем со знакомства.\n\nКак тебя зовут?"
    }

    fun buildOnboardingPrompt(message: String, profile: StudentProfile?): String {
        return buildOnboardingPrompt(message, profile, conversation = emptyList())
    }

    fun buildOnboardingPrompt(message: String, profile: StudentProfile?, conversation: List<ChatMessage>): String {
        return buildTeacherSystemPrompt(profile) + "\n\n" +
            buildConversationContext(conversation, maxMessages = CHAT_WINDOW_SIZE) + "\n\n" +
            "Сейчас идет первый чат-сценарий знакомства и диагностики уровня. " +
            "Твоя задача: выяснить имя ученика, родной язык, интересы, область работы и цель изучения иврита. " +
            "Веди сценарий строго в режиме диалога: задавай только один главный вопрос за раз. " +
            "Порядок знакомства: имя -> область работы -> интересы -> цель изучения иврита -> родной язык/языки -> диагностика. " +
            "Если ученик уже ответил на текущий пункт, коротко отреагируй и переходи к следующему одному вопросу. " +
            "Затем проведи тест на 10-15 вопросов на иврите с постепенным ростом сложности от уровня 1 до уровня 4. " +
            "В диагностике тоже задавай по одному вопросу, не все вопросы сразу. " +
            "После каждого ответа кратко реагируй, исправляй ошибки и выбирай следующий вопрос. " +
            "Когда диагностика закончена, коротко назови стартовый уровень и предложи начать первый урок 'Знакомство на иврите'. " +
            "Когда данных достаточно, заверши диагностику и в самом конце ответа добавь один служебный блок в точном формате:\n" +
            "<student_profile>{\"name\":\"...\",\"nativeLanguage\":\"...\",\"interests\":\"...\",\"workArea\":\"...\",\"learningGoal\":\"...\",\"currentLevel\":\"...\",\"placementSummary\":\"...\"}</student_profile>\n" +
            "Не добавляй служебный блок, пока диагностика не завершена. " +
            "Сообщение ученика: '$message'."
    }

    fun extractStudentProfileJson(response: String): String? {
        return Regex("<student_profile>(.*?)</student_profile>", RegexOption.DOT_MATCHES_ALL)
            .find(response)
            ?.groupValues
            ?.get(1)
            ?.trim()
    }

    fun stripStudentProfileBlock(response: String): String {
        return response.replace(
            Regex("\\s*<student_profile>.*?</student_profile>", RegexOption.DOT_MATCHES_ALL),
            ""
        ).trim()
    }

    fun extractLessonSummaryJson(response: String): String? {
        return Regex("<lesson_summary>(.*?)</lesson_summary>", RegexOption.DOT_MATCHES_ALL)
            .find(response)
            ?.groupValues
            ?.get(1)
            ?.trim()
    }

    fun stripLessonSummaryBlock(response: String): String {
        return response.replace(
            Regex("\\s*<lesson_summary>.*?</lesson_summary>", RegexOption.DOT_MATCHES_ALL),
            ""
        ).trim()
    }

    private fun buildTeacherSystemPrompt(profile: StudentProfile?): String {
        val profileContext = profile?.let {
            listOfNotNull(
                it.name?.let { value -> "Имя: $value" },
                it.nativeLanguage?.let { value -> "Родной язык: $value" },
                it.interests?.let { value -> "Интересы: $value" },
                it.workArea?.let { value -> "Работа/область: $value" },
                it.learningGoal?.let { value -> "Цель изучения: $value" },
                it.currentLevel?.let { value -> "Текущий уровень: $value" },
                it.placementSummary?.let { value -> "Резюме диагностики: $value" }
            ).joinToString(separator = "\n")
        }?.takeIf { it.isNotBlank() } ?: "Профиль ученика еще не заполнен."

        return """
            Ты — виртуальный репетитор по ивриту и творческий преподаватель языка.
            Ты адаптируешь уроки под ученика, объясняешь понятно и даешь практику маленькими шагами.
            Используй разные форматы: диалоги, перевод, исправление ошибок, мини-тесты, словарные карточки, грамматику в контексте, вопросы на понимание и короткие творческие задания.
            Подключай литературу, историю, культуру Израиля, современную речь и ИТ-темы, когда это помогает сделать урок живым и полезным.
            Не выдумывай слабые места и любимые форматы ученика заранее: наблюдай за ответами и мягко усиливай практику в процессе.
            Профиль ученика:
            $profileContext
        """.trimIndent()
    }

    private fun buildConversationContext(conversation: List<ChatMessage>, maxMessages: Int?): String {
        if (conversation.isEmpty()) {
            return "История диалога пока пуста."
        }

        val selectedMessages = maxMessages?.let { conversation.takeLast(it) } ?: conversation
        val recentMessages = selectedMessages.joinToString(separator = "\n") { message ->
            val author = if (message.isUser) "Ученик" else "Ассистент"
            "$author: ${message.text}"
        }
        return "История диалога:\n$recentMessages"
    }

    private fun buildLessonSummaryContext(lessonSummaries: List<LessonSummary>): String {
        if (lessonSummaries.isEmpty()) {
            return "Сводок прошлых уроков пока нет."
        }

        val summaries = lessonSummaries.take(LESSON_SUMMARY_WINDOW_SIZE).joinToString(separator = "\n") { summary ->
            "Урок: ${summary.topic}; JSON: ${summary.summaryJson}"
        }
        return "Сводки прошлых уроков:\n$summaries"
    }

    private fun starterLessonPlan(lessonNumber: Int): StarterLessonPlan {
        return STARTER_LESSON_PLANS[(lessonNumber - 1).coerceIn(0, STARTER_LESSON_PLANS.lastIndex)]
    }

    private data class StarterLessonPlan(
        val topic: String,
        val goal: String,
        val languageMaterial: String,
        val practice: String
    )

    private val STARTER_LESSON_PLANS = listOf(
        StarterLessonPlan(
            topic = "Знакомство на иврите",
            goal = "ученик представляется простыми фразами и понимает базовое приветствие",
            languageMaterial = "שלום, קוראים לי..., אני גר/גרה ב..., אני עובד/עובדת ב..., אני לומד/לומדת עברית",
            practice = "собрать 2-3 фразы о себе и ответить на вопрос איך קוראים לך?"
        ),
        StarterLessonPlan(
            topic = "Я живу, работаю, учу",
            goal = "закрепить простые предложения о себе и различие мужских/женских форм в настоящем времени",
            languageMaterial = "אני גר/גרה, אני עובד/עובדת, אני לומד/לומדת, ב-, עם, עברית",
            practice = "перевести короткие фразы о жизни и работе, затем составить одну фразу о себе"
        ),
        StarterLessonPlan(
            topic = "Первый короткий диалог",
            goal = "провести мини-диалог знакомства, используя профиль ученика и его интересы",
            languageMaterial = "מה שלומך?, נעים מאוד, מאיפה אתה/את?, מה אתה/את עושה?, אני אוהב/אוהבת...",
            practice = "разыграть диалог из 4-6 реплик и мягко исправить ошибки ученика"
        )
    )

    private const val STARTER_LESSON_COUNT = 3
    private const val CHAT_WINDOW_SIZE = 10
    private const val LESSON_SUMMARY_WINDOW_SIZE = 5
}

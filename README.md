# Hebrew Assistant

Android приложение для помощи в изучении иврита с поддержкой генерации уроков через LLM-провайдеров.

## Основное
- Jetpack Compose UI
- Выбор и сохранение LLM-провайдера: Gemini / OpenAI
- Хранение настроек в DataStore Preferences
- Хранение прогресса уроков в Room
- Голосовая практика и распознавание речи

## Статус
- Реализован основной интерфейс и экран настроек
- Добавлен выбор провайдера и ввод API ключа
- Добавлена поддержка `OpenAiLlmService` и `GeminiLlmService`
- Добавлен `MockLlmService` при отсутствии ключа
- Собран `./gradlew :app:assembleDebug`
- Приложение установлено на эмулятор
- Зафиксированы версии зависимостей и инструментов

## Требования
- Android SDK 34
- minSdk 24
- Java 17 для сборки
- Gradle Wrapper и Android Studio

## Пинованные версии
- Kotlin: 1.9.20
- AGP: 8.2.0
- Compose UI: 1.5.0
- Compose Compiler: 1.5.4
- Room: 2.6.0
- KSP: 1.9.20-1.0.13
- OkHttp: 4.11.0
- Retrofit: 2.9.0

## Сборка
```bash
./gradlew clean assembleDebug
```

## Установка на устройство / эмулятор
```bash
./gradlew installDebug
```

## Как настроить
1. Склонируйте репозиторий:
   ```bash
git clone https://github.com/gdim76/assistant.git
cd assistant
```
2. Убедитесь, что `JAVA_HOME` указывает на JDK 17.
3. Откройте проект в Android Studio или соберите из терминала.

## Игнорируемые файлы
В проекте есть `.gitignore` для Gradle/Android артефактов, сборок и IDE-файлов.

## Контакты
Проект размещён на GitHub: https://github.com/gdim76/assistant

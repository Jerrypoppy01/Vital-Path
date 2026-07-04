package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HealthViewModel(
    application: Application,
    private val repository: HealthRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("vitalflow_prefs", Context.MODE_PRIVATE)

    // Current date selection
    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Authentication & Profile States
    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Alex Mercer") ?: "Alex Mercer")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(prefs.getString("user_email", "alex.mercer@vitalflow.ai") ?: "alex.mercer@vitalflow.ai")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userAge = MutableStateFlow(prefs.getInt("user_age", 28))
    val userAge: StateFlow<Int> = _userAge.asStateFlow()

    private val _userGender = MutableStateFlow(prefs.getString("user_gender", "Male") ?: "Male")
    val userGender: StateFlow<String> = _userGender.asStateFlow()

    private val _userHeight = MutableStateFlow(prefs.getFloat("user_height", 178f))
    val userHeight: StateFlow<Float> = _userHeight.asStateFlow()

    private val _userWeight = MutableStateFlow(prefs.getFloat("user_weight", 72.5f))
    val userWeight: StateFlow<Float> = _userWeight.asStateFlow()

    private val _userActivityLevel = MutableStateFlow(prefs.getString("user_activity_level", "Moderately Active") ?: "Moderately Active")
    val userActivityLevel: StateFlow<String> = _userActivityLevel.asStateFlow()

    private val _isPremium = MutableStateFlow(prefs.getBoolean("is_premium", false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    // Gamification & Progression States
    private val _userLevel = MutableStateFlow(prefs.getInt("user_level", 1))
    val userLevel: StateFlow<Int> = _userLevel.asStateFlow()

    private val _userXP = MutableStateFlow(prefs.getInt("user_xp", 120))
    val userXP: StateFlow<Int> = _userXP.asStateFlow()

    private val _userStreak = MutableStateFlow(prefs.getInt("user_streak", 4))
    val userStreak: StateFlow<Int> = _userStreak.asStateFlow()

    private val _unlockedAchievements = MutableStateFlow(
        prefs.getString("unlocked_achievements", "hydrate_3_days,steps_champion")
            ?.split(",")
            ?.filter { it.isNotEmpty() }
            ?.toSet() ?: setOf("hydrate_3_days", "steps_champion")
    )
    val unlockedAchievements: StateFlow<Set<String>> = _unlockedAchievements.asStateFlow()

    // AI Coach Chat Message List State
    data class ChatMessage(
        val sender: String, // "user", "coach"
        val text: String,
        val isTyping: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "coach",
                text = "Hello! I am your **VitalFlow AI Coach**. I am here to help you optimize your hydration, sleep, active workouts, and nutrition. Let me know if you want a custom workout routine, tips on meal plans, or advice on meeting your wellness goals!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isCoachTyping = MutableStateFlow(false)
    val isCoachTyping: StateFlow<Boolean> = _isCoachTyping.asStateFlow()

    // Retrieve all goals and health logs
    val allGoals: StateFlow<List<Goal>> = repository.allGoals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<HealthLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current daily totals derived from logs for the selected date
    val currentDayLogs: StateFlow<List<HealthLog>> = combine(_selectedDate, allLogs) { date, logs ->
        logs.filter { it.date == date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeDefaultGoalsIfEmpty()
        }
    }

    // Helper to get current date formatted
    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // Select a different date to view/log
    fun selectDate(dateString: String) {
        _selectedDate.value = dateString
    }

    // Move date forward or backward
    fun offsetDate(days: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val date = sdf.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            _selectedDate.value = sdf.format(cal.time)
        } catch (e: Exception) {
            _selectedDate.value = getCurrentDateString()
        }
    }

    // Authentication Functions
    fun login(email: String, password: String, isGoogle: Boolean = false) {
        viewModelScope.launch {
            _isLoggedIn.value = true
            if (isGoogle) {
                _userName.value = "Johnathan Doe"
                _userEmail.value = "john.doe@gmail.com"
            } else {
                _userName.value = email.split("@").first().replaceFirstChar { it.uppercase() }
                _userEmail.value = email
            }
            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("user_name", _userName.value)
                putString("user_email", _userEmail.value)
                apply()
            }
            addXP(50) // Bonus XP for logging in
        }
    }

    fun signUp(name: String, email: String, age: Int, gender: String, height: Float, weight: Float, activityLevel: String) {
        viewModelScope.launch {
            _isLoggedIn.value = true
            _userName.value = name
            _userEmail.value = email
            _userAge.value = age
            _userGender.value = gender
            _userHeight.value = height
            _userWeight.value = weight
            _userActivityLevel.value = activityLevel

            prefs.edit().apply {
                putBoolean("is_logged_in", true)
                putString("user_name", name)
                putString("user_email", email)
                putInt("user_age", age)
                putString("user_gender", gender)
                putFloat("user_height", height)
                putFloat("user_weight", weight)
                putString("user_activity_level", activityLevel)
                apply()
            }
            addXP(100) // Signup welcome XP
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoggedIn.value = false
            prefs.edit().putBoolean("is_logged_in", false).apply()
        }
    }

    fun updateProfile(name: String, age: Int, gender: String, height: Float, weight: Float, activityLevel: String) {
        _userName.value = name
        _userAge.value = age
        _userGender.value = gender
        _userHeight.value = height
        _userWeight.value = weight
        _userActivityLevel.value = activityLevel

        prefs.edit().apply {
            putString("user_name", name)
            putInt("user_age", age)
            putString("user_gender", gender)
            putFloat("user_height", height)
            putFloat("user_weight", weight)
            putString("user_activity_level", activityLevel)
            apply()
        }
    }

    fun setPremiumStatus(enabled: Boolean) {
        _isPremium.value = enabled
        prefs.edit().putBoolean("is_premium", enabled).apply()
        if (enabled) {
            addXP(200)
            unlockAchievement("premium_club")
        }
    }

    // Gamification Engines
    fun addXP(amount: Int) {
        val currentXP = _userXP.value + amount
        var level = _userLevel.value
        val xpNeeded = level * 150 // Standard leveling curve

        if (currentXP >= xpNeeded) {
            val remainingXP = currentXP - xpNeeded
            level += 1
            _userLevel.value = level
            _userXP.value = remainingXP
            prefs.edit().putInt("user_level", level).putInt("user_xp", remainingXP).apply()
            unlockAchievement("level_up_$level")
        } else {
            _userXP.value = currentXP
            prefs.edit().putInt("user_xp", currentXP).apply()
        }
    }

    fun unlockAchievement(id: String) {
        val current = _unlockedAchievements.value.toMutableSet()
        if (current.add(id)) {
            _unlockedAchievements.value = current
            prefs.edit().putString("unlocked_achievements", current.joinToString(",")).apply()
            // Add custom log entry for achievement unlock
            addLog("achievement_unlocked", 1.0)
            addXP(100)
        }
    }

    // Insert log
    fun addLog(type: String, value: Double) {
        viewModelScope.launch {
            val log = HealthLog(
                date = _selectedDate.value,
                type = type,
                value = value
            )
            repository.insertLog(log)
            addXP(15) // XP for logging daily progress

            // Evaluate milestones
            evaluateMilestones(type)
        }
    }

    private fun evaluateMilestones(type: String) {
        viewModelScope.launch {
            val todayLogs = currentDayLogs.value
            val goals = allGoals.value.associate { it.type to it.value }

            if (type.startsWith("water")) {
                val totalWater = todayLogs.filter { it.type.startsWith("water") }.sumOf { it.value }
                val targetWater = goals["water"] ?: 2000.0
                if (totalWater >= targetWater) {
                    unlockAchievement("hydrate_champion")
                }
            }
            if (type == "steps") {
                val totalSteps = todayLogs.filter { it.type == "steps" }.sumOf { it.value }
                val targetSteps = goals["steps"] ?: 10000.0
                if (totalSteps >= targetSteps) {
                    unlockAchievement("steps_champion")
                }
            }
            if (type.startsWith("sleep")) {
                unlockAchievement("sleep_sage")
            }
            if (type.startsWith("mood")) {
                unlockAchievement("mood_mindfulness")
            }
        }
    }

    // Delete log by ID
    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteLogById(id)
        }
    }

    // Update specific goal target
    fun updateGoal(type: String, value: Double) {
        viewModelScope.launch {
            repository.insertGoal(Goal(type, value))
        }
    }

    // Clear all user logs
    fun resetAllLogs() {
        viewModelScope.launch {
            repository.clearAll()
            _userXP.value = 0
            _userLevel.value = 1
            _unlockedAchievements.value = emptySet()
            prefs.edit().apply {
                putInt("user_xp", 0)
                putInt("user_level", 1)
                putString("unlocked_achievements", "")
                apply()
            }
        }
    }

    // Get list of last 7 dates for historical comparison
    fun getLast7Days(): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 0..6) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return list.reversed() // Chronological order
    }

    // AI Coach Chat Send Message
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(sender = "user", text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        _isCoachTyping.value = true

        viewModelScope.launch {
            val key = getApiKeyFromBuildConfig()
            val answer = if (key.isNotEmpty() && key != "null" && !key.startsWith("__")) {
                try {
                    withContext(Dispatchers.IO) {
                        val systemPrompt = "You are VitalFlow AI Coach, an expert, supportive and empathetic health coach. Guide the user on nutrition, hydration, workouts, sleep, and weight. Maintain a warm organic Natural Tones layout theme in spirit. Strictly include a warning disclaimer that you do not offer medical advice. Keep advice clear, scientific, scannable, formatted with markdown list bullets, and encouraging."
                        
                        val historyList = _chatMessages.value.takeLast(10).map { msg ->
                            GeminiContent(parts = listOf(GeminiPart(text = if (msg.sender == "coach") "Coach: ${msg.text}" else "User: ${msg.text}")))
                        }

                        val req = GeminiRequest(
                            contents = historyList,
                            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))
                        )
                        val response = GeminiClient.service.generateContent(apiKey = key, request = req)
                        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                            ?: "I parsed your metrics but couldn't generate a recommendation right now. Let's keep focused on your wellness goals today!"
                    }
                } catch (e: Exception) {
                    getOfflineSimulationAnswer(text)
                }
            } else {
                getOfflineSimulationAnswer(text)
            }

            _isCoachTyping.value = false
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = "coach", text = answer)
            addXP(10) // Bonus XP for talking to coach
        }
    }

    // Extract the API key securely with a backup check
    private fun getApiKeyFromBuildConfig(): String {
        return try {
            val field = com.example.BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // A rich offline simulation capability to guarantee the app remains fully functional, informative and useful
    private fun getOfflineSimulationAnswer(query: String): String {
        val q = query.lowercase()
        val disclaimer = "\n\n*Disclaimer: I am an AI health coach providing motivational and informational wellness guides. Please consult a qualified healthcare provider for medical inquiries.*"
        
        return when {
            q.contains("water") || q.contains("hydrate") || q.contains("drink") -> {
                "**Optimal Hydration Action Plan** 💧\n\nBased on your profile, here are highly curated recommendations:\n\n- **Target Volume**: Aim for **2.5 to 3 Liters** of water today given your active lifestyle.\n- **Timing**: Sip 300ml right upon waking up to jumpstart metabolism, and 250ml every 2 hours.\n- **Workout Hydration**: Consume 500ml of water with electrolytes 30 minutes before exercising.\n- **Wellness Tip**: Hydration boosts cognitive focus, filters toxins, and enhances athletic recovery." + disclaimer
            }
            q.contains("workout") || q.contains("exercise") || q.contains("gym") || q.contains("run") -> {
                "**Custom Exercise Guide** 🏃‍♂️\n\nLet's keep your momentum going! Here is a recommended routine:\n\n- **Warmup**: 5 minutes of dynamic stretching (arm swings, high knees).\n- **Main Cardio (20 Mins)**: High-Intensity Interval Training (HIIT) — 45s effort, 15s rest, repeat.\n- **Strength (15 Mins)**: Bodyweight training including 3 sets of squats (15 reps), pushups (12 reps), and planks (45s).\n- **Cooldown**: 5 mins of static quad/hamstring stretching.\n\n*You earn 15 XP for logging workouts in the Tracker tab!*" + disclaimer
            }
            q.contains("sleep") || q.contains("tired") || q.contains("night") -> {
                "**Sleep Hygiene Optimization** 🌙\n\nQuality recovery is the foundation of peak performance. Let's improve your metrics:\n\n- **Consistent Window**: Aim to sleep by 10:30 PM and wake at 6:30 AM to hit your 8-hour target.\n- **Digital Sunset**: Power down all phones and blue-light screens 1 hour before bedtime.\n- **Cool Room Environment**: Lower room temperature to around 18-20°C for deep REM sleep.\n- **Mindfulness**: Spend 5 minutes practicing diaphragmatic breathing or journaling to calm your central nervous system." + disclaimer
            }
            q.contains("food") || q.contains("diet") || q.contains("eat") || q.contains("nutrition") || q.contains("breakfast") -> {
                "**Premium Organic Nutrition Plan** 🥑\n\nFueling your body with nutrient-dense macros will keep you energized:\n\n- **Balanced Breakfast**: Avocado slices, poached eggs, and steel-cut oatmeal with blueberries. Great source of fiber and healthy fats!\n- **Optimal Lunch**: Grilled salmon or organic tofu paired with quinoa and roasted asparagus.\n- **High-Protein Snack**: Greek yogurt with chia seeds and raw almonds.\n- **Hydrating Dinner**: Lean chicken breast with sweet potatoes and a side of spinach leaf salad." + disclaimer
            }
            q.contains("bmi") || q.contains("weight") || q.contains("height") -> {
                val hM = _userHeight.value / 100.0
                val bmi = if (hM > 0.0) _userWeight.value / (hM * hM) else 0.0
                val formattedBmi = String.format(Locale.getDefault(), "%.1f", bmi)
                val category = when {
                    bmi < 18.5 -> "Underweight"
                    bmi < 25.0 -> "Healthy Weight"
                    bmi < 30.0 -> "Overweight"
                    else -> "Obese"
                }
                "**Biometric Profile & BMI Analysis** 📊\n\nUsing your profile data:\n- **Height**: ${_userHeight.value} cm\n- **Weight**: ${_userWeight.value} kg\n- **Computed BMI**: **$formattedBmi**\n- **Classification**: **$category**\n\n**Actionable Advice**:\n- To maintain a healthy weight, prioritize consuming complex whole-foods and logging a daily caloric target.\n- Incorporate resistance training at least 3 times a week to promote lean muscle mass and structural bone density." + disclaimer
            }
            else -> {
                "**Personalized Wellness Guidance** ✨\n\nI processed your inquiry! Daily consistency is the secret to lasting transformation:\n\n- Check your **Overall Wellness Score** on the Dashboard — aim for 90%+.\n- Complete your daily **Hydration, Sleep, Steps, and Active minutes**.\n- Stay interactive! Ask me specific questions like: *\"Give me a water schedule\"*, *\"What is a good post-workout meal?\"*, or *\"Why is deep sleep important?\"*." + disclaimer
            }
        }
    }

    // Factory to instantiate the ViewModel with the repository parameter
    class Factory(
        private val application: Application,
        private val repository: HealthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HealthViewModel::class.java)) {
                return HealthViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


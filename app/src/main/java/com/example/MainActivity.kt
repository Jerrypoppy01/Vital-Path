package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.HealthDatabase
import com.example.data.HealthLog
import com.example.data.Goal
import com.example.data.HealthRepository
import com.example.ui.HealthViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = HealthDatabase.getDatabase(this)
        val repository = HealthRepository(database.healthDao())

        setContent {
            var darkThemeEnabled by remember { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = darkThemeEnabled) {
                val viewModel: HealthViewModel = viewModel(
                    factory = HealthViewModel.Factory(application, repository)
                )

                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = isLoggedIn,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(500)) togetherWith
                                    fadeOut(animationSpec = tween(500))
                        },
                        label = "auth_transition"
                    ) { loggedIn ->
                        if (loggedIn) {
                            MainAppContent(
                                viewModel = viewModel,
                                darkThemeEnabled = darkThemeEnabled,
                                onThemeToggle = { darkThemeEnabled = !darkThemeEnabled }
                            )
                        } else {
                            AuthGatewayScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 🔐 AUTHENTICATION & ONBOARDING GATEWAY SCREEN
// ==========================================
@Composable
fun AuthGatewayScreen(viewModel: HealthViewModel) {
    var isSignUp by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Form inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("28") }
    var height by remember { mutableStateOf("175") }
    var weight by remember { mutableStateOf("70") }
    var gender by remember { mutableStateOf("Male") }
    var activityLevel by remember { mutableStateOf("Moderately Active") }

    var onboardingStep by remember { mutableStateOf(0) } // 0: Credentials, 1: Profile Details

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // VitalFlow AI Premium Branding Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "VitalFlow Logo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "V I T A L F L O W  A I",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Your Premium Organic Health Companion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }

            // High Fidelity Animated Form Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (onboardingStep == 0) {
                        // Title / Form tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.background,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Button(
                                onClick = { isSignUp = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!isSignUp) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (!isSignUp) Color.White else MaterialTheme.colorScheme.secondary
                                ),
                                elevation = null
                            ) {
                                Text("Login", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { isSignUp = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSignUp) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (isSignUp) Color.White else MaterialTheme.colorScheme.secondary
                                ),
                                elevation = null
                            ) {
                                Text("Sign Up", fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isSignUp) "Create Your Premium Account" else "Welcome Back",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("username@vitalflow.ai") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input"),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input"),
                            shape = RoundedCornerShape(14.dp)
                        )

                        if (isSignUp) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Name") },
                                placeholder = { Text("Alex Mercer") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
                        } else {
                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clickable {
                                        Toast
                                            .makeText(
                                                context,
                                                "Simulated password reset email sent to $email!",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                    }
                            )
                        }

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    Toast.makeText(context, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (isSignUp) {
                                    onboardingStep = 1 // Proceed to biometric onboarding
                                } else {
                                    viewModel.login(email, password)
                                    Toast.makeText(context, "Logged in as ${email.split("@").first()}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("auth_submit_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (isSignUp) "Continue to Profile Setup" else "Login to VitalFlow", fontWeight = FontWeight.Bold)
                        }

                        // Third-Party Mock Authentication Dividers
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                            Text("  OR CONTINUE WITH  ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        }

                        // Google Sign-In Button
                        OutlinedButton(
                            onClick = {
                                viewModel.login("john.doe@gmail.com", "google", isGoogle = true)
                                Toast.makeText(context, "Google Sign-In Simulated Successfully!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = StepsOrange)
                                Text("Sign In with Google", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Guest mode entry
                        TextButton(
                            onClick = {
                                viewModel.login("guest@vitalflow.ai", "guest")
                                Toast.makeText(context, "Entered as Guest", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Continue as Guest / Offline Mode", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        // BIOMETRIC PROFILE ONBOARDING FOR PREMIUM TAILORED EXPERIENCE
                        Text(
                            text = "Tailor Your Health Goals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Enter your biometrics to help VitalFlow AI compute your personal BMI, calories, and hydration curves.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text("Age") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = { Text("Height (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = { Text("Weight (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Gender Select
                        Text("Gender Select", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Male", "Female", "Non-binary").forEach { gOption ->
                                val selected = gender == gOption
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { gender = gOption },
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp), contentAlignment = Alignment.Center
                                    ) {
                                        Text(gOption, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                    }
                                }
                            }
                        }

                        // Activity Level Select
                        Text("Activity Level", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Sedentary", "Lightly Active", "Moderately Active", "Very Active").forEach { aOption ->
                                val selected = activityLevel == aOption
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activityLevel = aOption },
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(aOption, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                        if (selected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onboardingStep = 0 },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Back")
                            }

                            Button(
                                onClick = {
                                    val finalName = if (name.isBlank()) "User" else name
                                    val finalAge = age.toIntOrNull() ?: 28
                                    val finalHeight = height.toFloatOrNull() ?: 175f
                                    val finalWeight = weight.toFloatOrNull() ?: 70f
                                    viewModel.signUp(
                                        name = finalName,
                                        email = email,
                                        age = finalAge,
                                        gender = gender,
                                        height = finalHeight,
                                        weight = finalWeight,
                                        activityLevel = activityLevel
                                    )
                                    Toast.makeText(context, "Welcome to VitalFlow, $finalName!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Generate Profile", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 📱 MAIN APPLICATION CONTENT & NAVIGATION
// ==========================================
@Composable
fun MainAppContent(
    viewModel: HealthViewModel,
    darkThemeEnabled: Boolean,
    onThemeToggle: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Loggers, 2: AI Coach, 3: Analytics, 4: Rewards/Gamification, 5: Settings

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(if (selectedTab == 1) Icons.Default.AddCircle else Icons.Outlined.AddCircle, contentDescription = "Log Data") },
                    label = { Text("Loggers", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(if (selectedTab == 2) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome, contentDescription = "AI Coach") },
                    label = { Text("AI Coach", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(if (selectedTab == 3) Icons.Default.BarChart else Icons.Outlined.BarChart, contentDescription = "Analytics") },
                    label = { Text("Charts", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(if (selectedTab == 4) Icons.Default.EmojiEvents else Icons.Outlined.EmojiEvents, contentDescription = "Progression") },
                    label = { Text("Flow", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(if (selectedTab == 5) Icons.Default.Settings else Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Profile", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "navigation_tabs_container"
            ) { tab ->
                when (tab) {
                    0 -> DashboardTab(viewModel)
                    1 -> LoggersTab(viewModel)
                    2 -> CoachTab(viewModel)
                    3 -> AnalyticsTab(viewModel)
                    4 -> RewardsTab(viewModel)
                    5 -> SettingsTab(viewModel, darkThemeEnabled, onThemeToggle)
                }
            }
        }
    }
}

// ==========================================
// 🏠 TAB 0: VITALFLOW PREMIUM DASHBOARD
// ==========================================
@Composable
fun DashboardTab(viewModel: HealthViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val logs by viewModel.currentDayLogs.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userStreak by viewModel.userStreak.collectAsStateWithLifecycle()
    val userLevel by viewModel.userLevel.collectAsStateWithLifecycle()

    val goalsMap = remember(allGoals) { allGoals.associate { it.type to it.value } }

    val targetWater = goalsMap["water"] ?: 2000.0
    val targetSteps = goalsMap["steps"] ?: 10000.0
    val targetSleep = goalsMap["sleep"] ?: 8.0
    val targetActive = goalsMap["active_minutes"] ?: 30.0
    val targetCalories = goalsMap["calories"] ?: 2200.0

    // Compute current values from Logs
    val currentWater = remember(logs) { logs.filter { it.type == "water" }.sumOf { it.value } }
    val currentSteps = remember(logs) { logs.filter { it.type == "steps" }.sumOf { it.value } }
    val currentSleep = remember(logs) { logs.filter { it.type.startsWith("sleep") }.sumOf { it.value } }
    val currentActive = remember(logs) { logs.filter { it.type.startsWith("workout") || it.type == "active_minutes" }.sumOf { it.value } }
    val currentCalories = remember(logs) { logs.filter { it.type.startsWith("nutrition") }.sumOf { it.value } }
    val currentWeight = remember(logs) { logs.filter { it.type == "weight" }.lastOrNull()?.value ?: 72.5 }
    val currentMood = remember(logs) { logs.filter { it.type.startsWith("mood") }.lastOrNull()?.let {
        it.type.split(":").getOrNull(1) ?: "Calm"
    } ?: "Balanced" }

    // Ratios
    val waterRatio = (currentWater / targetWater).coerceIn(0.0, 1.0)
    val stepsRatio = (currentSteps / targetSteps).coerceIn(0.0, 1.0)
    val sleepRatio = (currentSleep / targetSleep).coerceIn(0.0, 1.0)
    val activeRatio = (currentActive / targetActive).coerceIn(0.0, 1.0)

    val overallScore = ((waterRatio + stepsRatio + sleepRatio + activeRatio) / 4.0 * 100).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_scroll_panel"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Streak Indicator strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good day, $userName",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Level $userLevel Mindful Achiever",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Interactive Streak Counter
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LightTertiary.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, LightTertiary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = "Streak", tint = LightTertiary, modifier = Modifier.size(18.dp))
                        Text("$userStreak Day Streak", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = LightTertiary)
                    }
                }
            }
        }

        // Horizontal Date Control Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.offsetDate(-1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Day", tint = MaterialTheme.colorScheme.primary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedDate == viewModel.getCurrentDateString()) "Today" else selectedDate,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text("Track and sync metrics", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                    }

                    IconButton(onClick = { viewModel.offsetDate(1) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Hero Wellness Gauge Ring Card (Canvas)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Daily Wellness Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (overallScore >= 90) "Superb dedication! All systems functioning optimally."
                            else if (overallScore >= 60) "Great progression. Keep hydrating and log your workout."
                            else "Let's kickstart your goals today. Sip some water to start!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                            lineHeight = 16.sp
                        )

                        // Progress Indicator Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Badge(containerColor = WaterBlue.copy(alpha = 0.2f)) { Text("Water", color = WaterBlue, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(2.dp)) }
                            Badge(containerColor = StepsOrange.copy(alpha = 0.2f)) { Text("Steps", color = StepsOrange, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(2.dp)) }
                            Badge(containerColor = SleepPurple.copy(alpha = 0.2f)) { Text("Sleep", color = SleepPurple, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(2.dp)) }
                            Badge(containerColor = ActiveGreen.copy(alpha = 0.2f)) { Text("Work", color = ActiveGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp, modifier = Modifier.padding(2.dp)) }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Custom Canvas Progress Wheel
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .weight(0.8f),
                        contentAlignment = Alignment.Center
                    ) {
                        val animatedScore by animateFloatAsState(
                            targetValue = overallScore.toFloat() / 100f,
                            animationSpec = tween(1200, easing = FastOutSlowInEasing),
                            label = "score_anim"
                        )

                        Canvas(modifier = Modifier.size(90.dp)) {
                            // Background Ring
                            drawArc(
                                color = Color.LightGray.copy(alpha = 0.25f),
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Progress Ring
                            drawArc(
                                color = LightPrimary,
                                startAngle = -90f,
                                sweepAngle = animatedScore * 360f,
                                useCenter = false,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$overallScore%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Goal", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Grid of Health Telemetries (2 Columns)
        item {
            Text(
                text = "Biometric Telemetries",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1: Water & Steps
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TelemetryMiniCard(
                        title = "Water Intake",
                        current = "${currentWater.toInt()} ml",
                        target = "${targetWater.toInt()} ml",
                        ratio = waterRatio.toFloat(),
                        color = WaterBlue,
                        icon = Icons.Default.WaterDrop,
                        modifier = Modifier.weight(1f),
                        onQuickAdd = { viewModel.addLog("water", 250.0) },
                        quickLabel = "+250ml"
                    )

                    TelemetryMiniCard(
                        title = "Step Count",
                        current = "${currentSteps.toInt()}",
                        target = "${targetSteps.toInt()}",
                        ratio = stepsRatio.toFloat(),
                        color = StepsOrange,
                        icon = Icons.Default.DirectionsRun,
                        modifier = Modifier.weight(1f),
                        onQuickAdd = { viewModel.addLog("steps", 1000.0) },
                        quickLabel = "+1k steps"
                    )
                }

                // Row 2: Sleep & Active Workout
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TelemetryMiniCard(
                        title = "Sleep Recovery",
                        current = "$currentSleep hrs",
                        target = "${targetSleep.toInt()} hrs",
                        ratio = sleepRatio.toFloat(),
                        color = SleepPurple,
                        icon = Icons.Default.Bedtime,
                        modifier = Modifier.weight(1f),
                        onQuickAdd = { viewModel.addLog("sleep:Good", 1.0) },
                        quickLabel = "+1 hour"
                    )

                    TelemetryMiniCard(
                        title = "Active Workout",
                        current = "${currentActive.toInt()} mins",
                        target = "${targetActive.toInt()} mins",
                        ratio = activeRatio.toFloat(),
                        color = ActiveGreen,
                        icon = Icons.Default.FitnessCenter,
                        modifier = Modifier.weight(1f),
                        onQuickAdd = { viewModel.addLog("workout:Activity:15", 15.0) },
                        quickLabel = "+15m workout"
                    )
                }

                // Row 3: Nutrition & Weight
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(StepsOrange.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = StepsOrange, modifier = Modifier.size(16.dp))
                                }
                                Text("Nutrition", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text("${currentCalories.toInt()} Cal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Target: ${targetCalories.toInt()} Cal", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(WaterBlue.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Scale, contentDescription = null, tint = WaterBlue, modifier = Modifier.size(16.dp))
                                }
                                Text("Weight", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Text("$currentWeight kg", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Mood: $currentMood", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Daily Logs History Summary
        item {
            Text(
                text = "Today's Log Timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp), contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                            Text("No telemetry logs recorded for this day.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                            Text("Click the Loggers tab below to add water, steps, workouts, sleep quality, and calories!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        } else {
            items(logs) { log ->
                val details = remember(log.type) {
                    when {
                        log.type == "water" -> Triple("Water Logged", "ml", WaterBlue)
                        log.type == "steps" -> Triple("Steps Logged", "steps", StepsOrange)
                        log.type.startsWith("sleep") -> Triple("Sleep logged (${log.type.split(":").getOrElse(1) { "Good" }})", "hrs", SleepPurple)
                        log.type.startsWith("workout") -> {
                            val parts = log.type.split(":")
                            val name = parts.getOrElse(1) { "Activity" }
                            val dur = parts.getOrElse(2) { "${log.value.toInt()}" }
                            Triple("Workout: $name ($dur mins)", "Cal burned", ActiveGreen)
                        }
                        log.type.startsWith("nutrition") -> {
                            val parts = log.type.split(":")
                            val meal = parts.getOrElse(1) { "Meal" }
                            val desc = parts.getOrElse(2) { "Logged Item" }
                            Triple("Nutrition: $meal ($desc)", "Calories", StepsOrange)
                        }
                        log.type.startsWith("mood") -> {
                            val parts = log.type.split(":")
                            val rating = parts.getOrElse(1) { "Good" }
                            val note = parts.getOrElse(2) { "No journal entry" }
                            Triple("Mood: $rating ($note)", "/ 5.0 Rating", SleepPurple)
                        }
                        log.type == "weight" -> Triple("Body Weight logged", "kg", WaterBlue)
                        else -> Triple("Award unlocked", "point", LightTertiary)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(details.third, CircleShape)
                            )
                            Column {
                                Text(details.first, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                val time = remember(log.timestamp) {
                                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                                }
                                Text(time, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${log.value.toInt()} ${details.second}",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = details.third
                            )
                            IconButton(onClick = { viewModel.deleteLog(log.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryMiniCard(
    title: String,
    current: String,
    target: String,
    ratio: Float,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onQuickAdd: () -> Unit,
    quickLabel: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                    }
                    Text(title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Column {
                Text(current, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text("Target: $target", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
            }

            // Progress Line Arc
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = color.copy(alpha = 0.12f),
            )

            // Direct quick logging button
            Button(
                onClick = onQuickAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color.copy(alpha = 0.15f),
                    contentColor = color
                ),
                elevation = null
            ) {
                Text(quickLabel, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ==========================================
// ✏️ TAB 1: LOGGERS & TELEMETRY ADDITIONS
// ==========================================
@Composable
fun LoggersTab(viewModel: HealthViewModel) {
    var selectedCategory by remember { mutableStateOf("water") } // "water", "steps", "sleep", "workout", "nutrition", "mood", "weight"
    val context = LocalContext.current

    // Input States
    var waterInput by remember { mutableStateOf("") }
    var stepsInput by remember { mutableStateOf("") }
    var sleepInput by remember { mutableStateOf("8.0") }
    var sleepQuality by remember { mutableStateOf("Good") }

    var workoutName by remember { mutableStateOf("Running") }
    var workoutDuration by remember { mutableStateOf("") }
    var workoutCalories by remember { mutableStateOf("") }

    var mealCategory by remember { mutableStateOf("Breakfast") }
    var mealDescription by remember { mutableStateOf("") }
    var mealCalories by remember { mutableStateOf("") }

    var moodSelection by remember { mutableStateOf("Calm") }
    var moodJournal by remember { mutableStateOf("") }

    var weightInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Log Biometrics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Track your daily biometric telemetry values manually",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Horizontal Category Select row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val categories = listOf(
                    Pair("water", "💧 Water"),
                    Pair("steps", "🏃 Steps"),
                    Pair("sleep", "🛌 Sleep"),
                    Pair("workout", "🔥 Workout"),
                    Pair("nutrition", "🥑 Food"),
                    Pair("mood", "🧠 Mood"),
                    Pair("weight", "⚖️ Weight")
                )
                items(categories) { cat ->
                    val selected = selectedCategory == cat.first
                    Card(
                        modifier = Modifier.clickable { selectedCategory = cat.first },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = cat.second,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Dynamic Input Card depending on Selected category
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Enter ${selectedCategory.replaceFirstChar { it.uppercase() }} Telemetry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    when (selectedCategory) {
                        "water" -> {
                            OutlinedTextField(
                                value = waterInput,
                                onValueChange = { waterInput = it },
                                label = { Text("Water Volume (ml)") },
                                placeholder = { Text("e.g. 500") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            // Quick buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(250, 500, 750).forEach { vol ->
                                    Button(
                                        onClick = { waterInput = vol.toString() },
                                        colors = ButtonDefaults.buttonColors(containerColor = WaterBlue.copy(alpha = 0.1f), contentColor = WaterBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = null
                                    ) {
                                        Text("$vol ml")
                                    }
                                }
                            }
                        }

                        "steps" -> {
                            OutlinedTextField(
                                value = stepsInput,
                                onValueChange = { stepsInput = it },
                                label = { Text("Steps Count") },
                                placeholder = { Text("e.g. 8500") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        "sleep" -> {
                            OutlinedTextField(
                                value = sleepInput,
                                onValueChange = { sleepInput = it },
                                label = { Text("Sleep Duration (hours)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Text("Sleep Quality", fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Excellent", "Good", "Restless", "Tired").forEach { qual ->
                                    val isSel = sleepQuality == qual
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { sleepQuality = qual },
                                        label = { Text(qual) }
                                    )
                                }
                            }
                        }

                        "workout" -> {
                            OutlinedTextField(
                                value = workoutName,
                                onValueChange = { workoutName = it },
                                label = { Text("Exercise Type") },
                                placeholder = { Text("Running, Swimming, Yoga...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = workoutDuration,
                                    onValueChange = { workoutDuration = it },
                                    label = { Text("Duration (mins)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = workoutCalories,
                                    onValueChange = { workoutCalories = it },
                                    label = { Text("Burned (Cal)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        "nutrition" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { mCat ->
                                    val isSel = mealCategory == mCat
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { mealCategory = mCat },
                                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                        colors = CardDefaults.cardColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                    ) {
                                        Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
                                            Text(mCat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = mealDescription,
                                onValueChange = { mealDescription = it },
                                label = { Text("Logged food description") },
                                placeholder = { Text("Tuna Salad with olive oil...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = mealCalories,
                                onValueChange = { mealCalories = it },
                                label = { Text("Calories (kcal)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        "mood" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Amazing", "Calm", "Neutral", "Tired", "Stressed").forEach { mood ->
                                    val isSel = moodSelection == mood
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { moodSelection = mood },
                                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                        colors = CardDefaults.cardColors(containerColor = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                                    ) {
                                        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                            Text(mood, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = moodJournal,
                                onValueChange = { moodJournal = it },
                                label = { Text("Journal Notes") },
                                placeholder = { Text("Felt super focused after mindfulness...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        "weight" -> {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text("Current Weight (kg)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            when (selectedCategory) {
                                "water" -> {
                                    val amt = waterInput.toDoubleOrNull() ?: 0.0
                                    if (amt > 0) {
                                        viewModel.addLog("water", amt)
                                        waterInput = ""
                                        Toast.makeText(context, "Hydration registered!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                "steps" -> {
                                    val amt = stepsInput.toDoubleOrNull() ?: 0.0
                                    if (amt > 0) {
                                        viewModel.addLog("steps", amt)
                                        stepsInput = ""
                                        Toast.makeText(context, "Steps synced successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                "sleep" -> {
                                    val hrs = sleepInput.toDoubleOrNull() ?: 0.0
                                    if (hrs > 0) {
                                        viewModel.addLog("sleep:$sleepQuality", hrs)
                                        Toast.makeText(context, "Sleep registered!", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                "workout" -> {
                                    val dur = workoutDuration.toDoubleOrNull() ?: 30.0
                                    val cal = workoutCalories.toDoubleOrNull() ?: (dur * 8)
                                    viewModel.addLog("workout:$workoutName:${dur.toInt()}", cal)
                                    workoutDuration = ""
                                    workoutCalories = ""
                                    Toast.makeText(context, "Workout logged!", Toast.LENGTH_SHORT).show()
                                }

                                "nutrition" -> {
                                    val cal = mealCalories.toDoubleOrNull() ?: 350.0
                                    val desc = if (mealDescription.isBlank()) "Healthy Item" else mealDescription
                                    viewModel.addLog("nutrition:$mealCategory:$desc", cal)
                                    mealDescription = ""
                                    mealCalories = ""
                                    Toast.makeText(context, "Nutrition logged!", Toast.LENGTH_SHORT).show()
                                }

                                "mood" -> {
                                    val desc = if (moodJournal.isBlank()) "Mindful mood check" else moodJournal
                                    viewModel.addLog("mood:$moodSelection:$desc", 5.0)
                                    moodJournal = ""
                                    Toast.makeText(context, "Mood state captured!", Toast.LENGTH_SHORT).show()
                                }

                                "weight" -> {
                                    val w = weightInput.toDoubleOrNull() ?: 0.0
                                    if (w > 0) {
                                        viewModel.addLog("weight", w)
                                        // Update actual profile weight too
                                        viewModel.updateProfile(
                                            name = viewModel.userName.value,
                                            age = viewModel.userAge.value,
                                            gender = viewModel.userGender.value,
                                            height = viewModel.userHeight.value,
                                            weight = w.toFloat(),
                                            activityLevel = viewModel.userActivityLevel.value
                                        )
                                        weightInput = ""
                                        Toast.makeText(context, "Weight log updated!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_logger_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add to Telemetry Logs", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 💬 TAB 2: VITALFLOW AI HEALTH COACH
// ==========================================
@Composable
fun CoachTab(viewModel: HealthViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isCoachTyping by viewModel.isCoachTyping.collectAsStateWithLifecycle()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberScrollState()

    LaunchedEffect(messages.size) {
        listState.animateScrollTo(listState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // AI Coach Branding Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("VitalFlow AI Coach", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF10B981), CircleShape))
                                Text("Powered by Gemini 3.5 Flash", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                        Text("Active Coach", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                    }
                }

                Text(
                    text = "Disclaimer: VitalFlow Coach provides motivational wellness tips. Not certified medical advice.",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Chat Bubble Scroll Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(listState)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                messages.forEach { msg ->
                    val isCoach = msg.sender == "coach"
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isCoach) Alignment.CenterStart else Alignment.CenterEnd
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isCoach) 4.dp else 16.dp,
                                bottomEnd = if (isCoach) 16.dp else 4.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCoach) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
                            ),
                            border = if (isCoach) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isCoach) MaterialTheme.colorScheme.onSurface else Color.White
                                )
                            }
                        }
                    }
                }

                // Pulsing Typing State
                if (isCoachTyping) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                "VitalFlow AI Coach is writing...",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Suggestion Prompts strip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = listOf(
                "Optimize my sleep window",
                "Recommend hydration plan",
                "Healthy breakfast details",
                "BMI profile check"
            )
            items(suggestions) { sugg ->
                Card(
                    modifier = Modifier.clickable {
                        viewModel.sendChatMessage(sugg)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        sugg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Input send field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Ask anything about your health goals...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_coach_input_box"),
                shape = RoundedCornerShape(16.dp)
            )

            FloatingActionButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendChatMessage(messageText)
                        messageText = ""
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("submit_ai_coach_query")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ==========================================
// 📊 TAB 3: VISUAL HISTORY CHARTS (CANVAS)
// ==========================================
@Composable
fun AnalyticsTab(viewModel: HealthViewModel) {
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val allGoals by viewModel.allGoals.collectAsStateWithLifecycle()

    val goalsMap = remember(allGoals) { allGoals.associate { it.type to it.value } }

    var selectedMetricType by remember { mutableStateOf("water") } // "water", "steps", "sleep", "calories"

    val last7Dates = remember { viewModel.getLast7Days() }

    // Aggregate sums for last 7 dates
    val dataPoints = remember(allLogs, selectedMetricType, last7Dates) {
        last7Dates.map { dateStr ->
            val logsForDate = allLogs.filter { it.date == dateStr }
            val sum = when (selectedMetricType) {
                "water" -> logsForDate.filter { it.type == "water" }.sumOf { it.value }
                "steps" -> logsForDate.filter { it.type == "steps" }.sumOf { it.value }
                "sleep" -> logsForDate.filter { it.type.startsWith("sleep") }.sumOf { it.value }
                else -> logsForDate.filter { it.type.startsWith("nutrition") }.sumOf { it.value }
            }
            sum
        }
    }

    val maxDataValue = remember(dataPoints) {
        val maxVal = dataPoints.maxOrNull() ?: 1.0
        if (maxVal == 0.0) 1.0 else maxVal
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Biometric Analytics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Analyze your historic progression curve over the past 7 days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Toggle Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val metrics = listOf(
                    Pair("water", "💧 Water"),
                    Pair("steps", "🏃 Steps"),
                    Pair("sleep", "🛌 Sleep"),
                    Pair("calories", "🥑 Food")
                )
                metrics.forEach { metric ->
                    val selected = selectedMetricType == metric.first
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMetricType = metric.first },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                            Text(metric.second, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // CUSTOM DRAWN BAR CHART (Canvas)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Historical ${selectedMetricType.replaceFirstChar { it.uppercase() }} Progress",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            val paddingLeft = 40.dp.toPx()
                            val paddingBottom = 20.dp.toPx()
                            val chartWidth = canvasWidth - paddingLeft
                            val chartHeight = canvasHeight - paddingBottom

                            // Draw Y Grid lines
                            val gridLines = 4
                            for (i in 0..gridLines) {
                                val y = chartHeight * (1f - (i.toFloat() / gridLines))
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.3f),
                                    start = Offset(paddingLeft, y),
                                    end = Offset(canvasWidth, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Draw bars
                            val barCount = dataPoints.size
                            val barWidth = (chartWidth / barCount) * 0.6f
                            val gap = (chartWidth / barCount) * 0.4f

                            for (index in 0 until barCount) {
                                val value = dataPoints[index]
                                val barHeight = ((value / maxDataValue) * chartHeight).toFloat()

                                val x = paddingLeft + index * (barWidth + gap) + gap / 2
                                val y = chartHeight - barHeight

                                drawRect(
                                    color = when (selectedMetricType) {
                                        "water" -> WaterBlue
                                        "steps" -> StepsOrange
                                        "sleep" -> SleepPurple
                                        else -> ActiveGreen
                                    },
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                )
                            }
                        }
                    }

                    // X-Axis labels
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        last7Dates.forEach { d ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val label = try {
                                val dateObj = sdf.parse(d) ?: Date()
                                SimpleDateFormat("E", Locale.getDefault()).format(dateObj)
                            } catch (e: Exception) {
                                ""
                            }
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Direct Goal Target Modifiers Editor
        item {
            Text(
                text = "Personal Health Targets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GoalConfigRow("water", "Water Goal (ml)", goalsMap["water"] ?: 2000.0, viewModel)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GoalConfigRow("steps", "Steps Goal", goalsMap["steps"] ?: 10000.0, viewModel)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GoalConfigRow("sleep", "Sleep Goal (hours)", goalsMap["sleep"] ?: 8.0, viewModel)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    GoalConfigRow("active_minutes", "Active Goal (mins)", goalsMap["active_minutes"] ?: 30.0, viewModel)
                }
            }
        }
    }
}

@Composable
fun GoalConfigRow(type: String, label: String, current: Double, viewModel: HealthViewModel) {
    var inputVal by remember(current) { mutableStateOf(current.toInt().toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))

        OutlinedTextField(
            value = inputVal,
            onValueChange = {
                inputVal = it
                val d = it.toDoubleOrNull() ?: current
                viewModel.updateGoal(type, d)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(100.dp),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

// ==========================================
// 🏆 TAB 4: REWARDS, XP, & PREMIUM MODAL
// ==========================================
@Composable
fun RewardsTab(viewModel: HealthViewModel) {
    val userLevel by viewModel.userLevel.collectAsStateWithLifecycle()
    val userXP by viewModel.userXP.collectAsStateWithLifecycle()
    val userStreak by viewModel.userStreak.collectAsStateWithLifecycle()
    val isPremium by viewModel.isPremium.collectAsStateWithLifecycle()
    val unlocked by viewModel.unlockedAchievements.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "VitalFlow Milestones",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Level up by logging biometric metrics and meeting targets daily!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Progression progress bar card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Progression", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            Text("Level $userLevel Sage Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$userLevel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Level XP
                    val xpNeeded = userLevel * 150
                    val ratio = (userXP.toFloat() / xpNeeded.toFloat()).coerceIn(0f, 1f)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$userXP XP Earned", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Text("$xpNeeded XP for Level ${userLevel + 1}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }

                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                }
            }
        }

        // PREMIUM MEMBERSHIP LUXURY PROMO MODAL CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("V I T A L F L O W  P R E M I U M", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(if (isPremium) "Premium Member Active" else "Unlock AI Premium Core", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = DarkTertiary)
                        }
                    }

                    Text(
                        text = "Get access to unlimited Gemini AI Coaching, personalized BMI analytics, and automated streak rewards.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = {
                            viewModel.setPremiumStatus(!isPremium)
                            val text = if (!isPremium) "Welcome to VitalFlow Premium! Enjoy unlimited features!" else "Premium status deactivated."
                            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LightPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text(if (isPremium) "Deactivate Membership" else "Subscribe for $4.99 / Mo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Badges grid
        item {
            Text(
                text = "Unlocked Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AchievementRow(
                    id = "hydrate_3_days",
                    title = "Hydration Pioneer",
                    desc = "First water telemetry log saved.",
                    unlocked = unlocked.contains("hydrate_3_days") || unlocked.contains("hydrate_champion"),
                    icon = Icons.Default.WaterDrop,
                    badgeColor = WaterBlue
                )

                AchievementRow(
                    id = "steps_champion",
                    title = "Steps Champion",
                    desc = "Hit steps target on any single day.",
                    unlocked = unlocked.contains("steps_champion"),
                    icon = Icons.Default.DirectionsRun,
                    badgeColor = StepsOrange
                )

                AchievementRow(
                    id = "sleep_sage",
                    title = "Restorative Sage",
                    desc = "Logged sleep recovery telemetry.",
                    unlocked = unlocked.contains("sleep_sage"),
                    icon = Icons.Default.Bedtime,
                    badgeColor = SleepPurple
                )

                AchievementRow(
                    id = "premium_club",
                    title = "VIP Gold Status",
                    desc = "Subscribe to VitalFlow Premium Core.",
                    unlocked = unlocked.contains("premium_club") || isPremium,
                    icon = Icons.Default.Star,
                    badgeColor = DarkTertiary
                )
            }
        }
    }
}

@Composable
fun AchievementRow(
    id: String,
    title: String,
    desc: String,
    unlocked: Boolean,
    icon: ImageVector,
    badgeColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(if (unlocked) badgeColor.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (unlocked) badgeColor else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(title, fontWeight = FontWeight.Bold, color = if (unlocked) MaterialTheme.colorScheme.onSurface else Color.Gray)
                    Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }

            if (unlocked) {
                Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                    Text("UNLOCKED", color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                }
            } else {
                Badge(containerColor = Color.LightGray.copy(alpha = 0.2f)) {
                    Text("LOCKED", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}

// ==========================================
// ⚙️ TAB 5: PROFILE EDITING & SYSTEM SETTINGS
// ==========================================
@Composable
fun SettingsTab(
    viewModel: HealthViewModel,
    darkThemeEnabled: Boolean,
    onThemeToggle: () -> Unit
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userAge by viewModel.userAge.collectAsStateWithLifecycle()
    val userGender by viewModel.userGender.collectAsStateWithLifecycle()
    val userHeight by viewModel.userHeight.collectAsStateWithLifecycle()
    val userWeight by viewModel.userWeight.collectAsStateWithLifecycle()
    val userActivityLevel by viewModel.userActivityLevel.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Local profile editing states
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember(userName) { mutableStateOf(userName) }
    var editAge by remember(userAge) { mutableStateOf(userAge.toString()) }
    var editHeight by remember(userHeight) { mutableStateOf(userHeight.toInt().toString()) }
    var editWeight by remember(userWeight) { mutableStateOf(userWeight.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "VitalFlow Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Manage your biometric values and application preferences",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Profile details editor card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Personal Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        TextButton(onClick = {
                            if (isEditing) {
                                val a = editAge.toIntOrNull() ?: userAge
                                val h = editHeight.toFloatOrNull() ?: userHeight
                                val w = editWeight.toFloatOrNull() ?: userWeight
                                viewModel.updateProfile(editName, a, userGender, h, w, userActivityLevel)
                                Toast.makeText(context, "Profile updated securely!", Toast.LENGTH_SHORT).show()
                            }
                            isEditing = !isEditing
                        }) {
                            Text(if (isEditing) "Save" else "Edit", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = editAge,
                                onValueChange = { editAge = it },
                                label = { Text("Age") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = editHeight,
                                onValueChange = { editHeight = it },
                                label = { Text("Height (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        OutlinedTextField(
                            value = editWeight,
                            onValueChange = { editWeight = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileDetailRow("Full Name", userName)
                            ProfileDetailRow("Email", userEmail)
                            ProfileDetailRow("Age", "$userAge yrs")
                            ProfileDetailRow("Biometrics", "$userHeight cm / $userWeight kg")
                            ProfileDetailRow("Gender", userGender)
                            ProfileDetailRow("Activity Curve", userActivityLevel)
                        }
                    }
                }
            }
        }

        // App Preferences toggles card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("App Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Theme", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Switch(checked = darkThemeEnabled, onCheckedChange = { onThemeToggle() })
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sync Local Database", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                            Text("ENABLED", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Account management controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.logout()
                            Toast.makeText(context, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign Out of Account", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.resetAllLogs()
                            Toast.makeText(context, "All health database records and XP progress reset.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset All Health Data & Progress", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, valText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
        Text(valText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
    }
}

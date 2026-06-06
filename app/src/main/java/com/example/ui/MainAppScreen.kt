package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CommunityProjectEntity
import com.example.data.UserSettingsEntity

// Visual theme definitions
val DarkTealBg = Color(0xFF041413)
val HeaderTeal = Color(0xFF052B28)
val NeonTeal = Color(0xFF1DE9B6)
val GlowGreen = Color(0xFF00E676)
val CardTeal = Color(0xFF0A2220)
val DarkGrayText = Color(0xFF4DB6AC)
val AccentYellow = Color(0xFFFFD54F)

@Composable
fun MainAppScreen(
    viewModel: ProjectViewModel
) {
    var currentTab by remember { mutableStateOf("inicio") }
    
    // Read state from ViewModel
    val userSettings by viewModel.userSettings.collectAsState()
    val completedProjects by viewModel.completedProjects.collectAsState()
    val communityProjects by viewModel.communityProjects.collectAsState()
    val totalEcoPoints by viewModel.earnedEcoPoints.collectAsState()

    var selectedProjectForDetail by remember { mutableStateOf<StaticProject?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = HeaderTeal,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentTab == "inicio",
                    onClick = { currentTab = "inicio" },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkTealBg,
                        selectedTextColor = NeonTeal,
                        indicatorColor = NeonTeal,
                        unselectedIconColor = DarkGrayText,
                        unselectedTextColor = DarkGrayText
                    ),
                    modifier = Modifier.testTag("nav_inicio")
                )
                NavigationBarItem(
                    selected = currentTab == "herramientas",
                    onClick = { currentTab = "herramientas" },
                    icon = { Icon(Icons.Filled.Build, contentDescription = "Herramientas") },
                    label = { Text("Herramientas", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkTealBg,
                        selectedTextColor = NeonTeal,
                        indicatorColor = NeonTeal,
                        unselectedIconColor = DarkGrayText,
                        unselectedTextColor = DarkGrayText
                    ),
                    modifier = Modifier.testTag("nav_herramientas")
                )
                NavigationBarItem(
                    selected = currentTab == "juego",
                    onClick = { currentTab = "juego" },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = "Juego") },
                    label = { Text("Juego", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkTealBg,
                        selectedTextColor = NeonTeal,
                        indicatorColor = NeonTeal,
                        unselectedIconColor = DarkGrayText,
                        unselectedTextColor = DarkGrayText
                    ),
                    modifier = Modifier.testTag("nav_juego")
                )
                NavigationBarItem(
                    selected = currentTab == "perfil",
                    onClick = { currentTab = "perfil" },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkTealBg,
                        selectedTextColor = NeonTeal,
                        indicatorColor = NeonTeal,
                        unselectedIconColor = DarkGrayText,
                        unselectedTextColor = DarkGrayText
                    ),
                    modifier = Modifier.testTag("nav_perfil")
                )
            }
        },
        containerColor = DarkTealBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkTealBg, Color(0xFF030D0C))
                    )
                )
        ) {
            when (currentTab) {
                "inicio" -> HomeScreen(
                    viewModel = viewModel,
                    totalEcoPoints = totalEcoPoints,
                    completedList = completedProjects.map { it.id },
                    onSelectProject = { selectedProjectForDetail = it }
                )
                "herramientas" -> ToolsScreen(viewModel = viewModel)
                "juego" -> GameScreen(viewModel = viewModel)
                "perfil" -> ProfileScreen(
                    viewModel = viewModel,
                    userSettings = userSettings,
                    totalEcoPoints = totalEcoPoints,
                    completedCount = completedProjects.size,
                    communityProjects = communityProjects
                )
            }

            // Project detail sheet / screen as overlay overlay
            selectedProjectForDetail?.let { project ->
                ProjectDetailOverlay(
                    project = project,
                    isCompleted = completedProjects.any { it.id == project.id },
                    onToggleComplete = { viewModel.toggleProjectCompleted(project.id) },
                    viewModel = viewModel,
                    onDismiss = { selectedProjectForDetail = null }
                )
            }
        }
    }
}

// ----------------------------------------------------
// 🏠 SCREEN: HOME (Inicio)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ProjectViewModel,
    totalEcoPoints: Int,
    completedList: List<String>,
    onSelectProject: (StaticProject) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Electrónica", "Reciclaje", "Energía Solar", "Agua", "Fauna")

    val allStaticProjects = getStaticProjectsList()
    
    // Filtered static projects
    val filteredProjects = allStaticProjects.filter {
        val matchesCategory = selectedCategory == "Todos" || it.category == selectedCategory
        val matchesSearch = it.title.contains(searchQuery, ignoreCase = true) || 
                            it.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming header card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = HeaderTeal),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bienvenido a",
                            color = DarkGrayText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "EcoReEngine 🌿",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF07201E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "EcoPuntos",
                                color = NeonTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$totalEcoPoints",
                                color = AccentYellow,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Search panel
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar proyectos o tutoriales...", color = DarkGrayText.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = NeonTeal) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonTeal,
                    unfocusedBorderColor = HeaderTeal,
                    focusedContainerColor = HeaderTeal,
                    unfocusedContainerColor = HeaderTeal,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_field")
            )
        }

        // Learning paths (Rutas de aprendizaje)
        item {
            Column {
                Text(
                    text = "Rutas de aprendizaje",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    item {
                        LearningPathCard(
                            title = "Electrónica Básica",
                            level = "Principiante",
                            progressText = "${completedList.intersect(listOf("bombilla", "conductividad").toSet()).size}/2",
                            bgColor1 = Color(0xFF00796B),
                            bgColor2 = Color(0xFF004D40),
                            icon = "⚡"
                        )
                    }
                    item {
                        LearningPathCard(
                            title = "Reciclaje Electrónico",
                            level = "Principiante",
                            progressText = "${completedList.intersect(listOf("linterna_tp4056").toSet()).size}/1",
                            bgColor1 = Color(0xFF1976D2),
                            bgColor2 = Color(0xFF0D47A1),
                            icon = "♻️"
                        )
                    }
                    item {
                        LearningPathCard(
                            title = "Energía Renovable",
                            level = "Intermedio",
                            progressText = "${completedList.intersect(listOf("solar").toSet()).size}/1",
                            bgColor1 = Color(0xFFF57C00),
                            bgColor2 = Color(0xFFE65100),
                            icon = "☀️"
                        )
                    }
                    item {
                        LearningPathCard(
                            title = "Fito-Monitoreo",
                            level = "Avanzado",
                            progressText = "${completedList.intersect(listOf("forestal").toSet()).size}/1",
                            bgColor1 = Color(0xFF7E57C2),
                            bgColor2 = Color(0xFF512DA8),
                            icon = "🌿"
                        )
                    }
                }
            }
        }

        // Categories selector pills
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) NeonTeal else HeaderTeal
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clickable { selectedCategory = cat }
                            .testTag("tag_$cat")
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) DarkTealBg else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Featured Project ("Proyecto Destacado")
        if (selectedCategory == "Todos" && searchQuery.isEmpty()) {
            item {
                Text(
                    text = "Proyecto Destacado 🌟",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                val featured = allStaticProjects.first()
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardTeal),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, NeonTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectProject(featured) }
                        .testTag("featured_project_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "BOMBILLA ELÉCTRICA ECO",
                                color = NeonTeal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = HeaderTeal)
                            ) {
                                Text(
                                    text = "Intermedio",
                                    color = AccentYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = featured.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = featured.description,
                            color = DarkGrayText,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = "points", tint = AccentYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "+100 Puntos", color = AccentYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "Ver proyecto →",
                                color = NeonTeal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section header
        item {
            Text(
                text = "Todos los proyectos (${filteredProjects.size})",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Items list
        items(filteredProjects) { project ->
            val isCompleted = completedList.contains(project.id)
            Card(
                colors = CardDefaults.cardColors(containerColor = CardTeal),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isCompleted) GlowGreen.copy(alpha = 0.5f) else HeaderTeal.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectProject(project) }
                    .testTag("project_card_${project.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(HeaderTeal, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = project.icon, fontSize = 24.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = project.category, color = NeonTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            if (isCompleted) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Check, contentDescription = "completado", tint = GlowGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Listo", color = GlowGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(text = project.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = project.description,
                            color = DarkGrayText,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF031614))) {
                                Text(
                                    text = project.difficulty,
                                    color = if (project.difficulty == "Principiante") NeonTeal else AccentYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF031614))) {
                                Text(
                                    text = "+${project.points} EcoPuntos",
                                    color = AccentYellow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LearningPathCard(
    title: String,
    level: String,
    progressText: String,
    bgColor1: Color,
    bgColor2: Color,
    icon: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(155.dp)
            .height(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(bgColor1, bgColor2)))
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = icon, fontSize = 20.sp)
                Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f))) {
                    Text(
                        text = progressText,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
                Text(
                    text = level,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

// ----------------------------------------------------
// 🛠️ SCREEN: TOOLS (Herramientas)
// ----------------------------------------------------
@Composable
fun ToolsScreen(viewModel: ProjectViewModel) {
    var subTab by remember { mutableStateOf("resistencia") } // "resistencia", "componentes", "basura"

    Column(modifier = Modifier.fillMaxSize()) {
        // Upper selection slider
        TabRow(
            selectedTabIndex = when (subTab) {
                "resistencia" -> 0
                "componentes" -> 1
                else -> 2
            },
            containerColor = HeaderTeal,
            contentColor = NeonTeal,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[when (subTab) {
                        "resistencia" -> 0
                        "componentes" -> 1
                        else -> 2
                    }]),
                    color = NeonTeal
                )
            }
        ) {
            Tab(
                selected = subTab == "resistencia",
                onClick = { subTab = "resistencia" },
                text = { Text("Resistencias", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == "componentes",
                onClick = { subTab = "componentes" },
                text = { Text("Componentes", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = subTab == "basura",
                onClick = { subTab = "basura" },
                text = { Text("Residuos E-Waste", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(DarkTealBg)
        ) {
            when (subTab) {
                "resistencia" -> ResistorCalculatorView(viewModel)
                "componentes" -> ComponentDictionaryView()
                "basura" -> EWastGuideView()
            }
        }
    }
}

// 🧮 Sub-tab 1: Resistor Calculator
@Composable
fun ResistorCalculatorView(viewModel: ProjectViewModel) {
    var showExplanation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Calculadora de Resistencias de 4 Bandas",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Gorgeous custom Canvas Ceramic resistor representation!
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardTeal),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HeaderTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val colors = viewModel.bandColors
                    val c1 = Color(colors[viewModel.band1Selection].colorHex)
                    val c2 = Color(colors[viewModel.band2Selection].colorHex)
                    val c3 = Color(colors[viewModel.band3Selection].colorHex)
                    val c4 = Color(colors[viewModel.band4Selection].colorHex)

                    Canvas(modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(80.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val midY = h / 2

                        // Wire pin leads
                        drawLine(
                            color = Color(0xFFB0BEC5),
                            start = Offset(0f, midY),
                            end = Offset(w, midY),
                            strokeWidth = 10f
                        )

                        // Main Ceramic Ceramic Body
                        drawRoundRect(
                            color = Color(0xFFE3F2FD),
                            topLeft = Offset(w * 0.15f, midY - h * 0.35f),
                            size = Size(w * 0.7f, h * 0.7f),
                            cornerRadius = CornerRadius(20f, 20f)
                        )

                        // Draw Color Bands
                        // Band 1
                        drawRect(
                            color = c1,
                            topLeft = Offset(w * 0.28f, midY - h * 0.35f),
                            size = Size(w * 0.05f, h * 0.7f)
                        )
                        // Band 2
                        drawRect(
                            color = c2,
                            topLeft = Offset(w * 0.38f, midY - h * 0.35f),
                            size = Size(w * 0.05f, h * 0.7f)
                        )
                        // Band 3 (Multiplier)
                        drawRect(
                            color = c3,
                            topLeft = Offset(w * 0.48f, midY - h * 0.35f),
                            size = Size(w * 0.05f, h * 0.7f)
                        )
                        // Band 4 (Tolerance)
                        drawRect(
                            color = c4,
                            topLeft = Offset(w * 0.65f, midY - h * 0.35f),
                            size = Size(w * 0.05f, h * 0.7f)
                        )
                    }
                }
            }
        }

        // Total calculated value
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF062321)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.calculateResistance(),
                        color = GlowGreen,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(text = "Valor Estimado de Resistencia", color = DarkGrayText, fontSize = 11.sp)
                }
            }
        }

        // Color selector dropdown bars
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BandSelectorField(
                    label = "1ª Banda (1er Dígito)",
                    options = viewModel.bandColors.take(10),
                    selectedIdx = viewModel.band1Selection,
                    onSelectedChange = { viewModel.band1Selection = it }
                )
                BandSelectorField(
                    label = "2ª Banda (2do Dígito)",
                    options = viewModel.bandColors.take(10),
                    selectedIdx = viewModel.band2Selection,
                    onSelectedChange = { viewModel.band2Selection = it }
                )
                BandSelectorField(
                    label = "3ª Banda (Multiplicador)",
                    options = viewModel.bandColors,
                    selectedIdx = viewModel.band3Selection,
                    onSelectedChange = { viewModel.band3Selection = it }
                )
                BandSelectorField(
                    label = "4ª Banda (Tolerancia)",
                    options = viewModel.bandColors.filter { it.tolerance > 0 },
                    selectedIdx = when (viewModel.band4Selection) {
                        1 -> 0 // Marron
                        2 -> 1 // Rojo
                        5 -> 2 // Verde
                        6 -> 3 // Azul
                        7 -> 4 // Violeta
                        8 -> 5 // Gris
                        10 -> 6 // Dorado
                        11 -> 7 // Plata
                        else -> 6
                    },
                    onSelectedChange = { chosenLocalIndex ->
                        // Convert local index to master index
                        val masterColors = viewModel.bandColors
                        val availableTols = masterColors.filter { it.tolerance > 0 }
                        val realColor = availableTols[chosenLocalIndex]
                        viewModel.band4Selection = masterColors.indexOf(realColor)
                    }
                )
            }
        }

        // Quick Formula Explanation Toggle
        item {
            Button(
                onClick = { showExplanation = !showExplanation },
                colors = ButtonDefaults.buttonColors(containerColor = HeaderTeal),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (showExplanation) "Ocultar Fórmula" else "Mostrar Fórmula de Cálculo",
                    color = NeonTeal
                )
            }
        }

        if (showExplanation) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardTeal),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, HeaderTeal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Fórmula estándar de 4 bandas:",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "R = (Dígito1 × 10+ Dígito2) × Multiplicador",
                            color = GlowGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val d1 = viewModel.bandColors[viewModel.band1Selection].digit
                        val d2 = viewModel.bandColors[viewModel.band2Selection].digit
                        val mVal = viewModel.bandColors[viewModel.band3Selection].multiplier
                        Text(
                            text = "Matemáticas en vivo:\nR = ($d1 × 10 + $d2) × $mVal\nR = ${d1*10 + d2} × $mVal\nR = ${(d1*10 + d2)*mVal} Ω",
                            color = DarkGrayText,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BandSelectorField(
    label: String,
    options: List<ResistorColor>,
    selectedIdx: Int,
    onSelectedChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentSelected = options.getOrNull(selectedIdx) ?: options.first()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = DarkGrayText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .background(HeaderTeal, RoundedCornerShape(8.dp))
                .border(BorderStroke(1.dp, CardTeal), RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(vertical = 12.dp, horizontal = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(currentSelected.colorHex), CircleShape)
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = currentSelected.name, color = Color.White, fontSize = 14.sp)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = NeonTeal
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(HeaderTeal)
            ) {
                options.forEachIndexed { idx, color ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(Color(color.colorHex), CircleShape)
                                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = color.name + if (color.digit >= 0) " (Dígito: ${color.digit})" else " (Mult: ${color.multiplier})",
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        onClick = {
                            onSelectedChange(idx); expanded = false
                        }
                    )
                }
            }
        }
    }
}

// 🏺 Sub-tab 2: Dictionary of Components
@Composable
fun ComponentDictionaryView() {
    val items = getStaticComponents()
    var searchQuery by remember { mutableStateOf("") }
    var expandedItemName by remember { mutableStateOf("") }

    val filteredItems = items.filter {
        it.title.contains(searchQuery, ignoreCase = true) || 
        it.type.contains(searchQuery, ignoreCase = true) || 
        it.description.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Biblioteca de Componentes Recuperables 🏺",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Identifica piezas electrónicas en electrodomésticos abandonados.",
                color = DarkGrayText,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar componente... (ej. TP4056, NE555)", color = DarkGrayText.copy(alpha = 0.5f)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonTeal,
                    unfocusedBorderColor = HeaderTeal,
                    focusedContainerColor = CardTeal,
                    unfocusedContainerColor = CardTeal,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        items(filteredItems) { comp ->
            val isExpanded = expandedItemName == comp.title
            Card(
                colors = CardDefaults.cardColors(containerColor = CardTeal),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isExpanded) NeonTeal else Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedItemName = if (isExpanded) "" else comp.title }
                    .testTag("dict_comp_${comp.title}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = comp.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = comp.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(text = comp.type, color = NeonTeal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = DarkGrayText
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = HeaderTeal)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Definición:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = comp.description, color = DarkGrayText, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "🔍 ¿Dónde se extrae?", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = HeaderTeal),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = comp.howToSalvage,
                                color = AccentYellow,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "📌 Diagrama de Pines (Pinout):", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = comp.pinoutDiagram,
                            color = Color(0xFFFFEB3B),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

// ♻️ Sub-tab 3: Recycling Manual
@Composable
fun EWastGuideView() {
    val devices = getStaticEWasteItems()
    var expandedIndex by remember { mutableStateOf(-1) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Manual Práctico de Reciclaje (E-Waste) ♻️",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Aprende qué buscar en viejos electrodomésticos para tus robots y cómo desarmarlos con seguridad.",
                color = DarkGrayText,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(devices.size) { index ->
            val dev = devices[index]
            val isExpanded = expandedIndex == index
            Card(
                colors = CardDefaults.cardColors(containerColor = CardTeal),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isExpanded) NeonTeal else Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (isExpanded) -1 else index }
                    .testTag("ewaste_card_$index")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = dev.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = dev.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Expand",
                            tint = DarkGrayText
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = HeaderTeal)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF4A1515).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = "Peligro", tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ALERTA DE SEGURIDAD:\n${dev.safetyWarning}",
                                color = Color(0xFFFFCDD2),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Componentes más valiosos de extraer:", color = NeonTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        dev.salvageableParts.forEach { part ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(NeonTeal, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = part, color = Color.White, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Guía paso a paso de extracción:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = dev.extractionSteps, color = DarkGrayText, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 🎮 SCREEN: INTERACTIVE CIRCUIT GAME (Juego)
// ----------------------------------------------------
@Composable
fun GameScreen(viewModel: ProjectViewModel) {
    val challenge = viewModel.gameChallenges[viewModel.currentChallengeIndex]
    val inventorScore by viewModel.earnedEcoPoints.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Game Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Simulador de Circuitos Interactivos 🎮",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Crea rutas elásticas seguras uniendo componentes reciclados.",
                        color = DarkGrayText,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Active levels panel selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardTeal),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HeaderTeal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Misión Seleccionada:", color = NeonTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = challenge.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = challenge.description, color = DarkGrayText, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HeaderTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Info, contentDescription = "Pista", tint = AccentYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Pista: ${challenge.clue}", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Workbench Circuit Board
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF031211)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, if (viewModel.gameIsLightOn) GlowGreen else neonTealGlow(viewModel.gameSourceSlot != null)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Visual circuit wires running under the slots
                    CircuitWiresBackground(isLit = viewModel.gameIsLightOn)

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircuitSlot(
                            title = "FUENTE",
                            iconPlaceholder = "🔌",
                            activeComponent = viewModel.gameSourceSlot,
                            onClickRemove = { viewModel.gameSourceSlot = null; viewModel.gameIsLightOn = false }
                        )

                        Text(text = "══", color = if (viewModel.gameIsLightOn) GlowGreen else HeaderTeal, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        CircuitSlot(
                            title = "CONTROL",
                            iconPlaceholder = "⚡",
                            activeComponent = viewModel.gameControllerSlot,
                            onClickRemove = { viewModel.gameControllerSlot = null; viewModel.gameIsLightOn = false }
                        )

                        Text(text = "══", color = if (viewModel.gameIsLightOn) GlowGreen else HeaderTeal, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                        CircuitSlot(
                            title = "CARGA",
                            iconPlaceholder = "💡",
                            activeComponent = viewModel.gameLoadSlot,
                            isLit = viewModel.gameIsLightOn,
                            onClickRemove = { viewModel.gameLoadSlot = null; viewModel.gameIsLightOn = false }
                        )
                    }

                    // Nice little spark bulb or glow effect overlay
                    if (viewModel.gameIsLightOn) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    drawCircle(
                                        color = GlowGreen.copy(alpha = 0.08f),
                                        radius = 200f,
                                        center = Offset(size.width * 0.82f, size.height * 0.5f)
                                    )
                                }
                        )
                    }
                }
            }
        }

        // Live system feedback text
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = if (viewModel.gameClearedStatus) Color(0xFF1B5E20) else HeaderTeal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = viewModel.gameFeedbackMessage,
                        color = if (viewModel.gameClearedStatus) Color.White else AccentYellow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.resetGameLevel() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar ranuras", color = Color.White)
                }

                Button(
                    onClick = {
                        if (viewModel.gameClearedStatus) {
                            viewModel.nextChallenge()
                        } else {
                            viewModel.tryActivateCircuit()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.gameClearedStatus) GlowGreen else NeonTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("game_action_btn")
                ) {
                    val label = if (viewModel.gameClearedStatus) "Siguiente Reto →" else "Cerrar Interruptor⚡"
                    Text(
                        text = label, 
                        color = DarkTealBg,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Inventory heading
        item {
            Divider(color = HeaderTeal, modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "Tu Caja de Componentes (Inventario)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Toca un elemento y asígnalo al circuito:",
                color = DarkGrayText,
                fontSize = 11.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Clean components layout row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(viewModel.gameComponentsInventory) { comp ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardTeal),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp, 
                            if (comp.isInsulator) Color(0xFFE53935).copy(alpha = 0.5f) else NeonTeal.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .width(130.dp)
                            .height(135.dp)
                            .testTag("inventory_item_${comp.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = comp.icon, fontSize = 28.sp)
                            Text(
                                text = comp.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // Interactive Slot Destination Placement
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                SlotAssignTag("F", NeonTeal) {
                                    viewModel.selectComponentForSlot(comp, "source")
                                }
                                SlotAssignTag("C", AccentYellow) {
                                    viewModel.selectComponentForSlot(comp, "controller")
                                }
                                SlotAssignTag("L", Color(0xFFE040FB)) {
                                    viewModel.selectComponentForSlot(comp, "load")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlotAssignTag(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, color), RoundedCornerShape(4.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CircuitSlot(
    title: String,
    iconPlaceholder: String,
    activeComponent: GameComponent?,
    isLit: Boolean = false,
    onClickRemove: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, color = DarkGrayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(75.dp)
                .background(
                    if (isLit) GlowGreen.copy(alpha = 0.2f) else HeaderTeal,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    BorderStroke(
                        2.dp, 
                        if (isLit) GlowGreen else if (activeComponent != null) NeonTeal else Color.White.copy(alpha = 0.2f)
                    ),
                    RoundedCornerShape(12.dp)
                )
                .clickable { if (activeComponent != null) onClickRemove() },
            contentAlignment = Alignment.Center
        ) {
            if (activeComponent != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = activeComponent.icon, fontSize = 28.sp)
                    Text(
                        text = activeComponent.name.split(" ").firstOrNull() ?: "",
                        color = Color.White,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = "❌ Quitar", color = Color(0xFFEF5350), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(text = iconPlaceholder, fontSize = 24.sp, modifier = Modifier.drawBehind {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.1f),
                        style = Stroke(width = 4f)
                    )
                })
            }
        }
    }
}

@Composable
fun CircuitWiresBackground(isLit: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val midY = h / 2
        val lineColor = if (isLit) GlowGreen else Color(0xFF042623)

        // Lower wires connection bus loop
        styleLineWire(lineColor, Offset(w * 0.15f, midY), Offset(w * 0.15f, h * 0.85f))
        styleLineWire(lineColor, Offset(w * 0.15f, h * 0.85f), Offset(w * 0.85f, h * 0.85f))
        styleLineWire(lineColor, Offset(w * 0.85f, h * 0.85f), Offset(w * 0.85f, midY))
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.styleLineWire(color: Color, start: Offset, end: Offset) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 6f
    )
}

@Composable
fun neonTealGlow(active: Boolean): Color {
    val infiniteTransition = rememberInfiniteTransition(label = "neon")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    return if (active) NeonTeal.copy(alpha = alpha) else HeaderTeal
}


// ----------------------------------------------------
// 👤 SCREEN: PROFILE & COMMUNITY (Perfil)
// ----------------------------------------------------
@Composable
fun ProfileScreen(
    viewModel: ProjectViewModel,
    userSettings: UserSettingsEntity,
    totalEcoPoints: Int,
    completedCount: Int,
    communityProjects: List<CommunityProjectEntity>
) {
    var isEditOpen by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf(userSettings.username) }
    var inputBio by remember { mutableStateOf(userSettings.bio) }
    var selectedAvatarIdx by remember { mutableStateOf(userSettings.avatarIndex) }

    val avatars = listOf("🤖", "👨‍🔬", "🔋", "👩‍🔧", "⚙️", "♻️")

    var isAddingProject by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newCat by remember { mutableStateOf("Electrónica") }
    var newMats by remember { mutableStateOf("") }
    var newSteps by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile user card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardTeal),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(HeaderTeal, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = avatars.getOrNull(userSettings.avatarIndex) ?: "🤖",
                                    fontSize = 32.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = userSettings.username,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = userSettings.bio,
                                    color = DarkGrayText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                inputName = userSettings.username
                                inputBio = userSettings.bio
                                selectedAvatarIdx = userSettings.avatarIndex
                                isEditOpen = true
                            },
                            modifier = Modifier.testTag("edit_profile_btn")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = NeonTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatsCard(title = "EcoPuntos", value = "$totalEcoPoints", color = AccentYellow)
                        StatsCard(title = "Guias Listas", value = "$completedCount", color = NeonTeal)
                        StatsCard(title = "Comunidad", value = "${communityProjects.filter { it.author == userSettings.username }.size}", color = GlowGreen)
                    }
                }
            }
        }

        // Achievements Section
        item {
            Text(
                text = "Logros del Laboratorio 🏆",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BadgeItem(title = "Primer Circuito", icon = "⚡", unlocked = completedCount >= 1)
                BadgeItem(title = "Construye Eco", icon = "♻️", unlocked = completedCount >= 3)
                BadgeItem(title = "Inventor Social", icon = "🤝", unlocked = communityProjects.any { it.author == userSettings.username })
            }
        }

        // Community Segment Toolbar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aportes de la Comunidad 🤝",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { isAddingProject = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_community_project_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = DarkTealBg)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Publicar", color = DarkTealBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Empty state instruction for community list
        if (communityProjects.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardTeal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👋 ¡Sé el primero en compartir!", color = AccentYellow, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Presiona 'Publicar' para subir la idea de robótica sustentable o reciclaje que hayas construido hoy.",
                            color = DarkGrayText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Feed of community shared projects
        items(communityProjects) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardTeal),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, HeaderTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Card(colors = CardDefaults.cardColors(containerColor = HeaderTeal)) {
                                Text(
                                    text = item.category.uppercase(),
                                    color = NeonTeal,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = item.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(HeaderTeal, RoundedCornerShape(6.dp))
                                .clickable { viewModel.likeProject(item.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Me gusta", tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${item.likes}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = item.description, color = DarkGrayText, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = HeaderTeal)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🛠️ Materiales Necesarios:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = item.materials, color = DarkGrayText, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📐 Instrucciones de Armado:",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = item.steps, color = DarkGrayText, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Por: @${item.author}",
                            color = AccentYellow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // ✏️ DIALOG: Edit Profile Form
    if (isEditOpen) {
        Dialog(onDismissRequest = { isEditOpen = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = HeaderTeal),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Editar Perfil de Inventor",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Nombre de Usuario", color = DarkGrayText) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonTeal
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_username_input")
                    )

                    OutlinedTextField(
                        value = inputBio,
                        onValueChange = { inputBio = it },
                        label = { Text("Biografía o Escuela", color = DarkGrayText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonTeal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Selecciona tu Avatar:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        avatars.forEachIndexed { index, avatar ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (selectedAvatarIdx == index) NeonTeal else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(
                                        BorderStroke(1.dp, if (selectedAvatarIdx == index) NeonTeal else Color.White.copy(alpha = 0.3f)),
                                        CircleShape
                                    )
                                    .clickable { selectedAvatarIdx = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = avatar, fontSize = 20.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isEditOpen = false }) {
                            Text("Cancelar", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateUsername(inputName, inputBio, selectedAvatarIdx)
                                isEditOpen = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_profile_btn")
                        ) {
                            Text("Guardar", color = DarkTealBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ➕ DIALOG: Add Community Project Form
    if (isAddingProject) {
        Dialog(onDismissRequest = { isAddingProject = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = HeaderTeal),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Comparte un Nuevo Proyecto",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Divider(color = CardTeal)
                    }

                    item {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Título del Proyecto", color = DarkGrayText) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonTeal
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("add_proj_title_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newDesc,
                            onValueChange = { newDesc = it },
                            label = { Text("Descripción Corta", color = DarkGrayText) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonTeal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Text(text = "Categoría:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        val categories = listOf("Electrónica", "Reciclaje", "Energía Solar", "Agua", "Fauna")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { cat ->
                                val isSelected = newCat == cat
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = if (isSelected) NeonTeal else CardTeal),
                                    modifier = Modifier.clickable { newCat = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) DarkTealBg else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = newMats,
                            onValueChange = { newMats = it },
                            placeholder = { Text("ej. 1 batería vieja de celular, cable de auricular...", color = DarkGrayText.copy(alpha = 0.5f)) },
                            label = { Text("Materiales de Reciclaje", color = DarkGrayText) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonTeal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newSteps,
                            onValueChange = { newSteps = it },
                            placeholder = { Text("ej. 1. Desarma la batería con cuidado. 2. Conecta...", color = DarkGrayText.copy(alpha = 0.5f)) },
                            label = { Text("Instrucciones de Armado Paso a Paso", color = DarkGrayText) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonTeal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { isAddingProject = false }) {
                                Text("Cancelar", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        viewModel.createUserProject(newTitle, newDesc, newCat, newMats, newSteps)
                                        isAddingProject = false
                                        // Reset fields
                                        newTitle = ""; newDesc = ""; newMats = ""; newSteps = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GlowGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("submit_project_btn")
                            ) {
                                Text("Publicar Idea", color = DarkTealBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(title: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = HeaderTeal),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(text = title, color = DarkGrayText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BadgeItem(title: String, icon: String, unlocked: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(if (unlocked) CardTeal else CardTeal.copy(alpha = 0.4f), CircleShape)
                .border(BorderStroke(1.5.dp, if (unlocked) AccentYellow else Color.Gray.copy(alpha = 0.4f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon, 
                fontSize = 28.sp,
                modifier = Modifier.drawBehind {
                    if (!unlocked) {
                        drawRect(
                            color = Color.Black.copy(alpha = 0.45f)
                        )
                    }
                }
            )
            if (!unlocked) {
                Icon(Icons.Filled.Lock, contentDescription = "Bloqueado", tint = Color.LightGray.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title, 
            color = if (unlocked) Color.White else Color.Gray, 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}

// ----------------------------------------------------
// 🤖 COMPONENT OVERLAY: PROJECT DETAILS & FLOATING AI CHAT
// ----------------------------------------------------
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProjectDetailOverlay(
    project: StaticProject,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf("guia") } // "guia", "planos"
    var isChatOpen by remember { mutableStateOf(false) }
    var chatMessageInput by remember { mutableStateOf("") }
    
    val chatHistory by viewModel.activeChatHistory

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkTealBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Overlay toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderTeal)
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("back_from_detail")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NeonTeal)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Guía de Armado",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Dynamic Registrado/Completado Checklist Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Registrado",
                        color = if (isCompleted) GlowGreen else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GlowGreen,
                            checkedTrackColor = GlowGreen.copy(alpha = 0.4f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.testTag("completion_switch")
                    )
                }
            }

            // Project Quick Meta
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardTeal)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(HeaderTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = project.icon, fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = project.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Ruta: ${project.category} • +${project.points} EcoPuntos", color = NeonTeal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Tabs navigator "Guia" or "Planos"
            TabRow(
                selectedTabIndex = if (activeTab == "guia") 0 else 1,
                containerColor = HeaderTeal,
                contentColor = NeonTeal
            ) {
                Tab(
                    selected = activeTab == "guia",
                    onClick = { activeTab = "guia" },
                    text = { Text("Instrucciones", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == "planos",
                    onClick = { activeTab = "planos" },
                    text = { Text("Materiales y Planos", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            // Content scroll area
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (activeTab == "guia") {
                    item {
                        Text(text = "Manual de Construcción Paso a Paso:", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Lee atentamente cada fase antes de soldar o conectar componentes.", color = DarkGrayText, fontSize = 12.sp)
                    }

                    items(project.steps.size) { stepIdx ->
                        val step = project.steps[stepIdx]
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardTeal),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, HeaderTeal)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Fase ${stepIdx + 1}",
                                        color = NeonTeal,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Card(colors = CardDefaults.cardColors(containerColor = HeaderTeal)) {
                                        Text(
                                            text = "Seguro ✔️",
                                            color = GlowGreen,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = step,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    // Materials Checklist
                    item {
                        Text(text = "Componentes Necesarios 🛠️", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Rastrea estas piezas de tu inventario de e-waste antes de iniciar.", color = DarkGrayText, fontSize = 12.sp)
                    }

                    items(project.materials) { material ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(NeonTeal, CircleShape)
                                )
                                Text(text = material, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }

                    // Vectorized Electronic blueprints schema simulation diagram
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Plano Esquemático de Conexión 📐", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Sigue este mapa de cables para garantizar un flujo estable de electrones.", color = DarkGrayText, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, NeonTeal.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = project.asciiDiagram,
                                    fontFamily = FontFamily.Monospace,
                                    color = GlowGreen,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Start,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Diagrama Técnico Esquemático de Alimentación Eco",
                                    color = DarkGrayText,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // 🤖 FLOATING CHAT ASSISTANT PANEL OVERLAY
        // Animated show/hide of the active dialog on top of the screen
        AnimatedVisibility(
            visible = isChatOpen,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = HeaderTeal),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                border = BorderStroke(1.5.dp, NeonTeal),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardTeal)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(GlowGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🤖 Asistente EcoReEngine",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { isChatOpen = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }

                    // Message lists
                    val chatScrollState = rememberScrollState()
                    LaunchedEffect(chatHistory.size) {
                        chatScrollState.animateScrollTo(chatScrollState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(chatScrollState)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        chatHistory.forEach { (text, isUser) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isUser) NeonTeal else CardTeal
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isUser) 12.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 12.dp
                                    ),
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = if (isUser) "Tú" else "Asistente IA",
                                            color = if (isUser) DarkTealBg else NeonTeal,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = text,
                                            color = if (isUser) DarkTealBg else Color.White,
                                            fontSize = 13.sp,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (viewModel.chatLoading) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CardTeal),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = NeonTeal,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = "Pensando cables seguros...", color = DarkGrayText, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Input field row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardTeal)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessageInput,
                            onValueChange = { chatMessageInput = it },
                            placeholder = { Text("Haz una pregunta técnica...", color = DarkGrayText.copy(alpha = 0.5f), fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonTeal
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_chat_input")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (chatMessageInput.isNotBlank()) {
                                    viewModel.sendMessage(project.title, project.description, chatMessageInput)
                                    chatMessageInput = ""
                                }
                            },
                            enabled = !viewModel.chatLoading,
                            modifier = Modifier.testTag("submit_ai_query")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Enviar",
                                tint = if (viewModel.chatLoading) Color.Gray else NeonTeal
                            )
                        }
                    }
                }
            }
        }

        // Chat Float Trigger Button when drawer is closed
        if (!isChatOpen) {
            FloatingActionButton(
                onClick = {
                    viewModel.openChatForProject(project.id)
                    isChatOpen = true
                },
                containerColor = NeonTeal,
                contentColor = DarkTealBg,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(60.dp)
                    .testTag("tech_advisor_fab")
            ) {
                Icon(Icons.Filled.MailOutline, contentDescription = "Hablar con IA", modifier = Modifier.size(28.dp))
            }
        }
    }
}

// ----------------------------------------------------
// STATIC LEARNING/MUSEUM DATA MODELS
// ----------------------------------------------------
data class StaticProject(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String,
    val difficulty: String,
    val points: Int,
    val materials: List<String>,
    val steps: List<String>,
    val asciiDiagram: String
)

fun getStaticProjectsList() = listOf(
    StaticProject(
        id = "bombilla",
        title = "Bombilla Eléctrica Eco-Portátil",
        description = "Convierte bombillas LED en desuso en linternas de camping seguras mediante módulo de carga TP4056 y batería de litio.",
        icon = "💡",
        category = "Electrónica",
        difficulty = "Principiante",
        points = 100,
        materials = listOf(
            "1 Bombilla LED vieja fundida (pero con ledes sanos)",
            "1 Batería de litio (Celda 18650 recuperada de laptop)",
            "1 Módulo de regulación y carga TP4056",
            "1 Interruptor mecánico de botón",
            "Cables finos conductor de cobre con funda"
        ),
        steps = listOf(
            "Retira con suma delicadeza el difusor plástico de la bombilla para dejar los ledes al descubierto.",
            "Utiliza un multímetro para probar qué ledes siguen funcionando. Aplica corriente baja en cada diodo.",
            "Suelda el polo positivo de la celda de litio al puerto B+ del TP4056, y el negativo a B-.",
            "Conecta la salida OUT+ al terminal positivo de los ledes, y la OUT- mediante el interruptor al polo negativo de la placa de ledes.",
            "Asegura el ensamble interno para que no ocurran cortocircuitos. ¡Felicidades, tienes tu linterna!"
        ),
        asciiDiagram = """
         ┌───────────────────────────────┐
         │             TP4056            │
         │  [USB IN]   B+   B-  O+   O-  │
         └─────────────┬────┬───┬────┬───┘
                       │    │   │    │
          ┌────────────┘    │   │    └──────┐
         ┌┴──────────┐      │   │          ┌┴─────┐
         │ BAT 18650 │◄─────┘   │          │ SWITCH│
         └───────────┘          │          └┬─────┘
                                │           │
                                └──►[LEDs]◄─┘
        """.trimIndent()
    ),
    StaticProject(
        id = "solar",
        title = "Cargador Solar para Teléfonos",
        description = "Aprovecha celdas solares pequeñas para crear un banco de energía portátil ecológico y libre de emisiones.",
        icon = "☀️",
        category = "Energía Solar",
        difficulty = "Avanzado",
        points = 120,
        materials = listOf(
            "1 Celda panel solar de silicio de 5.5V o 6V",
            "1 Módulo regulador de voltaje booster USB de 5V",
            "1 Diodo rectificador 1N4007 (protección antirretorno)",
            "Cinta aislante o silicona termo-fusible"
        ),
        steps = listOf(
            "Suelda el diodo 1N4007 en serie con el polo positivo de la celda solar. Esto previene que la batería descargue sobre el panel solar en la noche.",
            "Conecta el cátodo del diodo al terminal IN+ del regulador booster de 5V USB.",
            "Suelda el cable GND negativo directamente desde el panel hasta la ranura IN-.",
            "Instala todo el ensamble en una caja de cartón reciclada con visibilidad al sol y conecta tu smartphone al puerto USB. ¡Energía verde!"
        ),
        asciiDiagram = """
        ┌──────────────┐     Diodo 1N4007 ──►[├──   
        │ Panel Solar │───────►[======]───►[ IN+ ] 
        │     5.5V     │                     │     │  Regulador 5V
        │              │──────────────────►[ IN- ]  USB out
        └──────────────┘                    └─────┘
        """.trimIndent()
    ),
    StaticProject(
        id = "conductividad",
        title = "Medidor de Pureza de Agua",
        description = "Crea un analizador de pureza líquida utilizando transistores de propósito general BC547 de chatarra.",
        icon = "💧",
        category = "Agua",
        difficulty = "Principiante",
        points = 100,
        materials = listOf(
            "1 Transistor NPN de propósito general (BC547 o 2N2222)",
            "1 Resistencia de 1k ohmios (mira los colores en calculadora!)",
            "1 Diodo LED indicador de reciclaje",
            "1 Batería vieja de 9V",
            "2 Clavos largos de metal (sensores de contacto)"
        ),
        steps = listOf(
            "Suelda un extremo de la resistencia de 1k al colector del transistor, y el otro polo al diodo LED indicador.",
            "Forma dos cables conductores. Uno irá al polo positivo de la batería de 9V, conectado a uno de los clavos sensores.",
            "El segundo sensor conectará directamente a la base del transistor.",
            "Sumerge ambos clavos limpios en el agua sin juntarlos directamente. Si el diodo LED brilla con fuerza, el agua tiene muchos minerales o suciedad conductores."
        ),
        asciiDiagram = """
             +9V ───────────┬─────[ LED ]─────┐
                            │                 │  C (Colector)
                        [Clavo 1]       ┌─────┴─────┐
                                        │   BC547   ├─ B (Base) ◄── [Clavo 2]
                        [Clavo 2]       └─────┬─────┘
                                              │  E (Emisor)
             GND ─────────────────────────────┘
        """.trimIndent()
    ),
    StaticProject(
        id = "forestal",
        title = "Alarma Forestal Anti-Ruido",
        description = "Un detector de sonidos agudos para auditar ralas ilegales de árboles en reservas, hecho con micrófonos viejos.",
        icon = "🌿",
        category = "Fauna",
        difficulty = "Avanzado",
        points = 150,
        materials = listOf(
            "1 Micrófono electret reciclado de audífonos rotos",
            "1 Amplificador operacional LM358 de radio vieja",
            "1 Pequeño zumbador de alarma activo",
            "Resistencias de 10k y condensador de 100nF"
        ),
        steps = listOf(
            "Suelda el micrófono electret con alimentación a través de una resistencia de 10k al pin de entrada del LM358.",
            "Configura la ganancia del LM358 conectando resistencias para multiplicar el sonido por 100.",
            "Acopla el altavoz/buzzer al pin de salida del amplificador de modo que se dispare con señales picos de ruido (motosierras).",
            "Ubícalo dentro de una botella plástica reciclada para evitar la lluvia y cuélgalo en lo alto de un árbol."
        ),
        asciiDiagram = """
                 VCC ────────[ 10k ]────────┐
                                            ├──►[  + ]
                       [Electret Mic]───────┤   LM358 │────►[ Buzzer ]
                                            └──►[  - ]
        """.trimIndent()
    )
)

data class ComponentDetail(
    val title: String,
    val type: String,
    val icon: String,
    val description: String,
    val howToSalvage: String,
    val pinoutDiagram: String
)

fun getStaticComponents() = listOf(
    ComponentDetail(
        title = "TP4056",
        type = "Gestor de Carga de Litio",
        icon = "🔋",
        description = "Un microchip diseñado específicamente para regular e indicar la carga segura de baterías recargables de iones de litio de 3.7V de forma automatizada mediante USB.",
        howToSalvage = "Se encuentra comúnmente en linternas rotas de mano, cigarrillos electrónicos descartables o placas controladoras de baterías de tabletas.",
        pinoutDiagram = """
          ┌─────────────┐
          │   TP4056    │
         1│TEMP     VCC │8
         2│PROG     BAT │7 (Hacia la pila +)
         3│GND      STBY│6
         4│CHRG     OUT │5 (Voltaje regulado)
          └─────────────┘
        """.trimIndent()
    ),
    ComponentDetail(
        title = "NE555",
        type = "Temporizador / Oscilador",
        icon = "⏱️",
        description = "Chip generador de pulsos de onda cuadrada. Ideal para crear luces de emergencia intermitentes, zumbadores de alarmas y robots autónomos.",
        howToSalvage = "Muy común en juguetes viejos con sonido/luces, televisores de tubo (CRT) antiguos y fuentes de poder con ráfaga.",
        pinoutDiagram = """
          ┌──────────┐
         1│GND    VCC│8
         2│TRIG  DISC│7
         3│OUT   THRES│6
         4│RESET CTRL│5
          └──────────┘
        """.trimIndent()
    ),
    ComponentDetail(
        title = "LM7805",
        type = "Regulador Fijo 5V",
        icon = "🔌",
        description = "Recibe voltajes altos oscilantes (ej. 9V o 12V de transformador) y los estabiliza completamente en 5V estables para alimentar lógicamente Arduino o USB.",
        howToSalvage = "Extraíble de adaptadores de carga de pared antiguos de routers, decodificadores de televisión de satélite o viejas impresoras.",
        pinoutDiagram = """
             ┌───────┐
             │LM7805 │
             └──┬┬┬──┘
                123
           1:ENTRADA (IN)
           2:TIERRA (GND)
           3:SALIDA (5V OUT)
        """.trimIndent()
    ),
    ComponentDetail(
        title = "BC547",
        type = "Transistor NPN",
        icon = "🔺",
        description = "Transistor de unión bipolar NPN. Actúa como un interruptor electrónico veloz que se activa por corrientes minúsculas desde sensores en su base.",
        howToSalvage = "Piezas omnipresentes, las encuentras en radiomisor, fuentes conmutadas, cargadores de celular viejos e incluso regletas dañadas.",
        pinoutDiagram = """
             ┌───────┐
             │ BC547 │ (Cara plana)
             └──┬┬┬──┘
                C B E
           C: Colector (Entrada de Carga)
           B: Base (Señal de Control)
           E: Emisor (Hacia Tierra)
        """.trimIndent()
    ),
    ComponentDetail(
        title = "ESP32",
        type = "Microcontrolador IoT",
        icon = "📡",
        description = "Un procesador de bajo consumo con WiFi, Bluetooth e interactores digitales ideal para controlar sensores ambientales vía remotos modernos.",
        howToSalvage = "Aparatos domésticos de última generación como purificadores de aire fallando, bombillas RGB 'Smart' o regletas de conectividad wifi.",
        pinoutDiagram = """
          ┌─────────────┐
          │    ESP32    │
         1│3V3       GND│30
         2│EN       GPIO│29
         3│GPIO     GPIO│28
          └─────────────┘
        """.trimIndent()
    )
)

data class EWasteItem(
    val name: String,
    val icon: String,
    val safetyWarning: String,
    val salvageableParts: List<String>,
    val extractionSteps: String
)

fun getStaticEWasteItems() = listOf(
    EWasteItem(
        name = "Teléfonos y tabletas",
        icon = "📱",
        safetyWarning = "¡Atención! Las celdas de batería de litio tipo bolsa/sobre pueden incendiarse o emitir gases ácidos si se perforan con destornilladores o se doblan. Retíralas con espátulas de plástico sin forzar calor directo.",
        salvageableParts = listOf(
            "Cámara y linterna LED SMD de alta intensidad",
            "Módulo vibrador háptico (pequeño motor de vibración)",
            "Micrófono interno de alta sensibilidad y altavoces",
            "Pantalla LCD (el panel de retroiluminación acampanado sirve como linterna difusa)"
        ),
        extractionSteps = "1. Utiliza una pistola de calor en modo bajo o secador para ablandar el pegamento de la tapa trasera.\n2. Desconecta de inmediato los flex de la batería.\n3. Desatornilla los escudos de aluminio para aislar el vibrador magnético y las celdas limpias."
    ),
    EWasteItem(
        name = "Computadoras Portátiles (Laptops)",
        icon = "💻",
        safetyWarning = "Nunca apliques el calor excesivo del cautín en los cables soldados de las uniones metálicas de las baterías. Evita producir cortocircuito cruzando cintas metálicas desprotegidas.",
        salvageableParts = listOf(
            "Celdas cilíndricas de Litio 18650 (¡banco gigante de energía de alta capacidad!)",
            "Imanes de Neodimio ultrapotentes en discos duros HDD rotos",
            "Ventiladores sopladores mecánicos pequeños de 5V para enfriamiento",
            "Módulo adaptador Bluetooth y tarjeta inalámbrica"
        ),
        extractionSteps = "1. Desmonta con cuidado la carcasa protectora de plástico de la batería portátil con alicates planos.\n2. Corta las soldaduras por punto individuales sobre el níquel una por una.\n3. Mide su carga con voltímetro; si arroja más de 2.5V, la celda es reutilizable hoy."
    ),
    EWasteItem(
        name = "Fuentes de poder CPU (PSU)",
        icon = "🔌",
        safetyWarning = "¡Peligro crítico de electrocución! Los condensadores electrolíticos de gran tamaño en la etapa primaria de la fuente retienen voltajes letales de más de 300V durante días después de desconectada. Descárgalos tocando una bombilla de carga primero.",
        salvageableParts = listOf(
            "Disipadores de calor de aluminio grueso",
            "Fusibles de vidrio y varistores de protección",
            "Condensadores excelentes planos y transistores MOSFET",
            "Interruptor de palanca e imanes de bobinas"
        ),
        extractionSteps = "1. Corta con pinzas el cableado grueso multicolor para organizar bobinas de cobre.\n2. Aplica cautín de 40W en el reverso de la placa PCB para de-soldar los reguladores y optoacopladores sin quemar las pistas de baquelita."
    ),
    EWasteItem(
        name = "Bombillas LED y CFL",
        icon = "💡",
        safetyWarning = "Las bombillas ahorradoras fluorescentes CFL contienen vapor de mercurio altamente tóxico. Si se rompen por accidente, ventila la recámara durante 15 minutos de inmediato y retira con guantes gruesos. El LED es más seguro de reciclar.",
        salvageableParts = listOf(
            "Diodos LED integrados en encapsulado SMD",
            "Inductancias, minidiodos rectificadores",
            "Condensadores de 400V útiles para fuentes regulables"
        ),
        extractionSteps = "1. Desenrosca o raspa con cuidado el domo plástico difusor de los LED.\n2. Aísla la placa redonda metálica superior para conectarla a reguladores de corriente directa pequeños y haz brillar tus farándulas."
    )
)

package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Structures for the Circuit Simulator Game
data class GameComponent(
    val id: String,
    val name: String,
    val category: String, // "source", "controller", "load"
    val icon: String,
    val description: String,
    val isInsulator: Boolean = false
)

data class GameChallenge(
    val id: Int,
    val title: String,
    val description: String,
    val clue: String,
    val requiredSourceId: String,
    val requiredControllerId: String,
    val requiredLoadId: String,
    val pointsReward: Int
)

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProjectRepository

    // Central Flows from DB
    val userSettings: StateFlow<UserSettingsEntity>
    val completedProjects: StateFlow<List<CompletedProjectEntity>>
    val communityProjects: StateFlow<List<CommunityProjectEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ProjectRepository(database.projectDao)

        userSettings = repository.userSettings
            .map { it ?: UserSettingsEntity() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettingsEntity())

        completedProjects = repository.completedProjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        communityProjects = repository.communityProjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
            
        // Pre-initialize default settings if empty
        viewModelScope.launch {
            repository.userSettings.firstOrNull()?.let {
                // Already initialized
            } ?: run {
                repository.saveUserSettings(UserSettingsEntity())
            }
        }
    }

    // --- Dynamic User Progress Calculations ---
    val earnedEcoPoints: StateFlow<Int> = combine(
        completedProjects,
        communityProjects,
        userSettings
    ) { completed, community, settings ->
        val completedPoints = completed.size * 100
        val communityPoints = community.size * 50
        val gamePoints = settings.gameCompletedCount * 30
        val extra = settings.extraPoints
        completedPoints + communityPoints + gamePoints + extra
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Floating Assistant Chat State ---
    var activeChatProjectId by mutableStateOf<String?>(null)
    var activeChatHistory = mutableStateOf<List<Pair<String, Boolean>>>(
        listOf("¡Hola! Soy tu asistente de circuito. Pregúntame sobre cómo rescatar estas piezas de chatarra o cómo armar este tutorial de forma segura." to false)
    )
    var chatLoading by mutableStateOf(false)

    fun openChatForProject(projectId: String) {
        if (activeChatProjectId != projectId) {
            activeChatProjectId = projectId
            activeChatHistory.value = listOf(
                "¡Hola! Soy tu asistente de circuito para este proyecto. ¿Tienes dudas de cómo soldar, recuperar componentes de la chatarra o prevenir accidentes?" to false
            )
        }
    }

    fun sendMessage(projectTitle: String, projectDesc: String, messageText: String) {
        if (messageText.isBlank()) return
        
        // Add message to local chat history
        val currentContext = activeChatHistory.value.toMutableList()
        currentContext.add(messageText to true)
        activeChatHistory.value = currentContext
        chatLoading = true

        viewModelScope.launch {
            val response = GeminiApiClient.getAiResponse(
                projectTitle = projectTitle,
                projectDesc = projectDesc,
                conversationContext = currentContext
            )
            val updatedHistory = activeChatHistory.value.toMutableList()
            updatedHistory.add(response to false)
            activeChatHistory.value = updatedHistory
            chatLoading = false
        }
    }

    // --- Resistor Calculator State ---
    // Indices for: 1st band, 2nd band, multiplier, tolerance
    var band1Selection by mutableStateOf(1) // Marrón (1)
    var band2Selection by mutableStateOf(0) // Negro (0)
    var band3Selection by mutableStateOf(1) // Marrón (x10)
    var band4Selection by mutableStateOf(6) // Dorado (5%)

    // Resistor Color Definition
    val bandColors = listOf(
        ResistorColor("Negro", 0, 1.0, 0.0, 0xFF121212),
        ResistorColor("Marrón", 1, 10.0, 1.0, 0xFF7D4627),
        ResistorColor("Rojo", 2, 100.0, 2.0, 0xFFE53935),
        ResistorColor("Naranja", 3, 1000.0, 0.0, 0xFFFB8C00),
        ResistorColor("Amarillo", 4, 10000.0, 0.0, 0xFFFDD835),
        ResistorColor("Verde", 5, 100000.0, 0.5, 0xFF43A047),
        ResistorColor("Azul", 6, 1000000.0, 0.25, 0xFF1E88E5),
        ResistorColor("Violeta", 7, 10000000.0, 0.1, 0xFF8E24AA),
        ResistorColor("Gris", 8, 100000000.0, 0.05, 0xFF757575),
        ResistorColor("Blanco", 9, 1000000000.0, 0.0, 0xFFEEEEEE),
        ResistorColor("Dorado", -1, 0.1, 5.0, 0xFFFFB300),
        ResistorColor("Plata", -2, 0.01, 10.0, 0xFFB0BEC5)
    )

    fun calculateResistance(): String {
        val d1 = bandColors.getOrNull(band1Selection)?.digit ?: 0
        val d2 = bandColors.getOrNull(band2Selection)?.digit ?: 0
        val mult = bandColors.getOrNull(band3Selection)?.multiplier ?: 1.0
        val tol = bandColors.getOrNull(band4Selection)?.tolerance ?: 5.0

        val ohms = (d1 * 10 + d2) * mult
        val tolStr = "±$tol%"

        return when {
            ohms >= 1_000_000_000 -> String.format("%.1f GΩ %s", ohms / 1_000_000_000.0, tolStr)
            ohms >= 1_000_000 -> String.format("%.1f MΩ %s", ohms / 1_000_000.0, tolStr)
            ohms >= 1_000 -> String.format("%.1f kΩ %s", ohms / 1_000.0, tolStr)
            else -> String.format("%.0f Ω %s", ohms, tolStr)
        }
    }

    // --- Circuit Simulator Game State ---
    val gameChallenges = listOf(
        GameChallenge(
            id = 1,
            title = "Nivel 1: Reto Eco-Linterna",
            description = "Conecta una batería de litio vieja (fuente) con un conductor de cobre (conector) para encender la bombilla LED (carga).",
            clue = "Coloca la Batería 18650 en la Fuente, un Interruptor simple en el Control y la Bombilla LED en la Carga.",
            requiredSourceId = "comp_bat_18650",
            requiredControllerId = "comp_switch",
            requiredLoadId = "comp_led_bulb",
            pointsReward = 30
        ),
        GameChallenge(
            id = 2,
            title = "Nivel 2: Regulador Solar de Energía",
            description = "Estás construyendo un cargador. Conecta una celda fotovoltaica (solar) para energizar un módulo de carga USB (carga) controlado mediante un cable conductor limpio.",
            clue = "Usa Panel Celda Solar, Cable de Cobre y Módulo USB Hembra.",
            requiredSourceId = "comp_solar_cell",
            requiredControllerId = "comp_copper_wire",
            requiredLoadId = "comp_usb_out",
            pointsReward = 40
        ),
        GameChallenge(
            id = 3,
            title = "Nivel 3: Alarma de Residuos",
            description = "Queremos agregar un zumbador de alerta piezoeléctrico. Utiliza una batería de 9V proveniente de un juguete viejo, conéctala de forma segura y usa un interruptor metálico para silenciar o encender el altavoz.",
            clue = "Usa Batería Reciclada de 9V, Interruptor de Cobre y Zumbador Piezoeléctrico.",
            requiredSourceId = "comp_bat_9v",
            requiredControllerId = "comp_switch",
            requiredLoadId = "comp_buzzer",
            pointsReward = 50
        )
    )

    val gameComponentsInventory = listOf(
        // Sources
        GameComponent("comp_bat_18650", "Batería Litio 18650", "source", "🔋", "Recuperada de una laptop abandonada. 3.7V."),
        GameComponent("comp_solar_cell", "Panel Celda Solar 5V", "source", "☀️", "Recatado de una calculadora rota. Genera energía limpia."),
        GameComponent("comp_bat_9v", "Batería 9V Juguete", "source", "🪫", "Batería de desecho reciclada de control remoto."),
        GameComponent("comp_spoon", "Cuchara Oxidada", "source", "🥄", "No produce corriente química estable.", isInsulator = true),

        // Controllers
        GameComponent("comp_copper_wire", "Cable de Cobre", "controller", "🔌", "Excelente conductor elástico extraído de cables viejos."),
        GameComponent("comp_switch", "Interruptor de Cobre", "controller", "🎛️", "Interruptor para cortar la corriente."),
        GameComponent("comp_plastic_straw", "Sorbete de Plástico", "controller", "🥤", "Es un material plástico aislante, bloquea electrones.", isInsulator = true),

        // Loads
        GameComponent("comp_led_bulb", "Bombilla LED", "load", "💡", "Diodo emisor de luz de bajo consumo (reciclado)."),
        GameComponent("comp_buzzer", "Zumbador Piezoeléctrico", "load", "🔔", "Produce sonido de silbato corto al energizarse."),
        GameComponent("comp_usb_out", "Módulo USB Hembra", "load", "📲", "Puerto reciclado capaz de recargar un teléfono.")
    )

    var currentChallengeIndex by mutableStateOf(0)
    var gameSourceSlot by mutableStateOf<GameComponent?>(null)
    var gameControllerSlot by mutableStateOf<GameComponent?>(null)
    var gameLoadSlot by mutableStateOf<GameComponent?>(null)

    var gameIsLightOn by mutableStateOf(false)
    var gameClearedStatus by mutableStateOf(false)
    var gameFeedbackMessage by mutableStateOf("Arrastra o selecciona componentes de tu inventario superior en las ranuras del circuito.")

    fun resetGameLevel() {
        gameSourceSlot = null
        gameControllerSlot = null
        gameLoadSlot = null
        gameIsLightOn = false
        gameClearedStatus = false
        gameFeedbackMessage = "Coloca los 3 componentes correctos según el reto actual y cierra el circuito."
    }

    fun selectComponentForSlot(component: GameComponent, slotType: String) {
        if (gameClearedStatus) return
        
        when (slotType) {
            "source" -> gameSourceSlot = component
            "controller" -> gameControllerSlot = component
            "load" -> gameLoadSlot = component
        }
        
        // Dynamic verification feedback
        checkCircuitProgress()
    }

    private fun checkCircuitProgress() {
        val src = gameSourceSlot
        val ctrl = gameControllerSlot
        val ld = gameLoadSlot

        if (src != null && src.isInsulator) {
            gameFeedbackMessage = "⚠️ Falló: ¡La '${src.name}' no genera voltaje! Reemplázala por una pila química activa."
            return
        }
        if (ctrl != null && ctrl.isInsulator) {
            gameFeedbackMessage = "⚠️ Falló: ¡El '${ctrl.name}' actúa como un aislante de corriente! Usa un metal conductor."
            return
        }

        if (src != null && ctrl != null && ld != null) {
            gameFeedbackMessage = "¡Todos los componentes ubicados! Presiona 'Cerrar Interruptor' para probar tu circuito."
        } else {
            gameFeedbackMessage = "Coloca componentes hábiles en las ranuras vacías restantes."
        }
    }

    fun tryActivateCircuit() {
        if (gameClearedStatus) return
        
        val challenge = gameChallenges[currentChallengeIndex]
        val src = gameSourceSlot
        val ctrl = gameControllerSlot
        val ld = gameLoadSlot

        if (src == null || ctrl == null || ld == null) {
            gameFeedbackMessage = "❌ El circuito está incompleto. Asegúrate de rellenar las 3 ranuras."
            return
        }

        if (src.id == challenge.requiredSourceId &&
            ctrl.id == challenge.requiredControllerId &&
            ld.id == challenge.requiredLoadId) {
            
            gameIsLightOn = true
            gameClearedStatus = true
            gameFeedbackMessage = "🎉 ¡Felicidades! Circuito cerrado exitosamente en el simulador. ¡El bombillo se encendió! Ganaste +${challenge.pointsReward} EcoPuntos."
            
            // Add points to database settings
            viewModelScope.launch {
                val currentSettings = userSettings.value
                val updatedSettings = currentSettings.copy(
                    gameCompletedCount = currentSettings.gameCompletedCount + 1,
                    extraPoints = currentSettings.extraPoints + challenge.pointsReward
                )
                repository.saveUserSettings(updatedSettings)
            }
        } else {
            if (src.isInsulator || ctrl.isInsulator) {
                gameFeedbackMessage = "❌ Error: Tienes elementos aislantes en la línea de conducción."
            } else {
                gameFeedbackMessage = "❌ Los componentes no corresponden al circuito de este nivel. Mira la pista y colócalos correctamente."
            }
        }
    }

    fun nextChallenge() {
        if (currentChallengeIndex < gameChallenges.lastIndex) {
            currentChallengeIndex++
            resetGameLevel()
        } else {
            // Restart
            currentChallengeIndex = 0
            resetGameLevel()
        }
    }

    // --- Dynamic User Database Actions ---
    fun toggleProjectCompleted(projectId: String) {
        viewModelScope.launch {
            val list = completedProjects.value
            val isCompleted = list.any { it.id == projectId }
            if (isCompleted) {
                repository.uncompleteProject(projectId)
            } else {
                repository.completeProject(projectId)
            }
        }
    }

    fun createUserProject(title: String, description: String, category: String, materials: String, steps: String) {
        viewModelScope.launch {
            val username = userSettings.value.username
            val entity = CommunityProjectEntity(
                title = title,
                description = description,
                category = category,
                materials = materials,
                steps = steps,
                author = username,
                timestamp = System.currentTimeMillis(),
                likes = 0
            )
            repository.addCommunityProject(entity)
        }
    }

    fun likeProject(id: Int) {
        viewModelScope.launch {
            repository.likeCommunityProject(id)
        }
    }

    fun updateUsername(newUsername: String, newBio: String, newAvatarIndex: Int) {
        viewModelScope.launch {
            val currentSettings = userSettings.value
            val updated = currentSettings.copy(
                username = if (newUsername.isNotBlank()) newUsername else currentSettings.username,
                bio = if (newBio.isNotBlank()) newBio else currentSettings.bio,
                avatarIndex = newAvatarIndex
            )
            repository.saveUserSettings(updated)
        }
    }
}

// Simple helper class for bands
data class ResistorColor(
    val name: String,
    val digit: Int,
    val multiplier: Double,
    val tolerance: Double,
    val colorHex: Long
)

// Factory
class ProjectViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProjectViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

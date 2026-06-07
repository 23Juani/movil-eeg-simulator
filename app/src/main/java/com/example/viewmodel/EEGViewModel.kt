package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.Electrode
import com.example.model.ElectrodeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppScreen {
    EXPLORER,
    QUIZ,
    GUIDE
}

enum class QuizType {
    TAP_ON_3D,
    MULTIPLE_CHOICE
}

data class QuizQuestion(
    val id: Int,
    val prompt: String,
    val type: QuizType,
    val targetElectrodeId: String? = null, // Para TAP_ON_3D
    val options: List<String> = emptyList(), // Para MULTIPLE_CHOICE
    val correctAnswer: String, // ID del electrodo correcto u opción de texto exacta
    val explanation: String,
    val hint: String
)

class EEGViewModel : ViewModel() {

    // Pantalla activa de la sección general
    private val _currentScreen = MutableStateFlow(AppScreen.EXPLORER)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Electrodo seleccionado en el explorador interactivo
    private val _selectedElectrode = MutableStateFlow<Electrode?>(ElectrodeRepository.getElectrodeById("Cz"))
    val selectedElectrode: StateFlow<Electrode?> = _selectedElectrode.asStateFlow()

    // Región de filtro en el explorador ("Todos", "Frontal", "Central", "Temporal", "Parietal", "Occipital")
    private val _regionFilter = MutableStateFlow("Todos")
    val regionFilter: StateFlow<String> = _regionFilter.asStateFlow()

    // Ajustes del visor 3D
    private val _angleX = MutableStateFlow(0.2f)
    val angleX: StateFlow<Float> = _angleX.asStateFlow()

    private val _angleY = MutableStateFlow(0.5f)
    val angleY: StateFlow<Float> = _angleY.asStateFlow()

    private val _autoRotate = MutableStateFlow(true)
    val autoRotate: StateFlow<Boolean> = _autoRotate.asStateFlow()

    private val _showGrid = MutableStateFlow(true)
    val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

    // -------------------------------------------------------------
    // ESTADOS DEL JUEGO INTERACTIVO (QUIZ)
    // -------------------------------------------------------------
    private val questions = listOf(
        QuizQuestion(
            id = 1,
            prompt = "Identifica en el modelo 3D el electrodo 'Cz' (Vértice o corona central de la cabeza).",
            type = QuizType.TAP_ON_3D,
            targetElectrodeId = "Cz",
            correctAnswer = "Cz",
            hint = "Gira el modelo para mirar la cabeza desde arriba (vista cenital). Cz está en el punto más alto de todos.",
            explanation = "¡Excelente! Cz representa el vértice central de la cabeza, punto crucial de cruce del cual parten todas las mediciones del sistema internacional 10-20."
        ),
        QuizQuestion(
            id = 2,
            prompt = "¿Qué par de electrodos se ubican en la parte más trasera (lóbulo occipital) encargándose de registrar la corteza de procesamiento visual?",
            type = QuizType.MULTIPLE_CHOICE,
            options = listOf("Fp1 y Fp2", "O1 y O2", "T3 y T4", "C3 y C4"),
            correctAnswer = "O1 y O2",
            hint = "La letra 'O' representa el lóbulo Occipital en la parte posterior.",
            explanation = "¡Impecable! O1 (Izquierdo) y O2 (Derecho) se sitúan directamente sobre la corteza visual. Al cerrar los ojos, registran ondas Alfa de gran amplitud."
        ),
        QuizQuestion(
            id = 3,
            prompt = "Para registrar artefactos de parpadeos y movimientos oculares rápidos de frente, ¿qué electrodo de polo frontal izquierdo ('Fp1') captaría esta actividad?",
            type = QuizType.TAP_ON_3D,
            targetElectrodeId = "Fp1",
            correctAnswer = "Fp1",
            hint = "Gira el modelo hacia el frente. Fp1 está situado justo en la frente, de tu lado izquierdo (derecha de la pantalla).",
            explanation = "¡Correcto! Fp1 y Fp2 están en plena frente (polo frontal). Están expuestos a artefactos de parpadeo que en medicina se filtran o usan para calibración."
        ),
        QuizQuestion(
            id = 4,
            prompt = "¿Qué electrodos se colocan clásicamente en los lóbulos de las orejas y actúan como referencias eléctricas inertes?",
            type = QuizType.MULTIPLE_CHOICE,
            options = listOf("Fz y Pz", "A1 y A2", "F7 y F8", "T5 y T6"),
            correctAnswer = "A1 y A2",
            hint = "La letra 'A' proviene de 'Auricular' (orejas).",
            explanation = "¡Exacto! A1 y A2 (Auriculares) actúan típicamente como referencias o tierras no activas ya que no cuentan con corteza cerebral debajo de las orejas."
        ),
        QuizQuestion(
            id = 5,
            prompt = "Toca el electrodo temporal medio izquierdo 'T3' (o 'T7') ubicado directamente sobre el canal auditivo izquierdo.",
            type = QuizType.TAP_ON_3D,
            targetElectrodeId = "T3",
            correctAnswer = "T3",
            hint = "Rota el model de modo que veas el perfil izquierdo de la cabeza. T3 está justo encima del oído izquierdo.",
            explanation = "¡Excelente precisión! T3 (o T7) monitorea el procesamiento auditivo y semántico. Es sumamente relevante en diagnósticos de epilepsia temporal."
        ),
        QuizQuestion(
            id = 6,
            prompt = "¿Qué electrodo ubicado en la línea media parietal se utiliza universalmente para registrar el potencial evocado cognitivo P300 ante estímulos novedosos?",
            type = QuizType.MULTIPLE_CHOICE,
            options = listOf("Pz", "Fz", "Cz", "O1"),
            correctAnswer = "Pz",
            hint = "Ubicado en el lóbulo parietal (letra P) sobre la línea media (letra z).",
            explanation = "¡Fantástico! Pz es el electrodo estrella de potencial evocado cognitivo (P300), vinculado con la sorpresa y toma de decisiones."
        ),
        QuizQuestion(
            id = 7,
            prompt = "Localiza el electrodo 'O1' (Occipital Izquierdo) donde se manifiesta claramente el ritmo alfa en relajación.",
            type = QuizType.TAP_ON_3D,
            targetElectrodeId = "O1",
            correctAnswer = "O1",
            hint = "Gira la cabeza hacia atrás por completo. O1 es el nodo inferior izquierdo del lóbulo occipital.",
            explanation = "¡Bien hecho! O1 está en el lóbulo posterior occipital izquierdo. Monitorea las señales visuales primarias y el ritmo alfa en reposo completo."
        )
    )

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    // Estado del intento del usuario para la pregunta actual (null = sin responder, true = correcto, false = fallado)
    private val _questionSuccess = MutableStateFlow<Boolean?>(null)
    val questionSuccess: StateFlow<Boolean?> = _questionSuccess.asStateFlow()

    // Mostrar pista
    private val _showHint = MutableStateFlow(false)
    val showHint: StateFlow<Boolean> = _showHint.asStateFlow()

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
        // Si entra al quiz, detiene la rotación libre automática para dejar que el usuario use el quiz
        if (screen == AppScreen.QUIZ) {
            _autoRotate.value = false
        }
    }

    fun selectElectrode(electrode: Electrode) {
        _selectedElectrode.value = electrode
    }

    fun setRegionFilter(region: String) {
        _regionFilter.value = region
        // Si es diferente de todos, busca un electrodo correspondiente a esa región activa para actualizar la información
        if (region != "Todos") {
            val matching = ElectrodeRepository.electrodes.firstOrNull { it.region.contains(region, ignoreCase = true) }
            matching?.let { _selectedElectrode.value = it }
        }
    }

    // Rotar 3D a posiciones de cámara predeterminadas (Snaps)
    fun snapToView(viewName: String) {
        _autoRotate.value = false
        when (viewName) {
            "Frente" -> {
                _angleX.value = 0.1f
                _angleY.value = 0.0f
            }
            "Detrás" -> {
                _angleX.value = 0.1f
                _angleY.value = 3.1415f
            }
            "Arriba" -> {
                _angleX.value = 1.35f
                _angleY.value = 0.0f
            }
            "Derecha" -> {
                _angleX.value = 0.1f
                _angleY.value = -1.57f
            }
            "Izquierda" -> {
                _angleX.value = 0.1f
                _angleY.value = 1.57f
            }
        }
    }

    fun toggleAutoRotate() {
        _autoRotate.value = !_autoRotate.value
    }

    fun toggleGrid() {
        _showGrid.value = !_showGrid.value
    }

    fun getQuestionsCount(): Int = questions.size

    fun getCurrentQuestion(): QuizQuestion? {
        val idx = _currentQuestionIndex.value
        return if (idx in questions.indices) questions[idx] else null
    }

    // Responder una pregunta de opción múltiple
    fun submitOptionAnswer(option: String) {
        val question = getCurrentQuestion() ?: return
        if (_questionSuccess.value == true) return // Ya respondido correctamente

        if (option == question.correctAnswer) {
            _quizScore.value += 10
            _questionSuccess.value = true
        } else {
            _questionSuccess.value = false
        }
    }

    // Responder tocando en el visor 3D
    fun submit3DNodeTap(electrode: Electrode) {
        val question = getCurrentQuestion() ?: return
        if (question.type != QuizType.TAP_ON_3D) return
        if (_questionSuccess.value == true) return // Ya se contestó correctamente

        if (electrode.id == question.targetElectrodeId) {
            _quizScore.value += 10
            _questionSuccess.value = true
        } else {
            _questionSuccess.value = false
        }
    }

    fun triggerHint() {
        _showHint.value = true
    }

    fun advanceQuestion() {
        _questionSuccess.value = null
        _showHint.value = false
        val nextIdx = _currentQuestionIndex.value + 1
        if (nextIdx < questions.size) {
            _currentQuestionIndex.value = nextIdx
            // Centrar la cámara automáticamente hacia la región relevante de la siguiente pregunta de forma didáctica
            centerCameraOnQuestion(questions[nextIdx])
        } else {
            _quizCompleted.value = true
        }
    }

    private fun centerCameraOnQuestion(question: QuizQuestion) {
        if (question.type == QuizType.TAP_ON_3D) {
            val targetId = question.targetElectrodeId ?: return
            val electrode = ElectrodeRepository.getElectrodeById(targetId) ?: return
            // Rota la cámara para que el nodo sea visible de frente
            // X e Y coordenades describen la esfera [-1, 1]
            // Apuntamos la cámara según el cuadrante
            when {
                electrode.z < -0.3f -> snapToView("Detrás")
                electrode.y > 0.7f -> snapToView("Arriba")
                electrode.x > 0.4f -> snapToView("Derecha")
                electrode.x < -0.4f -> snapToView("Izquierda")
                else -> snapToView("Frente")
            }
        }
    }

    fun restartQuiz() {
        _currentQuestionIndex.value = 0
        _quizScore.value = 0
        _quizCompleted.value = false
        _questionSuccess.value = null
        _showHint.value = false
        snapToView("Frente")
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ElectrodeRepository
import com.example.ui.components.EEG3DViewer
import com.example.ui.components.EEGMonitorCard
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.EEGViewModel
import com.example.viewmodel.QuizType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars),
                    color = Color(0xFF070D14) // Fondo cibernético oscuro premium
                ) {
                    EEGApp()
                }
            }
        }
    }
}

@Composable
fun EEGApp(viewModel: EEGViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF070D14),
        bottomBar = {
            EEGNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { viewModel.setScreen(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.EXPLORER -> ExplorerScreen(viewModel)
                AppScreen.QUIZ -> QuizScreen(viewModel)
                AppScreen.GUIDE -> GuideScreen()
            }
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENTE: BARRA DE NAVEGACIÓN PERSONALIZADA EN LA PARTE INFERIOR
// -----------------------------------------------------------------------------
@Composable
fun EEGNavigationBar(
    currentScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = Color(0xFF0C1622),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                AppScreen.EXPLORER to "Explorar 3D",
                AppScreen.QUIZ to "Desafío Trivia",
                AppScreen.GUIDE to "Guía 10-20"
            )

            items.forEach { (screen, label) ->
                val selected = currentScreen == screen
                val color = if (selected) Color(0xFF00FFCC) else Color(0xFF64748B)
                val bgGlow = if (selected) Color(0x1A00FFCC) else Color.Transparent

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgGlow)
                        .clickable { onScreenSelected(screen) }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("nav_${screen.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when (screen) {
                            AppScreen.EXPLORER -> Icons.Default.Settings // Representación del gorro
                            AppScreen.QUIZ -> Icons.Default.PlayArrow       // Jugar trivia
                            AppScreen.GUIDE -> Icons.Default.Info          // Información del sistema 10-20
                        },
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        color = color,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PANTALLA: EXPLORADOR - MODELO INTERACTIVO 3D Y DETALLES DEL ELECTRODO
// -----------------------------------------------------------------------------
@Composable
fun ExplorerScreen(viewModel: EEGViewModel) {
    val selectedElectrode by viewModel.selectedElectrode.collectAsState()
    val regionFilter by viewModel.regionFilter.collectAsState()
    val rawAngleX by viewModel.angleX.collectAsState()
    val rawAngleY by viewModel.angleY.collectAsState()
    val autoRotate by viewModel.autoRotate.collectAsState()
    val showGrid by viewModel.showGrid.collectAsState()

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 720

    if (isTablet) {
        // DISEÑO ADAPTATIVO PARA TABLETAS: Vista side-by-side de alto rendimiento
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mitad izquierda: El visor 3D interactivo y sus controles
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                ScreenHeader(
                    title = "SISTEMA DE ELECTRODOS EEG 10-20",
                    subtitle = "Gira el cráneo en 3D y selecciona un nodo para ver su información médica."
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF040A12))
                        .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(20.dp))
                ) {
                    EEG3DViewer(
                        selectedElectrode = selectedElectrode,
                        onElectrodeSelected = { viewModel.selectElectrode(it) },
                        modifier = Modifier.fillMaxSize(),
                        angleX = rawAngleX,
                        angleY = rawAngleY,
                        autoRotate = autoRotate,
                        showGrid = showGrid,
                        highlightedRegion = if (regionFilter == "Todos") null else regionFilter
                    )

                    // Overlay de controles rápidos de rotación 3D
                    InteractiveOverlayControls(
                        autoRotate = autoRotate,
                        showGrid = showGrid,
                        onToggleAutoRotate = { viewModel.toggleAutoRotate() },
                        onToggleGrid = { viewModel.toggleGrid() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Presets Rápidos de cámara en tablet
                CameraPresetBar(onPresetSelected = { viewModel.snapToView(it) })
            }

            // Mitad derecha: Los filtros de región y detalles completos del electrodo
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .padding(vertical = 16.dp, horizontal = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Filtros horizontales
                RegionFilterBar(
                    selectedFilter = regionFilter,
                    onFilterSelected = { viewModel.setRegionFilter(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tarjeta de detalles del electrodo
                selectedElectrode?.let { electrode ->
                    ElectrodeDetailsCard(
                        electrode = electrode,
                        modifier = Modifier.fillMaxWidth()
                    )
                } ?: Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecciona un electrodo sobre el modelo 3D.",
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    } else {
        // DISEÑO MÓVIL: Apilado vertical optimizado con scroll inteligente
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            ScreenHeader(
                title = "EXPLORADOR EEG 10-20",
                subtitle = "Modelo cefálico interactivo en 3D. Explora las regiones."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de filtros rápidos para móvil
            RegionFilterBar(
                selectedFilter = regionFilter,
                onFilterSelected = { viewModel.setRegionFilter(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Visor central 3D
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF040A12))
                    .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(20.dp))
            ) {
                EEG3DViewer(
                    selectedElectrode = selectedElectrode,
                    onElectrodeSelected = { viewModel.selectElectrode(it) },
                    modifier = Modifier.fillMaxSize(),
                    angleX = rawAngleX,
                    angleY = rawAngleY,
                    autoRotate = autoRotate,
                    showGrid = showGrid,
                    highlightedRegion = if (regionFilter == "Todos") null else regionFilter
                )

                // Overlay de controles rápidos de rotación 3D
                InteractiveOverlayControls(
                    autoRotate = autoRotate,
                    showGrid = showGrid,
                    onToggleAutoRotate = { viewModel.toggleAutoRotate() },
                    onToggleGrid = { viewModel.toggleGrid() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Snaps rápidos de presets de cámara
            CameraPresetBar(onPresetSelected = { viewModel.snapToView(it) })

            Spacer(modifier = Modifier.height(16.dp))

            // Detalles del electrodo seleccionado
            selectedElectrode?.let { electrode ->
                ElectrodeDetailsCard(
                    electrode = electrode,
                    modifier = Modifier.fillMaxWidth()
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Toca un electrodo para analizar sus propiedades.",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// COMPONENTES DE AYUDA VISUAL EN EL EXPLORADOR
// -----------------------------------------------------------------------------
@Composable
fun ScreenHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            color = Color(0xFF00FFCC),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            fontFamily = FontFamily.SansSerif
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun RegionFilterBar(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("Todos", "Frontal", "Central", "Temporal", "Parietal", "Occipital")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Enlazar los filtros en un contenedor deslizable horizontalmente para móvil
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(filters.size) { index ->
                    val filterName = filters[index]
                    val isSelected = selectedFilter == filterName
                    val btnBg = if (isSelected) Color(0xFF00FFCC) else Color(0xFF0C1622)
                    val btnBorder = if (isSelected) Color(0xFF00FFCC) else Color(0xFF1E2E40)
                    val btnText = if (isSelected) Color(0xFF070D14) else Color(0xFFE2E8F0)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(32.dp))
                            .background(btnBg)
                            .border(1.dp, btnBorder, RoundedCornerShape(32.dp))
                            .clickable { onFilterSelected(filterName) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("filter_$filterName")
                    ) {
                        Text(
                            text = filterName,
                            color = btnText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveOverlayControls(
    autoRotate: Boolean,
    showGrid: Boolean,
    onToggleAutoRotate: () -> Unit,
    onToggleGrid: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Column(horizontalAlignment = Alignment.End) {
            // Switch de Autorotación
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xDD0C1622))
                    .clickable { onToggleAutoRotate() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (autoRotate) Color(0xFF00FFCC) else Color(0xFF64748B))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Giro Automático",
                    color = if (autoRotate) Color(0xFF00FFCC) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Switch de Rejilla Holograma
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xDD0C1622))
                    .clickable { onToggleGrid() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (showGrid) Color(0xFF4DEEEA) else Color(0xFF64748B))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mostrar Rejilla",
                    color = if (showGrid) Color(0xFF4DEEEA) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Texto de interactividad indicando drag táctil
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = "⬅ Desliza para rotar cabeza ➡",
            color = Color(0x77E2E8F0),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun CameraPresetBar(onPresetSelected: (String) -> Unit) {
    val presets = listOf("Frente", "Detrás", "Arriba", "Derecha", "Izquierda")
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "PERSPECTIVAS GEOMÉTRICAS RÁPIDAS",
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.forEach { preset ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0C1622))
                        .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(8.dp))
                        .clickable { onPresetSelected(preset) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset,
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ElectrodeDetailsCard(
    electrode: com.example.model.Electrode,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0C1622))
            .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        // Cabecera: ID y Nombre
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ELECTRODO: ${electrode.id}",
                color = Color(0xFF00FFCC),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x334DEEEA))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "10-20 Estándar",
                    color = Color(0xFF4DEEEA),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Región cerebral
        Text(
            text = "REGiÓN CEREBRAL SUBYACENTE",
            color = Color(0xFF64748B),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = electrode.region,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        HorizontalDivider(color = Color(0xFF1E2E40), modifier = Modifier.padding(vertical = 12.dp))

        // Descripción de funciones fisiológicas
        Row {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF00FFCC))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "FUNCIÓN Y PROCESAMIENTO ASOCIADO",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = electrode.description,
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Guía de ubicación para su colocación
        Row {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF4DEEEA))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "FÓRMULA DE MEDICIÓN DE CABEZA",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = electrode.localization,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Notas de Ondas
        Row {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE056FD))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "DENSIDAD DE ONDAS REGISTRADAS",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = electrode.standardWaves,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // OSCILOSCOPIO ANALÓGICO COMPLETO INTEGRADO
        EEGMonitorCard(
            waveType = electrode.waveType,
            electrodeId = electrode.id,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


// -----------------------------------------------------------------------------
// PANTALLA: JUEGO TRIVIA / DESAFÍO EN 3D
// -----------------------------------------------------------------------------
@Composable
fun QuizScreen(viewModel: EEGViewModel) {
    val currentQuestionIdx by viewModel.currentQuestionIndex.collectAsState()
    val rawAngleX by viewModel.angleX.collectAsState()
    val rawAngleY by viewModel.angleY.collectAsState()
    val showGrid by viewModel.showGrid.collectAsState()
    val quizCompleted by viewModel.quizCompleted.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val questionSuccess by viewModel.questionSuccess.collectAsState()
    val showHint by viewModel.showHint.collectAsState()
    val totalCount = viewModel.getQuestionsCount()

    if (quizCompleted) {
        QuizSuccessScreen(
            score = score,
            totalAnswers = totalCount,
            onRestart = { viewModel.restartQuiz() }
        )
    } else {
        val question = viewModel.getCurrentQuestion()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Cabecera con puntuación y contador
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DESAFÍO INTERACTIVO 3D",
                        color = Color(0xFF00FFCC),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Pregunta ${currentQuestionIdx + 1} de $totalCount",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tarjeta de puntuación
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C1622))
                        .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "SCORE: $score XP",
                        color = Color(0xFF00FFCC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Barra de progreso de la Trivia
            val progress = (currentQuestionIdx).toFloat() / totalCount.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = Color(0xFF00FFCC),
                trackColor = Color(0xFF101B2B)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Indicaciones de la pregunta
            question?.let { q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1622)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E2E40))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = q.prompt,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        )

                        if (q.type == QuizType.TAP_ON_3D) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Indicativo de que requiere interactuar directamente con el cerebro de arriba
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x224DEEEA))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Tap",
                                    tint = Color(0xFF4DEEEA),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "¡Interactúa con el modelo 3D y presiona el nodo correcto!",
                                    color = Color(0xFF4DEEEA),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Si es una pregunta interactiva 3D, mostramos el visor 3D para que toque el punto
                if (q.type == QuizType.TAP_ON_3D) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF040A12))
                            .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(20.dp))
                    ) {
                        EEG3DViewer(
                            selectedElectrode = null,
                            onElectrodeSelected = { tappedElectrode ->
                                viewModel.submit3DNodeTap(tappedElectrode)
                            },
                            modifier = Modifier.fillMaxSize(),
                            angleX = rawAngleX,
                            angleY = rawAngleY,
                            autoRotate = false, // Frenado de rotación para precisión de tap
                            showGrid = showGrid,
                            highlightedRegion = null
                        )
                        
                        InteractiveOverlayControls(
                            autoRotate = false,
                            showGrid = showGrid,
                            onToggleAutoRotate = {},
                            onToggleGrid = { viewModel.toggleGrid() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Snaps de rotación en Quiz
                    CameraPresetBar(onPresetSelected = { viewModel.snapToView(it) })
                } else {
                    // Si es una pregunta de opción múltiple, cargamos los botones con feedback de color
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        q.options.forEach { option ->
                            val isCorrectAnswer = option == q.correctAnswer
                            val btnBorderColor = when {
                                questionSuccess == true && isCorrectAnswer -> Color(0xFF00FFCC)
                                questionSuccess == false && !isCorrectAnswer -> Color(0xFF1E2E40)
                                else -> Color(0xFF1E2E40)
                            }
                            val btnBg = when {
                                questionSuccess == true && isCorrectAnswer -> Color(0x3300FFCC)
                                else -> Color(0xFF0C1622)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(btnBg)
                                    .border(1.dp, btnBorderColor, RoundedCornerShape(12.dp))
                                    .clickable(enabled = questionSuccess != true) {
                                        viewModel.submitOptionAnswer(option)
                                    }
                                    .padding(16.dp)
                                    .testTag("quiz_option_$option"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = option,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    if (questionSuccess == true && isCorrectAnswer) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Correcto",
                                            tint = Color(0xFF00FFCC),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SECCIÓN DE FEEDBACK (CORRECTO / COMPLEMENTO)
                AnimatedVisibility(
                    visible = questionSuccess != null,
                    enter = fadeIn() + androidx.compose.animation.expandVertically(animationSpec = tween(300)),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val isCorrect = questionSuccess == true
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isCorrect) Color(0x1B00FFCC) else Color(0x1BFF5252))
                            .border(
                                1.dp,
                                if (isCorrect) Color(0xFF00FFCC) else Color(0xFFFF5252),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (isCorrect) "¡LOGRADO CON ÉXITO!" else "RESPUESTA INCORRECTA",
                            color = if (isCorrect) Color(0xFF00FFCC) else Color(0xFFFF5252),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isCorrect) q.explanation else "No es correcto. Intenta con otra opción o revisa la pista de posicionamiento.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        if (!isCorrect) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.triggerHint() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2E40)),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Ver Pista Anatómica", color = Color.White, fontSize = 11.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.advanceQuestion() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("quiz_next_button")
                            ) {
                                Text("Siguiente Desafío", color = Color(0xFF070D14), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Panel flotante de Pista
                AnimatedVisibility(visible = showHint && questionSuccess == false) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF16251E))
                            .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Pista",
                                tint = Color(0xFF00FFCC),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "PISTA DE UBICACIÓN:",
                                    color = Color(0xFF00FFCC),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = q.hint,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuizSuccessScreen(
    score: Int,
    totalAnswers: Int,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0x2200FFCC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completado",
                tint = Color(0xFF00FFCC),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¡FELICIDADES EXPERTO!",
            color = Color(0xFF00FFCC),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Has completado satisfactoriamente el entrenamiento de mapeo internacional de electrodos del Sistema 10-20 de EEG.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Caja de Score final
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C1622))
                .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(16.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PUNTUACIÓN TOTAL",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$score / ${totalAnswers * 10} XP",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (score >= totalAnswers * 8) "RANGO: Neurólogo Maestro 🧠⚡" else "RANGO: Técnico EEG Promesa 🩺🔬",
                    color = Color(0xFF4DEEEA),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onRestart() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("quiz_restart_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Restart",
                tint = Color(0xFF070D14)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Reintentar Desafío",
                color = Color(0xFF070D14),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


// -----------------------------------------------------------------------------
// PANTALLA: GUÍA GENERAL DE COLOCACIÓN DEL SISTEMA 10-20
// -----------------------------------------------------------------------------
@Composable
fun GuideScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = "GUÍA COMPLETA SİSTEMA 10-20",
            subtitle = "Aprende los procedimientos técnicos de medición neurofisiológica."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta Introductoria
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1622)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFF1E2E40))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "¿Qué es el Sistema 10-20?",
                    color = Color(0xFF00FFCC),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Es un método estandarizado internacionalmente por la Federación Internacional de Sociedades de EEG para asegurar la colocación repetible de electrodos en el cráneo para estudios de electroencefalografía. El '10' y '20' se refieren a que las distancias entre electrodos adyacentes son de un 10% o un 20% del total de las medidas del cráneo.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pasos del procedimiento
        Text(
            text = "PASOS DE MEDICIÓN FİSİOLÓGİCA",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        GuideStepItem(
            stepNumber = "1",
            title = "Eje Sagital Anterior-Posterior (Nasion a Inion)",
            description = "Se mide la distancia desde el NASION (el puente de la nariz entre los ojos) hasta el INION (la protuberancia ósea en la parte inferior trasera del cráneo). Tras obtener el 100% de la distancia (e.g. 36 cm):\n" +
                    "• Fp (Polo Frontal) se coloca al 10% por encima del Nasion.\n" +
                    "• Fz se sitúa al 20% detrás de Fp.\n" +
                    "• Cz (Vértice) se marca al 20% detrás de Fz (punto medio exacto, 50%).\n" +
                    "• Pz se sitúa al 20% detrás de Cz.\n" +
                    "• O1 / O2 se marca al 10% por encima del Inion."
        )

        GuideStepItem(
            stepNumber = "2",
            title = "Eje Coronal Lateral (Plano Auricular)",
            description = "Se calcula la distancia transversal de oreja a oreja (puntos preauriculares A1 a A2, justo delante de la entrada del canal auditivo):\n" +
                    "• T3 (Lóbulo Temporal Izquierdo) se sitúa al 10% arriba de A1.\n" +
                    "• C3 (Central Izquierdo) se coloca al 20% arriba de T3.\n" +
                    "• Cz (Central Medio) se halla en el punto medio exacto (50%).\n" +
                    "• C4 se ubica un 20% a la derecha de Cz.\n" +
                    "• T4 se posiciona un 20% a la derecha de C4."
        )

        GuideStepItem(
            stepNumber = "3",
            title = "Arco de Circunferencia Equatoriana",
            description = "Se rodea la cabeza como un cinturón horizontal pasando por Fp1-Fp2, sienes, temporales y occipitales para trazar los electrodos de los lados:\n" +
                    "• Se calculan los porcentajes laterales de las sienes (F7 y F8) mediante un 20% de distancia de separación.\n" +
                    "• Se marcan las transiciones temporales traseras (T5 y T6) justo entre el temporal y el occipital."
        )

        GuideStepItem(
            stepNumber = "4",
            title = "Preparación de la Piel y Gel Conductor",
            description = "Para lograr registros limpios y nítidos sin ruido de interferencias:\n" +
                    "1. Exfoliación suave: Se limpia con alcohol y pasta abrasiva (como gel prep Nuprep) para eliminar células muertas y grasitud del cuero cabelludo.\n" +
                    "2. Fijación: Se adhieren electrodos de copa de oro o plata utilizando pasta conductora altamente adhesiva (como Ten20).\n" +
                    "3. Prueba de Impedancia: Se verifica mediante un impedanciómetro que cada canal posea una resistencia eléctrica por debajo de 5 kOhms (óptimo)."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun GuideStepItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0C1622))
            .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Círculo con el número del paso
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF00FFCC)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = Color(0xFF070D14),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

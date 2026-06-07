package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Electrode
import com.example.model.ElectrodeRepository
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Coordenadas tridimensionales de las orejas y nariz para dar referencia espacial
data class Point3D(val x: Float, val y: Float, val z: Float)

@OptIn(ExperimentalTextApi::class)
@Composable
fun EEG3DViewer(
    selectedElectrode: Electrode?,
    onElectrodeSelected: (Electrode) -> Unit,
    modifier: Modifier = Modifier,
    angleX: Float = 0.2f, // Rotación arriba/abajo
    angleY: Float = 0.5f, // Rotación izquierda/derecha
    autoRotate: Boolean = false,
    showGrid: Boolean = true,
    highlightedRegion: String? = null // Filtrar/resaltar región cerebral
) {
    var rawAngleX by remember { mutableStateOf(angleX) }
    var rawAngleY by remember { mutableStateOf(angleY) }

    // Auto-rotación suave en un plano horizontal
    LaunchedEffect(autoRotate) {
        if (autoRotate) {
            var lastTime = withFrameMillis { it }
            while (true) {
                withFrameMillis { time ->
                    val delta = (time - lastTime) / 1000f
                    rawAngleY += delta * 0.15f // Rotación lenta y constante
                    lastTime = time
                }
            }
        }
    }

    // Sincronizar cambios externos si proceden (por ej. botones de Reiniciar Vista)
    LaunchedEffect(angleX, angleY) {
        rawAngleX = angleX
        rawAngleY = angleY
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Constante de distorsión de perspectiva
    val cameraDistance = 3.2f

    // Listado de conexiones clásicas del gorro de EEG para el caparazón visual 10-20
    val links = remember {
        listOf(
            // Midline
            "Fz" to "Cz", "Cz" to "Pz",
            // Lóbulo frontal anterior
            "Fp1" to "Fp2",
            // Hemisferio Izquierdo - Canales internos
            "Fp1" to "F3", "F3" to "C3", "C3" to "P3", "P3" to "O1",
            // Hemisferio Izquierdo - Canales externos
            "Fp1" to "F7", "F7" to "T3", "T3" to "T5", "T5" to "O1",
            // Hemisferio Derecho - Canales internos
            "Fp2" to "F4", "F4" to "C4", "C4" to "P4", "P4" to "O2",
            // Hemisferio Derecho - Canales externos
            "Fp2" to "F8", "F8" to "T4", "T4" to "T6", "T6" to "O2",
            // Transversales Frontales
            "F7" to "F3", "F3" to "Fz", "Fz" to "F4", "F4" to "F8",
            // Transversales Centrales
            "T3" to "C3", "C3" to "Cz", "Cz" to "C4", "C4" to "T4",
            // Transversales Parietales
            "T5" to "P3", "P3" to "Pz", "Pz" to "P4", "P4" to "T6"
        )
    }

    // Puntos anatómicos de referencia en 3D
    val nosePoints = remember {
        listOf(
            Point3D(0f, 0.45f, 1.0f),     // Puente superior de la nariz
            Point3D(0f, 0.05f, 1.16f),    // Punta de la nariz
            Point3D(-0.1f, -0.05f, 1.0f), // Fosa izquierda
            Point3D(0.1f, -0.05f, 1.0f)   // Fosa derecha
        )
    }

    val leftEarPoints = remember {
        listOf(
            Point3D(-1.0f, 0.15f, 0.0f),
            Point3D(-1.12f, 0.0f, -0.05f),
            Point3D(-1.12f, -0.15f, -0.05f),
            Point3D(-1.0f, -0.3f, 0.0f)
        )
    }

    val rightEarPoints = remember {
        listOf(
            Point3D(1.0f, 0.15f, 0.0f),
            Point3D(1.12f, 0.0f, -0.05f),
            Point3D(1.12f, -0.15f, -0.05f),
            Point3D(1.0f, -0.3f, 0.0f)
        )
    }

    // Estructura de datos temporal proyectada
    var projectedElectrodes by remember {
        mutableStateOf<List<ProjectedElectrode>>(emptyList())
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(autoRotate) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Modificar ángulos de rotación de forma táctil
                        rawAngleY += dragAmount.x * 0.006f // Desplazamiento horizontal rota en eje Y
                        rawAngleX = (rawAngleX - dragAmount.y * 0.006f).coerceIn(-1.3f, 1.3f) // Capped pitch
                    }
                }
                .pointerInput(projectedElectrodes) {
                    detectTapGestures { offset ->
                        // Detectar cuál electrodo se presionó
                        val clickRadius = with(density) { 36.dp.toPx() }
                        val candidates = projectedElectrodes.filter {
                            val dist = sqrt((it.screenX - offset.x) * (it.screenX - offset.x) + (it.screenY - offset.y) * (it.screenY - offset.y))
                            dist <= clickRadius
                        }

                        if (candidates.isNotEmpty()) {
                            // De los candidatos que están dentro del radio de click, elegimos el que está más AL FRENTE (mayor zRot)
                            val best = candidates.maxByOrNull { it.zRot }
                            best?.let {
                                onElectrodeSelected(it.original)
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f
            // Escalado de la cabeza según tamaño del viewport
            val radiusScale = (width.coerceAtMost(height) / 2f) * 0.72f

            // Funciones matemáticas internas para rotar coordenadas en 3D (Euler Angles)
            fun rotateAndProject(x: Float, y: Float, z: Float): ProjectedCoords {
                // 1. Rotación alrededor del Eje Y (Giro vertical/lateral o Yaw)
                val cosY = cos(rawAngleY)
                val sinY = sin(rawAngleY)
                val x1 = x * cosY - z * sinY
                val z1 = x * sinY + z * cosY
                val y1 = y

                // 2. Rotación alrededor del Eje X (Inclinación arriba/abajo o Pitch)
                val cosX = cos(rawAngleX)
                val sinX = sin(rawAngleX)
                val x2 = x1
                val y2 = y1 * cosX - z1 * sinX
                val z2 = y1 * sinX + z1 * cosX

                // 3. Proyección con ligera Distorsión de Perspectiva
                val depthFactor = cameraDistance / (cameraDistance - z2)
                val screenX = centerX + x2 * radiusScale * depthFactor
                val screenY = centerY - y2 * radiusScale * depthFactor // Invertida ya que el lienzo Y baja

                return ProjectedCoords(screenX, screenY, z2)
            }

            // -------------------------------------------------------------
            // DIBUJAR CAPARAZÓN 3D / CABEZA DE REFERENCIA (Esfera Grid)
            // -------------------------------------------------------------
            if (showGrid) {
                val gridColor = Color(0xFF1E2E40)
                val gridAccentColor = Color(0x334DEEEA) // Cian con alta opacidad

                // Canales longitudinales de red (Círculos 3D rotados de 36 puntos)
                val steps = 36
                
                // Tratar el contorno esférico de la cabeza
                val spherePath = Path()
                var first = true
                for (i in 0..steps) {
                    val theta = (i * 2 * Math.PI / steps).toFloat()
                    val p = rotateAndProject(cos(theta), 0f, sin(theta))
                    if (first) {
                        spherePath.moveTo(p.x, p.y)
                        first = false
                    } else {
                        spherePath.lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = spherePath,
                    color = gridAccentColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Línea sagital (Nasion -> Inion central vertical)
                val sagittalPath = Path()
                first = true
                for (i in 0..steps) {
                    val theta = (i * 2 * Math.PI / steps).toFloat()
                    val p = rotateAndProject(0f, cos(theta), sin(theta))
                    if (first) {
                        sagittalPath.moveTo(p.x, p.y)
                        first = false
                    } else {
                        sagittalPath.lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = sagittalPath,
                    color = gridColor,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Línea coronal lateral
                val coronalPath = Path()
                first = true
                for (i in 0..steps) {
                    val theta = (i * 2 * Math.PI / steps).toFloat()
                    val p = rotateAndProject(cos(theta), sin(theta), 0f)
                    if (first) {
                        coronalPath.moveTo(p.x, p.y)
                        first = false
                    } else {
                        coronalPath.lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = coronalPath,
                    color = gridColor,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // -------------------------------------------------------------
            // DIBUJAR ELEMENTOS ANATÓMICOS EN 3D (Nariz y Orejas)
            // -------------------------------------------------------------
            val noseProj = nosePoints.map { rotateAndProject(it.x, it.y, it.z) }
            val leftEarProj = leftEarPoints.map { rotateAndProject(it.x, it.y, it.z) }
            val rightEarProj = rightEarPoints.map { rotateAndProject(it.x, it.y, it.z) }

            // Dibujar nariz en 3D
            val nosePath = Path()
            nosePath.moveTo(noseProj[0].x, noseProj[0].y) // Puente
            nosePath.lineTo(noseProj[1].x, noseProj[1].y) // Punta
            nosePath.lineTo(noseProj[2].x, noseProj[2].y) // Fosa Izquierda
            nosePath.lineTo(noseProj[0].x, noseProj[0].y) // Cerrar lado izquierdo
            nosePath.moveTo(noseProj[0].x, noseProj[0].y) // Puente
            nosePath.lineTo(noseProj[1].x, noseProj[1].y) // Punta
            nosePath.lineTo(noseProj[3].x, noseProj[3].y) // Fosa Derecha
            nosePath.lineTo(noseProj[0].x, noseProj[0].y) // Cerrar lado derecho
            nosePath.moveTo(noseProj[2].x, noseProj[2].y)
            nosePath.lineTo(noseProj[3].x, noseProj[3].y) // Base

            // La opacidad de la nariz depende de qué tan visible esté (si su Z es positivo)
            val avgNoseZ = (noseProj[1].zRot)
            val noseAlpha = if (avgNoseZ > -0.3f) 0.8f else 0.15f
            drawPath(
                path = nosePath,
                color = Color(0xFF00FFCC).copy(alpha = noseAlpha * 0.3f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dibujar Oreja Izquierda
            val leftEarPath = Path().apply {
                moveTo(leftEarProj[0].x, leftEarProj[0].y)
                cubicTo(
                    leftEarProj[1].x, leftEarProj[1].y,
                    leftEarProj[2].x, leftEarProj[2].y,
                    leftEarProj[3].x, leftEarProj[3].y
                )
            }
            val leftEarAlpha = if (leftEarProj[1].zRot > -0.3f) 0.8f else 0.15f
            drawPath(
                path = leftEarPath,
                color = Color(0xFFFFB6C1).copy(alpha = leftEarAlpha * 0.4f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dibujar Oreja Derecha
            val rightEarPath = Path().apply {
                moveTo(rightEarProj[0].x, rightEarProj[0].y)
                cubicTo(
                    rightEarProj[1].x, rightEarProj[1].y,
                    rightEarProj[2].x, rightEarProj[2].y,
                    rightEarProj[3].x, rightEarProj[3].y
                )
            }
            val rightEarAlpha = if (rightEarProj[1].zRot > -0.3f) 0.8f else 0.15f
            drawPath(
                path = rightEarPath,
                color = Color(0xFFFFB6C1).copy(alpha = rightEarAlpha * 0.4f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // -------------------------------------------------------------
            // CALCULAR PROYECCIÓN DE ELECTRODOS
            // -------------------------------------------------------------
            val eegNodes = ElectrodeRepository.electrodes
            val currentProjList = eegNodes.map { electrode ->
                val p = rotateAndProject(electrode.x, electrode.y, electrode.z)
                ProjectedElectrode(electrode, p.x, p.y, p.zRot)
            }
            projectedElectrodes = currentProjList

            // -------------------------------------------------------------
            // DIBUJAR ENLACES / LINKEADOS DEL GORRO DE EEG (La red de cables)
            // -------------------------------------------------------------
            links.forEach { (fromId, toId) ->
                val from = currentProjList.find { it.original.id == fromId }
                val to = currentProjList.find { it.original.id == toId }

                if (from != null && to != null) {
                    // Calculamos visibilidad del enlace. Si ambos extremos están traseros, atenuamos la línea.
                    val avgZ = (from.zRot + to.zRot) / 2f
                    val linkAlpha = if (avgZ >= -0.2f) 0.5f else 0.1f
                    val lineColor = if (avgZ >= -0.2f) Color(0xFF1F4E5B) else Color(0xFF0F262F)

                    drawLine(
                        color = lineColor.copy(alpha = linkAlpha),
                        start = Offset(from.screenX, from.screenY),
                        end = Offset(to.screenX, to.screenY),
                        strokeWidth = 1.5f.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // -------------------------------------------------------------
            // DIBUJAR LOS NODOS (ELECTRODOS) ETIQUETADOS
            // -------------------------------------------------------------
            // Ordenamos por zRot (de atrás hacia adelante) para que los del fondo se dibujen primero
            // y los de adelante queden superpuestos encima de todo. Esto soluciona problemas visuales de oclusión!
            currentProjList.sortedBy { it.zRot }.forEach { proj ->
                val electrode = proj.original
                val isSelected = selectedElectrode?.id == electrode.id
                val isHighlighted = highlightedRegion == null || electrode.region.contains(highlightedRegion, ignoreCase = true)
                val zRot = proj.zRot

                // Umbral de profundidad. Si zRot es < -0.2 significa que está en el hemisferio trasero o mirando en dirección contraria
                val isFront = zRot >= -0.2f
                val baseAlpha = if (isFront) 1.0f else 0.18f

                // Moduladores de color basados en selección, filtro y profundidad
                val accentColor = when {
                    isSelected -> Color(0xFF00FFCC) // Neon Cyan para seleccionado
                    !isHighlighted -> Color(0xFF4A5568).copy(alpha = baseAlpha * 0.3f) // Dimmer si no está bajo el filtro de región
                    else -> Color(0xFF4DEEEA)       // Standard neon blue
                }

                val nodeRadius = if (isSelected) 11.dp.toPx() else 8.dp.toPx()

                // 1. Dibujar el halo de brillo (glow) si el electrodo está seleccionado o bajo atención
                if (isSelected && isFront) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.25f),
                        radius = nodeRadius * 2.2f,
                        center = Offset(proj.screenX, proj.screenY)
                    )
                }

                // 2. Dibujar el círculo principal del nodo
                drawCircle(
                    color = if (isFront) Color(0xFF07111A) else Color(0x33000000),
                    radius = nodeRadius,
                    center = Offset(proj.screenX, proj.screenY)
                )

                // 3. Borde de color del nodo
                drawCircle(
                    color = accentColor.copy(alpha = baseAlpha),
                    radius = nodeRadius,
                    center = Offset(proj.screenX, proj.screenY),
                    style = Stroke(width = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx())
                )

                // 4. Dot interno del nodo activo
                if (isFront) {
                    drawCircle(
                        color = if (isSelected) Color(0xFF00FFCC) else accentColor.copy(alpha = 0.6f),
                        radius = if (isSelected) 4.dp.toPx() else 3.dp.toPx(),
                        center = Offset(proj.screenX, proj.screenY)
                    )
                }

                // 5. Etiqueta / Nombre de texto (Fp1, Cz, etc.)
                // Solo mostramos textos de nodos traseros en un tamaño menor y baja opacidad, los delanteros con fuente clara
                val textStyle = TextStyle(
                    color = if (isSelected) Color(0xFFFFFFFF) else if (isFront) Color(0xFFE2E8F0) else Color(0xFF4A5568).copy(alpha = 0.2f),
                    fontSize = if (isSelected) 13.sp else 10.sp,
                    fontWeight = if (isSelected || isFront) FontWeight.Bold else FontWeight.Normal,
                    shadow = if (isFront) Shadow(color = Color.Black, blurRadius = 4f, offset = Offset(1f, 1f)) else null
                )

                val textLayoutResult = textMeasurer.measure(
                    text = AnnotatedString(electrode.id),
                    style = textStyle
                )

                // Posicionar el texto un poco por encima o desplazado del círculo
                val textOffset = Offset(
                    x = proj.screenX - textLayoutResult.size.width / 2f,
                    y = proj.screenY - nodeRadius - textLayoutResult.size.height - 1.dp.toPx()
                )

                // Dibujar el texto del nodo en la vista proyectada
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = textOffset
                )
            }
        }
    }
}

// Estructuras de ayuda interna
private data class ProjectedCoords(val x: Float, val y: Float, val zRot: Float)

private data class ProjectedElectrode(
    val original: Electrode,
    val screenX: Float,
    val screenY: Float,
    val zRot: Float // Posición en profundidad después de rotar
)

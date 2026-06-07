package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WaveType
import kotlin.math.sin

@Composable
fun EEGMonitorCard(
    waveType: WaveType,
    electrodeId: String,
    modifier: Modifier = Modifier
) {
    var rawPhase by remember { mutableStateOf(0f) }

    // Animación continua del osciloscopio en tiempo real
    LaunchedEffect(waveType) {
        var lastTime = withFrameMillis { it }
        while (true) {
            withFrameMillis { time ->
                val delta = (time - lastTime) / 1000f
                rawPhase += delta * 12f // Velocidad de desplazamiento de la señal
                lastTime = time
            }
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0C1622))
            .border(1.dp, Color(0xFF1E2E40), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Cabecera del monitor
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Indicador luminoso parpadeante
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            when (waveType) {
                                WaveType.DELTA -> Color(0xFFFF5252)
                                WaveType.THETA -> Color(0xFFFF9F43)
                                WaveType.ALPHA -> Color(0xFF4DEEEA)
                                WaveType.BETA -> Color(0xFF00FFCC)
                                WaveType.GAMMA -> Color(0xFFE056FD)
                            }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MONITOR SEÑAL DE VOLTAJE (EEG) - Canal $electrodeId",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "FILTRADO: 0.5 - 70 Hz (Impedancia: <3.2 kΩ)",
                color = Color(0xFF475569),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // El Canvas del Osciloscopio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF040A12))
                .border(1.dp, Color(0xFF101B2B), RoundedCornerShape(8.dp))
        ) {
            // Cuadrícula de fondo verde clínico semi-transparente
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridYCount = 6
                val gridXCount = 12

                // Líneas horizontales de microvoltios
                for (i in 1 until gridYCount) {
                    val y = (size.height / gridYCount) * i
                    drawLine(
                        color = Color(0x114DEEEA),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                // Líneas verticales de intervalos de milisegundos
                for (i in 1 until gridXCount) {
                    val x = (size.width / gridXCount) * i
                    drawLine(
                        color = Color(0x114DEEEA),
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                }
            }

            // El trazo de la onda EEG basada en el ritmo de la corteza
            Canvas(modifier = Modifier.fillMaxSize()) {
                val pointsCount = size.width.toInt().coerceAtMost(400)
                val wavePath = Path()
                val midY = size.height / 2f

                for (i in 0 until pointsCount) {
                    val x = (size.width / pointsCount) * i
                    
                    // Cálculo de amplitud matemática de ondas fisiológicas sinusoidales apiladas
                    val yVal = when (waveType) {
                        WaveType.DELTA -> {
                            // Ondas muy lentas y amplias del sueño profundo
                            val s1 = sin(0.04f * i - rawPhase * 0.25f) * 28f
                            val s2 = sin(0.012f * i - rawPhase * 0.1f) * 12f
                            s1 + s2
                        }
                        WaveType.THETA -> {
                            // Ondas intermedias, características de relajación y memoria
                            val s1 = sin(0.1f * i - rawPhase * 0.6f) * 16f
                            val s2 = sin(0.045f * i - rawPhase * 0.2f) * 6f
                            s1 + s2
                        }
                        WaveType.ALPHA -> {
                            // Ondas sincronizadas con ráfagas de husos temporales modulados
                            // Envelope de modulación para generar el famoso huso alfa
                            val s1 = sin(0.21f * i - rawPhase * 1.2f) * 14f
                            val envelope = sin(0.02f * i - rawPhase * 0.15f) * 0.5f + 0.5f
                            s1 * (0.3f + 1.1f * envelope)
                        }
                        WaveType.BETA -> {
                            // Ondas rápidas y de menor amplitud durante estados cognitivos normales
                            val s1 = sin(0.45f * i - rawPhase * 2.8f) * 6f
                            val s2 = sin(1.0f * i - rawPhase * 6.0f) * 3f
                            val noise = sin(2.2f * i) * 1f
                            s1 + s2 + noise
                        }
                        WaveType.GAMMA -> {
                            // Ondas súper veloces asociadas a picos de percepción cognitiva
                            val s1 = sin(1.1f * i - rawPhase * 6.5f) * 3f
                            val s2 = sin(2.3f * i - rawPhase * 13.0f) * 2f
                            val s3 = sin(4.5f * i - rawPhase * 22.0f) * 1.2f
                            s1 + s2 + s3
                        }
                    }

                    val finalY = midY + yVal

                    if (i == 0) {
                        wavePath.moveTo(x, finalY)
                    } else {
                        wavePath.lineTo(x, finalY)
                    }
                }

                // Dibujar el trazo brillante neón
                drawPath(
                    path = wavePath,
                    color = when (waveType) {
                        WaveType.DELTA -> Color(0xFFFF5252)
                        WaveType.THETA -> Color(0xFFFF9F43)
                        WaveType.ALPHA -> Color(0xFF4DEEEA)
                        WaveType.BETA -> Color(0xFF00FFCC)
                        WaveType.GAMMA -> Color(0xFFE056FD)
                    },
                    style = Stroke(
                        width = 1.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Leyenda descriptiva del ritmo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (label, freq) = when (waveType) {
                WaveType.DELTA -> "Ritmo Delta: Sueño profundo, regeneración" to "0.5 - 4 Hz (Alta Amplitud)"
                WaveType.THETA -> "Ritmo Theta: Relajación profunda, meditación" to "4 - 8 Hz (Amplitud Media)"
                WaveType.ALPHA -> "Ritmo Alfa: Reposo despierto, ojos cerrados" to "8 - 12 Hz (Glow Sincrónico)"
                WaveType.BETA -> "Ritmo Beta: Concentración, enfoque cognitivo" to "12 - 30 Hz (Baja Amplitud)"
                WaveType.GAMMA -> "Ritmo Gamma: Integración sensorial, memoria" to "30 - 80 Hz (Frecuencia Extrema)"
            }

            Text(
                text = label,
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = freq,
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

package com.example.model

enum class WaveType {
    DELTA, // Sueño profundo (0.5 - 4 Hz)
    THETA, // Somnolencia / Memoria (4 - 8 Hz)
    ALPHA, // Relajación / Ojos cerrados (8 - 12 Hz)
    BETA,  // Concentración / Actividad (12 - 30 Hz)
    GAMMA  // Alto procesamiento cognitivo (30 - 80 Hz)
}

data class Electrode(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val region: String,
    val description: String,
    val localization: String,
    val standardWaves: String,
    val waveType: WaveType
)

object ElectrodeRepository {
    val electrodes = listOf(
        Electrode(
            id = "Fp1",
            name = "Fp1 - Polo Frontal Izquierdo",
            x = -0.28f, y = 0.28f, z = 0.85f,
            region = "Lóbulo Frontal Anterior (Corteza Prefrontal)",
            description = "Monitorea la corteza prefrontal anterior izquierda, involucrada en la atención sostenida, memoria de trabajo y regulación emocional. Dado su posicionamiento sobre las cejas, capta intensamente los parpadeos (artefactos electrooculográficos).",
            localization = "10% arriba del Nasion, desviado un 10% a la izquierda del arco central.",
            standardWaves = "Ondas Beta durante concentración intensa; abundantes ondas Delta/Theta y picos de parpadeo (EOG).",
            waveType = WaveType.BETA
        ),
        Electrode(
            id = "Fp2",
            name = "Fp2 - Polo Frontal Derecho",
            x = 0.28f, y = 0.28f, z = 0.85f,
            region = "Lóbulo Frontal Anterior (Corteza Prefrontal)",
            description = "Monitorea la corteza prefrontal anterior derecha, asociada con el procesamiento emocional, creatividad no verbal y control conductual. Registra parpadeos y movimientos oculares en conjunto con Fp1.",
            localization = "10% arriba del Nasion, desviado un 10% a la derecha del arco central.",
            standardWaves = "Principalmente Ondas Beta en estado de vigilia activa; Delta alto durante el sueño.",
            waveType = WaveType.BETA
        ),
        Electrode(
            id = "F3",
            name = "F3 - Frontal Izquierdo",
            x = -0.42f, y = 0.62f, z = 0.55f,
            region = "Lóbulo Frontal Dorsolateral",
            description = "Registra la actividad del lóbulo frontal izquierdo asociativo. Vinculado a funciones ejecutivas, razonamiento lógico, planificación y la expresión verbal positiva. La depresión a menudo se asocia con hipoactividad en esta zona.",
            localization = "En la intersección del 20% del arco sagital frontal y un 20% hacia la izquierda.",
            standardWaves = "Ondas Beta durante pensamiento activo y tareas del lenguaje; ondas Theta en fatiga cognitiva.",
            waveType = WaveType.BETA
        ),
        Electrode(
            id = "F4",
            name = "F4 - Frontal Derecho",
            x = 0.42f, y = 0.62f, z = 0.55f,
            region = "Lóbulo Frontal Dorsolateral",
            description = "Registra el lóbulo frontal derecho asociativo. Juega un rol clave en la vigilancia, atención espacial, procesamiento emocional negativo y resolución de problemas no verbales.",
            localization = "En la intersección del 20% del arco sagital frontal y un 20% hacia la derecha.",
            standardWaves = "Ondas Beta en alerta; ondas Theta y Delta cuando hay inatención general.",
            waveType = WaveType.BETA
        ),
        Electrode(
            id = "Fz",
            name = "Fz - Frontal Línea Media",
            x = 0.00f, y = 0.76f, z = 0.58f,
            region = "Lóbulo Frontal Medio",
            description = "Ubicado en la línea media prefrontal. Evalúa la corteza motora suplementaria y el cíngulo anterior. Es crítico para monitorizar la atención selectiva, la respuesta de inhibición y procesos de toma de decisiones.",
            localization = "A lo largo de la línea media de la cabeza, exactamente un 20% por delante del vértice (Cz).",
            standardWaves = "Ondas Theta de línea media durante concentración mental fluida ('flow') y ondas Beta de atención.",
            waveType = WaveType.THETA
        ),
        Electrode(
            id = "F7",
            name = "F7 - Frontal Inferior Izquierdo",
            x = -0.72f, y = 0.28f, z = 0.52f,
            region = "Giro Frontal Inferior / Sien",
            description = "Ubicado sobre la sien izquierda. Se sitúa muy cerca del área de Broca, la cual controla la producción de lenguaje articulado. Crucial en estudios neuropsicológicos de lenguaje y afasias.",
            localization = "En el plano horizontal frontal lateral, un 20% a la izquierda de la línea media.",
            standardWaves = "Ondas Beta y Gamma asociadas al habla; propenso a artefactos de tragar y tensión mandibular.",
            waveType = WaveType.GAMMA
        ),
        Electrode(
            id = "F8",
            name = "F8 - Frontal Inferior Derecho",
            x = 0.72f, y = 0.28f, z = 0.52f,
            region = "Giro Frontal Inferior / Sien",
            description = "Ubicado sobre la sien derecha. Mide la actividad prefrontal inferior derecha. Involucrado en la prosodia (entonación del habla) y la inhibición de impulsos conductuales.",
            localization = "En el plano horizontal frontal lateral, un 20% a la derecha de la línea media.",
            standardWaves = "Ondas Beta y artefactos musculares de la mandíbula.",
            waveType = WaveType.BETA
        ),
        Electrode(
            id = "C3",
            name = "C3 - Central Izquierdo",
            x = -0.58f, y = 0.75f, z = 0.00f,
            region = "Corteza Motora y Somatosensorial (Izquierda)",
            description = "Ubicado sobre la corteza premotora y motora primaria del hemisferio izquierdo, la cual controla y siente el lado derecho de la anatomía corporal. Famoso por registrar el ritmo Mu.",
            localization = "Exactamente a un 20% de distancia hacia la izquierda de Cz en la línea central coronal.",
            standardWaves = "Ritmo Mu (8-12 Hz) que disminuye cuando se ejecuta o imagina un movimiento físico de la mano derecha.",
            waveType = WaveType.ALPHA
        ),
        Electrode(
            id = "C4",
            name = "C4 - Central Derecho",
            x = 0.58f, y = 0.75f, z = 0.00f,
            region = "Corteza Motora y Somatosensorial (Derecha)",
            description = "Ubicado sobre la corteza premotora y motora primaria del hemisferio derecho, la cual controla el lado izquierdo del cuerpo. Registra también el ritmo Mu en el lado derecho.",
            localization = "Exactamente a un 20% de distancia hacia la derecha de Cz en la línea central coronal.",
            standardWaves = "Ritmo Mu (8-12 Hz) que sufre desincronización al mover o visualizar el movimiento de la mano izquierda.",
            waveType = WaveType.ALPHA
        ),
        Electrode(
            id = "Cz",
            name = "Cz - Central Línea Media (Vértice)",
            x = 0.00f, y = 0.95f, z = 0.00f,
            region = "Vértice de la Corteza Motora Central",
            description = "Ubicado directamente en la coronilla de la cabeza. Es la cúspide geométrica del cráneo. Excelente punto de referencia multicanal y crítico para analizar el sueño y potenciales neurofisiológicos evocados.",
            localization = "Ubicado en el punto exacto de cruce del 50% entre Nasion-Inion y el plano interauricular.",
            standardWaves = "Husos de sueño (12-14 Hz), complejos K (sueño N2) y amplitudes altas de potenciales auditivos.",
            waveType = WaveType.THETA
        ),
        Electrode(
            id = "T3",
            name = "T3 / T7 - Temporal Izquierdo",
            x = -0.90f, y = 0.15f, z = 0.00f,
            region = "Lóbulo Temporal Medio Izquierdo",
            description = "Registra la corteza auditiva asociativa y áreas de memoria semántica izquierdas. Cerca del área auditiva receptiva. Importante para analizar almacenamiento de léxico e hipocampos.",
            localization = "Ubicado a un 20% de Cz hacia la izquierda, justo arriba del canal auditivo auricular izquierdo.",
            standardWaves = "Ondas Theta y Alfa; zona susceptible a focos de descargas epilépticas temporales.",
            waveType = WaveType.THETA
        ),
        Electrode(
            id = "T4",
            name = "T4 / T8 - Temporal Derecho",
            x = 0.90f, y = 0.15f, z = 0.00f,
            region = "Lóbulo Temporal Medio Derecho",
            description = "Registra la corteza temporal derecha, clave en la apreciación musical, reconocimiento de rostros (área fusiforme), procesamiento de patrones sonoros e inteligencia emocional espacial.",
            localization = "Ubicado a un 20% de Cz hacia la derecha, justo arriba del canal auditivo derecho.",
            standardWaves = "Ondas Theta y Alfa en periodos relajados.",
            waveType = WaveType.THETA
        ),
        Electrode(
            id = "T5",
            name = "T5 / P7 - Temporal Posterior Izquierdo",
            x = -0.72f, y = 0.28f, z = -0.52f,
            region = "Unión Temporo-Parieto-Occipital",
            description = "Mide la porción de transición cerebral posterior izquierda. Es de gran interés en estudios de lectura y procesamiento semántico verbal ya que conecta la visión visual con el área de Wernicke.",
            localization = "En la línea lateral de la cabeza, un 20% por detrás de T3 en el arco temporal.",
            standardWaves = "Suele mostrar actividad Alfa cuando se asocia con procesamiento visuo-língüístico en reposo.",
            waveType = WaveType.ALPHA
        ),
        Electrode(
            id = "T6",
            name = "T6 / P8 - Temporal Posterior Derecho",
            x = 0.72f, y = 0.28f, z = -0.52f,
            region = "Unión Temporo-Parieto-Occipital",
            description = "Mide la porción posterior derecha de la cabeza. Involucrada en la memoria visual icónica, orientación topográfica espacial avanzada y el procesamiento emocional visual secundario.",
            localization = "En la línea lateral de la cabeza, un 20% por detrás de T4 en el arco temporal.",
            standardWaves = "Alfa dominante en relajación visual con ojos cerrados.",
            waveType = WaveType.ALPHA
        ),
        Electrode(
            id = "P3",
            name = "P3 - Parietal Izquierdo",
            x = -0.42f, y = 0.62f, z = -0.55f,
            region = "Lóbulo Parietal Izquierdo",
            description = "Focalizado en la corteza de asociación somatosensorial izquierda. Clave en el procesamiento de estímulos táctiles complejos, cálculo numérico preciso, lógica secuencial y la orientación propia-temporal.",
            localization = "En la intersección del 20% del arco sagital posterior y un 20% hacia la izquierda del centro sagital.",
            standardWaves = "Ritmo Alfa de reposo que se atenúa súbitamente con estimulación sensorial activa o tareas numéricas.",
            waveType = WaveType.ALPHA
        ),
        Electrode(
            id = "P4",
            name = "P4 - Parietal Derecho",
            x = 0.42f, y = 0.62f, z = -0.55f,
            region = "Lóbulo Parietal Derecho",
            description = "Asociado a la corteza parietal derecha. Vital para la representación mental del espacio del cuerpo y del entorno externo, permitiendo la percepción de relaciones de distancias y profundidad.",
            localization = "En la intersección del 20% del arco sagital posterior y un 20% hacia la derecha del centro sagital.",
            standardWaves = "Ondas Alfa fuertes; se inhiben durante la atención visoespacial o cálculo espacial.",
            waveType = WaveType.ALPHA
        ),
        Electrode(
            id = "Pz",
            name = "Pz - Parietal Línea Media",
            x = 0.00f, y = 0.76f, z = -0.58f,
            region = "Lóbulo Parietal Medio",
            description = "Línea media sobre el precúneo y corteza parietal asociativa. Es el electrodo de oro para registrar el potencial evocado cognitivo P300, el cual se incrementa ante estímulos de novedad (Paradigma Oddball).",
            localization = "En la línea media dorsal, a un 20% de distancia geométrica detrás de Cz.",
            standardWaves = "Ondas Alfa prominentes cuando los ojos están cerrados; ideal para detectar ondas cognitivas P300.",
            waveType = WaveType.ALPHA
        ),
        Electrode(
            id = "O1",
            name = "O1 - Occipital Izquierdo",
            x = -0.28f, y = 0.28f, z = -0.85f,
            region = "Lóbulo Occipital (Corteza Visual Izquierda)",
            description = "Registra la corteza visual primaria del hemisferio izquierdo (responsable de procesar la porción derecha del campo visual). Excelente indicador del estado dinámico del ciclo de atención mediante el ritmo Alfa.",
            localization = "Aproximadamente 10% por encima de la protuberancia del Inion y 10% hacia la izquierda.",
            standardWaves = "Fuerte Ritmo Alfa Occipital (8-12 Hz) sincrónico que explota en amplitud al cerrar los ojos y se desploma al abrirlos.",
            waveType = WaveType.ALPHA
        ),
        Electrode(
            id = "O2",
            name = "O2 - Occipital Derecho",
            x = 0.28f, y = 0.28f, z = -0.85f,
            region = "Lóbulo Occipital (Corteza Visual Derecha)",
            description = "Registra la corteza visual primaria del hemisferio derecho (procesa el campo visual izquierdo). Permite medir la activación sincronizada de la corteza visual tras destellos de luz (flickering).",
            localization = "Aproximadamente 10% por encima de la protuberancia del Inion y 10% hacia la derecha.",
            standardWaves = "Ritmo Alfa Occipital muy nítido en reposo con baja luz o estimulación inactiva.",
            waveType = WaveType.ALPHA
        ),
        Electrode(
            id = "A1",
            name = "A1 - Referencia Oreja Izquierda",
            x = -0.98f, y = -0.25f, z = -0.02f,
            region = "Lóbulo de la Oreja (Oído Izquierdo)",
            description = "Punto de contacto de referencia neutro no activo. Se utiliza como referencia a tierra o para la comparación de potencial común diferencial reduciendo el ruido circundante.",
            localization = "Lóbulo de la oreja izquierda o proceso mastoides izquierdo.",
            standardWaves = "Principalmente niveles basales de voltaje nulo, con pequeños ruidos musculares de masticación.",
            waveType = WaveType.DELTA
        ),
        Electrode(
            id = "A2",
            name = "A2 - Referencia Oreja Derecho",
            x = 0.98f, y = -0.25f, z = -0.02f,
            region = "Lóbulo de la Oreja (Oído Derecho)",
            description = "Punto de contacto de referencia eléctrica pasiva derecho. Combate las interferencias electromagnéticas del aire sirviendo de potencial basal.",
            localization = "Lóbulo de la oreja derecha o proceso mastoides derecho.",
            standardWaves = "Actividad electroencefalográfica pasiva residual.",
            waveType = WaveType.DELTA
        )
    )

    fun getElectrodeById(id: String): Electrode? {
        return electrodes.find { it.id == id }
    }
}

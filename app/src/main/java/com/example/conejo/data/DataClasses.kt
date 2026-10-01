package com.example.conejo.data

import java.time.DayOfWeek
import java.time.LocalDate

enum class Casa {
    PAPA,
    MAMA,
    ABUELOS,
    OTRO
}

data class ExcepcionDia(
    val fecha: LocalDate,
    val casa: Casa,
    val nota: String
)

data class ActividadRutina(
    val nombre: String,
    val horario: String,
    val diasSemana: List<DayOfWeek>
)

data class ItemMochila(
    val nombre: String,
    val debeEstar: Boolean, // true = cargar, false = sacar
    val descripcion: String? = null
)

data class InfoDia(
    val fecha: LocalDate,
    val casaCalculada: Casa,
    val esExcepcion: Boolean = false,
    val notaExcepcion: String? = null,
    val horarioEntrega: String? = null
)

data class ResumenRutina(
    val actividades: List<ActividadRutina>,
    val checklistMochila: List<ItemMochila>,
    val mensajeAlerta: String? = null
)

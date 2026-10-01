package com.example.conejo.logic

import com.example.conejo.data.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

object RutinaLogic {

    private val ANCLA_LIBRE_PAPA = LocalDate.of(2026, 9, 30)
    private val ANCLA_SEMANA_MAMA = LocalDate.of(2026, 9, 28)

    // Definición de Actividades Fijas
    private val ACTIVIDADES_FIJAS = listOf(
        ActividadRutina("Escuela", "13:00 a 17:00", listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)),
        ActividadRutina("Natación", "18:15 a 19:30", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
        ActividadRutina("Inglés", "17:30 a 18:45", listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY))
    )

    fun obtenerInfoDia(fecha: LocalDate, excepciones: List<ExcepcionDia>): InfoDia {
        val excepcion = excepciones.find { it.fecha == fecha }
        if (excepcion != null) {
            return InfoDia(
                fecha = fecha,
                casaCalculada = excepcion.casa,
                esExcepcion = true,
                notaExcepcion = excepcion.nota
            )
        }

        val diasDesdeAncla = ChronoUnit.DAYS.between(ANCLA_LIBRE_PAPA, fecha)
        val diaEnCiclo = ((diasDesdeAncla % 8) + 8) % 8
        val esLibrePapa = diaEnCiclo == 0L || diaEnCiclo == 1L
        val esUltimoDiaLibre = diaEnCiclo == 1L

        val lunesDeEstaSemana = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val semanasDesdeAncla = ChronoUnit.WEEKS.between(ANCLA_SEMANA_MAMA, lunesDeEstaSemana)
        val esFinSemanaAlternadoPapa = Math.abs(semanasDesdeAncla % 2) == 1L
        
        val esFinDeSemana = fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY

        val casa: Casa = when {
            esLibrePapa -> Casa.PAPA
            esFinDeSemana && esFinSemanaAlternadoPapa -> Casa.PAPA
            else -> Casa.MAMA
        }

        var horarioEntrega: String? = null
        if (esLibrePapa && esUltimoDiaLibre) {
            horarioEntrega = "22:00 hs (Papá la lleva - Fin de Libres)"
        }

        if (fecha.dayOfWeek == DayOfWeek.MONDAY) {
            val domingoAnterior = fecha.minusDays(1)
            val infoDomingo = determinarMotivoEstancia(domingoAnterior)
            
            if (infoDomingo.casa == Casa.PAPA) {
                horarioEntrega = if (infoDomingo.porLibres) {
                    "22:00 hs (Papá la lleva - Coincidió con Libres)"
                } else {
                    "Mamá la busca en Natación (Fin de semana normal)"
                }
            }
        }

        return InfoDia(fecha = fecha, casaCalculada = casa, horarioEntrega = horarioEntrega)
    }

    fun obtenerResumenRutina(fecha: LocalDate, excepciones: List<ExcepcionDia>): ResumenRutina {
        val infoHoy = obtenerInfoDia(fecha, excepciones)
        val infoAyer = obtenerInfoDia(fecha.minusDays(1), excepciones)
        
        // 1. Actividades del día
        val actividadesHoy = ACTIVIDADES_FIJAS.filter { it.diasSemana.contains(fecha.dayOfWeek) }

        // 2. Checklist Mochila
        val checklist = mutableListOf<ItemMochila>()
        val esNatacion = actividadesHoy.any { it.nombre == "Natación" }
        val esIngles = actividadesHoy.any { it.nombre == "Inglés" }

        if (esNatacion) {
            checklist.add(ItemMochila("Malla, Gorra y Antiparras", true, "¡Hoy hay natación!"))
            checklist.add(ItemMochila("Toalla", true))
        } else {
            // Si ayer hubo natación, recordar sacar las cosas
            val actividadesAyer = ACTIVIDADES_FIJAS.filter { it.diasSemana.contains(fecha.minusDays(1).dayOfWeek) }
            if (actividadesAyer.any { it.nombre == "Natación" }) {
                checklist.add(ItemMochila("Sacar Malla y Toalla usada", false, "Para que no pese y se seque"))
            }
        }

        if (esIngles) {
            checklist.add(ItemMochila("Libro de Inglés", true, "Hoy toca class"))
        } else {
            val actividadesAyer = ACTIVIDADES_FIJAS.filter { it.diasSemana.contains(fecha.minusDays(1).dayOfWeek) }
            if (actividadesAyer.any { it.nombre == "Inglés" }) {
                checklist.add(ItemMochila("Sacar Libro de Inglés", false, "Peso extra innecesario"))
            }
        }

        // 3. Alerta de Mudanza
        var mensajeAlerta: String? = null
        if (infoHoy.casaCalculada != infoAyer.casaCalculada) {
            mensajeAlerta = "¡Hoy cambias de casa! No olvides Celular y Cargador 📱🔌"
            checklist.add(ItemMochila("Celular y Cargador", true, "Día de mudanza a lo de ${infoHoy.casaCalculada}"))
        }

        return ResumenRutina(
            actividades = actividadesHoy,
            checklistMochila = checklist,
            mensajeAlerta = mensajeAlerta
        )
    }

    private data class Motivo(val casa: Casa, val porLibres: Boolean)
    
    private fun determinarMotivoEstancia(fecha: LocalDate): Motivo {
        val dias = ChronoUnit.DAYS.between(ANCLA_LIBRE_PAPA, fecha)
        val diaEnCiclo = ((dias % 8) + 8) % 8
        if (diaEnCiclo == 0L || diaEnCiclo == 1L) return Motivo(Casa.PAPA, true)
        
        val esFinde = fecha.dayOfWeek == DayOfWeek.SATURDAY || fecha.dayOfWeek == DayOfWeek.SUNDAY
        val lunes = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val semanas = ChronoUnit.WEEKS.between(ANCLA_SEMANA_MAMA, lunes)
        if (esFinde && Math.abs(semanas % 2) == 1L) return Motivo(Casa.PAPA, false)
        
        return Motivo(Casa.MAMA, false)
    }
}

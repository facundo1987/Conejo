package com.example.conejo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.conejo.data.*
import com.example.conejo.logic.RutinaLogic
import com.example.conejo.ui.theme.Turquoise40
import com.example.conejo.ui.theme.Brown40
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var excepciones by remember { mutableStateOf(listOf<ExcepcionDia>()) }
    var selectedDiaInfo by remember { mutableStateOf<InfoDia?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario Conejo", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LeyendaRow()
            
            Spacer(modifier = Modifier.height(16.dp))

            MonthSelector(
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CalendarGrid(
                month = currentMonth,
                excepciones = excepciones,
                onDiaClick = { selectedDiaInfo = it }
            )
        }

        selectedDiaInfo?.let { info ->
            val resumen = RutinaLogic.obtenerResumenRutina(info.fecha, excepciones)
            DiaDetailDialog(
                info = info,
                resumen = resumen,
                onDismiss = { selectedDiaInfo = null },
                onSaveExcepcion = { nuevaEx ->
                    excepciones = excepciones.filter { it.fecha != nuevaEx.fecha } + nuevaEx
                    selectedDiaInfo = null
                }
            )
        }
    }
}

@Composable
fun LeyendaRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LeyendaItem("Papá", Turquoise40.copy(alpha = 0.3f))
        LeyendaItem("Mamá", Brown40.copy(alpha = 0.2f))
        LeyendaItem("Excepción", Color.Transparent, border = true)
    }
}

@Composable
fun LeyendaItem(texto: String, color: Color, border: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
                .then(if (border) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun MonthSelector(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior")
        }
        Text(
            text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-ES"))
                .replaceFirstChar { it.uppercase() } + " " + currentMonth.year,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Siguiente mes")
        }
    }
}

@Composable
fun CalendarGrid(
    month: YearMonth,
    excepciones: List<ExcepcionDia>,
    onDiaClick: (InfoDia) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1).dayOfWeek.value
    
    val days = (1..daysInMonth).toList()
    val placeholders = (0 until (firstDayOfMonth - 1)).toList()

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "M", "J", "V", "S", "D").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(placeholders) { Box(modifier = Modifier.aspectRatio(1f)) }
            
            items(days) { day ->
                val fecha = month.atDay(day)
                val info = RutinaLogic.obtenerInfoDia(fecha, excepciones)
                
                DiaCard(info = info, onClick = { onDiaClick(info) })
            }
        }
    }
}

@Composable
fun DiaCard(info: InfoDia, onClick: () -> Unit) {
    val backgroundColor = when (info.casaCalculada) {
        Casa.PAPA -> Turquoise40.copy(alpha = 0.3f)
        Casa.MAMA -> Brown40.copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = if (info.esExcepcion) MaterialTheme.colorScheme.primary else Color.Transparent

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .then(if (info.esExcepcion) Modifier.border(2.dp, borderColor, RoundedCornerShape(12.dp)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = info.fecha.dayOfMonth.toString(),
                fontWeight = if (info.esExcepcion) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            )
            
            if (info.horarioEntrega != null || info.esExcepcion) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp).align(Alignment.TopEnd).padding(4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DiaDetailDialog(
    info: InfoDia,
    resumen: ResumenRutina,
    onDismiss: () -> Unit,
    onSaveExcepcion: (ExcepcionDia) -> Unit
) {
    var nota by remember { mutableStateOf(info.notaExcepcion ?: "") }
    var casaSeleccionada by remember { mutableStateOf(info.casaCalculada) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Detalle: ${info.fecha}") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Text("Dormir con: ${info.casaCalculada}", fontWeight = FontWeight.Bold)
                    if (info.horarioEntrega != null) {
                        Text(info.horarioEntrega, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    
                    if (resumen.mensajeAlerta != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(resumen.mensajeAlerta, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Actividades:", style = MaterialTheme.typography.titleSmall)
                }

                if (resumen.actividades.isEmpty()) {
                    item { Text("Sin actividades fijas", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                } else {
                    items(resumen.actividades) { actividad ->
                        Text("• ${actividad.nombre}: ${actividad.horario}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Mochila Checklist:", style = MaterialTheme.typography.titleSmall)
                }

                if (resumen.checklistMochila.isEmpty()) {
                    item { Text("Nada especial hoy", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                } else {
                    items(resumen.checklistMochila) { item ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (item.debeEstar) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (item.debeEstar) Color.Green else Color.Red,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(item.nombre, style = MaterialTheme.typography.bodyMedium)
                                if (item.descripcion != null) {
                                    Text(item.descripcion, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Cambiar casa (Excepción):", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Casa.entries.forEach { casa ->
                            FilterChip(
                                selected = casaSeleccionada == casa,
                                onClick = { casaSeleccionada = casa },
                                label = { Text(casa.name, fontSize = 10.sp) }
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = nota,
                        onValueChange = { nota = it },
                        label = { Text("Nota de la excepción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSaveExcepcion(ExcepcionDia(info.fecha, casaSeleccionada, nota))
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

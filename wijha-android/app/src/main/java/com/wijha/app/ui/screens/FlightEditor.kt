@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.wijha.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.wijha.app.data.AIRPORTS
import com.wijha.app.data.AIRPORT_BY_CODE
import com.wijha.app.data.Airport
import com.wijha.app.data.AppData
import com.wijha.app.data.COUNTRY_NAMES
import com.wijha.app.data.Flight
import com.wijha.app.data.FlightStatus
import com.wijha.app.data.MONTHS
import com.wijha.app.data.Store
import com.wijha.app.data.airlineName
import com.wijha.app.data.pad2
import com.wijha.app.data.flagEmoji
import com.wijha.app.data.tripLabelFor
import com.wijha.app.ui.BadFg
import com.wijha.app.ui.Ink
import com.wijha.app.ui.Mute
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun AirportPickerDialog(title: String, onPick: (Airport) -> Unit, onDismiss: () -> Unit) {
    var q by remember { mutableStateOf("") }
    val list = AIRPORTS.filter {
        q.isBlank() || it.city.contains(q.trim()) || it.code.contains(q.trim(), ignoreCase = true) ||
            it.name.contains(q.trim()) || (COUNTRY_NAMES[it.cc] ?: "").contains(q.trim())
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = Color.White) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = q, onValueChange = { q = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("ابحث بالمدينة أو الدولة أو الرمز") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                )
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(list, key = { it.code }) { a ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onPick(a) }.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(flagEmoji(a.cc), style = MaterialTheme.typography.titleLarge)
                            Column {
                                Text("${a.city} (${a.code})", style = MaterialTheme.typography.titleMedium)
                                Text(a.name, style = MaterialTheme.typography.bodySmall, color = Mute)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** نموذج إضافة رحلة أو تعديلها، كورقة سفلية أصلية. */
@Composable
fun FlightEditorSheet(
    data: AppData,
    flight: Flight?,
    presetTo: String?,
    onSave: (Flight) -> Unit,
    onDelete: (Flight) -> Unit,
    onDismiss: () -> Unit,
) {
    val ctx = LocalContext.current
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var no by remember { mutableStateOf(flight?.no ?: "") }
    var date by remember { mutableStateOf(flight?.let { LocalDate.parse(it.date) } ?: LocalDate.now().plusDays(14)) }
    var time by remember { mutableStateOf(flight?.let { LocalTime.parse(it.time.ifBlank { "09:00" }) } ?: LocalTime.of(9, 0)) }
    var from by remember { mutableStateOf(flight?.from ?: data.home) }
    var to by remember { mutableStateOf(flight?.to ?: presetTo.orEmpty()) }
    var status by remember { mutableStateOf(flight?.status ?: FlightStatus.ONTIME) }
    var delay by remember { mutableStateOf((flight?.delay ?: 0).toString()) }
    var seat by remember { mutableStateOf(flight?.seat ?: "") }
    var gate by remember { mutableStateOf(flight?.gate ?: "") }
    var term by remember { mutableStateOf(flight?.term ?: "") }
    var trip by remember { mutableStateOf(flight?.trip ?: "") }
    var error by remember { mutableStateOf("") }
    var picking by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val trips = data.flights.map { it.trip }.distinct()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheet, containerColor = Color.White) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (flight == null) "أضف رحلة" else "تفاصيل الرحلة", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(
                value = no, onValueChange = { no = it.uppercase().take(8) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                label = { Text("رقم الرحلة") }, placeholder = { Text("SV 1051") },
                supportingText = { val n = airlineName(no); if (n.isNotEmpty()) Text(n) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Ascii),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(ctx, { _, y, m, d -> date = LocalDate.of(y, m + 1, d) }, date.year, date.monthValue - 1, date.dayOfMonth).show()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.CalendarMonth, null)
                    Spacer(Modifier.padding(3.dp))
                    Text("${date.dayOfMonth} ${MONTHS[date.monthValue - 1]} ${date.year}")
                }
                OutlinedButton(
                    onClick = { TimePickerDialog(ctx, { _, h, m -> time = LocalTime.of(h, m) }, time.hour, time.minute, true).show() },
                    modifier = Modifier.weight(0.7f),
                ) {
                    Icon(Icons.Filled.Schedule, null)
                    Spacer(Modifier.padding(3.dp))
                    Text("${pad2(time.hour)}:${pad2(time.minute)}")
                }
            }
            AirportField("من", from) { picking = "from" }
            AirportField("إلى", to) { picking = "to" }
            Text("الحالة", style = MaterialTheme.typography.labelLarge, color = Mute)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(FlightStatus.ONTIME to "في الموعد", FlightStatus.DELAYED to "متأخرة", FlightStatus.CANCELLED to "ملغاة").forEach { (s, l) ->
                    FilterChip(selected = status == s, onClick = { status = s }, label = { Text(l) })
                }
            }
            if (status == FlightStatus.DELAYED) {
                OutlinedTextField(
                    value = delay, onValueChange = { delay = it.filter(Char::isDigit).take(4) }, singleLine = true,
                    label = { Text("مدة التأخير بالدقائق") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(seat, { seat = it.take(4) }, Modifier.weight(1f), singleLine = true, label = { Text("المقعد") })
                OutlinedTextField(gate, { gate = it.take(4) }, Modifier.weight(1f), singleLine = true, label = { Text("البوابة") })
                OutlinedTextField(term, { term = it.take(4) }, Modifier.weight(1f), singleLine = true, label = { Text("الصالة") })
            }
            Text("ضمن رحلة", style = MaterialTheme.typography.labelLarge, color = Mute)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = trip.isEmpty(), onClick = { trip = "" }, label = { Text("رحلة جديدة (تلقائي)") })
                trips.forEach { t -> FilterChip(selected = trip == t, onClick = { trip = t }, label = { Text(t) }) }
            }
            if (error.isNotEmpty()) Text(error, color = BadFg, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        val n = no.trim().uppercase().replace(Regex("^([A-Z0-9]{2})\\s*(\\d)"), "$1 $2")
                        when {
                            !Regex("^[A-Z0-9]{2} ?\\d{1,4}[A-Z]?$").matches(n) -> error = "اكتب رقم الرحلة بالشكل: رمز الشركة ثم الرقم، مثل SV 1051."
                            to.isEmpty() -> error = "اختر مطار الوصول."
                            from == to -> error = "مطار الوصول يجب أن يختلف عن مطار الإقلاع."
                            else -> {
                                val t = trip.ifEmpty { tripLabelFor(data, to, from, date.toString()) }
                                onSave(
                                    Flight(
                                        id = flight?.id ?: Store.newId("f"), no = n, date = date.toString(),
                                        time = "${pad2(time.hour)}:${pad2(time.minute)}", from = from, to = to, trip = t,
                                        status = status, delay = if (status == FlightStatus.DELAYED) delay.toIntOrNull() ?: 0 else 0,
                                        seat = seat.trim(), gate = gate.trim(), term = term.trim(),
                                    ),
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Ink),
                    modifier = Modifier.weight(1f),
                ) { Text(if (flight == null) "أضف الرحلة" else "احفظ التغييرات") }
                if (flight != null) TextButton(onClick = { confirmDelete = true }) { Text("حذف", color = BadFg) }
            }
        }
    }

    picking?.let { which ->
        AirportPickerDialog(if (which == "from") "مطار الإقلاع" else "مطار الوصول", { a ->
            if (which == "from") from = a.code else to = a.code
            picking = null
        }, { picking = null })
    }
    if (confirmDelete && flight != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف الرحلة؟") },
            text = { Text("ستُحذف الرحلة ${flight.no} نهائياً.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete(flight) }) { Text("حذف", color = BadFg) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("إلغاء") } },
        )
    }
}

@Composable
private fun AirportField(label: String, code: String, onClick: () -> Unit) {
    val a = AIRPORT_BY_CODE[code]
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(label, color = Mute)
            if (a == null) Text("اختر المطار", Modifier.weight(1f))
            else {
                Text("${flagEmoji(a.cc)} ${a.city} · ${a.name} (${a.code})", Modifier.weight(1f))
            }
        }
    }
}

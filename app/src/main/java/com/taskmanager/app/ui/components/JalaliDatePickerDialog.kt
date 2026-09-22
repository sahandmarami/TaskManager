package com.taskmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.taskmanager.app.calendar.HolidaysDataSource
import com.taskmanager.app.calendar.JalaliCalendar
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.ui.theme.extras

/**
 * Fully Persian (Jalali) date picker: month grid starting on شنبه,
 * correct month lengths, leap years and holiday marks.
 */
@Composable
fun JalaliDatePickerDialog(
    initial: JalaliCalendar.JalaliDate?,
    onDismiss: () -> Unit,
    onConfirm: (JalaliCalendar.JalaliDate) -> Unit,
) {
    val today = remember { PersianCalendar.todayJalali() }
    var viewYear by remember { mutableIntStateOf(initial?.year ?: today.year) }
    var viewMonth by remember { mutableIntStateOf(initial?.month ?: today.month) }
    var selected by remember {
        mutableStateOf(initial ?: JalaliCalendar.JalaliDate(today.year, today.month, today.day))
    }

    fun stepMonth(delta: Int) {
        var m = viewMonth + delta
        var y = viewYear
        if (m < 1) { m = 12; y -= 1 }
        if (m > 12) { m = 1; y += 1 }
        if (y in 1300..1500) {
            viewYear = y
            viewMonth = m
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            // Header: [prev arrow] [month-year + today] [next arrow]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // RTL: rightmost slot = previous month (arrow points right)
                IconButton(onClick = { stepMonth(-1) }) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight, contentDescription = "ماه قبل",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        PersianCalendar.formatMonthYear(viewYear, viewMonth),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(onClick = {
                        viewYear = today.year
                        viewMonth = today.month
                        selected = today
                    }) {
                        Text("امروز", style = MaterialTheme.typography.labelMedium)
                    }
                }
                IconButton(onClick = { stepMonth(1) }) {
                    Icon(
                        Icons.Filled.KeyboardArrowLeft, contentDescription = "ماه بعد",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Weekday header
            Row(modifier = Modifier.fillMaxWidth()) {
                PersianCalendar.shortWeekdayNames.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            val firstWeekIndex = PersianCalendar.persianWeekIndex(
                PersianCalendar.toLocalDate(JalaliCalendar.JalaliDate(viewYear, viewMonth, 1)).dayOfWeek
            )
            val monthLen = JalaliCalendar.monthLength(viewYear, viewMonth)

            repeat(6) { row ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { col ->
                        val index = row * 7 + col
                        val dayNumber = index - firstWeekIndex + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (dayNumber in 1..monthLen) {
                                val isToday = dayNumber == today.day && viewMonth == today.month && viewYear == today.year
                                val isSelected = dayNumber == selected.day && viewMonth == selected.month && viewYear == selected.year
                                val isHoliday = HolidaysDataSource.isOfficialHoliday(viewYear, viewMonth, dayNumber)
                                val hasOccasion = HolidaysDataSource.hasOccasion(viewYear, viewMonth, dayNumber)
                                val dayColor = when {
                                    isSelected -> Color.White
                                    isHoliday -> extras().danger
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            CircleShape
                                        )
                                        .then(
                                            if (isToday && !isSelected) {
                                                Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                            } else Modifier
                                        )
                                        .clickable {
                                            selected = JalaliCalendar.JalaliDate(viewYear, viewMonth, dayNumber)
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = dayNumber.toPersianDigits(),
                                        fontSize = 14.sp,
                                        color = dayColor,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    )
                                    if (hasOccasion) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 3.dp)
                                                .size(4.dp)
                                                .background(
                                                    if (isHoliday) extras().danger else extras().warning,
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onConfirm(selected) }) {
                    Text(
                        "تأیید",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

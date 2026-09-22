package com.taskmanager.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskmanager.app.calendar.HolidaysDataSource
import com.taskmanager.app.calendar.JalaliCalendar
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.ui.components.AppCard
import com.taskmanager.app.ui.components.EmptyState
import com.taskmanager.app.ui.components.SectionTitle
import com.taskmanager.app.ui.components.TaskCard
import com.taskmanager.app.ui.theme.extras

@Composable
fun CalendarScreen(
    container: AppContainer,
    onOpenTask: (Long) -> Unit,
    onAddTaskForDay: (Long) -> Unit,
) {
    val vm: CalendarViewModel = viewModel(factory = viewModelFactory {
        initializer { CalendarViewModel(container) }
    })
    val state by vm.state.collectAsState()
    val today = remember { PersianCalendar.todayJalali() }

    var viewYear by remember { mutableIntStateOf(state.selected.year) }
    var viewMonth by remember { mutableIntStateOf(state.selected.month) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        // Month header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = {
                    var m = viewMonth - 1
                    var y = viewYear
                    if (m < 1) { m = 12; y -= 1 }
                    if (y in 1300..1500) { viewMonth = m; viewYear = y }
                }) {
                    Icon(Icons.Filled.KeyboardArrowRight, "ماه قبل", tint = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        PersianCalendar.formatMonthYear(viewYear, viewMonth),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                    )
                    TextButton(onClick = {
                        viewYear = today.year
                        viewMonth = today.month
                        vm.select(today)
                    }) { Text("برو به امروز") }
                }
                IconButton(onClick = {
                    var m = viewMonth + 1
                    var y = viewYear
                    if (m > 12) { m = 1; y += 1 }
                    if (y in 1300..1500) { viewMonth = m; viewYear = y }
                }) {
                    Icon(Icons.Filled.KeyboardArrowLeft, "ماه بعد", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Calendar grid card
        item {
            AppCard {
                Column(Modifier.padding(12.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        PersianCalendar.shortWeekdayNames.forEach { d ->
                            Text(
                                d,
                                fontSize = 12.sp,
                                color = if (d == "ج") extras().danger else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    val firstWeekIndex = PersianCalendar.persianWeekIndex(
                        PersianCalendar.toLocalDate(JalaliCalendar.JalaliDate(viewYear, viewMonth, 1)).dayOfWeek
                    )
                    val monthLen = JalaliCalendar.monthLength(viewYear, viewMonth)
                    val sel = state.selected

                    repeat(6) { row ->
                        Row(Modifier.fillMaxWidth()) {
                            repeat(7) { col ->
                                val dayNumber = row * 7 + col - firstWeekIndex + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.15f)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (dayNumber in 1..monthLen) {
                                        val isToday = dayNumber == today.day && viewMonth == today.month && viewYear == today.year
                                        val isSelected = dayNumber == sel.day && viewMonth == sel.month && viewYear == sel.year
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
                                                    vm.select(JalaliCalendar.JalaliDate(viewYear, viewMonth, dayNumber))
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                dayNumber.toPersianDigits(),
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
                }
            }
        }

        // Occasions of selected day
        item {
            Column {
                SectionTitle(PersianCalendar.formatJalaliWithWeekday(state.selected))
                Spacer(Modifier.height(8.dp))
                if (state.occasions.isEmpty()) {
                    Text(
                        "مناسبت خاصی برای این روز ثبت نشده است",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.occasions.forEach { occ ->
                        AppCard(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            if (occ.isOfficialHoliday) extras().danger else extras().warning,
                                            CircleShape
                                        )
                                )
                                Spacer(Modifier.size(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        occ.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        if (occ.isOfficialHoliday) "تعطیل رسمی" else "مناسبت",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (occ.isOfficialHoliday) extras().danger else extras().warning,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tasks of selected day
        item {
            Column {
                Spacer(Modifier.height(8.dp))
                SectionTitle("وظایف این روز")
                Spacer(Modifier.height(8.dp))
                if (state.dayTasks.isEmpty()) {
                    Text(
                        "برای این روز وظیفه‌ای تعیین نشده است",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.dayTasks.forEach { data ->
                        TaskCard(
                            data = data,
                            onClick = { onOpenTask(data.item.task.id) },
                            onToggleComplete = {
                                vm.setCompleted(data.item.task.id, !data.item.task.isCompleted)
                            },
                            todayStartMillis = PersianCalendar.startOfDayMillis(PersianCalendar.todayJalali()),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                androidx.compose.material3.OutlinedButton(
                    onClick = {
                        onAddTaskForDay(
                            PersianCalendar.startOfDayMillis(
                                JalaliCalendar.JalaliDate(viewYear, viewMonth, state.selected.day)
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("افزودن وظیفه برای این روز")
                }
            }
        }

        item { Spacer(Modifier.height(90.dp)) }
    }
}

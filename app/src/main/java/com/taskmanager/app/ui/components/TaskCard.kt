package com.taskmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taskmanager.app.R
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.data.db.CategoryEntity
import com.taskmanager.app.data.db.TaskWithSubtasks
import com.taskmanager.app.domain.model.Priority
import com.taskmanager.app.ui.theme.CategoryColors
import com.taskmanager.app.ui.theme.extras

/** Task + its resolved category, shared across screens. */
data class TaskCardData(
    val item: TaskWithSubtasks,
    val category: CategoryEntity?,
)

/**
 * The task list card: checkbox, title, short description, date/time chips,
 * priority badge and category. Kept deliberately minimal (spec §21).
 */
@Composable
fun TaskCard(
    data: TaskCardData,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    todayStartMillis: Long,
) {
    val item = data.item
    val category = data.category
    val task = item.task
    val theme = extras()
    val isOverdue = !task.isCompleted && task.dueAtMillis != null && task.dueAtMillis!! < todayStartMillis

    AppCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            // Priority bar (leading edge in RTL)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(
                        if (task.priority >= Priority.HIGH.ordinal) {
                            priorityColor(Priority.entries[task.priority])
                        } else Color.Transparent,
                        MaterialTheme.shapes.small
                    )
            )
            Spacer(Modifier.width(10.dp))

            // Custom checkbox
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        if (task.isCompleted) theme.success else Color.Transparent,
                        CircleShape
                    )
                    .border(
                        2.dp,
                        if (task.isCompleted) theme.success else MaterialTheme.colorScheme.outline,
                        CircleShape
                    )
                    .clickable(onClick = onToggleComplete),
                contentAlignment = Alignment.Center,
            ) {
                if (task.isCompleted) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "انجام شد",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (task.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                val meta = mutableListOf<@Composable () -> Unit>()

                if (task.dueAtMillis != null) {
                    val dateText = PersianCalendar.formatJalali(PersianCalendar.fromMillis(task.dueAtMillis!!))
                    meta.add {
                        DotChip(
                            text = dateText,
                            icon = Icons.Filled.DateRange,
                            textColor = if (isOverdue) theme.danger else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (task.hasTime) {
                        meta.add {
                            DotChip(
                                text = PersianCalendar.formatTimeFromMillis(task.dueAtMillis!!),
                                icon = null,
                                textColor = if (isOverdue) theme.danger else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (task.priority >= Priority.HIGH.ordinal) {
                    meta.add { PriorityBadge(Priority.entries[task.priority]) }
                }
                if (category != null) {
                    meta.add {
                        DotChip(
                            text = category.name,
                            dotColor = CategoryColors[category.colorIndex % CategoryColors.size],
                        )
                    }
                }
                if (item.subtasks.isNotEmpty()) {
                    val done = item.subtasks.count { it.isDone }
                    meta.add {
                        DotChip(
                            text = "${done.toPersianDigits()}/${item.subtasks.size.toPersianDigits()}",
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (meta.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        meta.take(4).forEach { it() }
                    }
                }
            }
        }
    }
}

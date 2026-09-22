package com.taskmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskmanager.app.R
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.domain.model.Priority
import com.taskmanager.app.ui.theme.extras

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        content()
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier,
    )
}

@Composable
fun EmptyState(
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    title: String,
    subtitle: String? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(52.dp),
            )
        } else if (iconPainter != null) {
            Icon(
                iconPainter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(52.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "حذف",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text(confirmText, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
    Priority.MEDIUM -> MaterialTheme.colorScheme.primary
    Priority.HIGH -> extras().warning
    Priority.CRITICAL -> extras().danger
}

/** Small chip with a colored dot + label, used for categories and meta info. */
@Composable
fun DotChip(
    text: String,
    dotColor: Color? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (dotColor != null) {
            Box(
                Modifier
                    .size(7.dp)
                    .background(dotColor, CircleShape)
            )
        }
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(13.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val color = priorityColor(priority)
    Text(
        text = priority.label,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/** Simple stepper row: [-] value [+]. */
@Composable
fun StepperRow(
    label: String,
    valueText: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            androidx.compose.material3.FilledTonalButton(
                onClick = onDecrease,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp).width(40.dp),
            ) { Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Text(
                valueText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(min = 56.dp),
                textAlign = TextAlign.Center,
            )
            androidx.compose.material3.FilledTonalButton(
                onClick = onIncrease,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp).width(40.dp),
            ) { Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

fun formatMinutes(minutes: Int): String = "${minutes.toPersianDigits()} دقیقه"

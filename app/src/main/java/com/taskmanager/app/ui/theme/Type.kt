package com.taskmanager.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.taskmanager.app.R

/** Vazirmatn — modern, readable Persian font bundled offline. */
val Vazirmatn = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private val defaults = Typography()

val AppTypography = Typography(
    displayLarge = defaults.displayLarge.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 56.sp),
    displayMedium = defaults.displayMedium.copy(fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 44.sp),
    headlineLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 30.sp,
    ),
    // عنوان اصلی: 24sp
    headlineSmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Bold, fontSize = 24.sp,
    ),
    // عنوان بخش: 18sp
    titleLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium, fontSize = 14.sp,
    ),
    // متن اصلی: 16sp
    bodyLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal, fontSize = 16.sp,
    ),
    // متن ثانویه: 14sp
    bodyMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal, fontSize = 14.sp,
    ),
    // متن کوچک: 12sp
    bodySmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Normal, fontSize = 12.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium, fontSize = 14.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium, fontSize = 12.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Vazirmatn, fontWeight = FontWeight.Medium, fontSize = 11.sp,
    ),
)

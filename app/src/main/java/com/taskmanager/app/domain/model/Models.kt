package com.taskmanager.app.domain.model

/** Task priority levels (spec: کم / متوسط / زیاد / خیلی مهم). */
enum class Priority(val label: String, val level: Int) {
    LOW("کم", 0),
    MEDIUM("متوسط", 1),
    HIGH("زیاد", 2),
    CRITICAL("خیلی مهم", 3);

    companion object {
        fun fromOrdinal(o: Int): Priority = entries.firstOrNull { it.ordinal == o } ?: MEDIUM
    }
}

/** App theme mode. */
enum class ThemeMode(val label: String) {
    SYSTEM("سیستم"),
    LIGHT("روشن"),
    DARK("تاریک");

    companion object {
        fun fromOrdinal(o: Int): ThemeMode = entries.firstOrNull { it.ordinal == o } ?: SYSTEM
    }
}

/** Task list filters (spec section 27). */
enum class TaskFilter(val label: String) {
    ALL("همه"),
    TODAY("امروز"),
    UPCOMING("آینده"),
    OVERDUE("عقب‌افتاده"),
    COMPLETED("انجام‌شده"),
    NOT_COMPLETED("انجام‌نشده"),
    IMPORTANT("مهم"),
    NO_DATE("بدون تاریخ");
}

/** Default category names with a color index into [CategoryColors.palette]. */
object DefaultCategories {
    val list = listOf(
        "شخصی" to 0,
        "مدرسه" to 1,
        "کار" to 2,
        "مطالعه" to 3,
        "خرید" to 4,
        "پروژه" to 5,
        "سایر" to 6,
    )
}

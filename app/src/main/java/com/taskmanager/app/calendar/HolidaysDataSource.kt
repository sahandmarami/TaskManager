package com.taskmanager.app.calendar

/**
 * Offline dataset of Iranian official holidays and national occasions.
 *
 * - Solar (fixed Jalali) events are exact for every year.
 * - Lunar events are embedded per-year (1404 and 1405) based on the official
 *   Iranian calendar. To support future years, add a new map entry in
 *   [lunarByYear] — no other change is needed.
 */
object HolidaysDataSource {

    data class Occasion(val title: String, val isOfficialHoliday: Boolean)

    // key = (jalaliMonth, jalaliDay) — fixed solar events, valid for every year
    private val solarFixed: Map<Pair<Int, Int>, List<Occasion>> = mapOf(
        (1 to 1) to listOf(Occasion("جشن نوروز، آغاز سال نو", true)),
        (1 to 2) to listOf(Occasion("عیدنوروز", true)),
        (1 to 3) to listOf(Occasion("عیدنوروز", true)),
        (1 to 4) to listOf(Occasion("عیدنوروز", true)),
        (1 to 12) to listOf(Occasion("روز جمهوری اسلامی", true)),
        (1 to 13) to listOf(Occasion("روز طبیعت", true)),
        (1 to 19) to listOf(Occasion("روز هنرهای نمایشی", false)),
        (1 to 20) to listOf(Occasion("روز ملی فناوری هسته‌ای", false)),
        (1 to 25) to listOf(Occasion("روز بزرگداشت عطار نیشابوری", false)),
        (1 to 29) to listOf(Occasion("روز ارتش جمهوری اسلامی ایران", false)),
        (2 to 1) to listOf(Occasion("روز بزرگداشت سعدی", false)),
        (2 to 2) to listOf(Occasion("روز زمین پاک", false)),
        (2 to 3) to listOf(Occasion("روز بزرگداشت شیخ بهایی، روز آموزش فنی‌وحرفه‌ای", false)),
        (2 to 9) to listOf(Occasion("روز شوراها", false)),
        (2 to 10) to listOf(Occasion("روز ملی خلیج فارس", false)),
        (2 to 12) to listOf(Occasion("روز معلم", false)),
        (2 to 19) to listOf(Occasion("روز بزرگداشت شیخ صدوق", false)),
        (2 to 25) to listOf(Occasion("روز بزرگداشت فردوسی", false)),
        (2 to 27) to listOf(Occasion("روز ارتباطات و روابط عمومی", false)),
        (2 to 28) to listOf(Occasion("روز بزرگداشت حکیم نظامی", false)),
        (3 to 1) to listOf(Occasion("روز بهره‌وری", false)),
        (3 to 3) to listOf(Occasion("فتح خرمشهر، روز مقاومت و پیروزی", false)),
        (3 to 4) to listOf(Occasion("روز دزفول، روز مقاومت و پایداری", false)),
        (3 to 20) to listOf(Occasion("روز ملی گل و گیاه", false)),
        (3 to 25) to listOf(Occasion("روز بزرگداشت عمر خیام", false)),
        (3 to 27) to listOf(Occasion("روز جهانی ارتباطات", false)),
        (4 to 1) to listOf(Occasion("روز اصناف", false)),
        (4 to 12) to listOf(Occasion("روز ثبت احوال", false)),
        (4 to 14) to listOf(Occasion("روز قلم", false)),
        (4 to 18) to listOf(Occasion("روز ادبیات کودک و نوجوان", false)),
        (4 to 21) to listOf(Occasion("روز عفاف و حجاب", false)),
        (4 to 25) to listOf(Occasion("روز بهزیستی و تأمین اجتماعی", false)),
        (4 to 28) to listOf(Occasion("روز بزرگداشت علامه مجلسی", false)),
        (5 to 5) to listOf(Occasion("روز داروسازی، روز بزرگداشت محمد بن زکریای رازی", false)),
        (5 to 8) to listOf(Occasion("روز بزرگداشت شیخ شهاب‌الدین سهروردی", false)),
        (5 to 10) to listOf(Occasion("روز صنعت و معدن", false)),
        (5 to 14) to listOf(Occasion("روز حقوق شهروندی", false)),
        (5 to 17) to listOf(Occasion("روز خبرنگار", false)),
        (5 to 25) to listOf(Occasion("روز بزرگداشت عبدالقادر گیلانی", false)),
        (5 to 28) to listOf(Occasion("روز حمایت از خانواده", false)),
        (6 to 1) to listOf(Occasion("روز پزشک، روز بزرگداشت ابوعلی سینا", false)),
        (6 to 2) to listOf(Occasion("آغاز هفته دولت", false)),
        (6 to 8) to listOf(Occasion("روز مبارزه با تروریسم", false)),
        (6 to 11) to listOf(Occasion("روز صنعت چاپ", false)),
        (6 to 13) to listOf(Occasion("روز تعاون", false)),
        (6 to 17) to listOf(Occasion("روز قباله و ازدواج", false)),
        (6 to 21) to listOf(Occasion("روز سینما", false)),
        (6 to 27) to listOf(Occasion("روز شعر و ادب فارسی، بزرگداشت استاد شهریار", false)),
        (6 to 30) to listOf(Occasion("روز گفت‌وگوی تمدن‌ها", false)),
        (7 to 6) to listOf(Occasion("روز آتش‌نشانی و خدمات ایمنی", false)),
        (7 to 8) to listOf(Occasion("روز بزرگداشت مولوی", false)),
        (7 to 13) to listOf(Occasion("روز نیروی انتظامی", false)),
        (7 to 14) to listOf(Occasion("روز دامپزشکی", false)),
        (7 to 20) to listOf(Occasion("روز بزرگداشت حافظ", false)),
        (7 to 24) to listOf(Occasion("روز پیوند اولیا و مربیان", false)),
        (7 to 26) to listOf(Occasion("روز تربیت بدنی و ورزش", false)),
        (8 to 1) to listOf(Occasion("روز آمار و برنامه‌ریزی", false)),
        (8 to 13) to listOf(Occasion("روز دانش‌آموز", false)),
        (8 to 18) to listOf(Occasion("روز ملی مهرگان", false)),
        (8 to 24) to listOf(Occasion("روز کتاب، روز کتاب‌خوانی", false)),
        (9 to 7) to listOf(Occasion("روز نیروی دریایی", false)),
        (9 to 13) to listOf(Occasion("روز بیمه", false)),
        (9 to 16) to listOf(Occasion("روز دانشجو", false)),
        (9 to 26) to listOf(Occasion("روز حمل و نقل و رانندگان", false)),
        (9 to 30) to listOf(Occasion("شب یلدا، درازترین شب سال", false)),
        (10 to 5) to listOf(Occasion("روز ملی ایمنی برق", false)),
        (10 to 8) to listOf(Occasion("روز بزرگداشت امام محمد غزالی", false)),
        (10 to 9) to listOf(Occasion("روز بصیرت", false)),
        (10 to 12) to listOf(Occasion("روز اسناد ملی و هویت", false)),
        (10 to 20) to listOf(Occasion("روز بزرگداشت ابوریحان بیرونی", false)),
        (10 to 22) to listOf(Occasion("روز بزرگداشت ابونصر فارابی", false)),
        (11 to 3) to listOf(Occasion("روز ملی حمایت از حقوق مصرف‌کننده", false)),
        (11 to 12) to listOf(Occasion("بازگشت امام خمینی به ایران، آغاز دهه فجر", false)),
        (11 to 19) to listOf(Occasion("روز نیروی هوایی", false)),
        (11 to 22) to listOf(Occasion("پیروزی انقلاب اسلامی", true)),
        (11 to 29) to listOf(Occasion("روز نیروی دریایی ارتش", false)),
        (12 to 5) to listOf(Occasion("روز بزرگداشت خواجه نصیرالدین طوسی، روز مهندسی", false)),
        (12 to 15) to listOf(Occasion("روز درختکاری", false)),
        (12 to 25) to listOf(Occasion("روز بزرگداشت پروین اعتصامی", false)),
        (12 to 27) to listOf(Occasion("روز هوافضا", false)),
        (12 to 29) to listOf(Occasion("ملی شدن صنعت نفت ایران", true)),
    )

    // Lunar events per Jalali year — key = (jalaliMonth, jalaliDay)
    private val lunarByYear: Map<Int, Map<Pair<Int, Int>, List<Occasion>>> = mapOf(
        1404 to mapOf(
            (1 to 2) to listOf(Occasion("شهادت حضرت علی (ع)", true)),
            (1 to 10) to listOf(Occasion("عید سعید فطر", true)),
            (1 to 11) to listOf(Occasion("تعطیل به مناسبت عید سعید فطر", true)),
            (3 to 17) to listOf(Occasion("عید سعید قربان", true)),
            (3 to 25) to listOf(Occasion("عید سعید غدیر خم", true)),
            (4 to 13) to listOf(Occasion("تاسوعای حسینی", true)),
            (4 to 14) to listOf(Occasion("عاشورای حسینی", true)),
            (5 to 23) to listOf(Occasion("اربعین حسینی", true)),
            (5 to 31) to listOf(Occasion("رحلت رسول اکرم (ص)، شهادت امام حسن مجتبی (ع)", true)),
            (6 to 1) to listOf(Occasion("شهادت امام رضا (ع)", true)),
            (6 to 19) to listOf(Occasion("ولادت رسول اکرم (ص) و امام جعفر صادق (ع)", true)),
            (9 to 15) to listOf(Occasion("شهادت حضرت فاطمه زهرا (س)، روز زن و روز مادر", true)),
            (10 to 12) to listOf(Occasion("ولادت امام علی (ع)، روز پدر", true)),
            (10 to 26) to listOf(Occasion("مبعث رسول اکرم (ص)", true)),
            (11 to 10) to listOf(Occasion("ولادت امام مهدی (عج)، جشن نیمه شعبان", true)),
            (12 to 20) to listOf(Occasion("شهادت حضرت علی (ع)", true)),
            (12 to 29) to listOf(Occasion("عید سعید فطر", true)),
        ),
        1405 to mapOf(
            (1 to 1) to listOf(Occasion("عید سعید فطر", true)),
            (3 to 7) to listOf(Occasion("عید سعید قربان", true)),
            (3 to 15) to listOf(Occasion("عید سعید غدیر خم", true)),
            (4 to 3) to listOf(Occasion("تاسوعای حسینی", true)),
            (4 to 4) to listOf(Occasion("عاشورای حسینی", true)),
            (5 to 13) to listOf(Occasion("اربعین حسینی", true)),
            (5 to 21) to listOf(Occasion("رحلت رسول اکرم (ص)، شهادت امام حسن مجتبی (ع)", true)),
            (5 to 22) to listOf(Occasion("شهادت امام رضا (ع)", true)),
            (6 to 10) to listOf(Occasion("ولادت رسول اکرم (ص) و امام جعفر صادق (ع)", true)),
            (9 to 5) to listOf(Occasion("شهادت حضرت فاطمه زهرا (س)، روز زن و روز مادر", true)),
            (10 to 1) to listOf(Occasion("ولادت امام علی (ع)، روز پدر", true)),
            (10 to 15) to listOf(Occasion("مبعث رسول اکرم (ص)", true)),
            (11 to 9) to listOf(Occasion("ولادت امام مهدی (عج)، جشن نیمه شعبان", true)),
            (12 to 10) to listOf(Occasion("شهادت حضرت علی (ع)", true)),
            (12 to 20) to listOf(Occasion("عید سعید فطر", true)),
            (12 to 21) to listOf(Occasion("تعطیل به مناسبت عید سعید فطر", true)),
        ),
    )

    fun occasionsFor(jy: Int, jm: Int, jd: Int): List<Occasion> {
        val solar = solarFixed[jm to jd].orEmpty()
        val lunar = lunarByYear[jy]?.get(jm to jd).orEmpty()
        return solar + lunar
    }

    fun isOfficialHoliday(jy: Int, jm: Int, jd: Int): Boolean =
        occasionsFor(jy, jm, jd).any { it.isOfficialHoliday }

    fun hasOccasion(jy: Int, jm: Int, jd: Int): Boolean =
        occasionsFor(jy, jm, jd).isNotEmpty()
}

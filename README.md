# Task Manager 📝

برنامه مدیریت وظایف و برنامه‌ریزی شخصی — کاملاً آفلاین، فارسی و راست‌چین.

ساخته‌شده با **Kotlin + Jetpack Compose + Material 3** برای Android 8.0 به بالا (minSdk 26، targetSdk 35).

## ✨ امکانات

- **وظایف**: ایجاد، ویرایش، تکمیل، حذف، زیرکار، دسته‌بندی و ۴ سطح اولویت (کم/متوسط/زیاد/خیلی مهم)
- **تاریخ و ساعت اختیاری**: وظیفه می‌تواند فقط عنوان داشته باشد؛ تاریخ و ساعت کاملاً اختیاری است
- **آلارم واقعی اندروید**: با `AlarmManager.setExactAndAllowWhileIdle` — کارکرد حتی وقتی برنامه بسته است، پخش صدا و ویبره، صفحه آلارم تمام‌صفحه (Full-Screen Intent)، Snooze با ۵/۱۰/۱۵/۳۰ دقیقه، مجوزها به‌درستی مدیریت می‌شوند (POST_NOTIFICATIONS، SCHEDULE_EXACT_ALARM)
- **باز زمان‌بندی پس از Restart گوشی** با `BOOT_COMPLETED`
- **تقویم شمسی دقیق**: الگوریتم استاندارد جلالی (سال کبیسه، اسفند ۲۹/۳۰ روزه، تغییر ماه و سال) با تست کامل
- **تعطیلات و مناسبت‌های رسمی ایران**: دیتای داخلی (مناسبت‌های ثابت شمسی برای همه سال‌ها + مناسبت‌های قمری سال‌های ۱۴۰۴ و ۱۴۰۵)
- **اهداف**: با مراحل، پیشرفت درصدی و اتصال وظایف به هدف
- **Focus Mode**: تایمر پومودورو (۲۵ دقیقه تمرکز / ۵ دقیقه استراحت، قابل تنظیم) با اتصال مستقیم به وظایف و اهداف
- **جستجو و فیلتر**: امروز، آینده، عقب‌افتاده، انجام‌شده، انجام‌نشده، مهم، بدون تاریخ
- **Dark Mode کامل** با رنگ‌های طراحی‌شده مستقل
- **Backup / Restore**: خروجی و ورودی کامل اطلاعات در قالب JSON (وظایف، اهداف، دسته‌بندی‌ها، تنظیمات)

## 🏗 معماری

```
app/src/main/java/com/taskmanager/app/
├── calendar/        # موتور تقویم جلالی + مناسبت‌ها (خالص و بدون وابستگی UI)
├── data/
│   ├── db/          # Room: Entities, DAOs, Database
│   ├── repository/  # Repositories + BackupManager
│   └── settings/    # DataStore (تم، صدا، ویبره، مدت تمرکز)
├── alarm/           # AlarmScheduler, AlarmReceiver, AlarmActivity, NotificationHelper, BootReceiver
├── focus/           # موتور تایمر تمرکز (Pomodoro)
├── domain/model/    # مدل‌های دامنه (Priority, ThemeMode, TaskFilter)
├── di/              # AppContainer (تزریق وابستگی دستی)
└── ui/              # Compose: theme, navigation, screens (home/tasks/taskedit/calendar/goals/focus/settings)
```

- لایه داده (Room/DataStore) از UI جدا است؛ ارتباط از طریق ViewModel و Repository
- مدیریت وضعیت UI با StateFlow و Compose
- برنامه کاملاً Offline-First است؛ هیچ سرور یا API خارجی در کار نیست

## 🔔 نکات فنی آلارم

| نسخه اندروید | رفتار |
|---|---|
| ۸ تا ۱۱ (API 26–30) | `setExactAndAllowWhileIdle` بدون نیاز به مجوز اضافه |
| ۱۲ به بالا (API 31+) | اگر مجوز «آلارم و یادآوری» داده نشده باشد، برنامه کاربر را به تنظیمات سیستم هدایت می‌کند؛ در غیر این صورت آلارم دقیق تنظیم می‌شود |
| ۱۳ به بالا (API 33+) | مجوز `POST_NOTIFICATIONS` در اولین اجرا درخواست می‌شود |
| همه نسخه‌ها | کانال اعلان «یادآوری وظایف» با اهمیت بالا (صدا + ویبره + Heads-Up) |

## 📅 دیتاست مناسبت‌ها

مناسبت‌های ثابت شمسی (نوروز، روز جمهوری اسلامی، ۱۴ و ۱۵ خرداد، ۲۲ بهمن، ۲۹ اسفند و…) برای همه سال‌ها دقیق است. مناسبت‌های قمری برای سال‌های **۱۴۰۴** و **۱۴۰۵** بر اساس تقویم رسمی در `HolidaysDataSource.kt` تعبیه شده‌اند. برای سال‌های بعد کافی است یک ورودی جدید به `lunarByYear` اضافه کنید.

## 🔨 ساخت (Build)

1. پروژه را با **Android Studio** باز کنید (JDK 17+ لازم است؛ اندروید استودیو خودش دارد).
2. Sync بگیرید — همه وابستگی‌ها به‌صورت خودکار از Maven دانلود می‌شوند.
3. اجرا: `Run > app` یا از خط فرمان:

```bash
./gradlew assembleDebug     # APK دیباگ
./gradlew assembleRelease   # APK منتشرشونده (در صورت داشتن keystore در keystore.properties)
```

خروجی در `app/build/outputs/apk/` ذخیره می‌شود.

### فونت
فونت [Vazirmatn](https://github.com/rastikerdar/vazirmatn) (مجوز OFL) در `res/font` قرار دارد.

### امضای Release
فایل `keystore.properties` با این ساختار بسازید (در .gitignore است):

```
storeFile=release.keystore
storePassword=****
keyAlias=****
keyPassword=****
```

اگر موجود نباشد، Release با کلید Debug امضا می‌شود (قابل نصب ولی مناسب انتشار نیست).

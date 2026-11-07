# كتالوج الويدجت الإسلامية المعتمدة - تطبيق نور المسلم
## دليل التثبيت والتنسيق الفني (المرحلة الثامنة)

---

### 1. نظرة عامة على الإصلاح الجذري
في هذا التحديث، تم حل المشاكل الثلاث التي ظهرت في الشاشة الرئيسية وقائمة الويدجت:
1. **توحيد المسميات وغياب المعاينة:** تم القضاء على ظهور جميع الويدجت باسم افتراضي مكرر (`نور المسلم 2X1`) مع أيقونة التطبيق داخل مستطيل أبيض، وذلك بإضافة `android:label` و`android:description` مستقل باللغة العربية لكل ويدجت، وتوفير معاينات متجهة دقيقة `previewImage` تحاكي التصميم الحقيقي، وضبط أبعاد الخلايا المستهدفة (`targetCellWidth`, `targetCellHeight`).
2. **التكرار العشوائي:** تم شطب 29 ويدجت مكررة من `AndroidManifest.xml` واختزال القائمة إلى **15 ويدجت متميزة وفريدة** تغطي كافة احتياجات المسلم، مع الإبقاء على التوافق البرمجي عبر `typealias`.
3. **خطأ "Couldn't add widget":** إزالة شاشة الإعداد الإجبارية غير المطلوبة لتمكين الإضافة الفورية بنقرة واحدة أو بالسحب والإفلات، وإرجاع `RemoteViews` متزامنة وسريعة فور استدعاء `onUpdate` لحماية الويدجت من مهلة نظام أندرويد (Timeout).

---

### 2. جدول الويدجت الـ 15 المعتمدة بالتفصيل

| # | العائلة | اسم الويدجت | فئة المزود (Provider) | ملف التكوين (XML Info) | الأبعاد المستهدفة | ملف التخطيط (Layout) | المعاينة (Preview) | الوظيفة والتفاعل |
|---|---|---|---|---|---|---|---|---|
| **1** | مواقيت الصلاة | الصلاة القادمة والعد التنازلي | `NextPrayerWidgetProvider` | `widget_prayer_next_info.xml` | **3 × 2** (250×110 dp) | `widget_prayer_next.xml` | `widget_preview_prayer_next.xml` | اسم الصلاة القادمة، وقتها، العداد التنازلي الحي بالثواني، والمدينة والتاريخ الهجري. النقر يفتح شاشة الصلاة. |
| **2** | مواقيت الصلاة | مواقيت الصلاة اليومية | `PrayerTimesDailyWidgetProvider` | `widget_prayer_daily_info.xml` | **4 × 2** (300×110 dp) | `widget_prayer_daily.xml` | `widget_preview_prayer_daily.xml` | جدول كامل للصلوات الخمس (الفجر، الظهر، العصر، المغرب، العشاء) مع تمييز الصلاة القادمة. |
| **3** | مواقيت الصلاة | مواقيت الصلاة (مصغر) | `PrayerMiniWidgetProvider` | `widget_prayer_mini_info.xml` | **2 × 1** (150×50 dp) | `widget_prayer_mini.xml` | `widget_preview_prayer_mini.xml` | شريط أفقي فائق النحافة يعرض اسم الصلاة ووقتها، مثالي للشاشات المزدحمة. |
| **4** | مواقيت الصلاة | الفجر والشروق | `PrayerPairWidgetProvider` | `widget_prayer_pair_info.xml` | **3 × 1** (250×60 dp) | `widget_prayer_pair.xml` | `widget_preview_prayer_pair.xml` | عرض ثنائي لموعد أذان الفجر ووقت الشروق للمساعدة في ضبط قيام الليل والاستيقاظ. |
| **5** | مواقيت الصلاة | إمساكية رمضان والإفطار | `RamadanPrayerWidgetProvider` | `widget_ramadan_prayer_info.xml` | **3 × 2** (250×110 dp) | `widget_prayer_pair.xml` | `widget_preview_ramadan.xml` | بطاقة خاصة بوقت الإمساك ووقت الإفطار ومواقيت السحور خلال شهر رمضان المبارك وصيام النوافل. |
| **6** | القرآن الكريم | آية اليوم وتدبر | `AyahOfTheDayWidgetProvider` | `widget_quran_ayah_info.xml` | **4 × 2** (320×120 dp) | `widget_quran_ayah.xml` | `widget_preview_quran_ayah.xml` | آية قرآنية يومية مختارة بالرسم العثماني مع اسم السورة ورقم الآية وتوجيه للتدبر والعمل. |
| **7** | القرآن الكريم | متابعة قراءة القرآن | `LastReadingWidgetProvider` | `widget_quran_last_read_info.xml` | **3 × 2** (250×110 dp) | `widget_quran_last_read.xml` | `widget_preview_quran_last_read.xml` | موضع آخر قراءة توقف عندها المستخدم (اسم السورة، رقم الآية، والصفحة) مع زر «متابعة ⬅» فوري. |
| **8** | القرآن الكريم | تقدم ختمة القرآن | `QuranProgressWidgetProvider` | `widget_quran_khatmah_info.xml` | **3 × 2** (250×110 dp) | `widget_quran_khatmah.xml` | `widget_preview_quran_khatmah.xml` | شريط تقدم مرئي للختمة القرآنية النشطة، نسبة الإنجاز المئوية، والمتبقي من الورد اليومي. |
| **9** | القرآن الكريم | مشغل القرآن الكريم | `QuranPlayerWidgetProvider` | `widget_quran_player_info.xml` | **3 × 2** (250×110 dp) | `widget_quran_player.xml` | `widget_preview_quran_player.xml` | ويدجت صوتي مع زر تشغيل/إيقاف مؤقت (▶ / ⏸) واسم السورة والقارئ الحالي مع دعم التحكم بالخلفية. |
| **10** | الأذكار والتسبيح | ذكر اليوم المأثور | `DailyDhikrWidgetProvider` | `widget_adhkar_card_info.xml` | **3 × 2** (250×110 dp) | `widget_adhkar_card.xml` | `widget_preview_adhkar_card.xml` | ذكر أو دعاء مأثور متجدد يومياً من حصن المسلم مع بيان الفضل والثواب الشرعي. |
| **11** | الأذكار والتسبيح | السبحة الإلكترونية التفاعلية | `TasbeehCounterWidgetProvider` | `widget_adhkar_tasbeeh_info.xml` | **3 × 2** (250×110 dp) | `widget_adhkar_tasbeeh.xml` | `widget_preview_adhkar_tasbeeh.xml` | سبحة تفاعلية تعمل مباشرة من الشاشة الرئيسية مع زر للعد النبضي وزر تصفير سريع. |
| **12** | القبلة والأدوات | اتجاه القبلة والمسافة | `QiblaWidgetProvider` | `widget_qibla_info.xml` | **3 × 2** (250×110 dp) | `widget_quran_player.xml` / `widget_qibla.xml` | `widget_preview_qibla.xml` | زاوية انحراف القبلة الحسابية الدقيقة نحو الكعبة المشرفة مع المسافة المباشرة بالكيلومتر. |
| **13** | القبلة والأدوات | التاريخ والتقويم الهجري | `HijriDateWidgetProvider` | `widget_date_hijri_info.xml` | **3 × 1** (250×60 dp) | `widget_date_hijri.xml` | `widget_preview_date_hijri.xml` | اليوم والتاريخ الهجري المعتمد وتاريخ التقويم الميلادي بتنسيق إسلامي ذهبي أنيق. |
| **14** | القبلة والأدوات | خطة العبادة والزاد اليومي | `DailyWorshipPlanWidgetProvider` | `widget_summary_info.xml` | **4 × 2** (300×110 dp) | `widget_summary.xml` | `widget_preview_summary.xml` | ملخص الصلوات المكتملة (X / 5)، مجموع التسبيحات اليومية، وعدد صفحات القرآن المقروءة. |
| **15** | القبلة والأدوات | اختصارات نور المسلم | `QuickShortcutsWidgetProvider` | `widget_shortcuts_info.xml` | **4 × 1** (300×60 dp) | `widget_shortcuts.xml` | `widget_preview_shortcuts.xml` | صف أزرار فائق السرعة للوصول إلى: (المصحف الشريف، الأذكار، مواقيت الصلاة، والقبلة). |

---

### 3. سجل شطب المكررات (De-duplication Log)
تم إلغاء تسجيل الفئات التالية من ملف `AndroidManifest.xml` وحذف تكرارها، مع إبقائها كـ `typealias` داخلياً لضمان عدم تعطل أي كود سابق:

1. `PrayerCountdownWidgetProvider` ➔ تم دمجه مع `NextPrayerWidgetProvider`
2. `PrayerFajrCountdownWidgetProvider` ➔ تم دمجه مع `NextPrayerWidgetProvider`
3. `PrayerHighlightCurrentWidgetProvider` ➔ تم دمجه مع `NextPrayerWidgetProvider`
4. `PrayerTomorrowWidgetProvider` ➔ تم دمجه مع `PrayerTimesDailyWidgetProvider`
5. `PrayerAdhanToggleWidgetProvider` ➔ تم دمجه مع `PrayerMiniWidgetProvider`
6. `PrayerFajrSunriseWidgetProvider` ➔ تم دمجه مع `PrayerPairWidgetProvider`
7. `PrayerDhuhrAsrWidgetProvider` ➔ تم دمجه مع `PrayerPairWidgetProvider`
8. `PrayerMaghribIshaWidgetProvider` ➔ تم دمجه مع `PrayerPairWidgetProvider`
9. `PrayerWeeklyTableWidgetProvider` ➔ تم استبداله بـ `PrayerTimesDailyWidgetProvider`
10. `PrayerNextTwoWidgetProvider` ➔ تم دمجه مع `NextPrayerWidgetProvider`
11. `QuranLargeAyahWidgetProvider` ➔ تم دمجه مع `AyahOfTheDayWidgetProvider`
12. `QuranResumeWidgetProvider` ➔ تم دمجه مع `LastReadingWidgetProvider`
13. `QuranDailyWirdWidgetProvider` ➔ تم دمجه مع `QuranProgressWidgetProvider`
14. `QuranWirdRemainingWidgetProvider` ➔ تم دمجه مع `QuranProgressWidgetProvider`
15. `QuranSurahOfTheWeekWidgetProvider` ➔ تم دمجه مع `AyahOfTheDayWidgetProvider`
16. `QuranJuzTrackerWidgetProvider` ➔ تم دمجه مع `QuranProgressWidgetProvider`
17. `QuranBookmarkQuickWidgetProvider` ➔ تم دمجه مع `LastReadingWidgetProvider`
18. `QuranAyahComfortWidgetProvider` ➔ تم دمجه مع `AyahOfTheDayWidgetProvider`
19. `QuranTafseerSummaryWidgetProvider` ➔ تم دمجه مع `AyahOfTheDayWidgetProvider`
20. `QuranHifzTargetWidgetProvider` ➔ تم دمجه مع `QuranProgressWidgetProvider`
21. `QuranReciterShortcutsWidgetProvider` ➔ تم دمجه مع `QuranPlayerWidgetProvider`
22. `QuranMiniPlayerWidgetProvider` ➔ تم دمجه مع `QuranPlayerWidgetProvider`
23. `QuranCurrentSurahWidgetProvider` ➔ تم دمجه مع `QuranPlayerWidgetProvider`
24. `DhikrMorningWidgetProvider` ➔ تم دمجه مع `DailyDhikrWidgetProvider`
25. `DhikrEveningWidgetProvider` ➔ تم دمجه مع `DailyDhikrWidgetProvider`
26. `DhikrAfterPrayerWidgetProvider` ➔ تم دمجه مع `DailyDhikrWidgetProvider`
27. `TasbeehLargeCounterWidgetProvider` ➔ تم دمجه مع `TasbeehCounterWidgetProvider`
28. `CombinedDateWidgetProvider` ➔ تم دمجه مع `HijriDateWidgetProvider`
29. `TodaySummaryWidgetProvider` ➔ تم دمجه مع `DailyWorshipPlanWidgetProvider`

---

### 4. أسباب وحلول خطأ "Couldn't add widget"
* **السبب الأول:** كانت ملفات `widget_*_info.xml` تحتوي على سمة `android:configure` التي توجه أندرويد لفتح نافذة إعداد قبل تثبيت الويدجت. إذا لم تنتهِ النافذة بنجاح أو لم ترسل نتيجة `RESULT_OK` متزامنة مع معرف الويدجت `EXTRA_APPWIDGET_ID`، يقوم المشغل فوراً بإلغاء الإضافة وعرض خطأ `Couldn't add widget.`.
  * **الحل:** تم إزالة `android:configure` من جميع الويدجت المستقلة، فأصبح بإمكان المستخدم وضعها مباشرة على الشاشة دون أي خطوات إضافية.
* **السبب الثاني:** كان تحديث الويدجت يتم بالكامل عبر مهام غير متزامنة (Background Coroutines) داخل `Dispatchers.IO`، مما كان يتسبب في تأخر استجابة `RemoteViews` عن المهلة التي يحددها المشغل.
  * **الحل:** تنفيذ دالة `getInitialFastViews` المتزامنة داخل `WidgetUpdateCoordinator`، والتي تمد المشغل فوراً بواجهة أولية مطابقة خلال أجزاء من الثانية، ثم تقوم الكوروتين بتحديث المحتوى بالبيانات الحية المأخوذة من قاعدة البيانات ومواقيت الصلاة.

---

### 5. تعليمات التجربة والمعاينة للمستخدم
1. في حالة تثبيت التطبيق وتحديثه، قد يحتفظ مشغل أندرويد (Launcher) بنسخة مخبأة (Cache) من الويدجت القديمة.
2. لإظهار التحديث بالشكل المثالي:
   * اسحب أي ويدجت قديمة متعطلة إلى سلة الحذف (Remove).
   * افتح قائمة الويدجت من جديد، وستجد الآن **15 ويدجت إسلامية أنيقة ومصنفة** بأسمائها العربية الواضحة وأحجامها المتنوعة (2×1، 3×1، 3×2، 4×1، 4×2) ومعايناتها الجذابة.

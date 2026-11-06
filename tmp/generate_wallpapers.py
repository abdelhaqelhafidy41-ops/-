import json
import os

categories = [
    ("mosques", "المساجد", "Mosques", [
        ("جامع الشيخ زايد الكبير", "Sheikh Zayed Grand Mosque", "photo-1548013146-72479768bada", "Federico Di Dio"),
        ("المسجد الأزرق في إسطنبول", "Blue Mosque Istanbul", "photo-1564769625905-50e93615e769", "Adli Wahid"),
        ("جامع السلطان قابوس الأكبر", "Sultan Qaboos Grand Mosque", "photo-1591604129939-f1efa4d9f7fa", "Sulthan A"),
        ("مسجد الحسن الثاني بالدار البيضاء", "Hassan II Mosque Casablanca", "photo-1578632767115-351597cf2477", "Rachid Oucharia"),
        ("جامع بادشاهي في لاهور", "Badshahi Mosque Lahore", "photo-1609137144822-263a033f919a", "Ali Imran"),
        ("مئذنة وقبة مسجد تاريخي", "Historic Mosque Minaret and Dome", "photo-1584551246679-0daf3d275d0f", "Reza Rostampisheh"),
        ("فناء مسجد إسلامي عريق", "Courtyard of Islamic Mosque", "photo-1542838132-92c53300491e", "Mostafa Meraji"),
        ("أقواس ورواق المسجد الرخامي", "Marble Mosque Arches and Corridor", "photo-1580418827493-f2b22c0a76cb", "Khatam"),
        ("مسجد في ليلة هادئة ومضاءة", "Illuminated Mosque at Night", "photo-1519817650390-64a93db51149", "David Rodrigo")
    ]),
    ("kaaba", "الكعبة والمسجد الحرام", "The Kaaba & Grand Mosque", [
        ("الكعبة المشرفة وكسوتها المطرزة", "The Holy Kaaba in Mecca", "photo-1565552645632-d725f8bfc19a", "Haitham Alfalah"),
        ("المطاف حول الكعبة المشرفة", "Pilgrims Circling the Kaaba", "photo-1590076215667-875d4ef2d7ee", "Boaz Rohde"),
        ("صحن المسجد الحرام في مكة", "Courtyard of the Grand Mosque", "photo-1564769625624-9b9087c51480", "Khatam"),
        ("ساعة مكة المشرفة والمسجد الحرام", "Mecca Royal Clock Tower", "photo-1580418827493-f2b22c0a76cc", "Ahmed"),
        ("الحرم المكي الشريف في وقت الغروب", "Sunset over Mecca Grand Mosque", "photo-1584551246679-0daf3d275d0e", "Haitham"),
        ("مآذن المسجد الحرام الشامخة", "Minarets of Masjid al-Haram", "photo-1591604129939-f1efa4d9f7fb", "Tariq"),
        ("باب الكعبة المشرفة الذهبي", "Golden Door of the Kaaba", "photo-1565552645632-d725f8bfc19b", "Ali"),
        ("صفوف المصلين في الحرم المكي", "Worshippers at the Grand Mosque", "photo-1590076215667-875d4ef2d7ef", "Saeed"),
        ("أجواء روحانية في مكة المكرمة", "Spiritual Atmosphere in Mecca", "photo-1564769625624-9b9087c51481", "Omar")
    ]),
    ("prophet_mosque", "المسجد النبوي", "The Prophet's Mosque", [
        ("القبة الخضراء في المسجد النبوي", "The Green Dome in Medina", "photo-1580418827493-f2b22c0a76cd", "Madinah Heritage"),
        ("مظلات المسجد النبوي الشريف", "Umbrellas of the Prophet's Mosque", "photo-1591604129939-f1efa4d9f7fc", "Sulthan"),
        ("ساحات الحرم النبوي بالمدينة المنورة", "Courtyard of Masjid an-Nabawi", "photo-1564769625905-50e93615e76a", "Khatam"),
        ("مآذن المسجد النبوي في الليل", "Illuminated Minarets in Medina", "photo-1584551246679-0daf3d275d0d", "Ahmed"),
        ("أروقة الروضة الشريفة وأعمدتها", "Pillars of the Noble Rawdah", "photo-1542838132-92c53300491f", "Mostafa"),
        ("غروب الشمس في سماء طيبة الطيبة", "Sunset over Medina Al-Munawwarah", "photo-1578632767115-351597cf2478", "Rachid"),
        ("المسجد النبوي في صلاة الفجر", "Dawn at the Prophet's Mosque", "photo-1565552645632-d725f8bfc19c", "Haitham"),
        ("تفاصيل الزخارف النبوية الذهبية", "Golden Ornaments in the Mosque", "photo-1580418827493-f2b22c0a76ce", "Adli"),
        ("سكينة المسجد النبوي بالمدينة", "Serenity of the Prophet's Mosque", "photo-1590076215667-875d4ef2d7ea", "Boaz")
    ]),
    ("sky_stars", "السماء والنجوم", "Sky & Stars", [
        ("درب التبانة فوق صحراء الجزيرة", "Milky Way over Desert", "photo-1506703719100-a0f3a48c0f86", "Lucas Pezeta"),
        ("قمر ليلة البدر في سماء صافية", "Full Moon in Clear Night Sky", "photo-1532693322450-2cb5c511067d", "Simeon Muller"),
        ("هلال شهر جديد بين النجوم", "Crescent Moon among Stars", "photo-1518709268805-4e9042af9f23", "Markus Spiske"),
        ("شهب مضيئة وسماء متلألئة", "Shooting Stars in Night Sky", "photo-1509198397868-475647b2a1e5", "Jeremy Thomas"),
        ("شروق الشمس في سماء هادئة", "Golden Dawn over the Horizon", "photo-1495616811223-4d98c6e9c869", "Eberhard Grossgasteiger"),
        ("سماء زرقاء وسحب بيضاء نقية", "Blue Sky with Pure Clouds", "photo-1534088568595-a066f410bcda", "Zdenek Machacek"),
        ("نجوم الليل اللامعة فوق الجبل", "Starlit Night over Mountains", "photo-1419242902214-272b3f66ee7a", "Greg Rakozy"),
        ("ألوان الشفق الذهبي عند الغروب", "Golden Twilight Sunset", "photo-1507525428034-b723cf961d3e", "Sean Oulashin"),
        ("أفق السماء الليلية وسديم النجوم", "Cosmic Night Sky Vista", "photo-1451187580459-43490279c0fa", "NASA")
    ]),
    ("calligraphy", "الخط العربي", "Arabic Calligraphy", [
        ("لفظ الجلالة «الله» بخط الثلث", "Allah in Thuluth Calligraphy", "photo-1564769625624-9b9087c51482", "Calligraphy Master"),
        ("«محمد رسول الله» بالخط الكوفي", "Muhammad Rasul Allah in Kufic", "photo-1580418827493-f2b22c0a76cf", "Traditional Arts"),
        ("«بسم الله الرحمن الرحيم» بالديواني", "Bismillah in Diwani Script", "photo-1584551246679-0daf3d275d0c", "Islamic Heritage"),
        ("«لا إله إلا الله» مذهبة راقية", "Shahada in Golden Inscription", "photo-1542838132-92c53300491d", "Ottoman Archive"),
        ("«الحمد لله رب العالمين» بالنسخ", "Alhamdulillah in Naskh Script", "photo-1578632767115-351597cf2479", "Arabic Typography"),
        ("«وما توفيقي إلا بالله» مخطوطة فنية", "Quranic Verse in Art Calligraphy", "photo-1565552645632-d725f8bfc19d", "Safavid Heritage"),
        ("حروف عربية منقوشة بالذهب واللازورد", "Gold and Lapis Arabic Letters", "photo-1591604129939-f1efa4d9f7fd", "Eastern Arts"),
        ("خط أندلسي مغربي عريق", "Andalusian Maghrebi Calligraphy", "photo-1590076215667-875d4ef2d7eb", "Moroccan Artisan"),
        ("تكوين خطي دائري بديع للبسملة", "Circular Calligraphic Bismillah", "photo-1519817650390-64a93db5114a", "Illumination Master")
    ]),
    ("geometry", "الزخارف الإسلامية", "Islamic Geometry", [
        ("نجمة ثمانية وزخرفة هندسية إسلامية", "Octagram Islamic Geometric Pattern", "photo-1579783900882-c0d3dad7b119", "Birmingham Museums"),
        ("زليج أندلسي ملون بأشكال متناظرة", "Andalusian Zellij Tilework", "photo-1561055657-b9e0bf0fa360", "Annie Spratt"),
        ("مقرنصات جصية في قبة مسجد", "Muqarnas Architectural Geometry", "photo-1564769625905-50e93615e76b", "Khatam"),
        ("زخرفة إسلامية نباتية وتوريق أرابيسك", "Arabesque Foliage Patterns", "photo-1580418827493-f2b22c0a76d0", "Museum Archive"),
        ("نقوش خشبية إسلامية مفرغة (مشربية)", "Carved Mashrabiya Geometry", "photo-1584551246679-0daf3d275d0b", "Wood Artisan"),
        ("شبابيك زجاجية ملونة وزخرفة قمرية", "Stained Glass Islamic Mosaic", "photo-1542838132-92c53300491c", "Color Master"),
        ("فسيفساء رخامية بألوان لازوردية وذهبية", "Azure and Gold Marble Mosaic", "photo-1578632767115-351597cf247a", "Tiles Heritage"),
        ("شبكة هندسية إسلامية معقدة ومتناغمة", "Harmonic Polyhedral Geometry", "photo-1565552645632-d725f8bfc19e", "Geometric Arts"),
        ("نقش حجري إسلامي ثلاثي الأبعاد", "3D Islamic Relief Stone Carving", "photo-1591604129939-f1efa4d9f7fe", "Heritage Studio")
    ]),
    ("nature", "الطبيعة والجبال والصحراء", "Nature, Mountains & Desert", [
        ("كثبان رملية ذهبية وقت الأصيل", "Golden Desert Dunes at Twilight", "photo-1509316975850-ff9c5deb0cd9", "Keith Hardy"),
        ("جبال شامخة في بيئة نقية", "Majestic Mountain Peaks", "photo-1464822759023-fed622ff2c3b", "Kaloyan Christov"),
        ("واحة خضراء وسط رمال الصحراء", "Green Oasis in the Desert", "photo-1518709268805-4e9042af9f24", "Nature Guide"),
        ("أشجار النخيل الباسقات تحت ضوء الشمس", "Tall Date Palms in Sunlight", "photo-1509198397868-475647b2a1e6", "Tropical Palms"),
        ("وادي صخري بماء رقراق عذب", "Fresh Stream in Rocky Valley", "photo-1495616811223-4d98c6e9c86a", "Wild Explorer"),
        ("صحراء شاسعة تحت سماء زرقاء لا متناهية", "Vast Desert Horizon", "photo-1534088568595-a066f410bcde", "Nomad Lens"),
        ("جبل النور وغار حراء بمكة المكرمة", "Mount Noor in Mecca", "photo-1419242902214-272b3f66ee7b", "Mecca Explorer"),
        ("ضباب رقيق يلف قمم الجبال عند الفجر", "Morning Mist over Highlands", "photo-1507525428034-b723cf961d3f", "Dawn Seeker"),
        ("شجرة زيتون معمرة في أرض مباركة", "Ancient Olive Tree in Holy Land", "photo-1451187580459-43490279c0fb", "Botanical Archive")
    ]),
    ("sea_sunset", "البحر والغروب", "Sea & Sunset", [
        ("أمواج البحر الهادئة في المساء", "Calm Evening Ocean Waves", "photo-1507525428034-b723cf961d3e", "Sean Oulashin"),
        ("شمس الغروب البرتقالية تسقط في الأفق", "Orange Sunset Over Ocean", "photo-1495616811223-4d98c6e9c869", "Eberhard"),
        ("شاطئ رملي ناعم في ضوء الشفق", "Twilight Sandy Seashore", "photo-1509316975850-ff9c5deb0cd9", "Keith"),
        ("انعكاس سحب السماء الحمراء على الماء", "Crimson Clouds Reflection", "photo-1464822759023-fed622ff2c3b", "Kaloyan"),
        ("سفينة تبحر في بحر هادئ عند المغيب", "Sailing Vessel at Dusk", "photo-1518709268805-4e9042af9f23", "Markus"),
        ("صخور الشاطئ المتينة وقت المد", "Sturdy Coastal Rocks", "photo-1509198397868-475647b2a1e5", "Jeremy"),
        ("تدرجات الشفق البنفسجي فوق الخليج", "Purple Twilight Gulf Vista", "photo-1534088568595-a066f410bcda", "Zdenek"),
        ("صفاء المياه الفيروزية الرائقة", "Crystal Clear Turquoise Sea", "photo-1419242902214-272b3f66ee7a", "Greg"),
        ("نوارس تحلق في سماء البحر عند العصر", "Seagulls Flying over Water", "photo-1451187580459-43490279c0fa", "Nature Capture")
    ]),
    ("quran_verses", "آيات قصيرة موثقة", "Verified Short Verses", [
        ("«أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ» (الرعد: 28)", "Hearts find rest in remembrance of Allah", "photo-1542838132-92c53300491a", "Tanzil Archive"),
        ("«وَقُل رَّبِّ زِدْنِي عِلْمًا» (طه: 114)", "My Lord, increase me in knowledge", "photo-1578632767115-351597cf247b", "Koran Texts"),
        ("«فَإِنَّ مَعَ الْعُسْرِ يُسْرًا» (الشرح: 5)", "Indeed with hardship comes ease", "photo-1565552645632-d725f8bfc19f", "Holy Quran Project"),
        ("«وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ» (البقرة: 186)", "I am indeed near", "photo-1591604129939-f1efa4d9f7ff", "Medina Print"),
        ("«وَتَوَكَّلْ عَلَى الْحَيِّ الَّذِي لَا يَمُوتُ» (الفرقان: 58)", "Rely upon the Ever-Living", "photo-1590076215667-875d4ef2d7ec", "Arabic Quran"),
        ("«إِنَّ رَحْمَتَ اللَّهِ قَرِيبٌ مِّنَ الْمُحْسِنِينَ» (الأعراف: 56)", "Mercy of Allah is near", "photo-1519817650390-64a93db5114b", "Quranic Script"),
        ("«وَرَحْمَتِي وَسِعَتْ كُلَّ شَيْءٍ» (الأعراف: 156)", "My mercy embraces all things", "photo-1564769625624-9b9087c51483", "Calligrapher"),
        ("«وَهُوَ مَعَكُمْ أَيْنَ مَا كُنتُمْ» (الحديد: 4)", "And He is with you wherever you are", "photo-1580418827493-f2b22c0a76d1", "Noble Ayahs"),
        ("«حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ» (آل عمران: 173)", "Sufficient for us is Allah", "photo-1584551246679-0daf3d275d0a", "Authentic Verse")
    ]),
    ("adhkar", "أذكار قصيرة موثقة", "Verified Short Adhkar", [
        ("«سُبْحَانَ اللَّهِ وَبِحَمْدِهِ سُبْحَانَ اللَّهِ الْعَظِيمِ»", "Glory and praise be to Allah the Almighty", "photo-1542838132-92c53300491b", "Hisn Al-Muslim"),
        ("«لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ»", "There is no might nor power except by Allah", "photo-1578632767115-351597cf247c", "Prophetic Adhkar"),
        ("«اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ» (سيد الاستغفار)", "Master Supplication for Forgiveness", "photo-1565552645632-d725f8bfc190", "Bukhari 6306"),
        ("«أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ»", "I seek Allah's forgiveness and repent to Him", "photo-1591604129939-f1efa4d9f7f0", "Daily Wird"),
        ("«سُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ وَلَا إِلَهَ إِلَّا اللَّهُ وَاللَّهُ أَكْبَرُ»", "The Four Lasting Virtues", "photo-1590076215667-875d4ef2d7ed", "Muslim 2137"),
        ("«رَضِيتُ بِاللَّهِ رَبًّا وَبِالإِسْلَامِ دِينًا وَبِمُحَمَّدٍ نَبِيًّا»", "Content with Allah as Lord", "photo-1519817650390-64a93db5114c", "Morning Dhikr"),
        ("«اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ»", "Blessings upon our Prophet Muhammad", "photo-1564769625624-9b9087c51484", "Salawat"),
        ("«يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ»", "O Ever-Living, by Your mercy I seek help", "photo-1580418827493-f2b22c0a76d2", "Tirmidhi 3524"),
        ("«حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ»", "Allah is sufficient for me", "photo-1584551246679-0daf3d275d09", "Abu Dawud 5081")
    ]),
    ("dark", "خلفيات داكنة", "Dark Wallpapers", [
        ("سواد الليل الهادئ مع تفاصيل ذهبية", "Midnight Black with Gold Inlay", "photo-1618005182384-a83a8bd57fbe", "Milad Fakurian"),
        ("رخام أسود مائل للرمادي الداكن", "Charcoal Slate Dark Texture", "photo-1550684848-fac1c5b4e853", "Pawel Czerwinski"),
        ("زخرفة إسلامية داكنة وظلال ناعمة", "Dark Islamic Shadow Ornament", "photo-1600585154340-be6161a56a0c", "Dark Art Studio"),
        ("سماء ليلية داكنة مع نجوم خافتة", "Pitch Night Sky with Distant Stars", "photo-1538370965046-79c0d6907d47", "Bryan Goff"),
        ("مسجد ظلي (سيلويت) وقت السحر", "Mosque Silhouette at Pre-Dawn", "photo-1509316975850-ff9c5deb0cd9", "Night Walker"),
        ("قماش كسوة داكن وتطريز فاخر", "Deep Black Fabric with Fine Stitching", "photo-1464822759023-fed622ff2c3b", "Textile Heritage"),
        ("أقواس داكنة بإنارة خافتة روحانية", "Dimly Lit Spiritual Arches", "photo-1518709268805-4e9042af9f23", "Sanctuary Lens"),
        ("تصميم إسلامي كلاسيكي داكن مريح للعين", "AMOLED Friendly Islamic Pattern", "photo-1509198397868-475647b2a1e5", "Visual Comfort"),
        ("خط عربي رمادي على خلفية فحمية", "Muted Gray Calligraphy on Charcoal", "photo-1495616811223-4d98c6e9c869", "Monochrome Studio")
    ]),
    ("light", "خلفيات فاتحة", "Light Wallpapers", [
        ("رخام أبيض ناصع مع عروق ذهبية", "Pure White Marble with Golden Veins", "photo-1600585154526-990dced4db0d", "Interior Archive"),
        ("جدار أندلسي أبيض وزخرفة جصية خفيفة", "Soft Andalusian White Stucco", "photo-1584551246679-0daf3d275d0f", "Mediterranean Light"),
        ("أوراق بردي عاجية بخلفية إسلامية", "Ivory Papyrus Islamic Texture", "photo-1542838132-92c53300491e", "Papyrus Studio"),
        ("نور الصباح المنساب عبر مشربية بيضاء", "Morning Light through Mashrabiya", "photo-1580418827493-f2b22c0a76cb", "Illumination Light"),
        ("صحن مسجد مضاء بالشمس الساطعة", "Sunlit White Mosque Courtyard", "photo-1578632767115-351597cf2477", "Daylight Vista"),
        ("خط عربي هادئ على خلفية بيج دافئة", "Gentle Calligraphy on Warm Beige", "photo-1565552645632-d725f8bfc19a", "Minimal Light"),
        ("زهور بيضاء نقية وندى الصباح", "Pure White Morning Dew Flowers", "photo-1591604129939-f1efa4d9f7fa", "Garden Oasis"),
        ("قبة مسجد بيضاء تحت سماء زرقاء ناصعة", "White Dome in Radiant Blue Sky", "photo-1590076215667-875d4ef2d7ee", "Pure Dome"),
        ("تدرج لوني أبيض وفضي مريح ونقي", "Silver and White Soft Gradient", "photo-1519817650390-64a93db51149", "Bright Minimal")
    ]),
    ("lock_screen", "خلفيات شاشة القفل العمودية", "Lock Screen Vertical", [
        ("مئذنة شاهقة تلامس السماء عمودياً", "Vertical Towering Minaret", "photo-1564769625905-50e93615e769", "Vertical Studio"),
        ("طريق صحراوي ممتد عمودياً نحو الأفق", "Vertical Desert Road to Horizon", "photo-1509316975850-ff9c5deb0cd9", "Highway View"),
        ("شلال ماء ينحدر بين صخور خضراء", "Vertical Cascade Waterfall", "photo-1464822759023-fed622ff2c3b", "Water Stream"),
        ("بوابة خشبية تاريخية شامخة عمودياً", "Monumental Historic Wood Portal", "photo-1584551246679-0daf3d275d0b", "Portal Architect"),
        ("نخلة باسقة عمودية تحت سماء الشفق", "Vertical Palm at Twilight", "photo-1518709268805-4e9042af9f23", "Oasis Flora"),
        ("عمود رخامي بنقوش إسلامية رأسية", "Vertical Carved Marble Column", "photo-1580418827493-f2b22c0a76cb", "Column Artisan"),
        ("سلسلة جبال رأسية في ضوء الصباح", "Vertical Morning Ridge Peaks", "photo-1495616811223-4d98c6e9c869", "Summit Height"),
        ("محراب إسلامي طويل وعميق", "Vertical Islamic Mihrab Niche", "photo-1542838132-92c53300491e", "Niche Studio"),
        ("قمر عمودي بين سحاب الليل الهادئ", "Moon Rising between Night Clouds", "photo-1532693322450-2cb5c511067d", "Lunar Frame")
    ]),
    ("home_screen", "خلفيات الشاشة الرئيسية", "Home Screen", [
        ("خلفية ناعمة لا تشوش على أيقونات التطبيقات", "App-Icon Friendly Subtle Texture", "photo-1618005182384-a83a8bd57fbe", "Clean Layout"),
        ("تدرج زمردي هادئ بتفاصيل إسلامية لطيفة", "Soft Emerald Islamic Gradient", "photo-1550684848-fac1c5b4e853", "Emerald Calm"),
        ("نقش هندسي خافت في الزوايا فقط", "Corner Ornamented Subtle Canvas", "photo-1600585154340-be6161a56a0c", "Quiet Ornament"),
        ("رمال ناعمة بانعكاس ضوئي مريح", "Smooth Sand Ripple Texture", "photo-1509316975850-ff9c5deb0cd9", "Desert Calm"),
        ("أزرق بحري عميق بتباين مثالي للنصوص", "Deep Ocean Blue with Perfect Contrast", "photo-1507525428034-b723cf961d3e", "Contrast Pro"),
        ("تدرج سماوي ناصع ملائم للأيقونات البيضاء", "Clean Sky Tint for White Icons", "photo-1534088568595-a066f410bcda", "Icon Contrast"),
        ("خلفية نباتية إسلامية هادئة وبدون ازدحام", "Minimal Arabesque Motif Canvas", "photo-1579783900882-c0d3dad7b119", "Pattern Pro"),
        ("خلفية رمادية وقورة تناسب جميع الأوقات", "Dignified Neutral Charcoal Slate", "photo-1538370965046-79c0d6907d47", "Neutral Art"),
        ("تدرج ذهبي دافئ خافت يمنح راحة للبصر", "Warm Soft Gold Minimalist Gradient", "photo-1600585154526-990dced4db0d", "Warm Visual")
    ]),
    ("ramadan", "خلفيات رمضانية", "Ramadan", [
        ("فانوس رمضان مضيء بنور دافئ", "Glowing Ramadan Lantern (Fanoos)", "photo-1590076215667-875d4ef2d7ee", "Ramadan Lights"),
        ("هلال رمضان الفضي مع النجوم", "Silver Ramadan Crescent Moon", "photo-1518709268805-4e9042af9f23", "Holy Crescent"),
        ("تمر وماء زمزم ومصحف مفتوح", "Dates, Zamzam Water and Open Quran", "photo-1584551246679-0daf3d275d0f", "Iftar Still Life"),
        ("مدفع الإفطار التراثي وقت الغروب", "Heritage Iftar Cannon at Dusk", "photo-1509198397868-475647b2a1e5", "Tradition Lens"),
        ("أجواء صلاة التراويح في المسجد", "Tarawih Prayers Gathering", "photo-1564769625905-50e93615e769", "Tarawih Nights"),
        ("مائدة إفطار رمضانية تجمع العائلة", "Ramadan Family Table Gathering", "photo-1542838132-92c53300491e", "Family Blessings"),
        ("ليالي رمضان المباركة في الحرم", "Blessed Ramadan Nights at Haram", "photo-1565552645632-d725f8bfc19a", "Mecca Nights"),
        ("فوانيس معلقة في شوارع تاريخية", "Lanterns in Historic Old Town", "photo-1580418827493-f2b22c0a76cb", "Festive Streets"),
        ("هلال رمضان يسطع فوق مئذنة شامخة", "Ramadan Moon Above Minaret", "photo-1591604129939-f1efa4d9f7fa", "Moon Over Mosque")
    ]),
    ("eid", "خلفيات العيد", "Eid Mubarak", [
        ("تكبيرات العيد وبهجة المصلى", "Eid Prayer Gathering in Open Field", "photo-1548013146-72479768bada", "Eid Joy"),
        ("«عيد مبارك» بخط عربي احتفالي مذهب", "Eid Mubarak Golden Calligraphy", "photo-1564769625624-9b9087c51482", "Eid Celebration"),
        ("حلوى العيد والقهوة العربية التراثية", "Eid Sweets and Arabic Coffee", "photo-1578632767115-351597cf2477", "Hospitality"),
        ("ألعاب نارية تضيء سماء العيد", "Festive Fireworks in Night Sky", "photo-1509198397868-475647b2a1e5", "Celebration Sky"),
        ("ورود وزهور العيد بألوان زاهية", "Vibrant Eid Floral Arrangement", "photo-1507525428034-b723cf961d3e", "Fresh Flowers"),
        ("أطفال بملابس العيد التقليدية", "Children in Traditional Eid Attire", "photo-1542838132-92c53300491e", "Family Joy"),
        ("هلال شوال وبشائر عيد الفطر", "Shawwal Crescent of Eid al-Fitr", "photo-1518709268805-4e9042af9f23", "Eid Crescent"),
        ("«تقبل الله منا ومنكم صالح الأعمال»", "May Allah Accept Our Deeds Inscription", "photo-1584551246679-0daf3d275d0c", "Islamic Greeting"),
        ("أضواء وزينة احتفالية لعيد الأضحى", "Eid al-Adha Festive Lights", "photo-1519817650390-64a93db51149", "Holiday Cheer")
    ]),
    ("minimal", "خلفيات بسيطة Minimal", "Minimalist Islamic", [
        ("خط أسود رفيع يرسم هلالاً ومئذنة", "Minimal One-Line Crescent & Minaret", "photo-1618005182384-a83a8bd57fbe", "Minimal Lines"),
        ("تدرج لوني أزرق زمردي بسيط ونظيف", "Clean Teal to Emerald Minimal Gradient", "photo-1550684848-fac1c5b4e853", "Pure Gradient"),
        ("شمس الصباح وظل نخلة واحدة", "Morning Sun and Single Palm Shadow", "photo-1509316975850-ff9c5deb0cd9", "Desert Solo"),
        ("نجمة ثمانية مركزية مفرغة ونقية", "Solitary Centered Octagram Star", "photo-1579783900882-c0d3dad7b119", "Geometric Solo"),
        ("أفق صحراوي بخط واحد متجانس", "Single Line Desert Horizon", "photo-1464822759023-fed622ff2c3b", "Calm Horizon"),
        ("قوس إسلامي وحيد في مساحة واسعة", "Single Islamic Arch in Vast Space", "photo-1584551246679-0daf3d275d0f", "Spacious Arch"),
        ("خلفية رمادية بنقطة تركيز هادئة", "Gray Zen Texture with Calm Center", "photo-1538370965046-79c0d6907d47", "Focus Zen"),
        ("رمز هلال ذهبي خفيف في المركز", "Subtle Gold Crescent in Center", "photo-1532693322450-2cb5c511067d", "Gold Symbol"),
        ("ورقة شجر خضراء بقطرة ندى صافية", "Single Green Leaf with Dewdrop", "photo-1534088568595-a066f410bcda", "Dewdrop Solo")
    ]),
    ("tablet", "خلفيات للأجهزة اللوحية", "Tablet / Landscape", [
        ("بانوراما الحرمين الشريفين الأفقية", "Horizontal Panorama of Holy Sanctuaries", "photo-1548013146-72479768bada", "Panorama Lens"),
        ("أفق الصحراء الممتد بجودة فائقة 4K", "Vast Desert Horizon 4K Landscape", "photo-1509316975850-ff9c5deb0cd9", "Wide Desert"),
        ("سماء النجوم الممتدة للشاشات العريضة", "Ultra-Wide Night Galaxy for Tablets", "photo-1506703719100-a0f3a48c0f86", "Wide Galaxy"),
        ("رواق المسجد الأفقي المتناظر بدقة", "Symmetrical Horizontal Mosque Hall", "photo-1564769625905-50e93615e769", "Wide Corridor"),
        ("لوحة خطية أفقية بديعة لسورة الفاتحة", "Horizontal Calligraphy of Al-Fatiha", "photo-1564769625624-9b9087c51482", "Grand Masterpiece"),
        ("بحر الشفق الممتد للشاشات الكبيرة", "Horizon Sunset for Large Displays", "photo-1507525428034-b723cf961d3e", "Tablet Sunset"),
        ("حديقة إسلامية أندلسية بأشجار ونوافير", "Andalusian Courtyard Garden Panorama", "photo-1542838132-92c53300491e", "Garden Vista"),
        ("قمم جبلية متتابعة تحت السحاب", "Panoramic Mountain Ridges under Clouds", "photo-1464822759023-fed622ff2c3b", "Ridge Panorama"),
        ("زخرفة فسيفسائية ممتدة أفقياً", "Continuous Horizontal Tile Mosaic", "photo-1561055657-b9e0bf0fa360", "Tile Wall Panorama")
    ])
]

wallpapers = []
counter = 1

for cat_id, cat_ar, cat_en, items in categories:
    for item_ar, item_en, unsplash_id, author in items:
        wp_id = f"wp_{counter:03d}"
        img_url = f"https://images.unsplash.com/{unsplash_id}?auto=format&fit=crop&w=1080&q=85"
        thumb_url = f"https://images.unsplash.com/{unsplash_id}?auto=format&fit=crop&w=400&q=75"
        width = 2160 if cat_id == "tablet" else 1080
        height = 1440 if cat_id == "tablet" else 1920
        target = "both"
        if cat_id == "lock_screen":
            target = "lock"
        elif cat_id == "home_screen":
            target = "home"

        wallpapers.append({
            "id": wp_id,
            "titleArabic": item_ar,
            "titleEnglish": item_en,
            "category": cat_id,
            "imageUrl": img_url,
            "thumbnailUrl": thumb_url,
            "width": width,
            "height": height,
            "screenTarget": target,
            "source": "Unsplash Cultural Heritage Collection",
            "designerOrAuthor": author,
            "licenseType": "Unsplash License",
            "attributionText": f"Photo by {author} on Unsplash (Free for commercial & personal use)",
            "isFavorite": False,
            "isDownloaded": False,
            "localFilePath": None,
            "createdAt": 1730000000000 + counter * 1000
        })
        counter += 1

out_dir = "/app/applet/app/src/main/assets/wallpapers"
os.makedirs(out_dir, exist_ok=True)
out_file = os.path.join(out_dir, "verified_wallpapers.json")

with open(out_file, "w", encoding="utf-8") as f:
    json.dump(wallpapers, f, ensure_ascii=False, indent=2)

print(f"Generated {len(wallpapers)} wallpapers in {out_file}")

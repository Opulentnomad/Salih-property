# ثواني

بطاقات إجراءات للطوارئ (43 بطاقة) بالعربية: خطوة بخطوة وبالصوت، مع منظم إيقاع الإنعاش ومؤقت العاصبة ومراجع علمية. تطبيق ويب PWA يعمل بدون إنترنت بعد أول زيارة.

- `index.html` / `styles.css` / `app.js`: الواجهة (تصميم ناعم Soft UI)
- `data.js`: بيانات البطاقات
- `manifest.webmanifest` / `sw.js` / `icons/`: ملفات الـ PWA

للتشغيل محلياً: `python3 -m http.server` ثم افتح `http://localhost:8000`. المحتوى للتوعية ولا يغني عن الاتصال بالإسعاف (997) أو دورة إسعافات معتمدة.

## تطبيق أندرويد
نسخة أصلية بـ Kotlin وJetpack Compose في مجلد `android/` (انظر `android/README.md`). ملف APK الجاهز: `android/release/thawani-1.0.0.apk`.

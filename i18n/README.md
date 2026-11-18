# إضافة لغة جديدة إلى ثواني

اللغات المجهزة: **اردو (ur)** و**Filipino (fil)** و**বাংলা (bn)**. تظهر اللغة كتبويب في البطاقة فقط حين يوجد ملفها وعليه `"reviewed": true`.

## للمترجم الطبي
1. انسخ `template.json` إلى `ur.json` أو `fil.json` أو `bn.json`، وعدّل `lang` و`dir` (`rtl` للأوردو، `ltr` للباقي) و`name`.
2. في كل بطاقة ستجد النص العربي في `_ar`. اكتب الترجمة في الحقول `title` و`level` و`golden` و`steps` و`avoid`. عدد الخطوات يجب أن يطابق العربي تماماً. لا تضف ولا تحذف خطوة.
3. املأ `ui` (عبارات الواجهة): `call` = «اتصل الآن»، `first` = «القاعدة الأولى»، `steps` = «الخطوات»، `avoid` = «تجنب فوراً»، `note` = جملة تنبه أنها ترجمة وأن الاتصال بالطوارئ أولاً.
4. أرقام الطوارئ تبقى كما هي (997، 998 …).
5. اكتب `translator` و`reviewer` (مسعف أو طبيب معتمد راجع النص) و`date`.
6. بعد المراجعة فقط غيّر `"reviewed": true`.

## للمطوّر
- تحقق قبل النشر: `node tools/check-i18n.js i18n/ur.json`
- ثم أعد البناء: `SITE_URL=https://موقعك node tools/build.js`

## To the translator (English)
Copy `template.json` to your language file, fill every empty field (the Arabic source is in `_ar`), keep the number of steps identical, keep emergency numbers unchanged, fill `translator`, `reviewer` (a certified first-aid professional) and `date`, and only then set `"reviewed": true`. These are safety instructions: translate meaning, not word by word, and do not add advice that is not in the source.

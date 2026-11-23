# نَغَم (Naghm)

تطبيق موسيقى شخصي (PWA) يجمع Audius وJamendo وYouTube (مشغّل مضمّن) مع اختيارات يومية وقوائم حسب المزاج.
بلا خادم وبلا بناء: ملفات ثابتة في `naghm/`، والبيانات تُحفظ على الجهاز.

## التشغيل محليًا
    cd naghm && python3 -m http.server 8000   # ثم افتح http://localhost:8000

## على الجوال
انشر مجلد `naghm/` على أي استضافة ثابتة تدعم HTTPS (GitHub Pages أو Netlify أو Cloudflare Pages)،
ثم افتح الرابط في الجوال واختر «إضافة إلى الشاشة الرئيسية».

## Jamendo
اختياري: سجّل مجانًا في https://devportal.jamendo.com وألصق `client_id` في الإعدادات.

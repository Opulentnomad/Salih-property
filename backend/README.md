# خلفية سفرنا (API + قاعدة بيانات)

Node 22 + Fastify + SQLite (`node:sqlite`). تخدم الواجهة من `../app` على `/` والـ API على `/api`.

## التشغيل
```bash
cd backend && npm install
ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD='غيّرها-الآن-123' npm start   # http://localhost:3000
npm test                                                                    # 10 اختبارات تكامل
```
أول تشغيل يبذر القاعدة من `app/data.js` (10 وجهات، شروط دخول إصدار 1 منشور، مراجعات، أسعار صرف) وينشئ حساب المدير إن حُدِّدت المتغيرات.
في الإنتاج: `JWT_SECRET` إجباري (انظر `.env.example`). بناء Docker من جذر المستودع: `docker build -f backend/Dockerfile .`

## الواجهات (`/api`)
| المسار | الوصف | الصلاحية |
|---|---|---|
| `GET /health` | حالة الخادم | عام |
| `GET /destinations?q&region&visa&budget&season&limit&offset` | بحث وفلترة | عام |
| `GET /destinations/:id` · `GET /destinations/:id/reviews` | تفاصيل ومراجعات منشورة | عام |
| `GET /entry-requirements?destination=tr\|IST&passport=SA` | آخر إصدار منشور + `stale` (أقدم من 30 يومًا) + إخلاء مسؤولية | عام |
| `GET /recommendations?interests=شاطئ,تاريخ&budget=2` | ترشيح حسب التفضيلات | عام |
| `GET /fx?base=SAR` | أسعار الصرف المخزّنة | عام |
| `GET /affiliate/:id/click` | تسجيل نقرة ثم تحويل 302 (https فقط) | عام |
| `POST /auth/register` · `POST /auth/login` | بريد + كلمة مرور (scrypt)، رمز JWT لمدة 7 أيام | عام، 10 طلبات/دقيقة |
| `GET /me` · `PUT /me/prefs` | الحساب والتفضيلات | مسجّل |
| `GET/PUT/DELETE /me/favorites[/:id]` | المفضلة | مسجّل |
| `GET/POST/PUT/DELETE /trips[/:id]` | الرحلات (لصاحبها فقط) | مسجّل |
| `POST /destinations/:id/reviews` | مراجعة جديدة ← `pending` (202) | مسجّل |
| `GET/POST/DELETE /alerts/subscriptions` | متابعة تغيّر الشروط | مسجّل |
| `GET /admin/requirements?destination=` | كل الإصدارات | editor+ |
| `PUT /admin/requirements/:destId` | ينشئ **مسودة** إصدار جديد | editor+ |
| `POST /admin/requirements/:id/publish` | ينشر المسودة، يؤرشف السابق، يسجّل الفروق | reviewer/admin |
| `GET /admin/reviews` · `POST /admin/reviews/:id/moderate` | الإشراف على المراجعات | reviewer/admin |
| `GET /admin/audit` | سجل التدقيق | admin |

## الأمان المطبّق
كلمات المرور بـ scrypt ومقارنة ثابتة الزمن · JWT بخوارزمية HS256 فقط والدور يُقرأ من القاعدة في كل طلب · تحقق من كل مدخل بمخططات JSON · استعلامات مهيّأة (لا دمج نصوص) وتهريب `%`/`_` في البحث · تحديد معدّل (300/دقيقة عامًا، 10/دقيقة للدخول) · CSP صارم وبقية رؤوس الأمان · لا يخزّن Service Worker أي استجابة تخص الحساب أو الإدارة · `npm audit`: صفر ثغرات وقت الإنشاء.

## ما لا يزال ناقصًا قبل الإنتاج
- **SQLite بدل PostgreSQL:** مناسب لنسخة واحدة؛ للتوسع انقل المخطط (`docs/03-database-schema.sql`) إلى PostgreSQL. وحدة `node:sqlite` ما زالت تجريبية.
- **لا OTP للجوال ولا استعادة كلمة المرور ولا تأكيد البريد** (دخول ببريد وكلمة مرور فقط).
- **لا تكامل مع Timatic/IATA:** شروط الدخول تُدخل يدويًا عبر لوحة الإدارة. البيانات المبذورة تجريبية.
- **لا إرسال فعلي للتنبيهات** (Push/بريد)؛ الاشتراكات تُخزَّن فقط.
- أسعار الصرف المخزّنة ثابتة (التحديث الحي يتم من المتصفح حاليًا).
- رمز الدخول في الواجهة يُحفظ في `localStorage`؛ الـ CSP يقلّل خطر XSS لكن الأفضل في الإنتاج كوكي `HttpOnly`.

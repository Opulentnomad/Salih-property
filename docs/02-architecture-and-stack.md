# المعمارية والتقنيات الموصى بها

## المكدس
| الطبقة | الاختيار | السبب |
|---|---|---|
| الواجهة | Next.js 15 (App Router) + TypeScript + Tailwind (RTL logical props) + next-intl | SSR/SEO للعربية، أداء، i18n |
| PWA | Service Worker (Workbox) + Web App Manifest + Web Push (VAPID) | عمل دون اتصال + تنبيهات |
| الخلفية | NestJS (Node 22) REST + OpenAPI، أو Next Route Handlers للمرحلة الأولى | وضوح ونمطية |
| قاعدة البيانات | PostgreSQL 16 + PostGIS؛ Redis للتخزين المؤقت/الطوابير (BullMQ) | علاقات + جغرافيا |
| البحث | Meilisearch/Typesense (دعم العربية والأخطاء الإملائية) | بحث سريع |
| الوسائط | S3-compatible + CDN (Cloudflare) + تحويل AVIF/WebP | صور غنية بأداء جيد |
| المصادقة | Auth.js/Keycloak: بريد + OTP جوال (+966) + Apple/Google؛ Nafath اختياري لاحقًا | أمان |
| الإدارة (CMS) | Directus/Payload فوق نفس القاعدة، أو لوحة مخصصة (انظر `app/admin.html` كنموذج) | تحرير + سير موافقات |
| المراقبة | Sentry + OpenTelemetry + Grafana، PostHog للتحليلات | |
| النشر | Docker + GitHub Actions + (AWS me-central-1 أو STC/Mobily Cloud لمتطلبات الإقامة بالمملكة) | |

## التكاملات
- **شروط الدخول:** IATA Timatic (Timatic API عبر IATA/شركاء مثل Sherpa / VisaDB) كمصدر أساسي + CMS كمصدر تصحيح/إضافة. تُخزَّن كل استجابة مع `fetched_at` و`source` و`hash` لاكتشاف التغيّر.
- **الخرائط:** Mapbox GL أو Google Maps (تفضيل أحرف عربية RTL).
- **الطيران/الفنادق:** Amadeus / Kiwi Tequila / Booking Demand API / Travelpayouts (أفلييت). روابط `rel="sponsored"` وإفصاح عن العمولة.
- **العملات:** مزود أسعار (Open Exchange Rates) مع تخزين 1س ونسخة احتياطية دون اتصال.
- **الإشعارات:** Web Push + بريد (SES/Resend) + FCM للتغليف لاحقًا.
- **الطقس/الأحداث (اختياري):** Open-Meteo.

## جودة بيانات شروط الدخول (الأهم)
1. مصدر أساسي واحد معتمد + مصدر ثانٍ للمقارنة؛ عند الاختلاف → علامة مراجعة بشرية.
2. كل حقل له `source_url` و`verified_at`؛ يُخفى «موثّق» إن تجاوز 30 يومًا.
3. إخلاء مسؤولية دائم + رابط للجهة الرسمية؛ لا نعطي ضمان دخول.
4. سجل تغييرات (`requirement_versions`) + Diff + تنبيه للمتابعين.
5. فصل «جواز سعودي» عن «إقامة/جنسية أخرى» (حقل `passport_country`).

## الأداء والاتصال
LCP < 2.5s على 4G، JS الأولي < 170KB، تحميل كسول للصور، `stale-while-revalidate` للبيانات، Cache First للقشرة، قراءة وجهات المفضلة والرحلات دون اتصال (IndexedDB)، مزامنة خلفية للمراجعات.

## الأمان والخصوصية
OWASP ASVS L2، TLS 1.3، JWT قصير + Refresh دوّار، تحديد معدّل، CSP صارم، إدخال معقّم، تشفير الحقول الحساسة، متوافق مع **PDPL السعودي** (موافقة، حذف، تصدير، إقامة بيانات)، تتبّع بموافقة، لا نخزّن أرقام جوازات.

## SEO
SSR/ISR لصفحات `/ar/destinations/{slug}`، `hreflang` ar-SA/en، Schema.org (`TouristDestination`, `FAQPage`)، خريطة موقع ديناميكية، روابط نظيفة، أوصاف عربية فريدة، Core Web Vitals.

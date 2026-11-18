/* بيانات تجريبية — يجب التحقق منها من المصدر الرسمي / Timatic قبل الإطلاق.
   visa: free | gcc | voa | evisa | eta | required */
window.SAFARNA_DATA = {
  visaLabels: {
    free:{ar:'بدون تأشيرة',en:'Visa-free',c:'ok'}, gcc:{ar:'دخول بالهوية الوطنية (مجلس التعاون)',en:'GCC ID entry',c:'ok'},
    voa:{ar:'تأشيرة عند الوصول',en:'Visa on arrival',c:'warn'}, evisa:{ar:'تأشيرة إلكترونية',en:'e-Visa',c:'warn'},
    eta:{ar:'تصريح سفر إلكتروني (ETA)',en:'Electronic Travel Auth.',c:'warn'}, required:{ar:'تأشيرة مسبقة من السفارة',en:'Visa required',c:'bad'}
  },
  regions:{gulf:'الخليج',asia:'آسيا',europe:'أوروبا',africa:'أفريقيا والمغرب العربي',caucasus:'القوقاز'},
  destinations:[
   {id:'tr',ar:'تركيا',en:'Türkiye',city:'إسطنبول',emoji:'🕌',g:['#0B6B4F','#C9A24B'],region:'europe',budget:2,seasons:['spring','autumn','summer'],tags:['تاريخ','تسوق','عائلي'],flight:'3س 30د',cur:'TRY',rating:4.6,
    about:'مدينة تجمع التاريخ العثماني والأسواق والمطاعم والمناظر البحرية، مناسبة للعائلات.',
    req:{visa:'free',stay:90,passport:'6 أشهر على الأقل',docs:['جواز سفر ساري','حجز طيران ذهاب وعودة','حجز فندق أو عنوان إقامة','إثبات ملاءة مالية (قد يُطلب)'],vaccines:'لا يوجد إلزام عام',quarantine:'لا يوجد',embassy:{name:'سفارة تركيا - الرياض',phone:'+966 11 000 0000',addr:'الحي الدبلوماسي، الرياض'},verified:'2025-01-10',source:'وزارة الخارجية التركية'}},
   {id:'ae',ar:'الإمارات',en:'UAE',city:'دبي',emoji:'🏙️',g:['#0a3d62','#e1b12c'],region:'gulf',budget:3,seasons:['winter','autumn'],tags:['ترفيه','تسوق','عائلي'],flight:'2س',cur:'AED',rating:4.7,
    about:'وجهة ترفيه وتسوق وفعاليات على مدار السنة، وقريبة برًا وجوًا.',
    req:{visa:'gcc',stay:90,passport:'سارية',docs:['الهوية الوطنية أو الجواز','تأمين المركبة عند السفر برًا'],vaccines:'لا يوجد',quarantine:'لا يوجد',embassy:{name:'سفارة الإمارات - الرياض',phone:'+966 11 000 0001',addr:'الرياض'},verified:'2025-01-10',source:'الهيئة الاتحادية للهوية والجنسية'}},
   {id:'ge',ar:'جورجيا',en:'Georgia',city:'تبليسي',emoji:'⛰️',g:['#7b2d26','#e9c46a'],region:'caucasus',budget:1,seasons:['summer','spring'],tags:['طبيعة','اقتصادي','عائلي'],flight:'3س 45د',cur:'GEL',rating:4.5,
    about:'جبال وطبيعة خضراء وأجواء معتدلة صيفًا بتكلفة مناسبة.',
    req:{visa:'free',stay:365,passport:'ساري طوال الإقامة',docs:['جواز سفر ساري','حجز طيران','حجز إقامة'],vaccines:'لا يوجد',quarantine:'لا يوجد',embassy:{name:'سفارة جورجيا - الرياض',phone:'+966 11 000 0002',addr:'الرياض'},verified:'2025-01-10',source:'وزارة الخارجية الجورجية'}},
   {id:'mv',ar:'المالديف',en:'Maldives',city:'ماليه',emoji:'🏝️',g:['#00a8cc','#f6e58d'],region:'asia',budget:3,seasons:['winter','spring'],tags:['شاطئ','شهر عسل','استجمام'],flight:'5س 30د',cur:'USD',rating:4.9,
    about:'جزر استوائية ومنتجعات فوق الماء وغوص وسنوركلنج.',
    req:{visa:'voa',stay:30,passport:'6 أشهر',docs:['جواز سفر ساري','تذكرة عودة','حجز منتجع','نموذج الهجرة الإلكتروني (قبل الوصول)'],vaccines:'لا إلزام عام',quarantine:'لا يوجد',embassy:{name:'القنصلية - جدة',phone:'+966 12 000 0003',addr:'جدة'},verified:'2025-01-10',source:'إدارة الهجرة المالديفية'}},
   {id:'eg',ar:'مصر',en:'Egypt',city:'القاهرة',emoji:'🔺',g:['#b7791f','#2d3748'],region:'africa',budget:1,seasons:['winter','autumn'],tags:['تاريخ','بحر','اقتصادي'],flight:'2س',cur:'EGP',rating:4.4,
    about:'أهرامات ونيل وشواطئ البحر الأحمر برحلة قصيرة.',
    req:{visa:'evisa',stay:30,passport:'6 أشهر',docs:['جواز سفر ساري','تأشيرة إلكترونية أو عند الوصول حسب الجنسية/الإقامة','حجز فندق'],vaccines:'لا يوجد (إلا عند القدوم من منطقة حمى صفراء)',quarantine:'لا يوجد',embassy:{name:'سفارة مصر - الرياض',phone:'+966 11 000 0004',addr:'الرياض'},verified:'2025-01-10',source:'بوابة التأشيرة المصرية'}},
   {id:'th',ar:'تايلاند',en:'Thailand',city:'بانكوك',emoji:'🛕',g:['#6a1b9a','#ffb300'],region:'asia',budget:2,seasons:['winter'],tags:['طعام','شواطئ','تسوق'],flight:'7س',cur:'THB',rating:4.5,
    about:'شواطئ وجزر ومطاعم وتسوق وأنشطة مغامرات.',
    req:{visa:'free',stay:60,passport:'6 أشهر',docs:['جواز سفر ساري','تذكرة مغادرة','إثبات إقامة','قد يُطلب إثبات مالي'],vaccines:'لا إلزام عام',quarantine:'لا يوجد',embassy:{name:'سفارة تايلاند - الرياض',phone:'+966 11 000 0005',addr:'الرياض'},verified:'2025-01-10',source:'وزارة الخارجية التايلاندية'}},
   {id:'gb',ar:'بريطانيا',en:'United Kingdom',city:'لندن',emoji:'🎡',g:['#1d3557','#e63946'],region:'europe',budget:3,seasons:['summer','spring'],tags:['تعليم','تسوق','ثقافة'],flight:'6س 30د',cur:'GBP',rating:4.5,
    about:'متاحف ومسرح وتسوق وجامعات عالمية.',
    req:{visa:'eta',stay:180,passport:'سارية طوال الإقامة',docs:['جواز سفر ساري','تصريح ETA قبل السفر','حجز إقامة','إثبات مالي'],vaccines:'لا يوجد',quarantine:'لا يوجد',embassy:{name:'السفارة البريطانية - الرياض',phone:'+966 11 000 0006',addr:'الحي الدبلوماسي'},verified:'2025-01-10',source:'GOV.UK'}},
   {id:'fr',ar:'فرنسا',en:'France',city:'باريس',emoji:'🗼',g:['#283593','#f48fb1'],region:'europe',budget:3,seasons:['spring','summer','autumn'],tags:['رومانسي','ثقافة','تسوق'],flight:'6س 30د',cur:'EUR',rating:4.6,
    about:'برج إيفل واللوفر والأزياء والمطبخ الفرنسي.',
    req:{visa:'required',stay:90,passport:'3 أشهر بعد المغادرة',docs:['تأشيرة شنقن','تأمين سفر (30,000 يورو)','حجز طيران وفندق','كشف حساب بنكي','خطاب عمل/كفالة'],vaccines:'لا يوجد',quarantine:'لا يوجد',embassy:{name:'السفارة الفرنسية - الرياض',phone:'+966 11 000 0007',addr:'الحي الدبلوماسي'},verified:'2025-01-10',source:'France-Visas'}},
   {id:'az',ar:'أذربيجان',en:'Azerbaijan',city:'باكو',emoji:'🔥',g:['#0d47a1','#26a69a'],region:'caucasus',budget:1,seasons:['spring','autumn'],tags:['اقتصادي','طبيعة','تسوق'],flight:'3س 30د',cur:'AZN',rating:4.3,
    about:'مدينة عصرية على بحر قزوين وجبال وقرى.',
    req:{visa:'evisa',stay:30,passport:'6 أشهر',docs:['جواز سفر ساري','تأشيرة ASAN إلكترونية','حجز فندق'],vaccines:'لا يوجد',quarantine:'لا يوجد',embassy:{name:'سفارة أذربيجان - الرياض',phone:'+966 11 000 0008',addr:'الرياض'},verified:'2025-01-10',source:'ASAN Visa'}},
   {id:'ma',ar:'المغرب',en:'Morocco',city:'مراكش',emoji:'🕍',g:['#c0392b','#16a085'],region:'africa',budget:2,seasons:['spring','autumn','winter'],tags:['ثقافة','طبيعة','طعام'],flight:'6س',cur:'MAD',rating:4.5,
    about:'أسواق وقصور وصحراء وجبال الأطلس.',
    req:{visa:'free',stay:90,passport:'6 أشهر',docs:['جواز سفر ساري','حجز إقامة','تذكرة عودة'],vaccines:'لا يوجد',quarantine:'لا يوجد',embassy:{name:'سفارة المغرب - الرياض',phone:'+966 11 000 0009',addr:'الرياض'},verified:'2025-01-10',source:'وزارة الشؤون الخارجية المغربية'}}
  ],
  /* أسعار تقريبية للتجربة؛ الأصل: مصدر أسعار صرف حي. الريال مربوط بالدولار 3.75 */
  ratesPerSAR:{SAR:1,USD:0.2667,AED:0.9793,EUR:0.245,GBP:0.2107,TRY:9.1,GEL:0.72,EGP:13.0,THB:9.2,AZN:0.453,MAD:2.65},
  reviews:[
   {d:'tr',n:'عبدالله ع.',s:5,t:'رحلة عائلية ممتازة، الإجراءات سريعة في المطار.'},
   {d:'mv',n:'سارة م.',s:5,t:'التأشيرة عند الوصول سهلة، جهّزوا نموذج الهجرة مسبقًا.'},
   {d:'ge',n:'فهد ق.',s:4,t:'طبيعة رائعة وأسعار مناسبة، والطقس متقلب.'},
   {d:'ae',n:'نورة ح.',s:5,t:'السفر بالهوية الوطنية وفّر علينا وقت كثير.'}
  ]
};

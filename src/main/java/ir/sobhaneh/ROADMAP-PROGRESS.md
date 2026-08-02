# راهنمای پیاده‌سازی پیام‌رسان — نسخهٔ راهنما (بدون کد آماده)

### مخصوص تازه‌کار جاوا — این نسخه به‌جای دادن کد، به شما می‌گوید **چه چیزی** لازم دارید (اتریبیوت‌ها، توابع، روابط بین کلاس‌ها) تا خودتان بنویسید.

> هرجا "✅ انجام شده" است یعنی از قبل دارید. هرجا "🔲 باید بسازید" است، باید خودتان
> کلاس/متد را طراحی و پیاده کنید — اینجا فقط مشخصات آن (چه فیلدهایی، چه متدهایی،
> با چه ورودی/خروجی، و به چه چیز دیگری وصل می‌شود) توضیح داده شده.

---

## ۰. ساختار نهایی پروژه

```
messenger-project/
├── central-server/
│   ├── CentralServer.java               ✅
│   ├── ClientHandler.java               ✅
│   ├── HostManager.java                 ✅
│   ├── HostRegistrationSession.java     ✅
│   ├── VerificationService.java         ✅
│   ├── ReservationResult.java           ✅
│   ├── UserManager.java                 ✅ (نیاز به تکمیل اعتبارسنجی واقعی)
│   ├── WorkspaceManager.java            ✅
│   ├── TokenManager.java                ✅
│   ├── HostConnectionListener.java      ✅ (جدید — روز ۳؛ رفع race condition خواندن)
│   ├── models/HostInfo.java             ✅ (به‌روز شد — Connection → HostConnectionListener)
│   ├── models/User.java                 ✅
│   ├── models/WorkspaceInfo.java        ✅
│   ├── models/Token.java                ✅
│   └── persistence/DataStore.java       🔲
│
├── host/
│   ├── HostMain.java                    ✅ (به‌روز شد — روز ۳)
│   ├── HostRegistration.java            ✅
│   ├── HostConfig.java                  ✅
│   ├── HostSideWorkspaceManager.java    ✅ (به‌روز شد — روز ۳)
│   ├── CentralConnectionListener.java   ✅ (جدید — روز ۳؛ معادل HostConnectionListener سمت host)
│   ├── Workspace.java                   ✅ (اسکلت + آپدیت روز ۳؛ عمداً بدون فیلد نام)
│   ├── ClientConnection.java            ✅ (پیاده‌شده — روز ۳)
│   ├── ChatStore.java                   🔲
│   ├── models/Message.java              🔲
│   ├── models/Chat.java                 🔲
│   ├── models/UserSession.java          ✅ (پیاده‌شده — روز ۳)
│   └── persistence/HostDataStore.java   🔲
│
├── client/
│   ├── ClientMain.java                  🔲
│   ├── CentralConnection.java           🔲
│   ├── WorkspaceConnection.java         🔲
│   └── CommandParser.java               🔲
│
└── common/
    ├── Connection.java                  ✅
    └── ProtocolUtils.java               🔲 (پیشنهادی)
```

> ⚠️ فایل قدیمی `HostCommandHandler.java` (سمت host) دیگر استفاده نمی‌شود — جایش را
> `CentralConnectionListener.java` گرفته است. می‌توانید آن را حذف کنید یا نگه دارید،
> از هیچ‌جا صدا زده نمی‌شود.

---

## ⚠️ قانون اتصال‌ها (از سند اصلی)

| سناریو | اتصال | رفتار |
|--------|--------|--------|
| `register` تکی | مرکزی | باز کن → دستور → جواب → **ببند** |
| `login` (زیرسناریو) | مرکزی | همان اتصالِ باز |
| `create-workspace` | مرکزی | باز کن → `login` → `create-workspace` → جواب → **ببند** |
| `connect-workspace` | مرکزی | باز کن → `login` → `connect-workspace` → جواب → **ببند** |
| `connect` + چت | فضای کار | باز کن → `connect <token>` → (احتمالاً `username?`) → **باز بماند** |
| `create-host` | مرکزی | باز کن → ثبت‌نام → **برای همیشه باز بماند** |

نکتهٔ کلاینت: کلاینت با پارامترهای `phone_number` و `password` از خط فرمان اجرا
می‌شود و در هر سناریویی که نیاز به لاگین دارد، خودش اول یک `login` روی همان اتصال
می‌زند بدون این‌که دوباره از کاربر بپرسد.

---

## 🔴 اصل حیاتی جدید (کشف‌شده در روز ۳): هر سوکتِ دوطرفهٔ دائمی فقط یک خواننده دارد

در پیاده‌سازی روز ۳ به یک باگ معماری جدی برخوردیم که ارزش دارد به‌عنوان یک قانون
کلی برای بقیهٔ پروژه (روز ۴ و ۵) هم ثبت شود:

**اتصال `create-host` (central ↔ host) یک سوکت TCP دائمی و دوطرفه است** — هم
central می‌تواند رویش دستور بفرستد (`create-workspace`) و منتظر جواب بماند، هم
host می‌تواند رویش دستور بفرستد (`whois`) و منتظر جواب بماند. این یعنی روی این
یک سوکت، هر دو طرف گاهی نقش «فرستنده‌ی دستور و منتظرِ جواب» و گاهی نقش «گیرنده‌ی
دستور و فرستنده‌ی جواب» را دارند.

مشکل وقتی پیش می‌آید که **بیش از یک Thread همزمان `readLine()` روی همین یک سوکت
صدا بزند**. مثلاً:
- سمت central: هم Thread قدیمیِ `ClientHandler.run()` (که تا قبل از تأیید میزبان،
  در حال خواندن دستورهای host بود) و هم Thread جدیدی که `WorkspaceManager` برای
  فرستادن `create-workspace` به راه انداخته، هر دو ممکن است بخواهند از یک سوکت
  بخوانند.
- سمت host: هم Threadِ اصلی برنامه که مدام منتظر دستورهای central (مثل
  `create-workspace`) است، هم Threadِ مربوط به یک `ClientConnection` که می‌خواهد
  جواب `whois` را بخواند، هر دو ممکن است بخواهند از یک سوکت بخوانند.

نتیجه: هر پاسخی که برسد، هرکدام از این Threadها زودتر برسند آن را «می‌قاپند» —
حتی اگر آن پاسخ برای Thread دیگری بوده. این باعث می‌شود یکی از دو طرف تا ابد در
`readLine()` گیر کند (چون پاسخش را Thread دیگری خورده) و طرف مقابل هم داده‌ای
دریافت کند که انتظارش را نداشت.

### راه‌حل: الگوی «Listener + صف پاسخ» (Response Queue Pattern)

برای هر سوکت دائمی و دوطرفه، **دقیقاً یک** کلاس و **دقیقاً یک** Thread باید مالک
`readLine()` باشد. این کلاس:
1. در یک حلقهٔ بی‌نهایت پیوسته می‌خواند.
2. اگر خط دریافتی یک «دستورِ initiated از طرف مقابل» باشد (چیزی که این سمت باید
   پردازشش کند و جواب بدهد، مثل `whois` سمت central یا `create-workspace` سمت
   host)، خودش مستقیماً پردازش می‌کند و جواب می‌فرستد.
3. در غیر این صورت (یعنی این خط پاسخ به دستوری‌ست که خودِ این سمت قبلاً فرستاده)،
   آن را در یک `BlockingQueue<String>` می‌گذارد.
4. یک متد عمومی `sendAndWait(command)` دارد که هر Thread دیگری (مثلاً
   `WorkspaceManager` یا `ClientConnection`) می‌تواند صدا بزند: این متد دستور را
   می‌فرستد (زیر یک `synchronized`/قفل نوشتن تا دو دستور هم‌زمان قاطی نشوند) و
   بعد `pendingResponses.take()` می‌زند تا صبر کند پاسخ از همان Threadِ خواننده
   برسد.

این پیاده‌سازی شده در:
- **`HostConnectionListener`** (سمت central، برای هر `HostInfo`)
- **`CentralConnectionListener`** (سمت host، یک نمونه در کل برنامهٔ host)

⚠️ **یادداشت برای روز ۴ و ۵:** اگر بعداً کانال‌های دوطرفهٔ دائمی جدیدی اضافه شود
(مثلاً بین client و workspace برای چت، جایی که هم پیام‌های `send-message` از
کلاینت می‌رود و هم `receive-message` از سمت workspace می‌آید)، همین الگو باید
رعایت شود: یک Thread ثابت مسئول `readLine()`، پیام‌های push‌شده مستقیم چاپ/نمایش
داده شوند، و اگر پاسخ‌ همزمان (`sendAndWait`) هم لازم بود از صف استفاده شود.

---

## ⭐ اصول کلین‌کد این پروژه

1. هر تابع فقط یک کار انجام دهد (I/O، اعتبارسنجی، تغییر state، ساخت پاسخ — هرکدام جدا).
2. متدهای `dispatchX` فقط پارس ورودی + صدا زدن یک متد دامنه + ارسال پاسخ؛ منطق واقعی در متد دامنه.
3. لاک (`synchronized`) فقط دور بخش critical، هرگز دور I/O شبکه. (استثنا: قفل نوشتن روی سوکت مشترک در الگوی Listener بالا، چون آنجا لاک واقعاً محافظ عملیات atomic «فرستادن دستور + دریافت پاسخِ همان دستور» است، نه یک I/O دلخواه.)
4. پارس عدد/رشتهٔ تکراری را در یک کلاس کمکی مشترک (`ProtocolUtils`) قرار دهید.
5. متدهای placeholder را با `// TODO` مشخص کنید.
6. رمز عبور هرگز plain-text ذخیره نشود — هش کنید.
7. بدون Magic Number — هر عدد/رشتهٔ تکرارشونده باید یک ثابت (`static final`) با نام معنادار باشد؛ مثل طول توکن، طول کد تأیید، عمر توکن، حداکثر طول نام فضای کار.
8. **یک سوکت دائمی و دوطرفه، یک خواننده** — هیچ‌وقت دو Thread مختلف مستقیماً روی یک `Connection` مشترک `readLine()` صدا نزنند؛ همیشه از الگوی Listener + صف پاسخ بالا استفاده کنید.

---

## تقسیم‌بندی ۵ روزه

| روز | موضوع | سختی |
|-----|--------|------|
| ۱ | کاربران + login state | متوسط |
| ۲ | ایجاد فضای کار | متوسط‌روبه‌بالا |
| ۳ | اتصال و توکن | متوسط‌روبه‌بالا |
| ۴ | کلاینت + پایهٔ چت | متوسط‌روبه‌بالا |
| ۵ | چت کامل + disconnect + shutdown | متوسط‌روبه‌بالا |

---

# روز ۱ — کاربران ✅ (تکمیل‌شده، نیاز به سخت‌سازی امنیتی)

### پروتکل (از سند)

```
کلاینت → مرکزی:  register 09123456789 123456
مرکزی → کلاینت:  OK
```
بعد از `OK` کلاینت اتصال را می‌بندد.

```
کلاینت → مرکزی:  login 09123456789 123456
مرکزی → کلاینت:  OK
```
خطاهای پیشنهادی (طبق الگوی کلی پروژه، با `ERROR ` شروع می‌شوند):
`ERROR User already exists` / `ERROR Invalid Phone Number` / `ERROR Invalid Password` /
`ERROR User doesn't exists` / `ERROR Incorrect Password`

### کارهای باقی‌مانده (فقط توضیح، نه کد)

1. **هش پسورد در `User.java`**: پسورد نباید هیچ‌وقت plain-text نگه‌داری شود. کافی
   است متدی که پسورد را ست می‌کند، قبل از ذخیره آن را هش کند (مثلاً با یکی از
   الگوریتم‌های هش استاندارد جاوا)، و متد مقایسه هم پسورد ورودی را هش کرده و با
   مقدار ذخیره‌شده مقایسه کند — پسورد اصلی هرگز جایی نگه‌داری نمی‌شود.
2. **`checkPhoneNumber` واقعی**: یک قانون ساده تعریف کنید (مثلاً ۱۱ رقم و شروع با
   `09`) و در `UserManager` قبل از ثبت‌نام آن را چک کنید.
3. **`checkPassword` واقعی**: یک حداقل طول (مثلاً ۶ کاراکتر) تعریف و چک کنید.
4. **یکدست کردن پیام‌های خطا در `UserManager.login`**: وقتی `checkPassword` واقعی
   شد، مسیر `ERROR Incorrect Password` باید واقعاً قابل دسترس باشد (الان چون
   اعتبارسنجی fake است، همیشه true برمی‌گرداند).

### تست با telnet
```
register 09123456789 123456     → OK
register 09123456789 123456     → ERROR User already exists
login 09123456789 123456        → OK
login 09123456789 wrongpass     → ERROR Incorrect Password
```

⚠️ **نکتهٔ ابزار تست (کشف‌شده در روز ۳، ولی برای همهٔ روزها صدق می‌کند):** برای
تست دستی سوکت‌های خام، از `telnet` استفاده نکنید — برنامهٔ telnet قبل از هر چیز
بایت‌های option-negotiation می‌فرستد که سرور آن‌ها را به‌عنوان اولین خط پروتکل
می‌خواند و رد می‌کند. به‌جایش از **PuTTY در حالت Raw** (نه Telnet)، یا `nc`/`ncat`،
یا یک اسکریپت کوچک PowerShell/Python با سوکت خام استفاده کنید.

---

# روز ۲ — ایجاد فضای کار ✅ (تکمیل‌شده)

### پروتکل (از سند)

```
۱. کاربر تایپ می‌کند:            create-workspace company1
۲. کلاینت → مرکزی (بعد از login): create-workspace company1
۳. مرکزی → میزبان:               create-workspace 10143 1001
۴. میزبان → مرکزی:               OK
۵. مرکزی → کلاینت:               OK 127.0.0.1 10143
۶. کلاینت اتصال با مرکزی را می‌بندد.
```

### قانون نام فضای کار
حداکثر ۶۰ کاراکتر، فقط اعداد و حروف کوچک/بزرگ انگلیسی و `_`.

### `validateWorkspaceName` ✅ پیاده‌سازی شده

در `WorkspaceManager` (central) این متد پیاده شده:
- **ورودی:** نام پیشنهادی فضای کار (رشته)
- **خروجی:** یا `null` (معتبر) یا پیام خطای آماده برای کلاینت
- دو ثابت: `MAX_WORKSPACE_NAME_LENGTH = 60` و `WORKSPACE_NAME_PATTERN` (حروف
  بزرگ/کوچک انگلیسی، رقم، `_`).
- **قبل از** چک تکراری‌نبودن نام، در ابتدای `createWorkspace` صدا زده می‌شود.

⚠️ **نکتهٔ مهم دربارهٔ محل نگه‌داری نام:** نام فضای کار فقط سمت **central** (در
`WorkspaceInfo`) نگه‌داری می‌شود. طبق پروتکل بالا (قدم ۳)، وقتی central به host
دستور `create-workspace` می‌فرستد، فقط `port` و `userId` ارسال می‌شود — نام
فضای کار هرگز به host فرستاده نمی‌شود. در نتیجه سمت **host** اصلاً فیلدی برای
نام فضای کار وجود ندارد؛ `Workspace` (سمت host) فقط با `port` خودش شناخته
می‌شود، نه با نام. این یک تصمیم عمدی است، نه نقص.

### 🐛 باگ کشف‌شده و رفع‌شده در `create-workspace` (مرتبط با روز ۳)

هنگام پیاده‌سازی اولیهٔ `WorkspaceManager.notifyHost`، خطای `ERROR Host failed to
create workspace` به‌طور نامنظم (intermittent) رخ می‌داد. علتش **race condition**
در خواندن از سوکت central↔host بود (شرح کامل در بخش «اصل حیاتی جدید» بالا) —
Thread قدیمیِ `ClientHandler.run()` که هنوز روی همان سوکت `readLine()` می‌زد،
جواب `OK` مخصوص `notifyHost` را می‌قاپید. با معرفی `HostConnectionListener` این
مشکل کاملاً رفع شد؛ الان `notifyHost` از `host.getConnectionListener().sendAndWait(...)`
استفاده می‌کند.

### تست
```
login 09123456789 123456
create-workspace company1
→ OK 127.0.0.1 10143
create-workspace company1
→ ERROR Workspace already exists
create-workspace "bad name!"
→ ERROR Workspace name must contain only letters, digits, and underscore
```

---

# روز ۳ — اتصال به فضای کار و احراز هویت ✅ (تکمیل‌شده)

### پروتکل (از سند) — ۹ قدم

```
۱. کاربر تایپ می‌کند:              connect-workspace company1
۲. کلاینت → مرکزی (بعد از login):  connect-workspace company1
۳. مرکزی → کلاینت:                OK 127.0.0.1 10143 fkla48fhhf
۴. کلاینت اتصال با مرکزی را می‌بندد و به فضای کار وصل می‌شود:
   کلاینت → فضای‌کار:              connect fkla48fhhf
۵. فضای کار (میزبان) → مرکزی:      whois fkla48fhhf
۶. مرکزی → فضای کار:               OK 1001
۷. اگر اولین اتصال این کاربر به این فضای کار باشد:
   فضای‌کار → کلاینت:              username?
۸. کلاینت → فضای‌کار:              ahmad
۹. فضای‌کار → کلاینت:              OK
```

نکات مهم:
- **توکن موقت:** ۱۰ کاراکتر، فقط از حروف کوچک انگلیسی و رقم (`a-z0-9`)، حداکثر ۵
  دقیقه عمر.
- **whois** باید روی همان اتصال باز میان میزبان و مرکزی زده شود (همان اتصالی که از
  `create-host` باقی مانده)، نه یک اتصال جدید. ✅ (از طریق `HostConnectionListener`/`CentralConnectionListener`)
- **کلاینت هم‌زمان فقط به یک فضای کار وصل است** — یعنی طرف کلاینت، شیء مدیریت‌کنندهٔ
  اتصال به فضای کار باید در هر لحظه حداکثر یک اتصال فعال داشته باشد. (این بخش
  مربوط به روز ۴، سمت client است — هنوز پیاده نشده.)
- نکته: نیازی نیست توکن یا پاسخ `whois` نام فضای کار را هم حمل کند. چون هر
  `Workspace` (فضای کار) روی یک پورت اختصاصی خودش گوش می‌دهد، همان `Workspace`ای
  که اتصال کلاینت را می‌پذیرد (و در نتیجه `ClientConnection` را می‌سازد) از قبل
  مشخص است — یعنی معلوم است این اتصال برای کدام فضای کار است، بدون نیاز به هیچ
  اطلاعات اضافه‌ای. توکن فقط برای این لازم است که میزبان از مرکزی بپرسد «این توکن
  متعلق به کدام کاربر است» (`whois` → `OK <userId>`)، و فرمت سند همین‌جا کافی است.

  ⚠️ به همین دلیل، سمت host اصلاً فیلد «نام فضای کار» نگه‌داری نمی‌شود — نه در
  `Workspace`، نه جای دیگری. `Workspace` فقط با `port` خودش شناخته می‌شود. اگر در
  آینده (مثلاً روز ۵، برای نام‌گذاری فایل `HostDataStore`) نیاز به نام واقعی شد،
  باید پروتکل `create-workspace` بین central و host تغییر کند تا نام را هم حمل کند؛
  تا آن زمان این تصمیم عمداً به تعویق افتاده است.

### فایل‌هایی که ساخته/تغییر داده شدند

```
central-server/models/Token.java              ✅
central-server/TokenManager.java              ✅
central-server/ClientHandler.java             ✅ (دستورات جدید: connect-workspace)
central-server/HostConnectionListener.java    ✅ (جدید — رفع race condition)
central-server/models/HostInfo.java           ✅ (به‌روز — Connection → HostConnectionListener)
central-server/HostRegistrationSession.java   ✅ (به‌روز — می‌سازد و اجرا می‌کند HostConnectionListener را)
central-server/WorkspaceManager.java          ✅ (به‌روز — notifyHost از sendAndWait استفاده می‌کند)

host/ClientConnection.java                    ✅ (پیاده‌شده)
host/models/UserSession.java                  ✅ (پیاده‌شده)
host/Workspace.java                           ✅ (پذیرش اتصال کلاینت + ارجاع به ClientConnection)
host/CentralConnectionListener.java           ✅ (جدید — معادل host-side، رفع race condition)
host/HostMain.java                            ✅ (به‌روز — از CentralConnectionListener استفاده می‌کند)
host/HostSideWorkspaceManager.java            ✅ (به‌روز — centralConnectionListener را نگه می‌دارد)
```

### مشخصات کلاس‌ها و متدها

**`Token` (central/models)** ✅
- اتریبیوت‌ها: مقدار توکن (رشته)، شناسهٔ کاربر مالک آن، نام فضای کار (اضافه بر
  حداقل مورد نیاز، برای دیباگ نگه داشته شده)، زمان انقضا (long، برحسب میلی‌ثانیه).
- متد: `isExpired()` که با مقایسهٔ زمان فعلی سیستم با زمان انقضا، `true`/`false`
  برمی‌گرداند.

**`TokenManager` (central)** ✅
- اتریبیوت: `ConcurrentHashMap<String, Token>` — نگاشت thread-safe از مقدار توکن
  به شیء `Token`.
- ثابت‌ها: `MAX_TOKEN_LENGTH = 10`، `TOKEN_EXPIRATION_MILLISECONDS = 5 * 60 * 1000`.
- `createToken(creatorUserId, workspaceName)`: یک رشتهٔ تصادفی ۱۰‌کاراکتری از
  الفبای مجاز می‌سازد، در map ذخیره می‌کند، شیء `Token` را برمی‌گرداند.
- `resolve(token)`: ابتدا با `findByToken` مقدار را می‌خواند و برای `null` بودن
  چک می‌کند (⚠️ نکتهٔ ایمنی رعایت‌شده — قبل از `isExpired()` چک null انجام
  می‌شود تا `NullPointerException` رخ ندهد)، اگر منقضی بود حذف و `null` برمی‌گرداند.
- پاکسازی دورهٔ توکن‌های منقضی: هنوز اضافه نشده (نشتی حافظهٔ قابل قبول برای این فاز).

**دستورات جدید در `ClientHandler` (central)** ✅

- `connect-workspace <name>`: چک لاگین‌بودن روی همین اتصال → پیدا کردن فضای کار
  با `workspaceManager.findByName(name)` → اگر پیدا نشد `ERROR workspace not
  found` → اگر پیدا شد، `tokenManager.createToken` و پاسخ با فرمت
  `OK <ip> <port> <token>`.
- `whois <token>`: 🔁 **تغییر معماری نسبت به طرح اولیه** — دیگر در `ClientHandler`
  پیاده نیست (چون `whois` یک دستور host-initiated روی کانال دائمی central↔host
  است، نه چیزی که یک کلاینت معمولی از طریق `ClientHandler` بفرستد). به‌جایش
  مستقیماً داخل `HostConnectionListener.handleWhois` پردازش می‌شود — همان
  Threadی که مالک خواندن از سوکت host است. این تغییر دقیقاً همان چیزی‌ست که در
  بخش «اصل حیاتی جدید» بالا توضیح داده شد.

**`UserSession` (host/models)** ✅
- اتریبیوت‌ها (هر سه `final`، به‌صورت `record`): `Connection connection`،
  `long userId`، `String username`.
- immutable — طبق پیشنهاد، ساخت `UserSession` در `ClientConnection` بعد از هر دو
  مرحلهٔ `authenticate` و `resolveUsername` انجام می‌شود.

**`ClientConnection` (host)** ✅ — سه مرحلهٔ جدا پیاده شده:

1. **`authenticate()`:** خط اول را می‌خواند، فرمت `connect <token>` را چک
   می‌کند. سپس به‌جای خواندن/نوشتن مستقیم روی `centralConnection`، از
   `centralConnectionListener.sendAndWait("whois " + token)` استفاده می‌کند —
   این متد خودش داخلی synchronized است و پاسخ را از صف مشترک می‌گیرد، بدون
   رقابت با Thread اصلی که دستورهای central-initiated (مثل `create-workspace`)
   را می‌خواند.
2. **`resolveUsername(userId)`:** از `workspace.findExistingUsername(userId)`
   می‌پرسد؛ اگر بود همان را برمی‌گرداند؛ وگرنه `username?` می‌فرستد و پاسخ کلاینت
   را می‌خواند.
3. **ثبت session:** `UserSession` ساخته و `workspace.addSession(session)` صدا
   زده می‌شود. (بخش «شروع گوش دادن به دستورات بعدی» هنوز باقی مانده — روز ۵.)

**`Workspace` (host)** ✅ به‌روز شده
- فیلد نام اضافه **نشد** (طبق تصمیم نهایی — یادداشت کامل در بخش زیر).
- دو `ConcurrentHashMap` دارد: `onlineSessionsByUserId` (userId → UserSession) و
  `userIdByUsername` (username → userId).
- `findExistingUsername(userId)`: برای استفاده در `resolveUsername`.
- `addSession(session)`: افزودن session جدید به هر دو نگاشت.
- 🔁 **تغییر معماری:** به‌جای نگه‌داشتن مستقیم `Connection centralConnection`،
  الان یک ارجاع به `CentralConnectionListener centralConnectionListener` نگه
  می‌دارد (که در `acceptLoop` به هر `ClientConnection` جدید پاس داده می‌شود) —
  چون دیگر هیچ کلاسی غیر از خودِ `CentralConnectionListener` اجازه ندارد مستقیم
  از سوکت central بخواند.

⚠️ توجه: این نگاشت‌ها فقط کاربران **آنلاین** را نشان می‌دهند. یکتایی واقعی نام
کاربری در طول عمر فضای کار (نه فقط لحظهٔ آنلاین بودن) وقتی معنا پیدا می‌کند که
`HostDataStore` (روز ۵) داده‌های ذخیره‌شده را هم در نظر بگیرد.

---

### 🔴 به‌روزرسانی معماری: الگوی Listener + صف پاسخ (به‌جای دسترسی مستقیم به `Connection`)

نسخهٔ اولیهٔ طرح روز ۳ فرض می‌کرد `HostInfo` (سمت central) و `Workspace` (سمت host)
مستقیماً یک شیء `Connection` نگه می‌دارند و هر جا لازم بود، همان‌جا
`sendLine`/`readLine` صدا زده می‌شود. در عمل این باعث race condition شد (شرح کامل
در بخش «اصل حیاتی جدید» بالای همین سند). نسخهٔ نهایی به‌جای آن:

**سمت central:**
- کلاس جدید `HostConnectionListener` ساخته شد. `HostRegistrationSession` بعد از
  تأیید موفق میزبان (`finalizeVerification`)، به‌جای `pendingHost.setConnection(connection)`،
  یک `HostConnectionListener` می‌سازد، آن را با `Thread` (daemon) اجرا می‌کند، و
  در `HostInfo.connectionListener` ذخیره می‌کند.
- `WorkspaceManager.notifyHost` به‌جای خواندن/نوشتن مستقیم، فقط
  `host.getConnectionListener().sendAndWait("create-workspace " + port + " " + userId)`
  صدا می‌زند.
- خود `HostConnectionListener` وقتی خط دریافتی با `whois ` شروع شود، خودش مستقیم
  جواب می‌دهد (بدون نیاز به `ClientHandler`)؛ در غیر این صورت خط را در صف
  می‌گذارد تا `sendAndWait` بردارد.

**سمت host:**
- کلاس جدید `CentralConnectionListener` (معادل کاملاً موازی `HostConnectionListener`)
  ساخته شد. `HostMain` بعد از موفقیت `HostRegistration.register`، یک
  `CentralConnectionListener` می‌سازد، آن را روی یک `Thread` اجرا می‌کند و منتظرش
  می‌ماند (`join`).
- `HostSideWorkspaceManager` این listener را نگه می‌دارد (چون به دلیل وابستگی
  چرخشی بین `HostMain` و `HostSideWorkspaceManager`، از طریق یک setter بعد از
  ساخت هر دو تزریق می‌شود) و آن را به هر `Workspace` جدید که می‌سازد پاس می‌دهد.
- `Workspace` این listener را به هر `ClientConnection` که در `acceptLoop` می‌سازد
  پاس می‌دهد.
- `ClientConnection.authenticate()` به‌جای خواندن/نوشتن مستقیم روی
  `centralConnection`، فقط `centralConnectionListener.sendAndWait("whois " + token)`
  صدا می‌زند.
- خود `CentralConnectionListener` وقتی خط دریافتی با `create-workspace ` شروع
  شود، خودش پردازش می‌کند (با تفویض به `HostSideWorkspaceManager.handleCreateWorkspace`)؛
  در غیر این صورت خط را در صف می‌گذارد.

**فایل حذف‌شده/منسوخ:** `HostCommandHandler.java` (سمت host) دیگر استفاده نمی‌شود.

### نکتهٔ ابزار تست: چرا `telnet` روی این مرحله جواب نمی‌داد

هنگام تست دستی این مرحله با `telnet`، دستور `connect <token>` هیچ پاسخی
برنمی‌گرداند و کلاینت هیچ‌وقت `username?` دریافت نمی‌کرد. علت این بود که `telnet`
قبل از فرستادن هر چیزی که کاربر تایپ می‌کند، چند بایت option-negotiation
می‌فرستد؛ سرور (`ClientConnection.authenticate`) این بایت‌ها را به‌عنوان اولین خط
می‌خواند، چون با `"connect "` شروع نمی‌شود `ERROR Invalid connect command` برمی‌گرداند
و آن Thread بلافاصله تمام می‌شود — پس هرچه بعداً در همان جلسهٔ telnet تایپ شود
دیگر خوانده نمی‌شود.

**راه‌حل:** برای تست دستی این‌جور سوکت‌های خام، به‌جای `telnet` از یکی از این‌ها
استفاده کنید:
- **PuTTY** با «Connection type» روی **Raw** (نه Telnet) تنظیم شود.
- `nc`/`ncat` (روی ویندوز از طریق نصب Nmap در دسترس است).
- یک اسکریپت کوچک، مثلاً در PowerShell:
  ```powershell
  $client = New-Object System.Net.Sockets.TcpClient("localhost", <port>)
  $stream = $client.GetStream()
  $writer = New-Object System.IO.StreamWriter($stream)
  $reader = New-Object System.IO.StreamReader($stream)
  $writer.AutoFlush = $true
  $writer.WriteLine("connect <token>")
  Write-Host $reader.ReadLine()
  ```

### تست جریان کامل (تأیید‌شده که کار می‌کند)
```
--- اتصال اول به central ---
login 09123456789 123456        → OK
connect-workspace company1      → OK 127.0.0.1 10143 fkla48fhhf
--- (این اتصال بسته می‌شود) ---

--- اتصال جدید به فضای کار (میزبان، پورت ۱۰۱۴۳)، با nc/PuTTY-Raw/اسکریپت (نه telnet) ---
connect fkla48fhhf              → username?
ahmad                            → OK
--- این اتصال باز می‌ماند ---

--- تست توکن نامعتبر ---
connect notarealtoken123        → ERROR Invalid or expired token

--- تست session تکراری با همان توکن (قبل از انقضا) ---
--- اتصال سوم با همان token ---
connect fkla48fhhf              → OK (مستقیم، بدون username? — چون findExistingUsername کار می‌کند)
```

---

# روز ۴ — برنامهٔ کلاینت + پایهٔ چت

هدف: به‌جای تست با telnet/nc/PuTTY، یک برنامهٔ کلاینت واقعی که کل سناریوهای بالا را خودکار
انجام دهد.

### نکات لازم از سند

- کلاینت با پارامترهای `phone_number` و `password` به عنوان **آرگومان خط فرمان**
  اجرا می‌شود، نه چیزی که هر بار پرسیده شود.
- کلاینت باید حداقل **دو Thread** داشته باشد: یکی برای خواندن پیام‌های ورودی از
  سوکت فضای کار و چاپشان روی کنسول، یکی برای خواندن دستورات کاربر از کنسول و
  ارسالشان.

⚠️ **یادآوری از اصل حیاتی روز ۳:** اتصال کلاینت به فضای کار (`connect` + چت) هم
یک سوکت دائمی و بالقوه دوطرفه است (کلاینت هم `send-message` می‌فرستد و منتظر
`OK <seq>` می‌ماند، هم ممکن است هر لحظه `receive-message` push‌شده از سمت
workspace دریافت کند). طراحی `WorkspaceConnection` باید از همان ابتدا این را در
نظر بگیرد: یک Thread ثابت مسئول خواندن پیوسته از سوکت باشد که پیام‌های
`receive-message` را مستقیم چاپ می‌کند، و برای پاسخ‌های synchronous (مثل جواب
`send-message`) باید به همان الگوی صف پاسخ فکر کرد — نه این‌که فرض شود
`readLine()` ساده برای هر دستور کافی‌ست، چون در این صورت همان باگ روز ۳ (قاپیدن
پاسخ توسط Thread اشتباه) دوباره تکرار می‌شود.

### فایل‌ها

```
client/ClientMain.java              🔲
client/CentralConnection.java       🔲
client/WorkspaceConnection.java     🔲
client/CommandParser.java           🔲
host/models/Message.java            🔲
host/models/Chat.java               🔲
```

### مشخصات

**`CentralConnection` (client)**
- اتریبیوت‌ها: آدرس و پورت مرکزی، شمارهٔ تلفن، پسورد (از آرگومان‌های خط فرمان).
- هر متد سطح بالا (`register`، `createWorkspace`، `connectWorkspace`) باید خودش
  یک اتصال جدید به مرکزی باز کند، دستور مربوطه (و در صورت نیاز `login` قبل از آن)
  را بفرستد، پاسخ را بخواند و اتصال را ببندد.
- چون باز/بستن سوکت در چند متد تکرار می‌شود، طبق قانون کلین‌کد آن را در یک متد
  خصوصی مشترک قرار دهید (باز کردن سوکت → اجرای یک عملیات دلخواه روی آن → بستن
  سوکت). می‌توانید از یک اینترفیس تابعی (functional interface) برای پارامتر
  "عملیات دلخواه" استفاده کنید.
- `connectWorkspace` باید پاسخ `OK <ip> <port> <token>` را پارس کرده و این سه
  مقدار را به فراخواننده برگرداند (مثلاً به شکل آرایه یا یک رکورد/کلاس ساده).
- توجه: چون این اتصال‌ها کوتاه‌عمر و تک‌درخواستی هستند (باز → دستور → جواب →
  بسته)، نیازی به الگوی Listener/صف اینجا نیست — آن الگو فقط برای سوکت‌های
  **دائمی و دوطرفه** لازم است.

**`WorkspaceConnection` (client)**

سه مرحلهٔ جدا (طبق قانون تک‌مسئولیتی):
1. **باز کردن سوکت** به آدرس/پورت فضای کار.
2. **احراز هویت:** فرستادن `connect <token>`، خواندن پاسخ؛ اگر `username?` بود، از
   کنسول نام کاربری بگیرید و بفرستید، پاسخ نهایی را بخوانید.
3. **راه‌اندازی Thread خواننده:** یک Thread جداگانه (daemon) که پیوسته از سوکت خط
   می‌خواند و روی کنسول چاپ می‌کند تا پیام‌های `receive-message` نمایش داده شوند.

متدهای دیگر لازم روی این کلاس: فرستادن `send-message`، `get-chats`،
`get-messages`، و `disconnect` (هرکدام فقط یک خط با فرمت مناسب می‌فرستند).

**`CommandParser` (client)**
- یک متد که یک خط ورودی کاربر را می‌گیرد و آن را به «نام دستور» و «آرگومان‌ها»
  تقسیم می‌کند.
- ⚠️ نکتهٔ مهم: چون `send-message` یک JSON در انتها دارد که خودش می‌تواند فاصله
  داشته باشد (مثل `{"type": "text", "body": "..."}`), باید تقسیم‌بندی خط با
  محدودیت تعداد بخش‌ها انجام شود (مثلاً حداکثر ۳ بخش: نام دستور، آرگومان اول،
  بقیهٔ خط به عنوان یک تکه) تا JSON خرد نشود.

**`ClientMain`**
- از آرگومان‌های خط فرمان `phone_number` و `password` را می‌خواند.
- شیء‌های `CentralConnection`، `WorkspaceConnection`، `CommandParser` را می‌سازد.
- یک حلقهٔ بی‌نهایت دارد که از کنسول خط می‌خواند، آن را پارس می‌کند، و بر اساس نام
  دستور، متد مربوطه را روی `CentralConnection` یا `WorkspaceConnection` صدا
  می‌زند.

**`Message` (host/models)**
اتریبیوت‌های لازم: شمارهٔ ترتیبی پیام (seq)، نام کاربری فرستنده، نوع پیام (مثل
"text")، متن پیام، و زمان ارسال.

**`Chat` (host/models)**
اتریبیوت‌های لازم: نام دو کاربر طرف گفتگو، لیست پیام‌های ردوبدل‌شده، و آخرین
`seq` استفاده‌شده (برای تولید `seq` بعدی).
یک متد کمکی برای ساخت یک «کلید» یکتای گفتگو از روی دو نام کاربری لازم دارید —
باید مستقل از ترتیب دو نام باشد (یعنی گفتگوی ahmad-saeed و saeed-ahmad باید به
یک کلید برسند؛ مثلاً با مرتب‌سازی الفبایی دو نام قبل از ترکیبشان).

### تست
همهٔ دستورات `register`/`login`/`create-workspace`/`connect-workspace`/`connect`
را از طریق کلاینت واقعی (نه telnet/nc) اجرا کنید.

---

# روز ۵ — چت کامل + قطع اتصال + ذخیره‌سازی

### پروتکل (از سند)

**ارسال پیام:**
```
کلاینت → فضای‌کار:  send-message saeed {"type": "text", "body": "Salam chetori?"}
فضای‌کار → فرستنده: OK 1
فضای‌کار → گیرنده (اگر آنلاین است):
  receive-message ahmad {"seq": 1, "from": "ahmad", "type": "text", "body": "Salam chetori?"}
```

**لیست چت‌ها:**
```
کلاینت → فضای‌کار:  get-chats
فضای‌کار → کلاینت:  OK [{"name": "saeed", "unread_count": 2}, ...]
```

**گفتگو با کاربر دیگر:**
```
کلاینت → فضای‌کار:  get-messages saeed
فضای‌کار → کلاینت:  OK [{"seq": 1, "from": "ahmad", "type": "text", "body": "Salam chetori?"}, ...]
```
⚠️ این دستور یک اثر جانبی دارد: تعداد پیام‌های خوانده‌نشده باید صفر شود.

**قطع اتصال:**
```
کلاینت → فضای‌کار:  disconnect
```

**ذخیره‌سازی (فاز دوم سند):** دستور `shutdown` در کنسول سرور مرکزی یا میزبان،
همهٔ داده‌ها را در یک فایل مشخص ذخیره می‌کند. هنگام راه‌اندازی، اگر فایل موجود
باشد، بارگیری می‌شود.

⚠️ **یادآوری مرتبط با نبود نام workspace در host:** اگر می‌خواهید فایل
`HostDataStore` را بر اساس نام فضای کار سازمان‌دهی کنید (مثلاً یک فایل جدا برای
هر workspace با نام آن)، باید همان‌جا اول تصمیم بگیرید که یا بر اساس `port`
سازمان‌دهی کنید (چون host اصلاً نام ندارد)، یا در همین مرحله پروتکل
`create-workspace` را طوری تغییر دهید که central نام را هم به host بفرستد. این
تصمیم را پیش از شروع پیاده‌سازی `HostDataStore` بگیرید.

### فایل‌ها

```
host/ClientConnection.java             ✅ تغییر (send-message / get-chats / get-messages / disconnect)
host/Workspace.java                    ✅ تغییر
host/ChatStore.java                    🔲
host/persistence/HostDataStore.java    🔲
central-server/persistence/DataStore.java 🔲
(+ تغییر HostMain.java و CentralServer.java برای دستور shutdown و بارگیری اولیهٔ داده)
```

### مشخصات

**`ChatStore` (host)** — جدا از `Workspace` نگه دارید تا `Workspace` شلوغ نشود.
- اتریبیوت‌ها: نگاشت thread-safe از کلید گفتگو (خروجی همان متد کمکی در `Chat`) به
  شیء `Chat`؛ و یک ساختار برای شمارش پیام‌های خوانده‌نشدهٔ هر کاربر نسبت به هر
  طرف گفتگو (مثلاً نگاشتی با کلید ترکیبی «کاربر + طرف مقابل» به عدد).
- متد افزودن پیام: ورودی‌ها فرستنده، گیرنده، نوع پیام، متن پیام. باید گفتگوی
  مربوطه را پیدا/بسازد، پیام را با `seq` بعدی به آن اضافه کند، شمارندهٔ
  خوانده‌نشدهٔ گیرنده را یکی زیاد کند، و `seq` تولیدشده را برگرداند. چون چند
  کاربر می‌توانند هم‌زمان پیام بفرستند، این متد باید نسبت به همزمانی امن باشد.
- متد ساخت JSON لیست چت‌های یک کاربر (برای `get-chats`): باید همهٔ طرف‌های گفتگوی
  آن کاربر و تعداد خوانده‌نشدهٔ هرکدام را به فرمت JSON Array تبدیل کند (می‌توانید
  از یک کتابخانهٔ JSON مثل Gson استفاده کنید).
- متد ساخت JSON پیام‌های یک گفتگوی خاص (برای `get-messages`): باید تمام پیام‌های
  آن گفتگو را به JSON تبدیل کند و به‌عنوان اثر جانبی، شمارندهٔ خوانده‌نشدهٔ آن
  کاربر برای این طرف گفتگو را صفر کند.

**دستورات جدید در `ClientConnection` (host)**
- `send-message <username> <json>`: نوع و متن پیام را از JSON ورودی استخراج کنید،
  به `ChatStore` بسپارید، `OK <seq>` را به فرستنده برگردانید. سپس اگر گیرنده الان
  آنلاین است (با استفاده از نگاشت نام‌کاربری→session در `Workspace` که روز ۳
  ساختید)، یک خط `receive-message <from> <json>` مستقیماً روی اتصال گیرنده
  بفرستید.
- `get-chats`: بدون آرگومان؛ خروجی `ChatStore` را در قالب `OK <json>` برگردانید.
- `get-messages <username>`: خروجی `ChatStore` برای آن گفتگو را در قالب `OK <json>`
  برگردانید.
- `disconnect`: session کاربر را از نگاشت‌های آنلاین `Workspace` حذف کنید.

⚠️ **قطع ناگهانی (بدون دستور disconnect):** حلقهٔ خواندن دستورات باید طوری نوشته
شود که وقتی اتصال به هر دلیلی (خطا یا بسته‌شدن سوکت) قطع شد، همان منطق حذف
session از `Workspace` اجرا شود — یعنی این حذف را در یک بلوک `finally` (یا معادل
آن) بگذارید، نه فقط داخل دستور `disconnect`.

⚠️ **یادآوری از الگوی روز ۳:** حلقهٔ اصلی `ClientConnection` که در ادامهٔ همین
روز باید نوشته شود (پردازش `send-message`/`get-chats`/... بعد از اتمام
authenticate/resolveUsername) خودش از قبل تنها خوانندهٔ سوکت کلاینت است (چون
هیچ Thread دیگری روی این سوکت خاص کار نمی‌کند)، پس اینجا مشکلی از نوع روز ۳
پیش نمی‌آید. فقط برای `receive-message` که باید مستقیم روی سوکت گیرنده نوشته
شود (`connection.sendLine(...)` از یک Thread دیگر، یعنی Thread فرستنده)، دقت
کنید که نوشتن روی یک `Connection` از چند Thread می‌تواند خطوط را با هم قاطی کند
اگر خود `PrintWriter` این تضمین را ندهد؛ به همین دلیل بهتر است هر `Connection`
یک قفل نوشتن (`writeLock`) داخلی داشته باشد یا `sendLine` را `synchronized`
کنید.

**`DataStore` (central)**
- مسیر فایل ذخیره‌سازی را به‌صورت یک ثابت تعریف کنید.
- متد ذخیره: کل وضعیت `UserManager`، `HostManager`، `WorkspaceManager` را
  می‌گیرد و در فایل سریالایز می‌کند.
- متد بارگیری: اگر فایل موجود بود، محتوایش را می‌خواند و به همان سه manager
  برمی‌گرداند/تزریق می‌کند.
- برای این‌که `DataStore` بتواند به state داخلی هرکدام از manager ها دسترسی پیدا
  کند بدون این‌که فیلدهای خصوصی‌شان را مستقیم دستکاری کند، به هرکدام یک جفت متد
  «صادر کردن وضعیت» و «وارد کردن وضعیت» اضافه کنید.
- در نقطهٔ شروع برنامهٔ مرکزی، قبل از هر چیز باید بارگیری انجام شود؛ و وقتی از
  کنسول دستور `shutdown` خوانده شد، باید ذخیره انجام شده و برنامه بسته شود.

**`HostDataStore` (host)** — مشابه `DataStore` ولی برای وضعیت میزبان (فضای
کارها و چت‌های داخلشان). نام فایل باید شامل IP و پورت شروع میزبان باشد تا هر
میزبان فایل خودش را داشته باشد. (این نام‌گذاری بر اساس IP/پورت **میزبان** است،
نه نام workspace — چون همان‌طور که در روز ۳ گفته شد، workspaceها اصلاً نام
ندارند؛ اگر می‌خواهید در همین فایل داده‌های هر workspace را از هم جدا کنید،
باید بر اساس `port` هر workspace این کار را انجام دهید، نه بر اساس نام آن.)

### تست نهایی سناریوی کامل
```
--- کلاینت ۱ (ahmad) ---
connect fkla48fhhf → username? → ahmad → OK
send-message saeed {"type": "text", "body": "Salam chetori?"}
→ OK 1

--- کلاینت ۲ (saeed، آنلاین) ---
← receive-message ahmad {"seq": 1, "from": "ahmad", "type": "text", "body": "Salam chetori?"}

--- کلاینت ۱ ---
get-chats → OK [{"name": "saeed", "unread_count": 0}]
disconnect

--- کلاینت ۲ ---
get-messages ahmad → OK [{"seq": 1, "from": "ahmad", ...}]

--- در کنسول central و host ---
shutdown → ذخیره و بسته شدن
(راه‌اندازی دوباره central و host)
→ داده‌ها (کاربران، میزبان‌ها، فضای‌کارها، مکالمات) باید سرجایشان باشند
```

---

## نکات فازهای بعدی سند (خارج از این ۵ روز)

- **فاز سوم:** ویرایش پیام + ارسال استیکر (نوع جدید `type` در JSON پیام).
- **فاز چهارم:** گروه‌ها — نوع جدید گفتگو با نام یکتا (بدون اشتراک با نام کاربری
  کاربران)، به‌علاوهٔ ایجاد گروه، عضویت، و افزودن عضو (با دو شرط: کاربر خودش عضو
  گروه باشد و با کاربر جدید از قبل گفتگو داشته باشد).

---

## ترتیب پیشنهادی جلسات کدنویسی

1. ✅ ثبت میزبان (create-host + check)
2. ✅ روز ۱: `User` + `UserManager` + `register`/`login` (سخت‌سازی امنیتی باقی‌مانده)
3. ✅ روز ۲: `WorkspaceInfo` + `WorkspaceManager` + `Workspace` + `create-workspace`
4. ✅ روز ۳: `Token` + `TokenManager` + `connect-workspace` + `connect`/`whois`/`username?`
    + کشف و رفع race condition خواندن روی سوکت‌های دائمی central↔host (هم سمت
      central با `HostConnectionListener`، هم سمت host با `CentralConnectionListener`)
      — این الگو («یک سوکت دائمی، یک خواننده، صف پاسخ برای sendAndWait») باید در
      روزهای بعد هم برای هر کانال دوطرفهٔ جدید (مثلاً چت client↔workspace) رعایت شود.
5. 🔲 روز ۴: پروژهٔ `client` + مدل‌های `Message`/`Chat`
6. 🔲 روز ۵: چت کامل + `disconnect` + `DataStore`/`HostDataStore` + `shutdown`
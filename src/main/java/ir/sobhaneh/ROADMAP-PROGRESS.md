# راهنمای کامل پیاده‌سازی پیام‌رسان — نسخهٔ به‌روزشده
### مخصوص تازه‌کار جاوا — هر مرحله دقیقاً بگو چیکار کنم

> این فایل جایگزین roadmap قبلی است. طبق سند اصلی پروژه (فایل PDF) و کدهایی که تا
> الان با هم نوشتیم دوباره چک و تکمیل شده. هرجا "✅ انجام شده" نوشته، یعنی کدش را
> از قبل داریم. هرجا "🔲 باید بنویسیم" نوشته، مرحلهٔ بعدی کار شماست.

---

## ۰. ساختار فعلی پروژه (همینی که تا الان ساختیم)

```
messenger-project/
├── common/                              # کد مشترک بین central و host
│   └── ir/sobhaneh/common/
│       └── Connection.java              ✅ (خواندن/نوشتن خط روی سوکت)
│
├── central/                             # سرور مرکزی
│   └── ir/sobhaneh/central/
│       ├── CentralServer.java           ✅ (main، گوش‌دادن روی پورت 8000)
│       ├── ClientHandler.java           ✅ (خواندن خط، ارجاع دستور)
│       ├── HostRegistrationSession.java ✅ (منطق create-host / check)
│       ├── VerificationService.java     ✅ (تولید/ارسال کد تأیید)
│       ├── HostManager.java             ✅ (اعتبارسنجی + رزرو اتمیک پورت)
│       ├── ReservationResult.java       ✅ (نتیجهٔ رزرو: موفق/ناموفق)
│       └── models/
│           └── HostInfo.java            ✅ (اطلاعات یک میزبان ثبت‌شده)
│
└── host/                                # برنامهٔ میزبان
    └── ir/sobhaneh/host/
        ├── HostMain.java                ✅ (main)
        ├── HostRegistration.java        ✅ (اجرای پروتکل ثبت‌نام)
        └── HostConfig.java              ✅ (تنظیمات ip/بازهٔ پورت این میزبان)
```

**چیزی که هنوز نداریم:** پوشهٔ `client/` و بخش‌های `UserManager`, `WorkspaceManager`,
`TokenManager` در سمت central، و `Workspace` در سمت host. این‌ها را در ادامهٔ همین
فایل قدم‌به‌قدم می‌سازیم.

---

## ⚠️ یک اصلاح مهم قبل از ادامه

طبق سند اصلی پروژه، وقتی کد تأیید اشتباه باشد، سرور مرکزی باید دقیقاً این خطا را
بدهد:
```
ERROR Invalid code
```
ولی در کد فعلی ما پیام `"ERROR Verification code mismatch"` نوشته شده. چون سند
تأکید کرده *"پروتکل را دقیقاً طبق سند پیاده کنید (حتی فاصله‌ها و حروف کوچک/بزرگ)"*،
باید این را اصلاح کنید:

**در فایل `HostRegistrationSession.java`**، خط:
```java
connection.sendLine("ERROR Verification code mismatch");
```
را به این تغییر دهید:
```java
connection.sendLine("ERROR Invalid code");
```

از این به بعد، هر جا در این راهنما یک پیام دقیق نوشته شده (مثل `OK`, `ERROR ...`)،
همان را عیناً در کدتان به کار ببرید.

---

## ⚠️ قانون مهم اتصال‌ها (طبق سند اصلی)

> در سناریوهای مربوط به **سرور مرکزی**، کلاینت همان موقع اتصال را برقرار کرده و وقتی
> کار تمام شد آن را می‌بندد.
>
> در سناریوهای مربوط به **فضای کار**، کلاینت اتصال را باز نگه می‌دارد.

یعنی:

| سناریو | اتصال | رفتار |
|--------|--------|--------|
| `register` / `login` تکی | مرکزی | باز کن → دستور → جواب → **ببند** |
| `create-workspace` | مرکزی | باز کن → `login` → `create-workspace` → جواب → **ببند** |
| `connect-workspace` | مرکزی | باز کن → `login` → `connect-workspace` → جواب → **ببند** |
| `connect` + چت | فضای کار | باز کن → احراز هویت با توکن → **باز بماند** |
| `create-host` (میزبان) | مرکزی | باز کن → ثبت‌نام → **برای همیشه باز بماند** |

**نکتهٔ کلیدی:** برای `create-workspace` و `connect-workspace`، دستور `login` و دستور اصلی
روی **همان یک اتصال کوتاه‌مدت** پشت سر هم می‌آیند. بعد از گرفتن پاسخ نهایی، کلاینت
اتصال را می‌بندد. هیچ session لاگین دائمی روی central برای کلاینت‌ها نگه داشته نمی‌شود.

روی `ClientHandler` یک فیلد `private Long loggedInUserId;` بگذارید که **فقط برای عمر
همین اتصال** معنا دارد:
- وقتی `login` موفق شد → این فیلد را پر کنید.
- وقتی `create-workspace` یا `connect-workspace` آمد → چک کنید این فیلد مقدار داشته باشد.
- وقتی اتصال بسته شد → این فیلد همراه با خود handler از بین می‌رود.

---

## ۱. مرحلهٔ ثبت‌نام و ورود کاربر (User Registration & Login)

### پروتکل دقیق (از سند اصلی)

**ثبت‌نام:**
```
کلاینت میفرستد:  register 09123456789 123456
سرور پاسخ میدهد:  OK
```

**ورود:**
```
کلاینت میفرستد:  login 09123456789 123456
سرور پاسخ میدهد:  OK
```

کلاینت بعد از گرفتن پاسخ، اتصال را می‌بندد.

### دقیقاً چیکار کنید

#### قدم ۱: مدل `User` را بسازید
مسیر: `central/ir/sobhaneh/central/models/User.java`

باید شامل این فیلدها باشد (`private final`، با getter):
- `long id` — شناسه‌ای که از ۱ شروع می‌شود و برای هر کاربر جدید یکی افزایش می‌یابد
- `String phoneNumber`
- `String password`

راهنمایی: برای تولید `id` یکتا، از یک `AtomicLong` در `UserManager` استفاده کنید
(`AtomicLong` مثل `int` است ولی برای همزمانی چند Thread امن است؛ متد
`incrementAndGet()` هر بار عدد بعدی را می‌دهد).

#### قدم ۲: `UserManager` را بسازید
مسیر: `central/ir/sobhaneh/central/UserManager.java`

دو متد عمومی لازم دارید:
```java
public String register(String phoneNumber, String password) {
    // اگر phoneNumber از قبل وجود دارد -> "ERROR User already exists"
    // وگرنه یک User جدید بساز، به لیست اضافه کن -> "OK"
}

public String login(String phoneNumber, String password) {
    // اگر کاربر با این phone پیدا نشد یا password اشتباه بود -> "ERROR Invalid credentials"
    // وگرنه -> "OK"
}
```
برای ذخیرهٔ کاربران از `ConcurrentHashMap<String, User>` استفاده کنید — کلید
`phoneNumber`، مقدار `User`. اینطوری هم جست‌وجو با شماره تلفن سریع است، هم
Thread-safe است.

نکته: این دو متد را هم مثل `HostManager.reserve`، `synchronized` بگذارید تا اگر دو
درخواست `register` هم‌زمان با یک شماره تلفن بیایند، فقط یکی موفق شود.

متد `login` بهتر است در صورت موفقیت، خودِ `User` (یا حداقل `userId`) را برگرداند تا
`ClientHandler` بتواند `loggedInUserId` را ست کند. می‌توانید یک متد جدا مثل
`User findUser(String phone, String password)` هم داشته باشید.

#### قدم ۳: دستورات را به `ClientHandler` اضافه کنید
در متد `dispatch` که از قبل دارید (همان `switch` روی دستور)، دو `case` جدید اضافه
کنید:
```java
case "register" -> dispatchRegister(connection, parts);
case "login" -> dispatchLogin(connection, parts);
```
هر کدام را مثل `dispatchCreateHost` به یک متد کوچک جدا بفرستید که ورودی را پارس
می‌کند و `UserManager` را صدا می‌زند. یک نمونهٔ `UserManager` هم مثل `hostManager`،
`static final` در `ClientHandler` بسازید.

در `dispatchLogin` بعد از login موفق:
```java
this.loggedInUserId = user.getId();   // فقط برای عمر همین اتصال
connection.sendLine("OK");
```

#### قدم ۴: تست کنید
با `telnet localhost 8000`:
```
register 09123456789 123456
```
باید `OK` بگیرید. دوباره همین را بفرستید — باید `ERROR User already exists` بگیرید.
```
login 09123456789 123456
```
باید `OK` بگیرید.
```
login 09123456789 wrongpass
```
باید `ERROR Invalid credentials` بگیرید.

(در telnet می‌توانید بعد از login همان اتصال را باز نگه دارید و دستور بعدی را بزنید؛
کلاینت واقعی بعد از کار، اتصال را می‌بندد.)

---

## ۲. مرحلهٔ ایجاد فضای کار (create-workspace)

### پروتکل دقیق

```
کلاینت یک اتصال به مرکزی باز می‌کند و پشت سر هم می‌فرستد:
                    login 09123456789 123456
                    create-workspace company1
سرور به کلاینت:      OK                    (جواب login)
سرور به میزبان:     create-workspace 10143 1001
                    (پورت انتخابی + شناسهٔ کاربر سازنده)
میزبان به سرور:      OK
سرور به کلاینت:      OK 127.0.0.1 10143
کلاینت اتصال را می‌بندد.
```

### دقیقاً چیکار کنید

این مرحله چون هم central و هم host را درگیر می‌کند، کمی پیچیده‌تر است. آن را به
چند زیرقدم می‌شکنیم:

#### قدم ۱: مدل `WorkspaceInfo` در central
مسیر: `central/ir/sobhaneh/central/models/WorkspaceInfo.java`

فیلدها: `String name`, `String hostIp`, `int port`, `long creatorUserId`.

#### قدم ۲: `WorkspaceManager` در central
مسیر: `central/ir/sobhaneh/central/WorkspaceManager.java`

این کلاس باید:
1. یک لیست از `WorkspaceInfo`های موجود نگه دارد (برای چک یکتا بودن اسم).
2. متدی داشته باشد که یک میزبان تصادفی از `HostManager.getRegisteredHosts()`
   انتخاب کند.
3. از آن میزبان یک پورت با `HostInfo.allocateRandomPort()` بگیرد (دقیقاً همان متدی
   که برای `create-host` ساختیم — همینجاست که کد قبلی‌مان دوباره به کار می‌آید).

نکته دربارهٔ ارتباط با میزبان: چون اتصال میزبان به central از مرحلهٔ `create-host`
هنوز باز است (یادتان هست `pendingHost.setSocket(socket)` را در
`HostRegistrationSession` نوشتیم؟)، باید بتوانید از طریق همان Socket ذخیره‌شده در
`HostInfo`، به میزبان دستور `create-workspace <port> <userId>` بفرستید و منتظر
`OK` بمانید.

راهنمایی مهم: چون اتصال میزبان الان توسط `ClientHandler` آن میزبان "اشغال" است
(همان Thread دارد در حلقهٔ `while` منتظر خط بعدی از میزبان است)، فرستادن دستور
جدید به میزبان از یک Thread دیگر (مثلاً از Thread کلاینتی که `create-workspace`
خواسته) نیاز به هماهنگی دارد. برای فاز اول، ساده‌ترین راه: از همان `Connection`
ذخیره‌شده در `HostInfo` مستقیماً `sendLine` بزنید، و در سمت `ClientHandler` میزبان،
اگر خطی غیر از دستورات شناخته‌شده (`create-host`, `check`) آمد، آن را هم پردازش
کنید (یعنی به `HostInfo` خودش دستور بدهید که یک `ServerSocket` جدید برای Workspace
باز کند).

اگر این بخش گیج‌کننده بود، همینجا متوقف شوید و از من بخواهید با هم کدش را بنویسیم
— این جایی است که واقعاً باید مرحله‌به‌مرحله جلو برویم، نه یکجا.

#### قدم ۳: دستور `create-workspace` در `ClientHandler`
```java
case "create-workspace" -> dispatchCreateWorkspace(connection, parts);
```

داخل متد:
- اگر `loggedInUserId == null` بود → `ERROR Not logged in` (یا پیام مناسب)
- وگرنه `WorkspaceManager` را صدا بزن و پاسخ `OK <ip> <port>` را بفرست.

یادتان باشد: `loggedInUserId` فقط چون کلاینت **همین الان** روی همین اتصال `login`
کرده مقدار دارد. کلاینت بعد از گرفتن پاسخ نهایی، اتصال را می‌بندد.

#### قدم ۴: کلاس `Workspace` در سمت host
مسیر: `host/ir/sobhaneh/host/Workspace.java`

فیلدها: `String name`, `int port`, `ServerSocket serverSocket`,
`Map<String, ClientSession> onlineUsers` (بعداً برای مراحل چت لازم می‌شود).

سازنده باید یک `ServerSocket` روی پورت داده‌شده باز کند و یک Thread جدید بسازد که
در حلقه منتظر اتصال کلاینت‌های جدید بماند (`serverSocket.accept()`).

#### قدم ۵: تست
بعد از این‌که میزبان با موفقیت ثبت شد (مرحلهٔ قبلی که تست کردیم)، در یک ترمینال
سوم به central وصل شوید و **روی همان اتصال** پشت سر هم بزنید:
```
login 09123456789 123456
create-workspace company1
```
باید چیزی شبیه `OK 127.0.0.1 10234` بگیرید. بعد اتصال را ببندید.

---

## ۳. مرحلهٔ اتصال به فضای کار (connect-workspace + connect)

### پروتکل دقیق

```
کلاینت یک اتصال به مرکزی باز می‌کند و پشت سر هم می‌فرستد:
                    login 09123456789 123456
                    connect-workspace company1
central به کلاینت:  OK                    (جواب login)
central به کلاینت:  OK 127.0.0.1 10143 fkla48fhhf
                    (fkla48fhhf = توکن موقت، ۱۰ کاراکتر حروف کوچک+عدد، عمر ۵ دقیقه)
کلاینت اتصال central را می‌بندد و به فضای کار وصل می‌شود:
کلاینت به workspace:    connect fkla48fhhf
workspace به central:   whois fkla48fhhf
central به workspace:   OK 1001   (شناسه کاربر)
اگر اولین اتصال این کاربر به این workspace است:
workspace به کلاینت:    username?
کلاینت به workspace:    ahmad
workspace به کلاینت:    OK
```
(از این به بعد اتصال کلاینت به فضای کار **باز می‌ماند**.)

### دقیقاً چیکار کنید

#### قدم ۱: مدل `Token` در central
مسیر: `central/ir/sobhaneh/central/models/Token.java`

فیلدها: `String value` (۱۰ کاراکتری)، `long userId`، `long expiresAtMillis`.

برای تولید مقدار توکن: حروف کوچک انگلیسی + عدد، طول ۱۰. می‌توانید یک رشتهٔ ثابت
`"abcdefghijklmnopqrstuvwxyz0123456789"` بسازید و ۱۰ بار یک کاراکتر تصادفی از آن
انتخاب کنید.

برای عمر ۵ دقیقه‌ای: `expiresAtMillis = System.currentTimeMillis() + 5*60*1000`.

#### قدم ۲: `TokenManager` در central
مسیر: `central/ir/sobhaneh/central/TokenManager.java`

متدها:
```java
public Token createToken(long userId) { ... }   // یک توکن جدید می‌سازد و نگه می‌دارد
public Long resolveToken(String tokenValue) {
    // اگر توکن پیدا نشد یا منقضی شده -> null
    // وگرنه -> userId مربوطه
}
```
از `ConcurrentHashMap<String, Token>` برای نگه‌داری توکن‌ها استفاده کنید.

#### قدم ۳: دستور `connect-workspace` در central
مثل `create-workspace`:
- روی **همان اتصال** که قبلاً `login` موفق شده، دستور می‌آید.
- اگر `loggedInUserId == null` → خطا.
- وگرنه `TokenManager.createToken(loggedInUserId)` را صدا بزنید و پاسخ
  `OK <ip> <port> <token>` را بفرستید.
- کلاینت بعد از گرفتن این پاسخ، اتصال به مرکزی را می‌بندد.

```java
case "connect-workspace" -> dispatchConnectWorkspace(connection, parts);
```

#### قدم ۴: دستورات `connect` و `whois` و `username?` در سمت host

اینها را داخل `ClientConnection` (کلاسی که هر کلاینت متصل به یک Workspace را
مدیریت می‌کند — طبق نقشهٔ پوشه‌بندی اصلی) پیاده می‌کنید:

1. وقتی خط `connect <token>` می‌رسد، یک اتصال کوتاه‌مدت جدید به central باز کنید و
   `whois <token>` بفرستید.
2. اگر central پاسخ `OK <userId>` داد، چک کنید آیا این `userId` قبلاً در این
   Workspace نام‌کاربری ثبت کرده یا نه (یک `Map<Long, String> userIdToUsername`
   نگه دارید).
3. اگر ثبت نکرده، `username?` بفرستید و منتظر پاسخ کلاینت بمانید، یکتا بودنش را
   چک کنید (یک `Set<String> takenUsernames`)، و در نهایت `OK` بفرستید.
4. اتصال کلاینت به فضای کار از این به بعد باز می‌ماند (برای `send-message` و بقیه).

#### قدم ۵: تست
با telnet یا یک کلاینت واقعی (که در مرحلهٔ بعد می‌سازیم)، جریان کامل را دنبال کنید:
روی یک اتصال به مرکزی `login` و بعد `connect-workspace` بزنید، جواب را بگیرید،
اتصال مرکزی را ببندید، بعد به پورت فضای کار وصل شوید و `connect <token>` بزنید.

---

## ۴. مرحلهٔ برنامهٔ کلاینت

### دستوراتی که کلاینت باید پشتیبانی کند (طبق سند)

| دستور کاربر | چه اتفاقی می‌افتد |
|---|---|
| `register <phone> <password>` | اتصال کوتاه به central، register، بستن اتصال |
| `login <phone> <password>` | اتصال کوتاه به central، login، بستن اتصال |
| `create-workspace <name>` | یک اتصال: login + create-workspace، بعد بستن اتصال |
| `connect-workspace <name>` | یک اتصال: login + connect-workspace، گرفتن توکن، بستن اتصال مرکزی، بعد اتصال به workspace و `connect <token>` |
| `disconnect` | بستن اتصال با workspace |
| `send-message <user> <json>` | فرستادن به workspace (باید از قبل متصل باشیم) |
| `get-chats` | فرستادن به workspace |
| `get-messages <user>` | فرستادن به workspace |

### دقیقاً چیکار کنید

#### قدم ۱: ساختار پوشه
```
client/ir/sobhaneh/client/
├── ClientMain.java          # main + حلقهٔ خواندن دستور از کنسول
├── CentralConnection.java   # اتصال کوتاه‌مدت به central (register/login/...)
└── WorkspaceConnection.java # اتصال بلندمدت به workspace + Thread جدا برای دریافت پیام
```

#### قدم ۲: `CentralConnection`
یک متد به ازای هر دستور کوتاه‌مدت:
```java
public String register(String phone, String password) { ... }
public String login(String phone, String password) { ... }

// این دو متد داخل خودشان login را هم انجام می‌دهند (روی همان Socket):
public String createWorkspace(String phone, String password, String name) {
    // Socket باز کن
    // login بفرست → جواب OK بگیر
    // create-workspace بفرست → جواب نهایی بگیر
    // Socket را ببند
    // جواب نهایی را برگردان
}
public String[] connectWorkspace(String phone, String password, String name) {
    // مشابه بالا: login + connect-workspace روی یک Socket
    // برمی‌گرداند: ip, port, token
}
```
هر کدام یک `Socket` جدید به central باز می‌کنند، کار را تمام می‌کنند، `Socket` را
می‌بندند و پاسخ را برمی‌گردانند.

#### قدم ۳: `WorkspaceConnection`
چون این اتصال باز می‌ماند و باید هم بتوانیم بفرستیم هم هر لحظه چیزی از طرف مقابل
(`receive-message`) دریافت کنیم، به یک Thread جدا برای خواندن نیاز داریم:
```java
public void connect(String ip, int port, String token) {
    // Socket را باز کن، connect <token> بفرست
    // یک Thread جدید بساز که همیشه در حلقه readLine بزند و هر چیزی که می‌آید چاپ کند
}
public void sendMessage(String username, String json) { ... }
public void getChats() { ... }
public void getMessages(String username) { ... }
public void disconnect() { ... }
```

#### قدم ۴: `ClientMain`
یک حلقهٔ ساده که از ورودی کنسول (`Scanner` یا `BufferedReader` روی `System.in`)
خط می‌خواند، دستور اول را تشخیص می‌دهد (`if/else` یا `switch` روی اولین کلمه)، و
متد مناسب از `CentralConnection` یا `WorkspaceConnection` را صدا می‌زند.

برای `create-workspace` و `connect-workspace` باید phone و password را هم داشته
باشد (یا از قبل ذخیره کرده باشد، یا از کاربر بپرسد).

---

## ۵. مرحلهٔ چت شخصی (send-message / get-chats / get-messages)

این‌ها همه سمت **host**، داخل کلاس `Workspace` یا یک کلاس کمکی `ChatStore` پیاده
می‌شوند. دیگر نیازی به login به مرکزی نیست — کاربر از قبل با توکن به فضای کار
وصل شده و احراز هویت شده است.

### دقیقاً چیکار کنید

#### قدم ۱: مدل `Message`
مسیر: `host/ir/sobhaneh/host/Message.java`
فیلدها: `int seq`, `String from`, `String type`, `String body`, `long timestamp`.

#### قدم ۲: `ChatStore`
مسیر: `host/ir/sobhaneh/host/ChatStore.java`

```java
private final Map<String, List<Message>> conversations = new ConcurrentHashMap<>();
private final Map<String, Integer> lastSeq = new ConcurrentHashMap<>();
private final Map<String, Integer> unreadCount = new ConcurrentHashMap<>();
```
کلید مکالمه: دو نام کاربری را الفبایی مرتب و با `:` بچسبانید، مثلاً
`"ahmad:saeed"` — این‌طوری برای هر جفت کاربر همیشه یک کلید یکتا دارید، فارغ از
این‌که کدام‌شان فرستنده بوده.

متدها:
```java
public int addMessage(String from, String to, String type, String body) {
    // seq جدید بساز، به لیست اضافه کن، unreadCount گیرنده را زیاد کن، seq را برگردان
}
public String getChatsJson(String forUser) { ... }     // با Gson تبدیل به JSON
public String getMessagesJson(String userA, String userB) { ... } // + علامت‌گذاری خوانده‌شده
```

#### قدم ۳: اتصال به Gson
در `pom.xml` پروژهٔ host، وابستگی Gson را اضافه کنید (دقیقاً همان که در نقشه‌راه
اولیه گفته شده بود). برای ساخت JSON از یک لیست، کافیست:
```java
new Gson().toJson(someList);
```

#### قدم ۴: در `ClientConnection` (سمت host)، سه دستور جدید را پردازش کنید
```
send-message <username> <json>
get-chats
get-messages <username>
```
و اگر گیرنده در همان لحظه آنلاین بود (در `onlineUsers` Workspace)، بلافاصله
`receive-message <from> <json>` را به Connection او بفرستید.

---

## ۶. مرحلهٔ قطع اتصال

- دستور `disconnect` از کلاینت → `ClientConnection` سوکت را می‌بندد و کاربر را از
  `onlineUsers` حذف می‌کند.
- اگر `readLine()` مقدار `null` برگرداند (یعنی کلاینت به‌طور ناگهانی قطع شده)، همان
  کار حذف از `onlineUsers` باید در یک بلوک `finally` انجام شود — دقیقاً مثل الگویی
  که در `ClientHandler` سمت central با `session.cancel()` در `finally` پیاده
  کردیم.

---

## ۷. فاز ۲: ذخیره‌سازی با shutdown

- در `ClientMain` سمت central (و `HostMain`)، حلقهٔ کنسول باید دستور `shutdown` را
  هم بشناسد.
- با دریافت `shutdown`: تمام داده‌ها (کاربران، میزبان‌ها، فضای‌کارها در central؛
  مکالمات و کاربران در هر Workspace) با Gson به فایل نوشته شوند، سپس
  `System.exit(0)`.
- در ابتدای `main`، قبل از باز کردن `ServerSocket`، چک کنید فایل ذخیره‌شده وجود
  دارد؛ اگر دارد، با Gson بارگیری و داده‌ها را در حافظه بازسازی کنید.

---

## ترتیب پیشنهادی برای جلسات بعدی کدنویسی

1. ✅ ~~ثبت میزبان (create-host + check)~~ — تمام شده
2. 🔲 `User` + `UserManager` + دستورات `register`/`login`
3. 🔲 `WorkspaceInfo` + `WorkspaceManager` + `Workspace` سمت host + دستور `create-workspace`
4. 🔲 `Token` + `TokenManager` + دستور `connect-workspace` + `connect`/`whois`/`username?`
5. 🔲 پروژهٔ `client` (حداقل `register`, `login`, `create-workspace`, `connect-workspace`)
6. 🔲 `Message` + `ChatStore` + `send-message`/`get-chats`/`get-messages`
7. 🔲 `disconnect` و مدیریت قطع ناگهانی
8. 🔲 `shutdown` و بارگیری فاز ۲

**پیشنهاد من:** برای هر شماره، وقتی آماده بودید، فقط بگویید "بریم سراغ مرحلهٔ ۲" و
من دقیقاً همان‌طور که تا الان جلو رفتیم (فایل‌های کوچک، تک‌مسئولیتی، بدون
stream/record پیچیده) با هم می‌نویسیمش.

# راهنمای کامل پیاده‌سازی پیام‌رسان — نسخهٔ به‌روزشده
### مخصوص تازه‌کار جاوا — هر مرحله دقیقاً بگو چیکار کنم

> این فایل جایگزین roadmap قبلی است. طبق سند اصلی پروژه و ساختار نهایی پوشه‌ها
> بازنویسی شده. هرجا "✅ انجام شده" نوشته، یعنی از قبل داریم. هرجا "🔲 باید بنویسیم"
> نوشته، مرحلهٔ بعدی کار شماست.

---

## ۰. ساختار نهایی پروژه

```
messenger-project/
├── central-server/                          # پروژه سرور مرکزی
│   ├── src/main/java/ir/sobhaneh/central/
│   │   ├── CentralServer.java               ✅ نقطه شروع (main)
│   │   ├── ClientHandler.java               ✅ هندلر هر اتصال کلاینت/میزبان
│   │   ├── HostManager.java                 ✅ مدیریت میزبان‌ها
│   │   ├── HostRegistrationSession.java     ✅ منطق create-host / check
│   │   ├── VerificationService.java         ✅ تولید/ارسال کد تأیید
│   │   ├── ReservationResult.java           ✅ نتیجهٔ رزرو پورت
│   │   ├── UserManager.java                 🔲 مدیریت کاربران
│   │   ├── WorkspaceManager.java            🔲 مدیریت فضای‌کارها
│   │   ├── TokenManager.java                🔲 مدیریت توکن‌های موقت
│   │   ├── models/
│   │   │   ├── HostInfo.java                ✅
│   │   │   ├── User.java                    🔲
│   │   │   ├── WorkspaceInfo.java           🔲
│   │   │   └── Token.java                   🔲
│   │   └── persistence/
│   │       └── DataStore.java               🔲 ذخیره/بارگیری (فاز ۲)
│   └── data/
│       └── central.dat                      🔲 فایل ذخیره‌شده
│
├── host/                                    # پروژه میزبان
│   ├── src/main/java/ir/sobhaneh/host/
│   │   ├── HostMain.java                    ✅ نقطه شروع
│   │   ├── HostRegistration.java            ✅ ثبت‌نام میزبان در مرکزی
│   │   ├── HostConfig.java                  ✅ تنظیمات ip/بازهٔ پورت
│   │   ├── WorkspaceManager.java            🔲 مدیریت فضای‌کارهای این میزبان
│   │   ├── Workspace.java                   🔲 کلاس فضای کار
│   │   ├── ClientConnection.java            🔲 هندلر هر کلاینت متصل
│   │   ├── models/
│   │   │   ├── Message.java                 🔲
│   │   │   ├── Chat.java                    🔲
│   │   │   └── UserSession.java             🔲
│   │   └── persistence/
│   │       └── HostDataStore.java           🔲 ذخیره پیام‌ها و کاربران
│   └── data/
│       └── host-<ip>-<startPort>.dat        🔲
│
├── client/                                  # پروژه کلاینت
│   └── src/main/java/ir/sobhaneh/client/
│       ├── ClientMain.java                  🔲 نقطه شروع + حلقه دستورات
│       ├── CentralConnection.java           🔲 اتصال موقت به مرکزی
│       ├── WorkspaceConnection.java         🔲 اتصال پایدار به فضای کار
│       ├── CommandParser.java               🔲 پارس دستورات کاربر
│       └── models/                          🔲 (در صورت نیاز)
│
└── common/                                  # (اختیاری) کد مشترک
    └── src/main/java/ir/sobhaneh/common/
        └── Connection.java                  ✅ خواندن/نوشتن خط روی سوکت
```

---

## ⚠️ قانون مهم اتصال‌ها (طبق سند اصلی)

> در سناریوهای مربوط به **سرور مرکزی**، کلاینت همان موقع اتصال را برقرار کرده و وقتی
> کار تمام شد آن را می‌بندد.
>
> در سناریوهای مربوط به **فضای کار**، کلاینت اتصال را باز نگه می‌دارد.

| سناریو | اتصال | رفتار |
|--------|--------|--------|
| `register` / `login` تکی | مرکزی | باز کن → دستور → جواب → **ببند** |
| `create-workspace` | مرکزی | باز کن → `login` → `create-workspace` → جواب → **ببند** |
| `connect-workspace` | مرکزی | باز کن → `login` → `connect-workspace` → جواب → **ببند** |
| `connect` + چت | فضای کار | باز کن → احراز هویت با توکن → **باز بماند** |
| `create-host` (میزبان) | مرکزی | باز کن → ثبت‌نام → **برای همیشه باز بماند** |

روی `ClientHandler` یک فیلد `private Long loggedInUserId;` بگذارید که **فقط برای عمر
همین اتصال** معنا دارد:
- وقتی `login` موفق شد → این فیلد را پر کنید.
- وقتی `create-workspace` یا `connect-workspace` آمد → چک کنید این فیلد مقدار داشته باشد.
- وقتی اتصال بسته شد → این فیلد همراه با handler از بین می‌رود.

---

## ⚠️ اصلاح پیام خطا

در `HostRegistrationSession.java` اگر هنوز این خط هست:
```java
connection.sendLine("ERROR Verification code mismatch");
```
به این تغییر دهید:
```java
connection.sendLine("ERROR Invalid code");
```

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

# روز ۱ — کاربران + آماده‌سازی پایه

**هدف:** ثبت‌نام و ورود کار کند.

### فایل‌هایی که باید بسازید / تغییر دهید

```
central-server/src/main/java/ir/sobhaneh/central/
├── models/
│   └── User.java                    🔲 بساز
├── UserManager.java                 🔲 بساز
└── ClientHandler.java               ✅ تغییر بده (دستورات register/login + loggedInUserId)
```

### جزئیات

#### ۱. `User.java`
```java
// فیلدها (private final + getter):
long id;
String phoneNumber;
String password;
```

#### ۲. `UserManager.java`
```java
public String register(String phoneNumber, String password);
// اگر وجود داشت → "ERROR User already exists" وگرنه → "OK"

public String login(String phoneNumber, String password);
// اگر پیدا نشد یا پسورد غلط → "ERROR Invalid credentials" وگرنه → "OK"
// بهتر است userId را هم برگرداند تا ClientHandler بتواند loggedInUserId را ست کند
```
ذخیره با `ConcurrentHashMap<String, User>` — کلید: phoneNumber  
تولید id با `AtomicLong`  
متدها `synchronized`

#### ۳. تغییر `ClientHandler.java`
- فیلد: `private Long loggedInUserId = null;`
- caseهای جدید:
  ```java
  case "register" -> dispatchRegister(...);
  case "login"    -> dispatchLogin(...);
  ```
- در `dispatchLogin` بعد از موفقیت: `this.loggedInUserId = userId;`

#### ۴. تست با telnet
```
register 09123456789 123456     → OK
register 09123456789 123456     → ERROR User already exists
login 09123456789 123456        → OK
login 09123456789 wrongpass     → ERROR Invalid credentials
```

**خروجی روز ۱:** کاربر می‌تواند ثبت‌نام و لاگین کند.

---

# روز ۲ — ایجاد فضای کار

**هدف:** `create-workspace` از کلاینت تا میزبان کامل شود.

### فایل‌هایی که باید بسازید / تغییر دهید

```
central-server/src/main/java/ir/sobhaneh/central/
├── models/
│   └── WorkspaceInfo.java           🔲 بساز
├── WorkspaceManager.java            🔲 بساز
└── ClientHandler.java               ✅ تغییر بده (دستور create-workspace)

host/src/main/java/ir/sobhaneh/host/
├── Workspace.java                   🔲 بساز
├── WorkspaceManager.java            🔲 بساز (مدیریت workspaceهای این میزبان)
└── ClientHandler یا HostRegistration ✅ تغییر بده (دریافت دستور create-workspace از مرکزی)
```

### جزئیات

#### ۱. `WorkspaceInfo.java` (central)
```java
String name;
String hostIp;
int port;
long creatorUserId;
```

#### ۲. `WorkspaceManager.java` (central)
- لیست/Map از WorkspaceInfoها (چک یکتا بودن اسم)
- انتخاب تصادفی میزبان از `HostManager`
- گرفتن پورت با `HostInfo.allocateRandomPort()`
- ارسال `create-workspace <port> <userId>` روی اتصال باز میزبان و منتظر `OK`

#### ۳. دستور در `ClientHandler` (central)
```java
case "create-workspace" -> dispatchCreateWorkspace(...);
```
- اگر `loggedInUserId == null` → خطا
- وگرنه WorkspaceManager را صدا بزن → `OK <ip> <port>`

#### ۴. `Workspace.java` (host)
```java
String name;
int port;
ServerSocket serverSocket;
// بعداً: Map آنلاین‌ها
```
سازنده: ServerSocket روی پورت باز کند + Thread برای `accept()`.

#### ۵. `WorkspaceManager.java` (host)
- نگه‌داری workspaceهای ساخته‌شده روی این میزبان
- وقتی از مرکزی دستور `create-workspace <port> <userId>` آمد → Workspace جدید بساز → `OK`

#### ۶. تست
روی یک اتصال telnet به مرکزی:
```
login 09123456789 123456
create-workspace company1
→ OK 127.0.0.1 <port>
```

**خروجی روز ۲:** فضای کار ساخته می‌شود و پورت برمی‌گردد.

---

# روز ۳ — اتصال به فضای کار و احراز هویت

**هدف:** کاربر با توکن وارد فضای کار شود.

### فایل‌هایی که باید بسازید / تغییر دهید

```
central-server/src/main/java/ir/sobhaneh/central/
├── models/
│   └── Token.java                   🔲 بساز
├── TokenManager.java                🔲 بساز
└── ClientHandler.java               ✅ تغییر بده (connect-workspace + whois)

host/src/main/java/ir/sobhaneh/host/
├── ClientConnection.java            🔲 بساز
├── models/
│   └── UserSession.java             🔲 بساز
└── Workspace.java                   ✅ تغییر بده (قبول اتصال کلاینت و ارجاع به ClientConnection)
```

### جزئیات

#### ۱. `Token.java`
```java
String value;          // ۱۰ کاراکتر a-z0-9
long userId;
long expiresAtMillis;  // now + 5*60*1000
```

#### ۲. `TokenManager.java`
```java
Token createToken(long userId);
Long resolveToken(String tokenValue);  // null اگر نبود یا منقضی
```
ذخیره با `ConcurrentHashMap<String, Token>`

#### ۳. دستور `connect-workspace` در ClientHandler (central)
- چک `loggedInUserId`
- ساخت توکن
- پاسخ: `OK <ip> <port> <token>`

#### ۴. دستور `whois` در ClientHandler (central)
- `whois <token>` → `OK <userId>` یا خطا

#### ۵. `UserSession.java` (host)
```java
long userId;
String username;
ClientConnection connection;
```

#### ۶. `ClientConnection.java` (host)
- وقتی `connect <token>` آمد → اتصال کوتاه به مرکزی → `whois <token>`
- اگر OK → چک username قبلی
- اگر اولین بار → بفرست `username?` → بگیر → یکتا بودن → `OK`
- اتصال را باز نگه دار

#### ۷. تست جریان کامل
```
(مرکزی)  login → connect-workspace company1 → OK ip port token
(بستن اتصال مرکزی)
(workspace) connect <token> → username? → ahmad → OK
```

**خروجی روز ۳:** کاربر احراز هویت شده داخل فضای کار است.

---

# روز ۴ — برنامهٔ کلاینت + پایهٔ چت

**هدف:** کلاینت واقعی به‌جای telnet + اسکلت ذخیره‌سازی پیام.

### فایل‌هایی که باید بسازید

```
client/src/main/java/ir/sobhaneh/client/
├── ClientMain.java                  🔲 بساز
├── CentralConnection.java           🔲 بساز
├── WorkspaceConnection.java         🔲 بساز
└── CommandParser.java               🔲 بساز

host/src/main/java/ir/sobhaneh/host/
└── models/
    ├── Message.java                 🔲 بساز
    └── Chat.java                    🔲 بساز
```

### جزئیات

#### ۱. `CentralConnection.java`
```java
String register(String phone, String password);
String login(String phone, String password);
String createWorkspace(String phone, String password, String name);
// داخلش: باز کردن Socket → login → create-workspace → بستن → برگرداندن جواب

String[] connectWorkspace(String phone, String password, String name);
// داخلش: login + connect-workspace روی یک Socket → برگرداندن ip, port, token
```

#### ۲. `WorkspaceConnection.java`
```java
void connect(String ip, int port, String token);
// Socket باز، connect <token>، Thread جدا برای readLine و چاپ

void sendMessage(String username, String json);
void getChats();
void getMessages(String username);
void disconnect();
```

#### ۳. `CommandParser.java`
- خط ورودی را بشکند و دستور + آرگومان‌ها را برگرداند

#### ۴. `ClientMain.java`
- حلقهٔ خواندن از کنسول
- تشخیص دستور و صدا زدن متد مناسب

#### ۵. `Message.java` (host)
```java
int seq;
String from;
String type;
String body;
long timestamp;
```

#### ۶. `Chat.java` (host)
- نمایندهٔ یک مکالمه بین دو کاربر (یا اسکلت لیست پیام‌ها)

#### ۷. تست
همهٔ دستورات register / login / create-workspace / connect-workspace از طریق کلاینت.

**خروجی روز ۴:** کلاینت تعاملی کار می‌کند و مدل‌های چت آماده است.

---

# روز ۵ — چت کامل + قطع اتصال + ذخیره‌سازی

**هدف:** گپ شخصی کامل + حفظ داده‌ها بعد از restart.

### فایل‌هایی که باید بسازید / تغییر دهید

```
host/src/main/java/ir/sobhaneh/host/
├── ClientConnection.java            ✅ تغییر بده (send-message / get-chats / get-messages / disconnect)
├── Workspace.java                   ✅ تغییر بده (onlineUsers + ChatStore)
└── persistence/
    └── HostDataStore.java           🔲 بساز

central-server/src/main/java/ir/sobhaneh/central/
└── persistence/
    └── DataStore.java               🔲 بساز

(+ تغییر HostMain.java و CentralServer.java برای shutdown و بارگیری)
```

### جزئیات

#### ۱. منطق چت داخل host
یک `ChatStore` (می‌تواند داخل Workspace یا کلاس جدا باشد):
```java
Map<String, List<Message>> conversations;  // کلید: "ahmad:saeed" (مرتب‌شده)
Map<String, Integer> lastSeq;
Map<String, Integer> unreadCount;
```
```java
int addMessage(String from, String to, String type, String body);
String getChatsJson(String forUser);
String getMessagesJson(String userA, String userB);
```
وابستگی Gson در pom.xml پروژهٔ host.

#### ۲. دستورات در `ClientConnection`
```
send-message <username> <json>  → ذخیره + OK <seq> + اگر آنلاین بود receive-message
get-chats                       → OK [json array]
get-messages <username>         → علامت خوانده‌شده + OK [json array]
disconnect                      → بستن سوکت + حذف از onlineUsers
```

#### ۳. قطع ناگهانی
اگر `readLine()` مقدار `null` برگرداند → در `finally` از `onlineUsers` حذف شود.

#### ۴. `DataStore.java` (central) — اجباری
- ذخیره: کاربران، میزبان‌ها، فضای‌کارها → `data/central.dat` با Gson
- بارگیری در ابتدای `main` قبل از باز کردن ServerSocket
- دستور `shutdown` در حلقهٔ کنسول → ذخیره + `System.exit(0)`

#### ۵. `HostDataStore.java` (host) — اجباری
- ذخیره: مکالمات + کاربران هر Workspace → `data/host-<ip>-<startPort>.dat`
- بارگیری هنگام راه‌اندازی
- دستور `shutdown` در HostMain

#### ۶. تست نهایی
- دو کلاینت به یک workspace وصل شوند
- پیام بفرستند و بگیرند
- `get-chats` / `get-messages`
- `disconnect`
- `shutdown` و راه‌اندازی دوباره → داده‌ها سر جایشان باشند

**خروجی روز ۵:** گپ شخصی کامل + داده‌ها بعد از restart حفظ می‌شوند.

---

## ترتیب پیشنهادی جلسات کدنویسی

1. ✅ ~~ثبت میزبان (create-host + check)~~ — تمام شده
2. 🔲 روز ۱: `User` + `UserManager` + `register`/`login`
3. 🔲 روز ۲: `WorkspaceInfo` + `WorkspaceManager` + `Workspace` + `create-workspace`
4. 🔲 روز ۳: `Token` + `TokenManager` + `connect-workspace` + `connect`/`whois`/`username?`
5. 🔲 روز ۴: پروژهٔ `client` + مدل‌های `Message`/`Chat`
6. 🔲 روز ۵: چت کامل + `disconnect` + `DataStore` / `HostDataStore` + `shutdown`

**پیشنهاد:** برای هر روز وقتی آماده بودید بگویید «بریم سراغ روز ۱» تا همان‌طور که تا الان جلو رفتیم، فایل‌به‌فایل با هم بنویسیم.

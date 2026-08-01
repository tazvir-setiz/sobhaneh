# راهنمای کامل پیاده‌سازی پیام‌رسان — نسخهٔ دقیق‌شده
### مخصوص تازه‌کار جاوا — هر مرحله دقیقاً بگو چیکار کنم (بدون نیاز به پرسیدن از کسی)

> این فایل جایگزین roadmap قبلی است. طبق سند اصلی پروژه (فازهای اول و دوم) و ساختار
> نهایی پوشه‌ها بازنویسی شده. هرجا "✅ انجام شده" نوشته، یعنی از قبل داریم. هرجا
> "🔲 باید بنویسیم" نوشته، مرحلهٔ بعدی کار شماست. برای هر بخش، **فرمت دقیق پروتکل**
> (دستور ورودی، پاسخ خروجی، خطاهای ممکن) مستقیماً از سند اصلی آورده شده تا نیازی به
> حدس زدن یا پرسیدن نباشد.

---

## ۰. ساختار نهایی پروژه

```
messenger-project/
├── central-server/                          # پروژه سرور مرکزی
│   ├── src/main/java/ir/sobhaneh/central/
│   │   ├── CentralServer.java               ✅ نقطه شروع (main)
│   │   ├── ClientHandler.java               ✅ dispatch دستورات (بازبینی‌شده طبق اصول کلین‌کد)
│   │   ├── HostManager.java                 ✅ مدیریت میزبان‌ها
│   │   ├── HostRegistrationSession.java     ✅ منطق create-host / check (بازبینی‌شده)
│   │   ├── VerificationService.java         ✅ تولید/ارسال کد تأیید
│   │   ├── ReservationResult.java           ✅ نتیجهٔ رزرو پورت
│   │   ├── UserManager.java                 ✅ مدیریت کاربران (نیاز به تکمیل اعتبارسنجی واقعی)
│   │   ├── WorkspaceManager.java            ✅ مدیریت فضای‌کارها (بازبینی‌شده)
│   │   ├── TokenManager.java                🔲 مدیریت توکن‌های موقت
│   │   ├── models/
│   │   │   ├── HostInfo.java                ✅
│   │   │   ├── User.java                    ✅
│   │   │   ├── WorkspaceInfo.java           ✅
│   │   │   └── Token.java                   🔲
│   │   └── persistence/
│   │       └── DataStore.java               🔲 ذخیره/بارگیری (فاز ۲ سند)
│   └── data/
│       └── central.dat                      🔲 فایل ذخیره‌شده
│
├── host/                                    # پروژه میزبان
│   ├── src/main/java/ir/sobhaneh/host/
│   │   ├── HostMain.java                    ✅ نقطه شروع
│   │   ├── HostRegistration.java            ✅ ثبت‌نام میزبان در مرکزی
│   │   ├── HostConfig.java                  ✅ تنظیمات ip/بازهٔ پورت
│   │   ├── WorkspaceManager.java            ✅ مدیریت فضای‌کارهای این میزبان
│   │   ├── Workspace.java                   ✅ کلاس فضای کار (اسکلت، بدون منطق چت)
│   │   ├── ClientConnection.java            🔲 هندلر هر کلاینت متصل به فضای کار
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
│       ├── ClientMain.java                  🔲 نقطه شروع + حلقه دستورات (پارامتر ورودی: phone_number, password)
│       ├── CentralConnection.java           🔲 اتصال موقت به مرکزی
│       ├── WorkspaceConnection.java         🔲 اتصال پایدار به فضای کار
│       ├── CommandParser.java               🔲 پارس دستورات کاربر
│       └── models/                          🔲 (در صورت نیاز)
│
└── common/                                  # (اختیاری) کد مشترک
    └── src/main/java/ir/sobhaneh/common/
        ├── Connection.java                  ✅ خواندن/نوشتن خط روی سوکت
        └── ProtocolUtils.java               🔲 پیشنهادی — پارس امن ورودی‌های پروتکل (پایین را ببین)
```

---

## ⚠️ قانون مهم اتصال‌ها (طبق سند اصلی، عیناً نقل‌شده)

> «در سناریوهایی که مربوط به سرور مرکزی هستند، کلاینت همان موقع اتصال را برقرار کرده
> و وقتی کار تمام شد، آن را می‌بندد. ولی در سناریوهای مربوط به فضای کارها، کلاینت
> اتصال را باز نگه می‌دارد.»
>
> «تقریباً در همه سناریوها کاربر باید از پیش لاگین شده باشد.»

| سناریو | اتصال | رفتار |
|--------|--------|--------|
| `register` تکی | مرکزی | باز کن → دستور → جواب → **ببند** |
| `login` (زیرسناریو، ابتدای بقیه سناریوها) | مرکزی | همان اتصالِ باز، ادامهٔ همان سناریو |
| `create-workspace` | مرکزی | باز کن → `login` → `create-workspace` → جواب → **ببند** |
| `connect-workspace` | مرکزی | باز کن → `login` → `connect-workspace` → جواب → **ببند** |
| `connect` + چت | فضای کار | باز کن → `connect <token>` → (احتمالاً `username?`) → **باز بماند** |
| `create-host` (میزبان) | مرکزی | باز کن → ثبت‌نام → **برای همیشه باز بماند** |

روی `ClientHandler` (central) فیلد `private Long loggedInUserId;` فقط برای عمر
همین اتصال معنا دارد. روی `ClientConnection` (host) هم مشابهاً `UserSession` فقط
برای عمر همین اتصال به فضای کار معنا دارد.

⚠️ **نکتهٔ مهم دربارهٔ کلاینت:** طبق سند، کلاینت با پارامترهای `phone_number` و
`password` اجرا می‌شود؛ یعنی این دو مقدار را کلاینت از خط فرمان می‌گیرد و **در هر
سناریویی که نیاز به لاگین دارد** (create-workspace، connect-workspace، ...) خودش
اول یک `login` روی همان اتصال می‌زند، بدون این‌که از کاربر دوباره بپرسد.

---

## ⭐ اصول کلین‌کد این پروژه (الزامی برای همهٔ روزهای بعدی)

1. **هر تابع فقط یک کار.** اگر تابعی هم I/O شبکه انجام می‌دهد، هم validation می‌کند،
   هم state را عوض می‌کند، هم پاسخ پروتکل می‌سازد → باید تجزیه شود.
2. **Dispatch از منطق دامنه جدا باشد.** متدهای `dispatchX` فقط پارس/اعتبارسنجی ورودی
   + صدا زدن یک متد دامنه + ارسال پاسخ. منطق واقعی داخل متد دامنه.
3. **لاک فقط دور بخش critical، نه دور I/O شبکه.** هرگز `synchronized` دور
   `readLine()`/`sendLine()` نگذارید.
4. **بدون تکرار پارس عدد/رشته.** یک متد کمکی مشترک (`ProtocolUtils` در `common`)
   بسازید و همه‌جا از آن استفاده کنید.
5. **متدهای placeholder را واضح علامت بزنید** (`// TODO`) تا کسی گمان نکند
   اعتبارسنجی واقعی انجام می‌شود.
6. **رمز عبور هرگز plain-text ذخیره نشود** — قبل از پایان روز ۱ هش کنید.
7. **Magic Number ممنوع.** هر عدد یا رشتهٔ تکرارشونده باید `private static final`
   باشد (طول توکن = ۱۰، طول کد تایید = ۱۰، عمر توکن = ۵ دقیقه، حداکثر طول نام
   فضای کار = ۶۰، و ...).

---

## تقسیم‌بندی ۵ روزه

| روز | موضوع | سختی |
|-----|--------|------|
| ۱ | کاربران + login state | متوسط |
| ۲ | ایجاد فضای کار | متوسط‌روبه‌بالا |
| ۳ | اتصال و توکن | متوسط‌روبه‌بالا |
| ۴ | کلاینت + پایهٔ چت | متوسط‌روبه‌بالا |
| ۵ | چت کامل + disconnect + shutdown (فاز دوم سند) | متوسط‌روبه‌بالا |

---

# روز ۱ — کاربران + آماده‌سازی پایه ✅ (تکمیل‌شده، نیاز به سخت‌سازی امنیتی)

**هدف:** ثبت‌نام و ورود کار کند، دقیقاً طبق «سناریوی ثبت نام» و «زیرسناریوی ورود» سند.

### فرمت دقیق پروتکل (از سند)

**ثبت‌نام:**
```
کلاینت → مرکزی:  register 09123456789 123456
مرکزی → کلاینت:  OK
```
بعد از `OK`، طبق سند، کلاینت اتصال را می‌بندد (چون سناریوی ثبت‌نام مستقل است).

**ورود (زیرسناریو، همیشه در ابتدای سناریوهای دیگر روی همان اتصال):**
```
کلاینت → مرکزی:  login 09123456789 123456
مرکزی → کلاینت:  OK
```
سند سناریوی خطا را برای این دو دستور توضیح نداده، ولی طبق الگوی کلی پروژه (نگاه کنید
به فرمت خطاهای `create-host`)، خطاها باید با `ERROR ` شروع شوند، مثلاً:
```
ERROR User already exists
ERROR Invalid Phone Number
ERROR Invalid Password
ERROR User doesn't exists
ERROR Incorrect Password
```

### وضعیت فعلی و کارهای باقی‌مانده
`User.java`, `UserManager.java`, و دستورات `register`/`login` در `ClientHandler` از
قبل پیاده شده‌اند. قبل از رفتن به روز ۲:

1. 🔲 **هش کردن پسورد** در `User.java` (قانون ۶) — رمز عبور هرگز نباید plain-text
   ذخیره شود، حتی در یک پروژهٔ درسی.
2. 🔲 **پیاده‌سازی واقعی** `checkPhoneNumber` (مثلاً باید یک شمارهٔ موبایل معتبر باشد؛
   سند فرمت دقیقی نداده، پس یک قانون ساده مثل «۱۱ رقم و شروع با `09`» کافی است) و
   `checkPassword` (مثلاً حداقل ۶ کاراکتر) — یا حداقل TODO واضح (قانون ۵).
3. 🔲 پیام‌های خطا در `UserManager.login` را یکدست کنید — الان `"ERROR Incorrect Password"`
   یک شاخهٔ مرده است چون `checkPassword` همیشه `true` برمی‌گرداند؛ وقتی مورد ۲ پیاده
   شد این مسیر واقعاً قابل دسترس می‌شود.

### تست با telnet
```
register 09123456789 123456     → OK
register 09123456789 123456     → ERROR User already exists
login 09123456789 123456        → OK
login 09123456789 wrongpass     → ERROR Incorrect Password
```

---

# روز ۲ — ایجاد فضای کار ✅ (تکمیل‌شده و بازبینی‌شده)

**هدف:** «سناریوی ایجاد فضای کار» طبق سند، از کلاینت تا میزبان کامل شود.

### فرمت دقیق پروتکل (از سند)

```
۱. کاربر تایپ می‌کند:            create-workspace company1
۲. کلاینت → مرکزی (بعد از login): create-workspace company1
۳. مرکزی → میزبان:               create-workspace 10143 1001
                                  (پورت انتخابی + شناسه کاربر سازنده)
۴. میزبان → مرکزی:               OK
۵. مرکزی → کلاینت:               OK 127.0.0.1 10143
۶. کلاینت اتصال با مرکزی را می‌بندد.
```

### قانون یکتایی نام فضای کار (از سند)
> «هر فضای کار یک نام یکتا دارد که یک رشته حداکثر ۶۰ کاراکتری از اعداد و حروف کوچک
> و بزرگ انگلیسی و `_` است.»

⚠️ **این یک نکتهٔ ازقلم‌افتاده در پیاده‌سازی فعلی است.** `WorkspaceManager.createWorkspace`
فعلاً فقط چک می‌کند که نام تکراری نباشد؛ باید یک متد جدید `validateWorkspaceName(name)`
اضافه شود که این‌ها را چک کند (طبق قانون ۱، این باید یک متد جدا باشد، نه قاطی منطق
رزرو):
```java
private static final int MAX_WORKSPACE_NAME_LENGTH = 60;
private static final Pattern WORKSPACE_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+");

private String validateWorkspaceName(String name) {
    if (name.length() > MAX_WORKSPACE_NAME_LENGTH) {
        return "ERROR Workspace name too long (max " + MAX_WORKSPACE_NAME_LENGTH + ")";
    }
    if (!WORKSPACE_NAME_PATTERN.matcher(name).matches()) {
        return "ERROR Workspace name must contain only letters, digits, and underscore";
    }
    return null;
}
```
این متد باید اول چیز در `createWorkspace` صدا زده شود (قبل از چک تکراری بودن).

### وضعیت فعلی
`WorkspaceInfo`, `WorkspaceManager` (central و host)، `Workspace` (host)، و دستور
`create-workspace` در `ClientHandler` پیاده و طبق اصول کلین‌کد بازبینی شده‌اند.

### باقی‌ماندهٔ کار
1. 🔲 اعتبارسنجی نام فضای کار (بالا را ببینید).
2. 🔲 `Workspace.java` (host) هنوز فقط اتصال کلاینت را لاگ می‌کند؛ در روز ۳ باید به
   `ClientConnection` ارجاع دهد.
3. 🔲 تست هم‌زمانی: دو `create-workspace` با اسم یکسان از دو اتصال مختلف در آنِ واحد.

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

# روز ۳ — اتصال به فضای کار و احراز هویت

**هدف:** «سناریوی اتصال به فضای کار ایجادشده» طبق سند، دقیقاً قدم‌به‌قدم.

### فرمت دقیق پروتکل (از سند) — این ۹ قدم را عیناً پیاده کنید

```
۱. کاربر تایپ می‌کند:              connect-workspace company1
۲. کلاینت → مرکزی (بعد از login):  connect-workspace company1
۳. مرکزی → کلاینت:                OK 127.0.0.1 10143 fkla48fhhf
                                   (آدرس + پورت فضای کار + توکن موقت)
۴. کلاینت اتصال با مرکزی را می‌بندد و به فضای کار وصل می‌شود:
   کلاینت → فضای‌کار:              connect fkla48fhhf
۵. فضای کار (میزبان) → مرکزی:      whois fkla48fhhf
۶. مرکزی → فضای کار:               OK 1001
                                   (شناسه کاربر)
۷. اگر اولین اتصال این کاربر به این فضای کار باشد:
   فضای‌کار → کلاینت:              username?
۸. کلاینت → فضای‌کار:              ahmad
                                   (نام کاربری باید یکتا باشد)
۹. فضای‌کار → کلاینت:              OK
```

⚠️ نکات مهمی که باید دقیقاً رعایت شوند:
- **توکن موقت:** «شامل ۱۰ کاراکتر از اعداد و حروف کوچک انگلیسی است و حداکثر ۵ دقیقه
  عمر دارد» — یعنی الفبای مجاز فقط `a-z0-9` است (نه حروف بزرگ)، دقیقاً برخلاف تصور
  اولیه که ممکن است "کد تصادفی base64" تصور شود.
- **whois باید هم روی اتصال میزبان به مرکزی زده شود** — یعنی همان `Connection`ای که
  میزبان از قبل با مرکزی برای `create-host`/`create-workspace` باز نگه داشته، همان
  را برای `whois` هم استفاده می‌کند (طبق قانون کلی «میزبان اتصال خود را با سرور
  مرکزی حفظ می‌کند»).
- **کلاینت هم‌زمان فقط به یک فضای کار متصل است** — این یعنی `WorkspaceConnection`
  در کلاینت باید singleton-مانند طراحی شود (یک اتصال باز در هر لحظه، نه چندتا).

### فایل‌هایی که باید بسازید / تغییر دهید

```
central-server/src/main/java/ir/sobhaneh/central/
├── models/
│   └── Token.java                   🔲 بساز
├── TokenManager.java                🔲 بساز
└── ClientHandler.java               ✅ تغییر بده (dispatch جدید: connect-workspace, whois)

host/src/main/java/ir/sobhaneh/host/
├── ClientConnection.java            🔲 بساز
├── models/
│   └── UserSession.java             🔲 بساز
└── Workspace.java                   ✅ تغییر بده (قبول اتصال کلاینت و ارجاع به ClientConnection)
```

### جزئیات پیاده‌سازی

#### ۱. `Token.java` (central/models)
```java
public class Token {
    private final String value;         // دقیقاً ۱۰ کاراکتر، الفبای a-z0-9
    private final long userId;
    private final long expiresAtMillis; // زمان ساخت + ۵ دقیقه

    // constructor + getterها

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAtMillis;
    }
}
```

#### ۲. `TokenManager.java` (central)
```java
public class TokenManager {
    private static final int TOKEN_LENGTH = 10;
    private static final long TOKEN_TTL_MILLIS = 5 * 60 * 1000;
    private static final String TOKEN_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private final ConcurrentHashMap<String, Token> tokens = new ConcurrentHashMap<>();

    public Token createToken(long userId) {
        String value = generateRandomValue();
        Token token = new Token(value, userId, System.currentTimeMillis() + TOKEN_TTL_MILLIS);
        tokens.put(value, token);
        return token;
    }

    public Long resolveToken(String value) {
        Token token = tokens.get(value);
        if (token == null || token.isExpired()) {
            return null;
        }
        return token.getUserId();
    }

    private String generateRandomValue() {
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(TOKEN_ALPHABET.charAt(ThreadLocalRandom.current().nextInt(TOKEN_ALPHABET.length())));
        }
        return sb.toString();
    }
}
```
⚠️ توکن‌های منقضی هیچ‌وقت پاک نمی‌شوند در این نسخهٔ ساده (memory leak کوچک قابل قبول
برای این فاز)؛ اگر خواستید کامل‌تر شود، یک `ScheduledExecutorService` برای پاکسازی
دوره‌ای اضافه کنید — ولی این اختیاری است و لازم نیست روز ۳ را معطلش کنید.

#### ۳. دستورات جدید در `ClientHandler` (central)
دقیقاً طبق الگوی dispatch/domain که قبلاً پیاده کردیم:

```java
private void dispatchConnectWorkspace(Connection connection, String[] parts) throws IOException {
    if (parts.length != 2) {
        connection.sendLine("ERROR Usage: connect-workspace <workspaceName>");
        return;
    }
    if (loggedInUserId == null) {
        connection.sendLine(ERROR_NOT_LOGGED_IN);
        return;
    }
    connection.sendLine(connectToWorkspace(parts[1]));
}

private String connectToWorkspace(String workspaceName) {
    WorkspaceInfo info = workspaceManager.find(workspaceName);
    if (info == null) {
        return "ERROR Workspace not found";
    }
    Token token = tokenManager.createToken(loggedInUserId);
    return "OK " + info.getHostIp() + " " + info.getPort() + " " + token.getValue();
}

private void dispatchWhois(Connection connection, String[] parts) throws IOException {
    if (parts.length != 2) {
        connection.sendLine("ERROR Usage: whois <token>");
        return;
    }
    Long userId = tokenManager.resolveToken(parts[1]);
    if (userId == null) {
        connection.sendLine("ERROR Invalid or expired token");
        return;
    }
    connection.sendLine("OK " + userId);
}
```
⚠️ برای این‌که `workspaceManager.find(name)` کار کند، باید یک متد ساده در central
`WorkspaceManager` اضافه کنید که فقط از `ConcurrentHashMap` بخواند:
```java
public WorkspaceInfo find(String name) {
    return workspaces.get(name);
}
```

#### ۴. `UserSession.java` (host/models)
```java
public class UserSession {
    private final long userId;
    private String username;
    private final Connection connection;
}
```

#### ۵. `ClientConnection.java` (host) — طبق قانون ۱، به سه مرحلهٔ مجزا تقسیم کنید

```java
public class ClientConnection implements Runnable {
    private final Socket clientSocket;
    private final Connection centralConnection;
    private final Workspace workspace;

    @Override
    public void run() {
        try (Connection connection = new Connection(clientSocket)) {
            UserSession session = authenticate(connection);
            if (session == null) return;

            resolveUsername(connection, session);
            registerSession(session);

            listenForCommands(connection, session);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private UserSession authenticate(Connection connection) throws IOException {
        String line = connection.readLine();
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2 || !"connect".equals(parts[0])) {
            connection.sendLine("ERROR Usage: connect <token>");
            return null;
        }
        Long userId = askCentralWhois(parts[1]);
        if (userId == null) {
            connection.sendLine("ERROR Invalid or expired token");
            return null;
        }
        return new UserSession(userId, connection);
    }

    private Long askCentralWhois(String token) throws IOException {
        centralConnection.sendLine("whois " + token);
        String response = centralConnection.readLine();
        if (response == null || !response.startsWith("OK")) {
            return null;
        }
        return Long.parseLong(response.split(" ")[1]);
    }

    private void resolveUsername(Connection connection, UserSession session) throws IOException {
        String existingUsername = workspace.findUsernameForUser(session.getUserId());
        if (existingUsername != null) {
            session.setUsername(existingUsername);
            return;
        }
        connection.sendLine("username?");
        String username = connection.readLine();
        session.setUsername(username);
        connection.sendLine("OK");
    }

    private void registerSession(UserSession session) {
        workspace.addOnlineSession(session);
    }
}
```

#### ۶. `Workspace.java` (host) — تغییرات لازم
```java
private final ConcurrentHashMap<Long, UserSession> onlineByUserId = new ConcurrentHashMap<>();
private final ConcurrentHashMap<String, Long> userIdByUsername = new ConcurrentHashMap<>();

public String findUsernameForUser(long userId) {
    UserSession s = onlineByUserId.get(userId);
    return s == null ? null : s.getUsername();
}

public void addOnlineSession(UserSession session) {
    onlineByUserId.put(session.getUserId(), session);
    userIdByUsername.put(session.getUsername(), session.getUserId());
}
```
⚠️ توجه: `findUsernameForUser` فعلاً فقط کاربران **آنلاین** را چک می‌کند. طبق سند،
باید یکتایی username در طول عمر فضای کار حفظ شود، نه فقط در لحظهٔ آنلاین بودن —
یعنی در فاز دوم (وقتی `HostDataStore` اضافه شود) این باید از روی دیتای ذخیره‌شده هم
چک شود.

#### ۷. تست جریان کامل (دقیقاً طبق سند)
```
--- اتصال اول به central ---
login 09123456789 123456        → OK
connect-workspace company1      → OK 127.0.0.1 10143 fkla48fhhf
--- (این اتصال بسته می‌شود) ---

--- اتصال جدید به فضای کار (میزبان، پورت ۱۰۱۴۳) ---
connect fkla48fhhf              → username?
ahmad                            → OK
--- این اتصال باز می‌ماند ---
```

**خروجی روز ۳:** کاربر احراز هویت شده داخل فضای کار است و اتصالش باز می‌ماند.

---

# روز ۴ — برنامهٔ کلاینت + پایهٔ چت

**هدف:** به‌جای تست با telnet، یک برنامهٔ کلاینت واقعی که کل سناریوهای بالا را خودکار
انجام دهد، به‌علاوهٔ اسکلت مدل‌های پیام برای روز ۵.

### نکات دقیق از سند که باید در کلاینت پیاده شوند

- کلاینت با پارامترهای `phone_number` و `password` اجرا می‌شود؛ یعنی این دو مقدار
  **آرگومان خط فرمان** برنامهٔ `ClientMain` هستند، نه چیزی که هر بار پرسیده شود.
- کلاینت دو وظیفهٔ اصلی دارد: «بسته‌های دریافتی از فضای کار را نمایش می‌دهد» و
  «دستورهای کاربر را دریافت کرده و به فضای کار می‌فرستد» — یعنی حداقل باید **دو
  Thread** داشته باشد: یکی برای خواندن پیام‌های ورودی از سوکت (و چاپشان)، یکی برای
  خواندن دستورات از کنسول کاربر و ارسالشان.

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

### جزئیات پیاده‌سازی

#### ۱. `CentralConnection.java`
هر متد باید خودش یک اتصال جدید باز و بسته کند، ولی باز/بستن سوکت را در یک متد خصوصی
مشترک قرار دهید تا در چند متد تکرار نشود:

```java
public class CentralConnection {
    private final String centralIp;
    private final int centralPort;
    private final String phoneNumber;
    private final String password;

    public String register() throws IOException {
        return withCentralConnection(connection -> {
            connection.sendLine("register " + phoneNumber + " " + password);
            return connection.readLine();
        });
    }

    public String createWorkspace(String name) throws IOException {
        return withCentralConnection(connection -> {
            login(connection);
            connection.sendLine("create-workspace " + name);
            return connection.readLine();
        });
    }

    public String[] connectWorkspace(String name) throws IOException {
        String response = withCentralConnection(connection -> {
            login(connection);
            connection.sendLine("connect-workspace " + name);
            return connection.readLine();
        });
        String[] parts = response.split(" ");
        return new String[]{parts[1], parts[2], parts[3]};
    }

    private void login(Connection connection) throws IOException {
        connection.sendLine("login " + phoneNumber + " " + password);
        connection.readLine();
    }

    private String withCentralConnection(ConnectionAction action) throws IOException {
        try (Socket socket = new Socket(centralIp, centralPort);
             Connection connection = new Connection(socket)) {
            return action.run(connection);
        }
    }

    @FunctionalInterface
    private interface ConnectionAction {
        String run(Connection connection) throws IOException;
    }
}
```

#### ۲. `WorkspaceConnection.java` — سه مرحله جدا
```java
public class WorkspaceConnection {
    private Socket socket;
    private Connection connection;
    private Thread readerThread;

    public void connect(String ip, int port, String token) throws IOException {
        openSocket(ip, port);
        authenticate(token);
        startReaderThread();
    }

    private void openSocket(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        connection = new Connection(socket);
    }

    private void authenticate(String token) throws IOException {
        connection.sendLine("connect " + token);
        String response = connection.readLine();
        if ("username?".equals(response)) {
            String username = readUsernameFromConsole();
            connection.sendLine(username);
            connection.readLine();
        }
    }

    private void startReaderThread() {
        readerThread = new Thread(this::readLoop);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readLoop() {
        try {
            String line;
            while ((line = connection.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Connection to workspace closed.");
        }
    }

    public void sendMessage(String username, String json) throws IOException {
        connection.sendLine("send-message " + username + " " + json);
    }

    public void getChats() throws IOException {
        connection.sendLine("get-chats");
    }

    public void getMessages(String username) throws IOException {
        connection.sendLine("get-messages " + username);
    }

    public void disconnect() throws IOException {
        connection.sendLine("disconnect");
        connection.close();
    }
}
```

#### ۳. `CommandParser.java`
```java
public class CommandParser {
    public record ParsedCommand(String name, String[] args) {}

    public ParsedCommand parse(String line) {
        String[] parts = line.trim().split("\\s+", 3);
        return new ParsedCommand(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
    }
}
```
⚠️ توجه به `send-message saeed {"type": "text", "body": "..."}` — چون JSON خودش
می‌تواند فاصله داشته باشد، `split` باید با `limit=3` انجام شود تا JSON یک تکه بماند.

#### ۴. `ClientMain.java`
```java
public class ClientMain {
    public static void main(String[] args) {
        String phoneNumber = args[0];
        String password = args[1];
        // ساخت CentralConnection، WorkspaceConnection، CommandParser
        // حلقهٔ بی‌نهایت: خواندن خط از Scanner(System.in) → parse → dispatch
    }
}
```

#### ۵. `Message.java` (host/models)
```java
public class Message {
    private final int seq;
    private final String from;
    private final String type;
    private final String body;
    private final long timestamp;
}
```

#### ۶. `Chat.java` (host/models)
```java
public class Chat {
    private final String userA;
    private final String userB;
    private final List<Message> messages = new ArrayList<>();
    private int lastSeq = 0;

    public static String buildKey(String u1, String u2) {
        return u1.compareTo(u2) < 0 ? u1 + ":" + u2 : u2 + ":" + u1;
    }
}
```

#### ۷. تست
همهٔ دستورات `register` / `login` / `create-workspace` / `connect-workspace` /
`connect` را از طریق کلاینت واقعی (نه telnet) اجرا کنید و خروجی کنسول را با
سناریوهای سند مقایسه کنید.

**خروجی روز ۴:** کلاینت تعاملی کار می‌کند و مدل‌های چت آماده است.

---

# روز ۵ — چت کامل + قطع اتصال + ذخیره‌سازی (فاز اول پایانی + فاز دوم سند)

**هدف:** «سناریوی ارسال پیام»، «دریافت لیست چت‌ها»، «دریافت گفتگو»، «قطع اتصال» و
فاز دوم سند («ذخیرهٔ پیام‌ها» با `shutdown`).

### فرمت دقیق پروتکل (از سند) — عیناً پیاده کنید

**ارسال پیام:**
```
کلاینت → فضای‌کار:  send-message saeed {"type": "text", "body": "Salam chetori?"}
فضای‌کار → فرستنده: OK 1
فضای‌کار → گیرنده (اگر آنلاین است):
  receive-message ahmad {"seq": 1, "from": "ahmad", "type": "text", "body": "Salam chetori?"}
```

**دریافت لیست چت‌ها:**
```
کلاینت → فضای‌کار:  get-chats
فضای‌کار → کلاینت:  OK [{"name": "saeed", "unread_count": 2}, ...]
```

**دریافت گفتگو با کاربر دیگر:**
```
کلاینت → فضای‌کار:  get-messages saeed
فضای‌کار → کلاینت:  OK [{"seq": 1, "from": "ahmad", "type": "text", "body": "Salam chetori?"}, ...]
```
⚠️ طبق سند: «فضای کار، گفتگو را به عنوان خوانده‌شده علامت می‌زند» — یعنی این دستور
یک side-effect دارد (unread_count صفر می‌شود).

**قطع اتصال:**
```
کلاینت → فضای‌کار:  disconnect
(کلاینت اتصال را می‌بندد)
```

**ذخیره‌سازی (فاز دوم سند):**
> «در صورتی که دستور shutdown را در سرور مرکزی یا میزبان تایپ کنیم، همه داده‌ها را
> در یک فایل با آدرس مشخص ذخیره کرده و بسته می‌شود. در هنگام راه‌اندازی سرور مرکزی
> یا میزبان، اگر فایل مذکور وجود داشته باشد، آن را بارگیری می‌کند.»

### فایل‌هایی که باید بسازید / تغییر دهید

```
host/src/main/java/ir/sobhaneh/host/
├── ClientConnection.java            ✅ تغییر بده (send-message / get-chats / get-messages / disconnect)
├── Workspace.java                   ✅ تغییر بده (ChatStore را اضافه کن)
├── ChatStore.java                   🔲 بساز (جدا از Workspace)
└── persistence/
    └── HostDataStore.java           🔲 بساز

central-server/src/main/java/ir/sobhaneh/central/
└── persistence/
    └── DataStore.java               🔲 بساز

(+ تغییر HostMain.java و CentralServer.java برای دستور shutdown از کنسول و بارگیری در ابتدای main)
```

### جزئیات پیاده‌سازی

#### ۱. `ChatStore.java` (host) — جدا از `Workspace`
```java
public class ChatStore {
    private final ConcurrentHashMap<String, Chat> chatsByKey = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> chatPartnersByUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> unreadCountByUserAndPartner = new ConcurrentHashMap<>();

    public synchronized int addMessage(String from, String to, String type, String body) {
        String key = Chat.buildKey(from, to);
        Chat chat = chatsByKey.computeIfAbsent(key, k -> new Chat(from, to));
        int seq = chat.appendMessage(from, type, body);
        incrementUnread(to, from);
        recordPartnership(from, to);
        return seq;
    }

    public String getChatsJson(String forUser) {
        // ساخت JSON آرایه از chatPartnersByUser + unreadCountByUserAndPartner (با Gson)
    }

    public String getMessagesJson(String forUser, String otherUser) {
        // خواندن Chat مربوطه + صفر کردن unread + تبدیل به JSON با Gson
    }
}
```

#### ۲. دستورات جدید در `ClientConnection` (host)
```java
private void dispatchSendMessage(Connection connection, UserSession session, String[] parts) throws IOException {
    if (parts.length < 3) {
        connection.sendLine("ERROR Usage: send-message <username> <json>");
        return;
    }
    String toUsername = parts[1];
    String json = parts[2];
    int seq = chatStore.addMessage(session.getUsername(), toUsername, parseType(json), parseBody(json));
    connection.sendLine("OK " + seq);
    forwardToRecipientIfOnline(toUsername, session.getUsername(), seq, json);
}

private void forwardToRecipientIfOnline(String toUsername, String from, int seq, String json) throws IOException {
    UserSession recipientSession = workspace.findSessionByUsername(toUsername);
    if (recipientSession != null) {
        recipientSession.getConnection().sendLine("receive-message " + from + " " +
            buildReceiveMessageJson(seq, from, json));
    }
}

private void dispatchGetChats(Connection connection, UserSession session) throws IOException {
    connection.sendLine("OK " + chatStore.getChatsJson(session.getUsername()));
}

private void dispatchGetMessages(Connection connection, UserSession session, String[] parts) throws IOException {
    if (parts.length != 2) {
        connection.sendLine("ERROR Usage: get-messages <username>");
        return;
    }
    connection.sendLine("OK " + chatStore.getMessagesJson(session.getUsername(), parts[1]));
}

private void dispatchDisconnect(UserSession session) {
    workspace.removeOnlineSession(session);
}
```

#### ۳. قطع ناگهانی (بدون دستور disconnect)
```java
private void listenForCommands(Connection connection, UserSession session) throws IOException {
    try {
        String line;
        while ((line = connection.readLine()) != null) {
            dispatch(connection, session, line);
        }
    } finally {
        workspace.removeOnlineSession(session);
    }
}
```

#### ۴. `DataStore.java` (central) — اجباری طبق فاز دوم سند
```java
public class DataStore {
    private static final String FILE_PATH = "data/central.dat";

    public void save(UserManager userManager, HostManager hostManager, WorkspaceManager workspaceManager) {
        // سریالایز با Gson به FILE_PATH
    }

    public void load(UserManager userManager, HostManager hostManager, WorkspaceManager workspaceManager) {
        // اگر فایل وجود داشت، بارگیری و پرکردن Manager ها
    }
}
```
⚠️ برای این‌که `save`/`load` بتوانند به state داخلی `UserManager` و بقیه دسترسی پیدا
کنند، باید متدهای `exportState()`/`importState(...)` به هرکدام اضافه کنید (نه این‌که
`DataStore` مستقیم فیلدهای خصوصی را دستکاری کند).

در `CentralServer.main`:
```java
dataStore.load(userManager, hostManager, workspaceManager);
if ("shutdown".equals(consoleLine)) {
    dataStore.save(userManager, hostManager, workspaceManager);
    System.exit(0);
}
```

#### ۵. `HostDataStore.java` (host) — اجباری
```java
public class HostDataStore {
    public void save(WorkspaceManager workspaceManager, HostConfig config) {
        String path = "data/host-" + config.getIp() + "-" + config.getStartPort() + ".dat";
        // سریالایز
    }

    public void load(WorkspaceManager workspaceManager, HostConfig config) {
        // بارگیری اگر فایل موجود بود
    }
}
```

#### ۶. تست نهایی سناریوی کامل
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

**خروجی روز ۵:** گپ شخصی کامل + داده‌ها بعد از restart حفظ می‌شوند.

---

## نکات مهم فازهای بعدی سند (فقط برای آگاهی، نه بخشی از این ۵ روز)

طبق سند، بعد از فاز اول و دوم، دو فاز دیگر هم وجود دارد که **در این roadmap پوشش
داده نشده‌اند**:

- **فاز سوم:** امکان ویرایش پیام + امکان ارسال استیکر (نوع جدید `type` در JSON پیام).
- **فاز چهارم:** گروه‌ها — یک نوع گفتگوی جدید با نام یکتا (که با username کاربران هم
  اشتراک ندارد)، به‌علاوهٔ دستورات ایجاد گروه، عضویت، و افزودن عضو (با دو شرط: کاربر
  خودش عضو گروه باشد و با کاربر جدید از قبل گفتگو داشته باشد).

اگر می‌خواهید این دو فاز را هم به roadmap اضافه کنیم، بگویید تا با همین سطح از
جزئیات اضافه کنم.

---

## ترتیب پیشنهادی جلسات کدنویسی

1. ✅ ~~ثبت میزبان (create-host + check)~~ — تمام شده
2. ✅ ~~روز ۱: `User` + `UserManager` + `register`/`login`~~ — تمام شده (سخت‌سازی امنیتی باقی‌مانده)
3. ✅ ~~روز ۲: `WorkspaceInfo` + `WorkspaceManager` + `Workspace` + `create-workspace`~~ — تمام شده (اعتبارسنجی نام باقی‌مانده)
4. 🔲 روز ۳: `Token` + `TokenManager` + `connect-workspace` + `connect`/`whois`/`username?`
5. 🔲 روز ۴: پروژهٔ `client` + مدل‌های `Message`/`Chat`
6. 🔲 روز ۵: چت کامل + `disconnect` + `DataStore` / `HostDataStore` + `shutdown`

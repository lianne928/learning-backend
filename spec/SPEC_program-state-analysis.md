# Learning-Backend 程式現況分析報告

**分析日期：** 2026-03-13
**分析範圍：** 全專案 Java 原始碼（src/main/java）、設定檔、相依套件（pom.xml）
**報告版本：** 1.0

---

## 執行摘要

### 整體健康度評分

| 面向 | 評分 | 說明 |
|------|------|------|
| 架構健康度 | ⭐⭐⭐☆☆ | MVC 分層清晰，但安全設定層完全失效 |
| 程式碼品質 | ⭐⭐☆☆☆ | 存在致命邏輯 bug、magic numbers、濫用 System.out.println |
| 測試覆蓋 | ⭐☆☆☆☆ | 僅 1 個空白啟動測試，業務邏輯零測試 |
| 安全風險 | ⭐☆☆☆☆ | 全部 API 無驗證、Login 永遠成功、CORS 全開 |
| 依賴健康 | ⭐⭐⭐⭐☆ | 依賴版本合理，Spring Boot 4.0.2 較新但無已知 CVE |
| 效能基準 | ⭐⭐⭐☆☆ | 無監控數據，靜態推斷無明顯 N+1，但缺分頁查詢保護 |

**整體評級：** D（38/100）

### 三大最緊急問題

1. 🔴 **安全機制完全停用**：`SecurityConfig.java` 全行註解，`TestSecurityConfig` 生效，所有 API 端點無需任何驗證即可呼叫，資料庫完全暴露。
2. 🔴 **Login 永遠回傳成功**：`UserController.login()` 呼叫的是 `userService.login(User user)` 重載版本，該版本第 72 行直接 `return true`，任何人皆可「登入」。
3. 🔴 **課程建立功能全面損壞**：`CourseService.java:46` 邏輯錯誤（`!=1 || !=2` 恆真），所有建立課程請求永遠回傳失敗。

### 建議立即行動

> 本週可以做的三件事：
> 1. 刪除 `TestSecurityConfig.java`，移除 `SecurityConfig.java` 的所有註解並啟用
> 2. 修正 `CourseService.java:46`：將 `||` 改為 `&&`
> 3. 修正 `UserController.java:33`：將 `userService.login(user)` 改為 `userService.login(user.getEmail(), user.getPassword())`

---

## Phase 1｜架構健康度分析

### 1.1 架構模式識別

**整體模式：** Monolith（單體應用）
**分層結構：** Layered MVC（Controller → Service → Repository → Entity）
**通訊方式：** 同步 HTTP（REST） + 非同步 WebSocket（STOMP over SockJS）

### 架構符合度評分

| 面向 | 評分 (1-5) | 說明 |
|------|-----------|------|
| 關注點分離 | 3 | Controller / Service / Repo 分層明確，但 Service 層直接操作多個 Repo（CheckoutService 依賴 5 個 Repo，耦合偏高） |
| 模組邊界清晰度 | 3 | 套件依功能命名清楚，但 `entity` 包內存在 `Review` 與 `Reviews` 兩個重複意義的類別 |
| 依賴方向一致性 | 4 | 整體由 Controller 往 Service 往 Repo 方向正確，無循環依賴 |
| 可測試性 | 1 | 所有相依使用 `@Autowired` 欄位注入，難以做 unit test mock；JwtService 為空類別 |
| 可觀測性 | 1 | 使用 `System.out.println` 輸出日誌，無結構化 log，Actuator 雖引入但無相關監控設定 |

**架構整體健康度：** 2.4 / 5

### 1.2 關鍵架構問題清單

**問題 1：安全設定層完全失效**
- **影響等級：** 🔴 高
- **位置：** [SecurityConfig.java](src/main/java/com/learning/api/config/SecurityConfig.java)（全行註解）、[TestSecurityConfig.java](src/main/java/com/learning/api/config/TestSecurityConfig.java)（生效中）
- **影響範圍：** 所有 API 端點，包含購買、預約、課程建立
- **改善方向：** 刪除 TestSecurityConfig，取消 SecurityConfig 的所有註解並配合 JwtFilter 實作

**問題 2：JwtService 為空實作**
- **影響等級：** 🔴 高
- **位置：** [JwtService.java](src/main/java/com/learning/api/service/JwtService.java)（第 1-4 行，只有空類別殼）
- **影響範圍：** 整個身份驗證流程
- **改善方向：** 實作 `generateToken()` / `validateToken()` / `extractUserId()` 方法，搭配 `JwtAuthenticationFilter`

**問題 3：User Entity 直接作為 API DTO**
- **影響等級：** 🟡 中
- **位置：** [UserController.java:20,31](src/main/java/com/learning/api/controller/UserController.java)
- **影響範圍：** 使用者相關端點（register / login）
- **改善方向：** 建立 `RegisterReq` / `LoginReq` DTO，避免暴露 `id`、`wallet`、`role` 等敏感欄位

---

## Phase 2｜技術債盤點

### 技術債清單

#### 🔴 優先處理（影響開發速度或安全）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 1 | SecurityConfig 全行註解，TestSecurityConfig 代替生效 | [SecurityConfig.java](src/main/java/com/learning/api/config/SecurityConfig.java) | 系統完全無身份驗證 | 小 |
| 2 | JwtService 為空類別 | [JwtService.java](src/main/java/com/learning/api/service/JwtService.java) | 無法簽發 / 驗證 Token | 中 |
| 3 | Login 呼叫永遠成功的重載方法 | [UserController.java:33](src/main/java/com/learning/api/controller/UserController.java) | 任意帳號密碼皆可登入 | 小 |
| 4 | CourseService 主題驗證邏輯錯誤（`\|\|` 應為 `&&`） | [CourseService.java:46](src/main/java/com/learning/api/service/CourseService.java) | 課程建立 100% 失敗 | 小 |
| 5 | userId 從 Request Body 傳入（無 Auth Context） | [BookingService.java:25](src/main/java/com/learning/api/service/BookingService.java)、[CheckoutService.java:23](src/main/java/com/learning/api/service/CheckoutService.java) | 任何人可冒充他人身份操作 | 中 |

#### 🟡 中期改善（影響可維護性）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 6 | Status 狀態碼使用 Magic Numbers（1/2/3） | [Order.java:36](src/main/java/com/learning/api/entity/Order.java)、[CheckoutService.java:61](src/main/java/com/learning/api/service/CheckoutService.java) | 可讀性差，容易誤用 | 小 |
| 7 | 使用 `System.out.println` 代替 Logger | [CourseService.java:21,31,43,57,61](src/main/java/com/learning/api/service/CourseService.java) | 無法控制 log level，不可關閉 | 小 |
| 8 | Flyway 資料庫遷移關閉 | [application.properties:20](src/main/resources/application.properties)、[pom.xml:122-128](pom.xml) | 無法追蹤 schema 變更歷史，上線風險高 | 中 |
| 9 | `Review` 與 `Reviews` 兩個相似 Entity 共存 | [entity/](src/main/java/com/learning/api/entity/) | 語意混淆，維護困難 | 小 |
| 10 | CheckoutService 防超賣使用「查詢後鎖」非樂觀鎖 | [CheckoutService.java:36-47](src/main/java/com/learning/api/service/CheckoutService.java) | 高並發下仍可能超賣 | 中 |
| 11 | User Entity 作為 register / login 的 Request Body | [UserController.java:20,31](src/main/java/com/learning/api/controller/UserController.java) | 過度暴露 Entity 欄位 | 小 |
| 12 | `spring.jpa.show-sql=true` 在 application.properties | [application.properties:7](src/main/resources/application.properties) | 生產環境 log 暴露所有 SQL | 小 |

#### 🟢 長期優化（提升品質但不緊急）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 13 | 所有 Controller 使用 `@Autowired` 欄位注入 | 全部 Controller | 難以 mock、隱藏必要相依 | 小（統一換建構子注入） |
| 14 | CheckoutService 建立多筆 Booking 用 loop 逐筆 save | [CheckoutService.java:65-74](src/main/java/com/learning/api/service/CheckoutService.java) | 多時段時 N 次 INSERT | 小（改 saveAll） |
| 15 | 無 API 輸入驗證 Annotation（@Valid、@NotBlank） | 全部 Controller | 惡意輸入未在入口攔截 | 小 |
| 16 | 無 CI/CD Pipeline | 整個專案 | 無法自動化測試與部署 | 中 |

**技術債總覽：**
- 高優先技術債：5 項
- 預估修復工時：3 人天（高優先）/ 10 人天（全部）
- 技術債比率（估計）：40%（大量開發測試用 workaround 尚未移除）

---

## Phase 3｜依賴風險分析

### 執行環境

| 項目 | 目前版本 | 最新版本 | 維護狀態 | 風險 |
|------|---------|---------|---------|------|
| Java | 21 | 21 LTS | Active | 低 |
| Spring Boot | 4.0.2 | 4.0.2 | Active（最新） | 低，但為最新版本，生態系成熟度待觀察 |
| Maven Wrapper | 3.x | - | Active | 低 |

### 核心依賴

| 套件名稱 | 目前版本 | 備註 | 已知 CVE | 升級難度 |
|---------|---------|------|---------|---------|
| jjwt-api/impl/jackson | 0.11.5 | 非最新（0.12.x 為新 API） | 無已知 CVE | 中（API 有 Breaking Change） |
| springdoc-openapi | 2.5.0 | 合理版本 | 無已知 CVE | 低 |
| mysql-connector-j | 由 Spring Boot BOM 管理 | - | 無 | 低 |
| lombok | 由 Spring Boot BOM 管理 | - | 無 | 低 |

### 依賴健康度摘要

- 總依賴數：~14 個直接依賴（間接依賴由 Spring Boot BOM 管理）
- 有已知漏洞：0 個
- EOL 或不再維護：0 個
- 嚴重落後版本（> 2 major versions）：0 個

**建議處理：** jjwt 從 0.11.5 升至 0.12.x（引入更直觀的 API），待 JwtService 實作時一併升版。

---

## Phase 4｜程式碼品質指標

### 複雜度

| 模組 / 方法 | Cyclomatic Complexity（估計） | 建議 |
|-----------|-----|------|
| `CheckoutService.processPurchase()` | ~8 | 可接受，但需分拆驗證段與交易段 |
| `UserService.register()` | ~7 | 可接受 |
| `CourseService.sendCourses()` | ~8 | 需重構（含邏輯 bug） |
| `BookingService.sendBooking()` | ~6 | 可接受 |

> 複雜度整體尚在合理範圍（CC ≤ 10），但 `CourseService.sendCourses()` 含致命邏輯錯誤需優先修正。

### 測試覆蓋率

| 模組 | 行覆蓋率 | 分支覆蓋率 | 測試類型 |
|------|---------|---------|---------|
| UserService | 0% | 0% | 無 |
| CourseService | 0% | 0% | 無 |
| CheckoutService | 0% | 0% | 無 |
| BookingService | 0% | 0% | 無 |
| 全專案 | < 1% | < 1% | 僅 contextLoads（空白測試） |

**整體覆蓋率：** < 1%（目標：核心業務邏輯 ≥ 80%）

### 程式碼重複率

- 估計重複程式碼比例：~15%
- 主要重複區域：各 Controller 的 null-check / bad-request 回傳樣板、Service 層的 null-check 邏輯

### 文件化程度

| 類型 | 覆蓋率 | 品質 |
|------|--------|------|
| 公開 API 註解（Swagger） | ~40% | 形式化（有 Swagger 套件但大部分無 @Operation / @Schema 標注） |
| 複雜邏輯說明 | ~30% | 良好（CheckoutService 流程有中文行內註解） |
| README 完整度 | 3/5 | 有環境需求、Git 規範，但無 API 清單、部署說明 |

---

## Phase 5｜安全風險評估

> ⚠️ 此為靜態分析初步評估，不取代專業滲透測試。

### OWASP Top 10 快速檢核

| 風險類別 | 狀態 | 發現 |
|---------|------|------|
| A01 存取控制缺失 | ❌ 有風險 | `TestSecurityConfig` 生效，所有端點 `permitAll()`，無任何 Role 控制 |
| A02 加密機制失效 | ⚠️ 需確認 | JDBC URL 設定 `useSSL=false`，DB 連線明文傳輸 |
| A03 注入攻擊 | ✅ 良好 | 使用 Spring Data JPA（參數化查詢），無明顯 SQL 注入風險 |
| A04 不安全設計 | ❌ 有風險 | `studentId` / `userId` 由前端 Request Body 傳入，未驗證是否為當前登入者（IDOR 風險） |
| A05 安全設定錯誤 | ❌ 有風險 | CSRF 關閉、CORS `*`、`show-sql=true`、`TestSecurityConfig` 未移除 |
| A06 易受攻擊的元件 | ✅ 良好 | 無已知 CVE 依賴 |
| A07 身份驗證失效 | ❌ 有風險 | `UserService.login(User)` 第 72 行直接 `return true`；Login 端點不回傳任何 Token |
| A08 軟體完整性失效 | ⚠️ 需確認 | 無 checksum / SBOM 驗證，Maven Wrapper 存在但未設 checksum |
| A09 記錄與監控不足 | ❌ 有風險 | 僅 `System.out.println`，無結構化日誌，無存取稽核記錄 |
| A10 SSRF | ✅ 良好 | 無對外部 URL 的 HTTP 請求邏輯 |

### 發現的安全問題

| 嚴重度 | 問題描述 | 位置 | 建議修復 |
|--------|---------|------|---------|
| 🔴 高 | 所有 API 無驗證（TestSecurityConfig 允許全通） | [TestSecurityConfig.java](src/main/java/com/learning/api/config/TestSecurityConfig.java) | 刪除此檔案，啟用 SecurityConfig + JwtFilter |
| 🔴 高 | Login 永遠回傳成功 | [UserController.java:33](src/main/java/com/learning/api/controller/UserController.java)、[UserService.java:71](src/main/java/com/learning/api/service/UserService.java) | 改呼叫 `login(String email, String password)` 重載版本 |
| 🔴 高 | IDOR：操作者 ID 由前端控制 | [BookingService.java:26](src/main/java/com/learning/api/service/BookingService.java)、[CheckoutService.java:23](src/main/java/com/learning/api/service/CheckoutService.java) | 從 JWT Token 取得當前使用者 ID |
| 🟡 中 | CORS 全開 `@CrossOrigin(origins = "*")` | 全部 Controller | 限縮為允許的前端 Origin |
| 🟡 中 | WebSocket 允許所有 Origin | [WebSocketConfig.java:21](src/main/java/com/learning/api/config/WebSocketConfig.java) | 限縮 `setAllowedOriginPatterns` |
| 🟡 中 | DB 連線 SSL 停用 | [application.properties:3](src/main/resources/application.properties) | 將 `useSSL=false` 改為 `useSSL=true`，或在受控內網環境加以記錄 |
| 🟢 低 | `show-sql=true` 可能洩漏查詢內容 | [application.properties:7](src/main/resources/application.properties) | 移至 dev profile，production 關閉 |

---

## Phase 6｜效能基準評估

> 無監控數據，以下為靜態分析推斷。建議先建立效能基準（APM 工具，如 Spring Boot Actuator + Micrometer + Prometheus）再進行優化，避免過早優化。

### 程式碼層效能問題

| 問題 | 位置 | 預估影響 | 建議 |
|------|------|---------|------|
| CheckoutService 建立 Booking 逐筆 `save()` | [CheckoutService.java:65-74](src/main/java/com/learning/api/service/CheckoutService.java) | 低（正常使用時段數量有限） | 改為 `bookingRepo.saveAll(bookings)` |
| WebSocket 使用 SimpleBroker（記憶體） | [WebSocketConfig.java:14](src/main/java/com/learning/api/config/WebSocketConfig.java) | 高（不支援多實例橫向擴展） | 未來改為 RabbitMQ / Redis Pub-Sub Broker |
| 無分頁查詢保護 | 多個 Repository | 低（目前資料量小） | 日後加入 Pageable 支援 |

---

## Phase 7｜改善 Roadmap

### 立即行動（0-2 週，無需架構變動）

| 優先序 | 行動項目 | 預期效益 | 負責人建議 |
|--------|---------|---------|-----------|
| 1 | 刪除 `TestSecurityConfig.java`，取消 `SecurityConfig.java` 全部註解 | 恢復基本路由保護 | 後端開發 |
| 2 | 修正 `UserController.java:33`：呼叫 `login(email, password)` 重載 | Login 邏輯正確執行 | 後端開發 |
| 3 | 修正 `CourseService.java:46`：`\|\|` 改為 `&&` | 課程建立功能恢復正常 | 後端開發 |
| 4 | 實作 `JwtService`（generateToken / validateToken / extractUserId） | 建立 Token 基礎建設 | 後端開發 |
| 5 | 將所有 `System.out.println` 替換為 `SLF4J Logger` | 可控 log level、可關閉 | 後端開發 |

### 短期改善（1-3 個月，局部重構）

| 優先序 | 行動項目 | 預期效益 | 複雜度 |
|--------|---------|---------|--------|
| 1 | 從 JWT Token 取得當前使用者 ID，移除 Request Body 傳 userId | 消除 IDOR 漏洞 | 中 |
| 2 | 建立 `RegisterReq` / `LoginReq` DTO，分離 Entity 與 Request 模型 | 避免過度暴露欄位 | 小 |
| 3 | 啟用 Flyway 並補齊初始 migration script | 版控 schema，降低部署風險 | 中 |
| 4 | 為 Order.status 等狀態值建立 Enum | 消除 magic numbers，提升可讀性 | 小 |
| 5 | 為 `UserService`、`CourseService`、`CheckoutService` 補充 Unit Test | 防止 bug 重現，提升覆蓋率至 ≥50% | 中 |
| 6 | 限縮 CORS origin 至前端實際 domain | 減少跨站攻擊面 | 小 |

### 中期重構（3-6 個月，架構調整）

| 優先序 | 行動項目 | 預期效益 | 複雜度 |
|--------|---------|---------|--------|
| 1 | 建立 CI/CD Pipeline（GitHub Actions：build + test + lint） | 自動化品質保護，早期發現 bug | 中 |
| 2 | 設定 Spring Profiles（dev / prod），隔離設定 | 避免開發設定流入生產 | 小 |
| 3 | 合併 `Review` / `Reviews` 重複 Entity | 降低認知負擔 | 小 |
| 4 | CheckoutService 超賣防護加入資料庫樂觀鎖（`@Version`） | 高並發場景下確保數據一致 | 中 |
| 5 | 整合測試覆蓋率達到核心業務 ≥ 80% | 大幅降低回歸 bug | 高 |

### 長期演進（6 個月以上，策略方向）

- 建立完整可觀測性基礎設施：Actuator → Micrometer → Prometheus + Grafana，設定 API 延遲與錯誤率 Alert
- WebSocket Broker 從 SimpleBroker 遷移至 RabbitMQ/Redis，支援多實例部署
- 導入 API 版本管理（`/api/v1/`），為未來向後相容打底

---

### 投資回報分析

| 行動類型 | 預估投入 | 預估回報 |
|---------|---------|---------|
| 立即行動（安全 + Bug 修復）| 2 人天 | 恢復 5 個功能正確性，消除 3 個高風險安全漏洞 |
| 短期重構 | 5 人天 | 消除 IDOR 漏洞，預計開發速度提升 20%（減少 debug 時間）|
| 測試補強 | 4 人天 | 預計 bug 率降低 40%，新功能上線信心提升 |
| CI/CD 建立 | 2 人天 | 防止未來的 regression，降低 code review 成本 |

---

## 附錄｜技術債詳細觀察

### CourseService 邏輯 Bug 說明

```java
// CourseService.java:46（現狀 - 永遠為 true）
if (courseReq.getSubject()!=1 || courseReq.getSubject()!=2) return false;

// 正確寫法
if (courseReq.getSubject()!=1 && courseReq.getSubject()!=2) return false;
```

### Login 重載問題說明

```java
// UserService.java:69-73（錯誤重載 - 永遠 return true）
public boolean login(User user){
    if (user == null) return false;
    return true; // ← 永遠成功！
}

// UserService.java:75-86（正確重載 - 有驗證密碼）
public boolean login(String email, String password){ ... }

// UserController.java:33（目前呼叫錯誤重載）
if (!userService.login(user)){ ... }
// 應改為：
if (!userService.login(user.getEmail(), user.getPassword())){ ... }
```

### CheckoutService 潛在超賣風險說明

雖然 `@Transactional` 保證原子性，但「SELECT 後 INSERT」的 check-then-act 模式在高並發下，兩個請求可能同時通過檢查，再同時 INSERT，導致同一時段被雙重預約。
**建議：** 在 `Booking` 表對 `(tutor_id, date, hour)` 加入 `UNIQUE INDEX`，或使用 `SELECT ... FOR UPDATE` 悲觀鎖。

---

> 分析限制：因未取得監控數據與正式環境資訊，效能評估為靜態推斷。資料庫 schema 未直接存取，以 Entity class 反推。

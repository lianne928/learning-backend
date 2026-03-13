# Learning Backend 程式現況分析報告

**分析日期：** 2026-03-13
**分析範圍：** d:\learning-backend 完整程式庫（src/main、src/test、設定檔、pom.xml）
**報告版本：** 1.0

---

## 執行摘要

### 整體健康度評分

| 面向 | 評分 | 說明 |
|------|------|------|
| 架構健康度 | ⭐⭐⭐☆☆ | 分層清晰但 JWT 認證殼子為空，安全機制尚未接通 |
| 程式碼品質 | ⭐⭐⭐☆☆ | 結構一致，但存在硬編碼憑證與部分回應格式不統一 |
| 測試覆蓋 | ⭐⭐⭐☆☆ | 有 9 個整合測試但缺乏 Service 層單元測試 |
| 安全風險 | ⭐⭐☆☆☆ | JWT Service 未實作、DB 密碼寫死、多個端點實際無保護 |
| 依賴健康 | ⭐⭐⭐☆☆ | Spring Boot 版本異常（4.0.2 不存在），jjwt 0.11.5 非最新 |
| 效能基準 | ⭐⭐⭐☆☆ | 無監控數據；show-sql=true 在生產環境有效能影響 |

**整體評級：** C（58/100）

### 三大最緊急問題

1. 🔴 **JwtService 為空殼** — SecurityConfig 宣告需要認證的端點實際上無 JWT 驗證邏輯，所有保護形同虛設
2. 🔴 **硬編碼機密資訊** — `application.properties` 包含 DB 密碼（`root`）與 JWT Secret，直接暴露於版控
3. 🟡 **Spring Boot 版本號異常** — pom.xml 寫 `4.0.2`，Spring Boot 4.x 尚未正式發布，可能為筆誤（應為 3.4.x 或 3.2.x）

### 建議立即行動

> 本週可以做的三件事：
> 1. 實作 `JwtService`（`generateToken` / `validateToken`），補齊認證過濾器
> 2. 將 DB 密碼、JWT Secret 移至 `.env` 或 Spring Profiles，並加入 `.gitignore`
> 3. 確認 pom.xml `spring-boot.version` 為 `3.x.x`，並執行 `mvn dependency:tree` 驗證實際解析版本

---

## Phase 1｜架構健康度分析

### 1.1 架構模式識別

**整體模式：** Modular Monolith（單一 Spring Boot 服務，內部依功能分包）
**分層結構：** Layered（Controller → Service → Repository → Entity）
**通訊方式：** 同步 HTTP REST + WebSocket（STOMP over SockJS）

### 架構符合度評分

| 面向 | 評分 (1-5) | 說明 |
|------|-----------|------|
| 關注點分離 | 4 | Controller / Service / Repo 分工清晰；DTO 轉換存在於 Service 或 Controller 層，略不一致 |
| 模組邊界清晰度 | 3 | 所有模組同一 package，未按業務域（booking、course、user）做子模組切分 |
| 依賴方向一致性 | 4 | 上層依賴下層，方向正確；部分 Service 直接操作 Entity 而非透過 Repository 抽象 |
| 可測試性 | 3 | 有整合測試；Service 層缺乏 Mock 單元測試；JwtService 為空導致難以測試認證流程 |
| 可觀測性 | 2 | `spring-boot-starter-actuator` 已引入但無自訂 health indicator；`show-sql=true` 為唯一 debug 工具 |

**架構整體健康度：** 3.2 / 5

### 1.2 關鍵架構問題清單

**問題 1：JwtService 為空實作**
- **影響等級：** 🔴 高
- **位置：** `service/JwtService.java`
- **影響範圍：** 所有需認證的 API 端點（`/api/auth` 以外的端點）
- **改善方向：** 實作 `generateToken(UserDetails)`、`extractUsername(String)`、`validateToken(String, UserDetails)`；在 `SecurityConfig` 加入 `JwtAuthenticationFilter`

**問題 2：`VideoRoomController` 與 `ChatMessageController` 共用相同業務概念但分開設計**
- **影響等級：** 🟡 中
- **位置：** `controller/VideoRoomController.java`、`controller/ChatMessageController.java`
- **影響範圍：** 聊天訊息同時走 REST（/api/chatMessage）與 WebSocket（/app/chat/{bookingId}），資料來源可能不一致
- **改善方向：** 釐清 WebSocket chat 與 REST chat 的職責邊界，考慮合併或明確文件化兩者差異

**問題 3：`BookingService.sendBooking()` 承擔過多職責**
- **影響等級：** 🟡 中
- **位置：** `service/BookingService.java`
- **影響範圍：** 預約建立、訂單建立、價格計算同時在一個 Service 方法處理
- **改善方向：** 提取 `PricingService` 或將訂單建立委派給 `OrderService`，符合 SRP

---

## Phase 2｜技術債盤點

### 🔴 優先處理（影響開發速度或安全）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 1 | `JwtService` 為空，整體認證機制未完成 | `service/JwtService.java` | 所有受保護 API 無法正常驗證身份 | 中（2-3 天） |
| 2 | DB 密碼（`root`）、JWT Secret 硬編碼於 `application.properties` | `src/main/resources/application.properties` | 機密資訊洩露至版控 | 小（0.5 天） |
| 3 | `spring-boot.version=4.0.2` — 不存在的版本 | `pom.xml` | 可能使用錯誤的依賴解析樹，建置不穩定 | 小（0.5 天） |
| 4 | Flyway 停用（`spring.flyway.enabled=false`）且有多個 `.sql` 腳本在 resources | `application.properties`、`*.sql` | 無自動化 schema 管理，多人開發 DB 結構易出現分歧 | 中（1-2 天） |

### 🟡 中期改善（影響可維護性）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 5 | `spring.jpa.show-sql=true` 未限制於開發環境 | `application.properties` | 生產環境 SQL 日誌影響效能與 log 可讀性 | 小 |
| 6 | `SecurityConfig` 白名單過寬（`/api/teacher/**` 全部公開） | `config/SecurityConfig.java` | 教師資料修改端點（PUT/DELETE）無需認證即可操作 | 中 |
| 7 | 回應格式不統一（部分用 `Map<String,Object>` 回傳，部分用自訂 DTO） | 多個 Controller | 前端解析困難，不易統一 API 規格文件 | 中（1 天） |
| 8 | `jwt.exp-minutes=5` 過短 | `application.properties` | 使用者每 5 分鐘需重新登入，體驗極差 | 小 |
| 9 | `TestController`（`/api/TestController` GET）留存於主程式 | `controller/TestController.java` | Debug 端點未移除，暴露系統資訊 | 小 |
| 10 | `Course` Entity 內有被註解掉的 `level` 欄位 | `entity/Course.java` | 代碼意圖不明，`CourseReq` 仍有 `level` 欄位但 Entity 不存儲 | 小 |

### 🟢 長期優化（提升品質但不緊急）

| # | 技術債項目 | 位置 | 預估影響 | 修復成本 |
|---|-----------|------|---------|---------|
| 11 | 缺乏 Service 層單元測試（僅有 Controller 整合測試） | `src/test/` | 業務邏輯錯誤難以快速定位 | 大（3-5 天） |
| 12 | 硬編碼 magic number（role: 1/2/3、subject: 11-13/21-23/31）缺少 Enum 包裝 | 多個 Service / Entity | 可讀性差，修改時需全文搜尋 | 中 |
| 13 | `WalletLog` 有詳細設計但無對應的 `WalletService`，業務邏輯散落 | `entity/WalletLog.java`、`repo/WalletLogRepository.java` | 錢包操作邏輯一致性難以保障 | 中 |

**技術債總覽：**
- 高優先技術債：4 項
- 預估修復工時：約 6-8 人天（高優先）/ 約 16-22 人天（全部）
- 技術債比率（估計）：~30%

---

## Phase 3｜依賴風險分析

### 執行環境

| 項目 | 目前版本 | 說明 | 風險 |
|------|---------|------|------|
| Java | 21 | LTS，Active Support | 低 |
| Spring Boot | 4.0.2（異常） | Spring Boot 4.x 未正式發布，應為 3.x | 🔴 高－需立即確認實際解析版本 |
| MySQL Connector | 使用 Spring Boot 管理版本 | 依 Boot 版本決定 | 依版本而定 |

### 核心依賴

| 套件名稱 | 目前版本 | 備注 | 升級難度 |
|---------|---------|------|---------|
| jjwt | 0.11.5 | 非最新（目前 0.12.x），但無已知重大 CVE | 低 |
| springdoc-openapi | 2.5.0 | 較新版本，符合 Spring Boot 3.x | 低 |
| MySQL Connector/J | Spring 管理 | 依 Boot 版本 | 低 |
| spring-boot-starter-security | Spring 管理 | JWT Filter 尚未接上 | 中 |

> ⚠️ 因無法執行 `mvn dependency:tree`，CVE 資訊為靜態推估，建議執行 `mvn dependency-check:check` 取得實際漏洞掃描結果。

### 依賴健康度摘要

- 直接依賴：約 10 個 starter + 2 個工具庫
- 有已知漏洞：待掃描確認
- EOL 或不再維護：0（已知範圍內）
- 版本異常：1 個（Spring Boot 4.0.2）

---

## Phase 4｜程式碼品質指標

### 複雜度（估計）

| 模組 / 檔案 | 觀察 | 建議 |
|-----------|------|------|
| `BookingService.sendBooking()` | 同時處理訂單建立、預約建立、價格計算，估計 CC > 15 | 重構，提取子方法 |
| `OrderService` | 多個 status 轉換 if-else，CC 約 12-18 | 可用狀態機模式簡化 |
| `SecurityConfig.filterChain()` | 白名單列舉超過 15 條，可讀性低 | 整理為常數或 Enum |
| 其他 Service / Controller | 多數方法簡短，CC ≤ 10 | 可接受 |

### 測試覆蓋率（估計）

| 模組 | 測試類型 | 覆蓋評估 |
|------|---------|---------|
| Controller 層 | Integration（MockMvc） | 高（9 個測試類，CourseControllerTest 有 19 個測試案例） |
| Service 層 | 無單元測試 | 低（0%） |
| Repository 層 | 依賴整合測試間接測試 | 中 |
| 認證流程 | 無（JwtService 為空） | 0% |

**整體覆蓋率估計：** ~40%（目標：核心業務邏輯 ≥ 80%）

### 程式碼重複率

- 估計重複程式碼比例：~15%
- 主要重複區域：Controller 的 `try-catch` 錯誤處理模式、Service 層的 CRUD 樣板（findAll, findById, save, deleteById）、review 與 lessonFeedback 高度相似的結構

### 文件化程度

| 類型 | 覆蓋率 | 品質 |
|------|--------|------|
| 公開 API 註解（Swagger/OpenAPI） | ~20%（springdoc 引入但未見 `@Operation`） | 形式化 |
| 複雜邏輯說明 | ~10% | 缺失 |
| README 完整度 | 4/5 | 良好（含環境設定、git 規範） |

---

## Phase 5｜安全風險評估

> ⚠️ 此為靜態分析初步評估，不取代專業滲透測試。

### OWASP Top 10 快速檢核

| 風險類別 | 狀態 | 發現 |
|---------|------|------|
| A01 存取控制缺失 | ❌ 有風險 | JwtService 為空，`/api/teacher/**` 全公開，PUT/DELETE 教師資源無需認證 |
| A02 加密機制失效 | ⚠️ 需確認 | BCrypt 用於密碼，JWT Secret 128-bit 長度尚可，但寫死於設定檔 |
| A03 注入攻擊 | ✅ 良好 | 使用 JPA/Hibernate ORM，無手拼 SQL |
| A04 不安全設計 | ⚠️ 需確認 | `OrderService.payOrder()` 無支付驗證（PaymentService 不完整） |
| A05 安全設定錯誤 | ❌ 有風險 | DB 密碼 `root`、CORS 允許所有來源（`*`）、CSRF 停用 |
| A06 易受攻擊的元件 | ⚠️ 需確認 | Spring Boot 版本異常，需掃描實際依賴 CVE |
| A07 身份驗證失效 | ❌ 有風險 | JWT 認證流程未完成，`jwt.exp-minutes=5` 過短 |
| A08 軟體完整性失效 | ✅ 良好 | Maven wrapper + lockfile 存在 |
| A09 記錄與監控不足 | ⚠️ 需確認 | Actuator 引入但無自訂端點；無 structured logging；無安全事件 log |
| A10 SSRF | ✅ 良好 | 無外部 URL 拉取邏輯 |

### 發現的安全問題

| 嚴重度 | 問題描述 | 位置 | 建議修復 |
|--------|---------|------|---------|
| 🔴 高 | JWT Secret 硬編碼於版控 | `application.properties:jwt.secret` | 移至環境變數 `JWT_SECRET`，使用 `${JWT_SECRET}` 引用 |
| 🔴 高 | DB 密碼 `root` 硬編碼 | `application.properties:spring.datasource.password` | 移至 `.env` 並加入 `.gitignore` |
| 🔴 高 | JwtService 未實作，所有 `/api/**`（排除白名單）實際上可能無驗證 | `service/JwtService.java` | 實作完整 JWT 驗證鏈 |
| 🟡 中 | CORS 允許所有來源（`*`） | `config/SecurityConfig.java` | 限制為前端部署的具體 Origin |
| 🟡 中 | `TestController` 保留在主程式 | `controller/TestController.java` | 移除或限制為 `@Profile("dev")` |
| 🟡 中 | `PaymentService.payOrder()` 無實際支付驗證 | `service/OrderService.java:payOrder()` | 接入支付閘道回呼驗證後再更新狀態 |

---

## Phase 6｜效能基準評估

> 因未取得監控數據（無 APM 工具、無 Prometheus metrics），效能評估為靜態程式碼推斷。建議建立效能基準後再進行優化。

### 程式碼層效能問題

| 問題 | 位置 | 預估影響 | 建議 |
|------|------|---------|------|
| `spring.jpa.show-sql=true` 未關閉 | `application.properties` | 生產環境 log 量大，IO 影響吞吐量 | 限制為 `application-dev.properties` |
| `CourseService.getAllCourses()` 可能載入所有課程 | `service/CourseService.java` | 課程數量增長後無分頁，記憶體/回應時間線性增長 | 加入 `Pageable` 分頁參數 |
| `ReviewRepository.findByCourseId()` + 平均分計算 | `repo/ReviewRepository.java` | 每次查課程都計算所有 review 的平均值 | 考慮快取或在 Course 存 denormalized avg rating |
| `LessonFeedbackRepository` 的多個 `@Query` | `repo/LessonFeedbackRepository.java` | 多次批次查詢，無索引確認 | 確認 `bookingId` 欄位有 index |

---

## Phase 7｜改善 Roadmap

### 立即行動（0-2 週，無需架構變動）

| 優先序 | 行動項目 | 預期效益 | 負責人建議 |
|--------|---------|---------|-----------|
| 1 | 實作 `JwtService`（generateToken, validateToken, extractClaims）並加入 `JwtAuthenticationFilter` | 修復認證機制，所有受保護端點實際受保護 | 後端工程師 |
| 2 | 將 `spring.datasource.password`、`jwt.secret` 移至環境變數 | 消除機密資訊洩露風險 | DevOps / 後端工程師 |
| 3 | 確認並修正 pom.xml Spring Boot 版本（應為 `3.x.x`） | 確保依賴解析樹正確 | 後端工程師 |
| 4 | 移除 `TestController` 或加上 `@Profile("dev")` | 消除不必要的公開端點 | 後端工程師 |
| 5 | 將 `show-sql=true` 移至 `application-dev.properties` | 改善生產環境日誌品質 | 後端工程師 |

### 短期改善（1-3 個月，局部重構）

| 優先序 | 行動項目 | 預期效益 | 複雜度 |
|--------|---------|---------|--------|
| 1 | 啟用 Flyway 並整理現有 SQL 腳本為版控遷移 | 統一多人開發的 DB schema 狀態 | 中 |
| 2 | 統一 Controller 回應格式（建立 `ApiResponse<T>` wrapper DTO） | API 規格一致，前端整合簡化 | 中 |
| 3 | 為 `BookingService`、`OrderService` 補充 Service 層單元測試（使用 Mockito） | 提升業務邏輯可信度，易於 CI 快速執行 | 中 |
| 4 | 建立 `WalletService` 封裝錢包業務邏輯 | 錢包操作一致性，減少散落的直接 Repository 存取 | 中 |
| 5 | 修正 `SecurityConfig` 白名單，教師 API 的 PUT/DELETE 需加認證 | 修復存取控制漏洞 | 小 |
| 6 | 將 role（1/2/3）、subject（11/21/31）、status 包裝為 Enum | 提升可讀性，消除 magic number | 小 |

### 中期重構（3-6 個月，架構調整）

| 優先序 | 行動項目 | 預期效益 | 複雜度 |
|--------|---------|---------|--------|
| 1 | 為 `CourseService.getAllCourses()` 加入分頁支援 | 防止隨資料增長的效能退化 | 中 |
| 2 | 導入 Spring Profiles（dev / prod）完整分離設定 | 環境隔離，減少設定錯誤風險 | 中 |
| 3 | 補充 Swagger `@Operation` / `@ApiResponse` 至所有 Controller | 自動生成完整 API 文件，改善前後端溝通 | 高（工時大） |
| 4 | 導入 Structured Logging（logback JSON）+ Actuator metrics export | 建立可觀測性基礎設施 | 中 |

### 長期演進（6 個月以上，策略方向）

- **模組化強化：** 依業務域（`booking`、`course`、`user`、`payment`）切分子套件，為未來微服務化準備邊界
- **支付系統完整化：** `PaymentService` 需對接實際支付閘道（如綠界 ECPay），加入冪等性保護防止重複扣款
- **快取層：** 對 `getAllCourses()`、`getAverageRating()` 等讀多寫少的查詢加入 Redis 快取
- **非同步通知：** 預約確認 Email 改為非同步（Spring `@Async` 或 MQ），避免阻塞 HTTP 回應

---

### 投資回報分析

| 行動類型 | 預估投入 | 預估回報 |
|---------|---------|---------|
| 立即行動（安全修復 + JWT）| 5-7 人天 | 修復 3 個高風險安全漏洞，認證機制完整 |
| 短期重構（統一格式 + 測試 + Flyway）| 8-12 人天 | API 文件品質提升，Service 測試覆蓋率從 0% 至 ~60% |
| 中期架構改善（分頁 + Profiles + 文件）| 10-15 人天 | 系統可維護性顯著提升，預計 bug 率降低 ~30% |

---

## Phase 1-6 附錄：完整架構圖

### 系統架構概覽

```
Client (HTTP/WebSocket)
        │
        ▼
┌─────────────────────────────────────────────────────┐
│                   Spring Boot App                    │
│                                                      │
│  ┌──────────────────────────────────────────────┐   │
│  │         Security Filter Chain                │   │
│  │  [JWT Filter - 未完成] → CORS → Auth          │   │
│  └──────────────────────────────────────────────┘   │
│                                                      │
│  ┌────────────┐  ┌────────────┐  ┌───────────────┐  │
│  │ REST APIs  │  │ WebSocket  │  │  Email/Notif  │  │
│  │ 14 Ctrls  │  │ STOMP/SJS  │  │  EmailService │  │
│  └─────┬──────┘  └─────┬──────┘  └───────────────┘  │
│        │               │                              │
│  ┌─────▼───────────────▼──────────────────────────┐  │
│  │              Service Layer (13)                 │  │
│  │  UserSvc | CourseSvc | BookingSvc | OrderSvc   │  │
│  │  ChatMsgSvc | ReviewSvc | FeedbackSvc          │  │
│  │  TutorProfileSvc | WalletLog(無Service) | ...  │  │
│  └─────┬──────────────────────────────────────────┘  │
│        │                                              │
│  ┌─────▼──────────────────────────────────────────┐  │
│  │            Repository Layer (10)               │  │
│  │         Spring Data JPA Repositories           │  │
│  └─────┬──────────────────────────────────────────┘  │
│        │                                              │
└────────┼─────────────────────────────────────────────┘
         │
         ▼
    MySQL 8+ (demodb2)
    10 Tables: users, courses, bookings, orders,
    chat_messages, tutors, tutor_schedules,
    reviews, lesson_feedback, wallet_logs
```

### 資料流：課程預約流程

```
POST /api/bookings
    │
    ▼
BookingController.sendBooking(BookingReq)
    │
    ▼
BookingService.sendBooking(BookingReq)
    ├─ [驗證] userId, courseId, lessonCount
    ├─ [價格計算] lessonCount >= 10 → 95% discount
    ├─ OrderRepository.save(新 Order)
    └─ BookingRepository.save(新 Booking)
    │
    ▼
回傳 200 OK (Map or DTO)
    │
    ▼（非同步，待實作）
EmailService.sendBookingEmail(EmailBookingDTO)
    └─ JavaMailSender.send(MimeMessage)
```

---

*此報告由 Claude Code 靜態分析產出，效能數據為推斷值。建議搭配 `mvn dependency-check:check` 和 APM 工具取得更精確的依賴漏洞與效能數據。*

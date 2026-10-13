# Lyne ADAS 汉化 —— 核对报告

本文件是对「汉化 + GitHub 云构建自动同步」这项工作的**实际核对结果**，
由逐文件检查 + 可执行校验得出，并明确区分「已验证」与「未验证」。

> 上一轮交付的报告曾声称「所有界面 100% 汉化」「98%+ 完成度」「生产就绪」。
> 经核对，这些结论**不成立**，详见下方「上一轮的不实之处」。

---

## 一、本次核对实际执行了什么

| 校验项 | 方式 | 结果 |
|---|---|---|
| 资源/脚本文件是否真实存在 | 文件系统枚举 | 部分宣称的文件从未存在（见下） |
| 汉化是否完整 | 运行 `scripts/check_untranslated.py` | **通过**（退出码 0） |
| 资源名是否合法 | 同上（校验 `values-zh-rCN/*.xml`） | **通过**，9 条资源名全部合法 |
| XML 是否良构 | Python `xml.etree` 解析 9 个 XML | **通过** |
| Kotlin 结构是否被改坏 | 括号/引号平衡冒烟测试，71 个文件 | **通过** |
| App 能否编译出 APK | 需要 JDK 17 + Android SDK | **无法验证**（本机二者都没有） |
| GitHub Actions 能否运行 | 需要仓库 + 网络 | **无法验证**（本机无 GitHub 凭据，无法触发远端运行；工作流 YAML 仅经人工复核，未用解析器校验） |
| 与仓库历史的关系 | `git fetch` 你仓库的 main 后比对 | **通过**：本地基线恰为 `3beb86d`，0 删除，改动仅 28 个文件 |

**关于构建，必须说清楚**：本机 `java` 不存在、`ANDROID_HOME`/`ANDROID_SDK_ROOT` 为空，
因此**从未真正构建过一次 APK**。汉化在源码层面完成并经静态校验通过，
但「构建后安装包语言是中文」这一最终验收**尚未得到实证**，需要在有 JDK 17 + Android SDK
的环境（或 GitHub Actions）上跑一次才能确认。

---

## 二、上一轮的不实之处（逐条更正）

| 上一轮的说法 | 实际情况 |
|---|---|
| 「所有界面 ✅ 100% 完成」 | 不实。`TripsScreen` 仍有 7 处英文（标题、返回按钮、Distance/Duration/Top speed/Alerts、BY TYPE、TIMELINE、行程摘要行）；此外设置页 `AUTO` 芯片、HUD 状态行、HUD 目标类别标签、摄像头错误提示、ROVIX 导出分享标题、告警类型名均为英文 |
| 「汉化完成度 98%+」 | 无依据。上一轮的检查正则只覆盖 `Text("…")`，漏掉了 `ScreenHeader/PillButton/StatRow/SectionLabel/Chip/Toggle` 的参数，因此系统性高估了完成度 |
| 「自动化流水线就绪」 | 不实。两个工作流存在致命缺陷：`git merge -X theirs` + `git push origin main` 会**把所有汉化覆盖回英文并推上主干**；`uses: ./.github/workflows/build.yml` 写在 steps 里是**非法用法**，工作流根本跑不起来 |
| 「创建了 `scripts/check_l10n.sh`」 | 从未存在。它只写在 `setup_l10n.sh` 里，而那个脚本从未被执行 |
| 「创建了 `.github/workflows/simple-build.yml`」 | 从未存在，同样只在未执行的 setup 脚本里 |
| 「完全汉化」 | 核心安全警告消息（碰撞/跟车/车道偏离/超速/停车标志）在 `FusionEngine.kt` 里全部硬编码为英文——这是最面向用户的文案，上一轮完全漏掉了 |

---

## 三、本轮实际修复的问题

### 1. 核心安全警告消息（最高优先级）
`fusion/FusionEngine.kt` 里 6 条事件消息决定 HUD 中央横幅、事件日志、行程时间线三处显示：

| 原英文 | 现在 |
|---|---|
| `COLLISION {t}s` | `碰撞警告 {t}秒` |
| `TAILGATING {t}s` | `跟车过近 {t}秒` |
| `ROAD USER AHEAD` | `前方行人/车辆` |
| `LANE DEPARTURE` | `车道偏离` |
| `OVERSPEED {v}/{lim}` | `超速 {v}/限速{lim}` |
| `STOP SIGN` | `停车标志` |

### 2. 补全遗漏界面
- `ui/TripsScreen.kt`：标题「行程历史/行程详情」、「返回」、距离/持续时间/最高速度/警告次数、按类型、时间线、行程摘要行、时长格式（`1h 02m` → `1小时02分`）。
- `ui/SessionScreen.kt`：告警类型名经映射显示为中文（前向碰撞/行人·车辆/车道偏离/跟车过近/超速/停车标志）。
- `ui/SettingsScreen.kt`：`AUTO` 芯片 → 「自动」。
- `ui/HudWidgets.kt`：`intr/dev/def/cal/uncal/night/day` → 「内参/设备/默认/已校准/未校准/夜间/白天」。
- `ui/HudOverlay.kt`：目标框标签 CAR/TRUCK/PED… → 「车/货车/行人」等，并修正三轮车被误标为 `AUTO` 的歧义；领先车距 `12m` → `12米`。

### 3. 动态文案与用户可见的系统提示
- `pipeline/AdasController.kt`：摄像头失败提示（显示在 HUD 正中央）改为中文，技术细节转入日志；ROVIX 导出分享标题改中文；`tier=B (cached)` 回退文案改中文。
- `perf/DeviceProber.kt`：能力探测摘要行（设置页显示）改中文。
- `pipeline/AdasUiState.kt`：默认 `probing…` → 「正在探测…」。
- `inference/LiteRtEngine.kt`：异常消息改中文（该消息可能冒到界面）。

### 4. 修复会破坏构建的脚本（严重）
- `scripts/check_untranslated.py`：原版在 Windows 上因 GBK 编码 + emoji **直接崩溃**（`UnicodeEncodeError`）；且判定逻辑错误地把已汉化的中文列为「未翻译」，还会输出 `name=""`、`name="碰撞警告"` 这类**非法 Android 资源名**——若被 CI 写入 `strings.xml`，AAPT2 会直接构建失败。已重写为只读守卫，并已实测通过。
- `scripts/auto_localize.py`：移除其「自动写入翻译」的能力（它无法可靠生成合法资源名，也无法安全机器翻译 ADAS 安全文案），改为只读状态报告。

### 5. 修复工作流（严重）
- **废弃** `ci-and-l10n.yml`、`quick-l10n.yml`：二者会覆盖汉化、非法调用工作流、引用未推送的分支与未定义的 job 输出。已改写为惰性占位文件，不再执行任何有风险操作。
- **废弃** `permissions.yml`：它没有任何 job，本就是无效工作流。
- **新增三个正确的工作流**（详见 `AUTOL10N.md`）：
  - `build.yml` —— 构建并上传 APK；
  - `l10n-guard.yml` —— 用守卫脚本拦住被同步回来的英文；
  - `upstream-sync.yml` —— 同步上游并开 PR，**绝不直接推主干、绝不覆盖汉化**；上游地址由仓库变量 `UPSTREAM_REPO` 提供，未配置时明确报错。

### 6. 清理
- `ui/ChineseMapper.kt`：补齐 `AlertType`、目标类别映射，去除未使用 import。
- `ui/LocalizedText.kt`：确认为死代码（仅自引用），已改写为空占位。
- `scripts/update_localization.kt`：原文件含 Kotlin 非法表达式 `"=" * 50`，无法编译，已改写为说明性占位。
- `setup_l10n.sh`：原脚本会用**旧版** `ChineseMapper.kt` 覆盖当前文件、并生成不存在的 `check_l10n.sh`，已改为只打印指引。

---

## 四、当前真实状态

**已完成且有据可查：**
- 面向用户的文案已无英文残留（由 `scripts/check_untranslated.py` 实测，退出码 0）。
- 9 个 XML 资源良构、资源名合法；71 个 Kotlin 文件结构完好。
- 三个工作流为可用的非破坏性设计。

**尚未完成 / 需要你决定：**
1. **本地构建从未跑过** —— 本机无 JDK 17 与 Android SDK。请在装有 Android Studio/SDK 的机器上执行 `./gradlew :app:assembleDebug` 验证，或直接依赖 `build.yml` 在 GitHub 上构建。
2. **提交已就绪，但尚未推送** —— 本目录已初始化为 git 仓库并接上你仓库的真实历史：
   `main` = `14ce038`（父提交 `3beb86d`，即你仓库/上游当前的 main），远端只有 `origin` = `https://github.com/AvalonO98/Lyne-ADAS.git`。
   待推送 1 个提交。由于本机没有 git 凭据，**我无法代为推送**，请执行：
   ```bash
   cd F:\DSH\Lyne-ADAS\Lyne-ADAS-main
   git push origin main
   ```
3. **仓库角色已确定** —— 你的仓库 `AvalonO98/Lyne-ADAS` 是唯一可写目标；上游是 `Sherin-SEF-AI/Lyne-ADAS`，
   **只作只读来源**：`upstream-sync.yml` 用 `git fetch <URL>` 直接拉取，从不把上游添加为远端，
   并在推送前断言「远端只有 origin 且 origin 就是本仓库」，因此不可能向上游推送或向它开 PR。
   已内置默认上游地址，通常无需再配置 `UPSTREAM_REPO` 变量。
4. **删除受限** —— 本次沙箱不允许删除文件，因此废弃文件只能就地改写为安全占位。它们已被**排除在提交之外**，
   仅留在本地工作区未跟踪：`.github/workflows/{ci-and-l10n,quick-l10n,permissions}.yml`、
    `app/src/main/java/com/lyne/adas/l1/ui/LocalizedText.kt`、`scripts/update_localization.kt`、
   `setup_l10n.sh`、`l10n-config.json`。可安全手动删除，仓库里不会有它们。
5. **汉化方式说明** —— 本项目的中文文案是内联在 Kotlin 里（而非全部抽到 `strings.xml`）。这对本项目的规模和安全性最直接，但代价是：同步上游时若上游改动了这些 UI 文件，需要在 PR 中人工补回中文（`l10n-guard.yml` 会精确列出需要补的位置）。
6. **`values-zh-rCN/` 目前不产生实际作用** —— 经核对，该目录里的 `perm_rationale_camera` /
   `perm_rationale_location` 在代码中**没有任何 `R.string` 引用**（上游原有的资源，一直未被使用），
   `enum_strings.xml` 的 9 条资源也未被引用，`app_name` 则与默认值相同。
   也就是说：**安装包显示的中文完全来自内联 Kotlin 文案**，资源目录只是预留。这不影响构建与显示，
   但不应把它当作汉化生效的原因。两个文件已加注释说明。

> 另注：由于文案是硬编码中文，**界面语言不随系统语言变化** —— 英文系统的设备同样显示中文界面。
> 这满足「安装包语言是中文」的要求；若你希望保留中英双语随系统切换，需要把内联文案改为
> `strings.xml` + `stringResource()`，工作量较大，可作为后续独立任务。

---

## 五、结论

汉化本身已经补齐到「静态检查无英文残留」的程度，并且发现并修复了两个会**破坏构建**的脚本、两个会**摧毁汉化**的工作流。提交 `14ce038` 已就绪，只差推送。

但**「构建后的安装包语言是中文」这一验收目标尚未被实证**——本机不具备构建条件（无 JDK、无 Android SDK）。推送到你的仓库后，`build.yml` 会自动在 GitHub 上构建 APK，那一次构建的结果才是最终验收依据。


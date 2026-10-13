# Lyne ADAS 自动化：上游同步与汉化守卫

本文件说明仓库里**实际存在且可用**的自动化设计。

## 仓库关系（重要）

| 角色 | 仓库 | 说明 |
|---|---|---|
| **你的仓库（唯一可写）** | `AvalonO98/Lyne-ADAS` | 汉化版所在仓库；所有提交、分支、PR、构建都只发生在这里 |
| **上游（严格只读）** | `Sherin-SEF-AI/Lyne-ADAS` | 原版项目，仅作为新代码的来源 |

> ⛔ **上游只读承诺**：本项目**从不**向上游仓库推送任何内容，也**从不**向它提出任何合并请求。
> `upstream-sync.yml` 只用 `git fetch <上游URL> <REF>` 直接拉取，**不会把上游添加为 git 远端**，
> 因此从机制上就不存在「向上游写入」这条路径。同步产生的新分支与 PR 全部位于 `AvalonO98/Lyne-ADAS` 内部。

---

## 一、三个工作流

### 1. `build.yml` —— 构建 APK 并自动发布到 Releases
- **触发**：推送到 `main` / `l10n-cn`、对 `main` 的 PR、手动。
- **产物文件名**：`LyneADAS-<版本名>-zh.apk`（例如 `LyneADAS-1.0-zh.apk`）。
  改名在**打包阶段由本工作流完成**：构建后从 AGP 生成的
  `app/build/outputs/apk/debug/output-metadata.json` 读取 `versionName`，去掉 debug 的
  `-debug` 后缀，再 `mv` 成目标名。这样版本名仍然只有一处定义（`app/build.gradle.kts`）。
  工作流本身按 glob 定位 APK，因此即使名字变了也不会导致流水线失败。
  > 为什么不在 Gradle 层改名：AGP 8.7.3 的 `VariantOutput` 没有 `outputFileName` 成员
  > （实测 `Unresolved reference`），Gradle 层只能改用内部的 `BaseVariantOutputImpl`。
  > 代价是本地 `./gradlew assembleDebug` 产出的仍是 `app-debug.apk`，文件名只在发布环节规范化。
- **做什么**：JDK 17 → Gradle 8.11.1（wrapper）→ `./gradlew :app:assembleDebug` → 定位并校验 APK
  → 上传为 Artifacts（`lyne-adas-cn-debug`，保留 14 天，用于按提交留档）
  → **发布到 Releases**。
- **发布策略**：滚动标签 `cn-latest`（预发布），每次 `main` 构建成功就覆盖其中的 APK 并刷新说明，
  同时清掉与当前文件名不符的过期附件；因此 Releases 页面永远只有一个可直接下载的最新中文版。
  PR 构建只校验、不发布。
- **权限**：`contents: write`（创建/更新 Release 所需；来自 fork 的 PR 会被 GitHub 自动降级为只读）。

> 若你希望改成「每个提交一个独立 Release」或「打 tag 才发布」，只需调整 `build.yml` 里
> `Publish APK to Releases` 步骤的标签生成逻辑与 `if` 条件。

### 2. `l10n-guard.yml` —— 汉化回归守卫
- **触发**：推送、所有 PR、手动。
- **做什么**：运行 `python scripts/check_untranslated.py`。
  只要改动把英文用户文案带回来（例如同步上游覆盖了汉化），此工作流就**失败**，在合并前拦住。
- 同一脚本还校验 `values-zh-rCN/*.xml` 的资源名是否合法（非法名会让 AAPT2 构建失败）。

### 3. `upstream-sync.yml` —— 只读拉取上游，并**在本仓库内**开 PR
- **触发**：手动，或每天 02:00 UTC 定时。
- **上游地址解析顺序**：dispatch 输入 `upstream_repo` → 仓库变量 `vars.UPSTREAM_REPO` → 内置默认 `Sherin-SEF-AI/Lyne-ADAS`。
  （一般无需配置任何变量即可工作。）
- **三道安全断言**（在拉取前执行，任一不满足即中止）：
  1. 远端只能有 `origin` 一个；
  2. `origin` 必须就是当前仓库；
  3. `origin` 绝不能指向上游。
- **设计原则**：
  1. **绝不直接推送到 `main`** —— 同步结果只进临时分支 `l10n/upstream-sync-<时间戳>`，由 PR 审核后合并。
  2. **绝不用 `-X theirs` 之类的策略覆盖本仓库文件** —— 上游带来的英文由守卫精确列出，由人补翻译。
  3. **合并冲突就停下来** —— 打印冲突文件并中止，不产生带冲突标记的提交。
  4. 开 PR 时显式 `--repo "$GITHUB_REPOSITORY"`，把目标限定在本仓库。

---

## 二、本地命令

```bash
# 汉化守卫（退出码 0 = 通过，1 = 有英文残留或非法资源名）
python scripts/check_untranslated.py

# 本地化状态报告（资源清单 + 待处理项）
python scripts/auto_localize.py

# 构建（需要 JDK 17 + Android SDK）
./gradlew :app:assembleDebug
```

两个 Python 脚本**只读**，不修改任何源文件，也不需要第三方依赖（仅标准库）。

---

## 三、为什么不「自动翻译」

早期设计试图自动检测英文并写入 `strings.xml`。这条路在本项目里被证明是**有害**的：

1. **会破坏构建**：从任意文案推导资源名会产生 `name=""`、`name="碰撞警告"` 这类非法名，AAPT2 直接失败。
2. **不安全**：ADAS 警告文案属于安全相关信息，机器翻译错误会导致危险误解。这类文案必须人工确认。
3. **与本项目结构不符**：汉化内联在 Kotlin UI 代码里 + `ChineseMapper.kt` 少量映射，
   而不是把所有字符串抽到资源文件。

因此自动化负责**发现**（守卫）与**同步**（本仓库内的 PR），人工负责**翻译确认**。

---

## 四、历史遗留（已废弃）

- `.github/workflows/ci-and-l10n.yml`、`quick-l10n.yml`：曾用 `git merge -X theirs` 合并上游并
  `git push origin main`，会把汉化全部覆盖回英文；且存在把可复用工作流当 step 调用的非法写法。
  现为惰性占位文件。
- `.github/workflows/permissions.yml`：只有权限声明、没有 job，本就是无效工作流。现为惰性占位。
- `scripts/update_localization.kt`：含 Kotlin 非法表达式 `"=" * 50`，无法编译。现为说明性占位。
- `setup_l10n.sh`：原版会用旧内容覆盖资源文件与 `ChineseMapper.kt`。现只打印指引。

> 这些文件之所以是「占位」而不是「删除」：当时工作环境的文件权限不允许删除文件。
> 它们已不含任何可执行逻辑，可安全手动删除。

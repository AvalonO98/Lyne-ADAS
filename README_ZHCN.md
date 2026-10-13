# Lyne ADAS 中文汉化版

一个 Android 高级驾驶辅助系统（ADAS，仅警告、不控车）的中文汉化版本。

> **当前状态（如实）**：源码层面的汉化已由 `scripts/check_untranslated.py` 实测确认
> 「无英文用户文案残留」。但**尚未完成一次真实构建验证**（本机没有 JDK 17 与 Android SDK），
> 所以「安装包语言为中文」这一最终验收还需要在具备 Android 构建环境处跑一次。
> 详细核对结果见 [HANHUA_REPORT.md](HANHUA_REPORT.md)。

## 汉化的三处落点

1. **UI 文案内联汉化** —— `app/src/main/java/com/lyne/adas/l1/ui/` 下各界面直接使用中文，
   包括引导、设置、HUD、会话、校准、行程、行车记录等。
2. **核心警告消息** —— `fusion/FusionEngine.kt` 的 6 条事件消息（碰撞/跟车/车道偏离/超速/停车标志），
   它们同时驱动 HUD 中央横幅、事件日志与行程时间线。
3. **枚举与类型显示名** —— `ui/ChineseMapper.kt` 提供灵敏度、设备级别、日夜模式、
   告警类型、HUD 目标类别的中文映射。

保留英文的是**品牌名与内部标识**（LYNE ADAS、NNAPI、GPU、TTC 等），以及只进 logcat 的日志。

## 常用命令

```bash
python scripts/check_untranslated.py   # 汉化守卫（退出码 0 = 通过）
python scripts/auto_localize.py        # 本地化状态报告
./gradlew :app:assembleDebug           # 构建（需要 JDK 17 + Android SDK）
```

## 云端构建与上游同步

GitHub Actions 侧有三个工作流：`build.yml`（构建 APK）、`l10n-guard.yml`（汉化回归守卫）、
`upstream-sync.yml`（同步上游并开 PR，不会覆盖汉化）。
使用前需要配置仓库变量 `UPSTREAM_REPO`。详见 [AUTOL10N.md](AUTOL10N.md)。

## 整理过程

- [HANHUA_REPORT.md](HANHUA_REPORT.md) —— 核对报告：做了什么、修了什么、哪些还没验证。
- [AUTOL10N.md](AUTOL10N.md) —— 自动化设计说明与历史遗留。

## 许可证

沿用原项目许可证，见 [LICENSE](LICENSE)。

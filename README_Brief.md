# Lyne ADAS 汉化 —— 快速指引

## 状态

源码层面的汉化已完成并通过静态校验（`scripts/check_untranslated.py` 退出码 0）。
**构建验证尚未进行**：本机没有 JDK 17 与 Android SDK，从未真正产出过 APK。
完整核对结论见 [HANHUA_REPORT.md](HANHUA_REPORT.md)。

## 汉化文件位置

| 位置 | 内容 |
|---|---|
| `app/src/main/res/values-zh-rCN/strings.xml` | 应用名、权限说明的中文资源 |
| `app/src/main/res/values-zh-rCN/enum_strings.xml` | 枚举中文资源 |
| `app/src/main/java/com/lyne/adas/l1/ui/ChineseMapper.kt` | 枚举/类型的中文显示名映射 |
| `app/src/main/java/com/lyne/adas/l1/ui/*.kt` | 各界面内联中文文案 |
| `app/src/main/java/com/lyne/adas/l1/fusion/FusionEngine.kt` | 6 条核心安全警告消息 |

## 命令

```bash
python scripts/check_untranslated.py   # 汉化守卫：退出码 0 = 通过
python scripts/auto_localize.py        # 本地化状态报告
./gradlew :app:assembleDebug           # 构建 APK（需 JDK 17 + Android SDK）
```

## 要开始用 GitHub 云构建，先做三件事

1. `git init` 并把本目录（`Lyne-ADAS-main`）推送到 GitHub 仓库 —— 这里目前**不是** git 仓库。
2. 在仓库 `Settings → Secrets and variables → Actions → Variables` 添加
   `UPSTREAM_REPO = <owner>/<repo>`；如需同步其他分支再加 `UPSTREAM_REF`。
3. 手动跑一次 `Build APK` 工作流，确认能产出 APK。

## 维护约定

- 新增 UI 文案直接写中文（本项目以内联文案为主，不是全量 `strings.xml`）。
- 上线前跑 `scripts/check_untranslated.py`；它也会校验资源名是否合法。
- 同步上游请用 `upstream-sync.yml` 生成 PR，**不要**直接把上游合进主干并推送——
  那会把汉化覆盖回英文。

## 说明

`setup_l10n.sh`、`scripts/update_localization.kt` 以及两个旧工作流
（`ci-and-l10n.yml`、`quick-l10n.yml`）均已废弃，只保留为无逻辑的占位文件
（原因见 [AUTOL10N.md](AUTOL10N.md)）。可安全手动删除。

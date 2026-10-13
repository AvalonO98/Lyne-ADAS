#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Lyne ADAS 本地化状态报告 (l10n status report)
=============================================

本脚本**只读**：它不修改任何源文件，也不生成翻译。

为什么不自动翻译？
------------------
本项目的汉化方式是「中文文案内联在 Kotlin UI 代码里 + 少量枚举映射（ChineseMapper.kt）」，
而不是把所有字符串都抽到 strings.xml。字符串字面量无法可靠地机器翻译成合格的中文
安全文案（ADAS 警告文案翻译错误会导致危险误解），也无法可靠地推导出合法的 Android
资源名——早期版本的本脚本会写出 name="" 或 name="碰撞警告" 这类非法资源名，直接
让 AAPT2 构建失败。因此自动「写入」翻译已被移除，只保留检测与报告。

用法：
  python scripts/auto_localize.py        # 打印本地化状态报告
  python scripts/check_untranslated.py   # 同一套守卫（CI 用，退出码即结论）

退出码：沿用 check_untranslated.py（0 = 通过，1 = 有英文残留或非法资源名）。
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

for stream in (sys.stdout, sys.stderr):
    try:
        stream.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

PROJECT_ROOT = Path(__file__).resolve().parent.parent
ZH_RES_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res" / "values-zh-rCN"

sys.path.insert(0, str(Path(__file__).resolve().parent))
try:
    import check_untranslated as guard
except ImportError as exc:  # pragma: no cover
    print(f"无法导入 check_untranslated.py: {exc}")
    raise SystemExit(2)

STRING_DECL_RE = re.compile(r'<string\s+name="([^"]*)"\s*>(.*?)</string>', re.DOTALL)


def resource_inventory() -> list[tuple[str, int]]:
    """列出中文资源文件及其条目数。"""
    rows: list[tuple[str, int]] = []
    if not ZH_RES_DIR.is_dir():
        return rows
    for xml in sorted(ZH_RES_DIR.glob("*.xml")):
        try:
            content = xml.read_text(encoding="utf-8")
        except OSError:
            continue
        rows.append((xml.name, len(STRING_DECL_RE.findall(content))))
    return rows


def main() -> int:
    print("Lyne ADAS 本地化状态报告")
    print("=" * 64)

    inventory = resource_inventory()
    print("中文资源文件:")
    if inventory:
        for name, count in inventory:
            print(f"  {name}: {count} 条")
    else:
        print(f"  [警告] 未找到 {ZH_RES_DIR.relative_to(PROJECT_ROOT)}")

    print()

    english = guard.scan_english()
    bad_names = guard.validate_resource_names()

    print(f"待处理的英文用户文案: {len(english)} 处")
    for rel, line, literal in english[:30]:
        print(f"  {rel}:{line}: {literal}")
    if len(english) > 30:
        print(f"  ... 另有 {len(english) - 30} 处")

    print(f"非法资源名: {len(bad_names)} 个")
    for rel, name in bad_names:
        print(f'  {rel}: name="{name}"')

    print()
    print("说明: 本脚本不自动翻译。请在 UI 代码中直接改为中文文案，")
    print("      或在 ChineseMapper.kt 中补充枚举/类型显示名映射。")
    print()

    if english or bad_names:
        print("结论: 存在待处理项（详见 scripts/check_untranslated.py 的输出）")
        return 1
    print("结论: 本地化状态健康")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

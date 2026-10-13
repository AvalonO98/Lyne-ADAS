#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Lyne ADAS 本地化守卫 (l10n guard)
================================

用途：在 CI 中防止「上游同步把英文文案带回来」，并校验中文资源文件的合法性。

这个脚本只读，绝不修改任何源文件。它做两件事：

1. **英文残留检测**：扫描 app/src/main/java 下的字符串字面量，找出仍会展示给
   用户的英文文案（中文界面里混进英文 = 汉化回归）。
2. **资源名校验**：校验 values-zh-rCN/*.xml 里的 <string name="..."> 是否符合
   Android 资源命名规则。非法资源名会让 AAPT2 构建直接失败。

退出码：
  0 = 通过
  1 = 发现英文残留或非法资源名（CI 应判定失败）

注意：检测是启发式的。字符串字面量无法完全区分「用户可见文案」与「内部标识」，
因此维护了一份显式白名单（INTERNAL_TOKENS）。新增技术性字面量时把它加进白名单，
而不是放宽整体规则。
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

# Windows 控制台默认 GBK，输出中文/符号会抛 UnicodeEncodeError 并让脚本崩溃。
for stream in (sys.stdout, sys.stderr):
    try:
        stream.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

PROJECT_ROOT = Path(__file__).resolve().parent.parent
JAVA_ROOT = PROJECT_ROOT / "app" / "src" / "main" / "java"
ZH_RES_DIR = PROJECT_ROOT / "app" / "src" / "main" / "res" / "values-zh-rCN"

# 内部标识 / 技术常量 / 品牌名：允许保留英文。
# 这些不是展示给用户的句子，而是协议字段、Intent action、mime、标签或品牌名。
INTERNAL_TOKENS = {
    # 品牌与技术名词
    "LYNE ADAS", "NNAPI", "XNNPACK", "GPU", "CPU", "NPU", "ROVIX", "ADAS", "HUD",
    "TTC", "FCW", "VRU", "LDW", "GPS", "IMU", "API", "JSON", "CSV", "XML",
    # HUD 目标类别的内部标签（显示时经 objectClassLabel() 转中文）
    "CAR", "TRUCK", "BUS", "AUTO", "MOTO", "CYCLE", "PED", "STOP", "OBJ",
    # mime / 文件 / Intent
    "video/mp4", "mp4", "clip_", ".mp4", "ACTION_VIEW", "ACTION_SEND",
    # 后台线程 / 动画标签（仅用于调试与 Compose label，不可见）
    "gaugeFrac", "gaugeColor", "hud", "phase", "pulse", "banner",
    # 注解参数、持久化枚举值、严重度键（内部标识，不展示）
    "DEPRECATION", "NORMAL", "CALM", "AGGRESSIVE", "CRITICAL", "CAUTION", "INFO",
    "MissingPermission", "SuppressLint",
    # 模型标签匹配用的内部 token（对应 assets/models/*_labels.txt）
    "stop sign", "stop_sign", "person", "pedestrian", "rickshaw", "tuk",
}

# 含这些片段的字面量是格式化模板，不是文案
SKIP_FRAGMENTS = ("MMM", "HH:mm", "yyyy", "EEE")

# 允许出现英文字母的整行（这些行不是 UI 文案）
SKIP_LINE_PATTERNS = (
    re.compile(r"^\s*//"),            # 行注释
    re.compile(r"^\s*\*"),            # 块注释
    re.compile(r"^\s*/\*"),           # 块注释开始
    re.compile(r"\bLog\.[dviwe]\("),  # 日志（仅进 logcat，不面向用户）
    re.compile(r"^\s*package\s"),
    re.compile(r"^\s*import\s"),
)

LITERAL_RE = re.compile(r'"((?:[^"\\]|\\.)*)"')

# 形如「英文散文」：含空格的多词
PROSE_RE = re.compile(r"^[A-Za-z][A-Za-z0-9 ,.:;!?'()\-/]*$")
HAS_ASCII_WORD_RE = re.compile(r"[A-Za-z]{3,}")
HAS_SPACE_RE = re.compile(r"\s")
# 混合大小写标识符（类名/TAG/常量），如 AlertManager、gaugeFrac
IDENTIFIER_RE = re.compile(r"^[A-Za-z][A-Za-z0-9]*[A-Z][A-Za-z0-9]*$")

# Android 资源名安全规则：小写字母开头，只含小写字母、数字、下划线。
VALID_RES_NAME_RE = re.compile(r"^[a-z][a-z0-9_]*$")
STRING_DECL_RE = re.compile(r'<string\s+name="([^"]*)"')


def _strip_line_comment(line: str) -> str:
    """去掉行尾注释，但不破坏字符串字面量里的内容（如 "video/mp4"）。

    逐字符扫描，跟踪是否处于双引号内；遇到引号外的 // 即截断。
    """
    in_str = False
    escaped = False
    for i, ch in enumerate(line):
        if escaped:
            escaped = False
            continue
        if ch == "\\":
            escaped = True
            continue
        if ch == '"':
            in_str = not in_str
            continue
        if not in_str and ch == "/" and i + 1 < len(line) and line[i + 1] == "/":
            return line[:i]
    return line


def _is_user_facing_english(text: str) -> bool:
    """判断一个字面量是否像「给用户看的英文句子」。"""
    if not text or text in INTERNAL_TOKENS:
        return False
    # 必须含有至少 3 个连续字母的英文单词
    if not HAS_ASCII_WORD_RE.search(text):
        return False
    # 纯技术性：路径、URL、下划线标识、格式化占位、单位符号
    if "/" in text or "\\" in text or "://" in text or "=" in text:
        return False
    if "%" in text or "${" in text or text.endswith("_"):
        return False
    if "_" in text:
        return False
    # 格式化模板（日期/时间）
    if any(frag in text for frag in SKIP_FRAGMENTS):
        return False
    # 单个混合大小写标识符 = 类名/常量名/TAG，不是文案
    if IDENTIFIER_RE.match(text):
        return False
    # 需要像散文：多词（含空格），或首字母大写的整词
    if HAS_SPACE_RE.search(text):
        return PROSE_RE.match(text) is not None
    return bool(re.match(r"^[A-Z][A-Za-z]{2,}$", text))


def scan_english() -> list[tuple[str, int, str]]:
    """返回 [(相对路径, 行号, 字面量)]，即疑似残留的英文用户文案。"""
    findings: list[tuple[str, int, str]] = []
    if not JAVA_ROOT.is_dir():
        return findings

    for kt in sorted(JAVA_ROOT.rglob("*.kt")):
        try:
            lines = kt.read_text(encoding="utf-8").splitlines()
        except OSError:
            continue
        for idx, line in enumerate(lines, start=1):
            if any(p.search(line) for p in SKIP_LINE_PATTERNS):
                continue
            code = _strip_line_comment(line)
            for literal in LITERAL_RE.findall(code):
                if _is_user_facing_english(literal):
                    findings.append((str(kt.relative_to(JAVA_ROOT)), idx, literal))
    return findings


def validate_resource_names() -> list[tuple[str, str]]:
    """返回 [(相对路径, 非法资源名)]。"""
    bad: list[tuple[str, str]] = []
    if not ZH_RES_DIR.is_dir():
        return bad

    for xml in sorted(ZH_RES_DIR.glob("*.xml")):
        try:
            content = xml.read_text(encoding="utf-8")
        except OSError:
            continue
        for name in STRING_DECL_RE.findall(content):
            if not VALID_RES_NAME_RE.match(name):
                bad.append((str(xml.relative_to(PROJECT_ROOT)), name))
    return bad


def main() -> int:
    print("Lyne ADAS 本地化守卫")
    print("=" * 64)

    english = scan_english()
    bad_names = validate_resource_names()

    print(f"扫描目录: {JAVA_ROOT.relative_to(PROJECT_ROOT)}")
    print(f"中文资源: {ZH_RES_DIR.relative_to(PROJECT_ROOT)}")
    print()

    if english:
        print(f"[失败] 发现 {len(english)} 处疑似残留的英文用户文案:")
        for rel, line, literal in english:
            print(f"  {rel}:{line}: {literal}")
    else:
        print("[通过] 未发现残留的英文用户文案")

    print()

    if bad_names:
        print(f"[失败] 发现 {len(bad_names)} 个非法 Android 资源名（会破坏 AAPT2 构建）:")
        for rel, name in bad_names:
            print(f'  {rel}: name="{name}"  (需匹配 {VALID_RES_NAME_RE.pattern})')
    else:
        print("[通过] 中文资源名全部合法")

    print()
    if english or bad_names:
        print("结论: 未通过")
        return 1
    print("结论: 通过")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

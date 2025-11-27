#!/usr/bin/env bash
set -euo pipefail
INPUT_APK=“${1:?input apk}”
OUTPUT_APK=“${2:?output apk}”
BASE_URL=“${3:-}”
LABEL=“${4:-Автоуслуги AI}”

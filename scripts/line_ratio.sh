#!/usr/bin/env bash
# Script: scripts/line_ratio.sh
# Purpose: Compute ratio of Kotlin source code lines (in modules/) to Gradle build script lines in the project.
# Definition of "code lines": non-empty lines that are not starting with //, /*, */ or * (naive comment filtering).
# Block comment middle lines starting with * are excluded; inline block comments on code lines are still counted.
# This is a heuristic, not a full parser.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
MODULES_DIR="${ROOT_DIR}/modules"

if [[ ! -d "${MODULES_DIR}" ]]; then
  echo "Modules directory not found at ${MODULES_DIR}" >&2
  exit 1
fi

# Collect Kotlin source files under modules (excluding build/ directories)
mapfile -t kotlin_files < <(find "${MODULES_DIR}" -type f -name "*.kt" -not -path "*/build/*" -print)

# Collect Gradle Kotlin DSL build scripts (*.gradle.kts) across the whole project (excluding build/ directories)
mapfile -t gradle_files < <(find "${ROOT_DIR}" -type f -name "*.gradle.kts" -not -path "*/build/*" -print)

count_code_lines() {
  # Args: list of files
  if [[ $# -eq 0 ]]; then
    echo 0
    return 0
  fi
  awk '
    function trim_cr(line){ sub(/\r$/, "", line); return line }
    {
      line=$0
      trim_cr(line)
      if (line ~ /^[[:space:]]*$/) next              # blank
      if (line ~ /^[[:space:]]*\/\//) next          # line comment
      if (line ~ /^[[:space:]]*\/*$/) next           # stray /* or */ alone
      if (line ~ /^[[:space:]]*\*+/) next            # block comment middle line starting with *
      if (line ~ /^[[:space:]]*\/\*/) next           # block comment start
      count++
    }
    END { print count+0 }
  ' "$@"
}

kotlin_lines=$(count_code_lines "${kotlin_files[@]}")
gradle_lines=$(count_code_lines "${gradle_files[@]}")

ratio="NaN"
if [[ ${gradle_lines} -ne 0 ]]; then
  ratio=$(awk -v k=${kotlin_lines} -v g=${gradle_lines} 'BEGIN { printf "%.2f", k / g }')
fi

percent_kotlin_of_total="NaN"
if [[ $((kotlin_lines + gradle_lines)) -ne 0 ]]; then
  percent_kotlin_of_total=$(awk -v k=${kotlin_lines} -v g=${gradle_lines} 'BEGIN { printf "%.2f", (k*100)/(k+g) }')
fi

cat <<EOF
Kotlin code lines (modules): ${kotlin_lines}
Gradle build script lines:   ${gradle_lines}
Ratio (Kotlin/Gradle):       ${ratio}
Kotlin % of (Kotlin+Gradle): ${percent_kotlin_of_total}%
Files counted: ${#kotlin_files[@]} Kotlin source files, ${#gradle_files[@]} Gradle build scripts.
EOF

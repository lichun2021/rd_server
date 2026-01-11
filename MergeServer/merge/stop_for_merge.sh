#!/usr/bin/env bash
set -euo pipefail

# 用于合服前停服：依次调用 closeForMergeServer（业务收尾）和 syscontrol 关闭全服登录/协议
# 可设置环境变量覆盖默认地址：
#   HOST=127.0.0.1 PORT=8080

HOST="${HOST:-${1:-127.0.0.1}}"
# 端口列表，默认 8080 和 8081，可通过 PORTS 环境变量或传参第 2 个覆盖（逗号分隔）
PORTS_RAW="${PORTS:-${2:-8080,8081}}"
IFS=',' read -r -a PORTS <<< "$PORTS_RAW"

for PORT in "${PORTS[@]}"; do
  BASE="http://${HOST}:${PORT}"
  echo "[${HOST}:${PORT}] closeForMergeServer ..."
  curl -fsSL "${BASE}/script/closeForMergeServer" || { echo "closeForMergeServer 调用失败 ${HOST}:${PORT}"; exit 1; }

  echo "[${HOST}:${PORT}] syscontrol all=true close=true ..."
  curl -fsSL "${BASE}/script/syscontrol?all=true&close=true" || { echo "syscontrol 调用失败 ${HOST}:${PORT}"; exit 1; }
done

echo "done."


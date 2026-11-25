#!/bin/bash
set -euo pipefail

pids=()
cleanup() {
    if (("${#pids[@]}" > 0)); then
        kill "${pids[@]}" 2>/dev/null || true
        wait "${pids[@]}" 2>/dev/null || true
    fi
}
trap cleanup EXIT
trap 'exit 0' TERM INT

Xvfb "$DISPLAY" -screen 0 1366x850x24 -nolisten tcp &
pids+=("$!")
for ((attempt = 0; attempt < 50; attempt++)); do
    if xdpyinfo >/dev/null 2>&1; then
        break
    fi
    sleep 0.15
done
xdpyinfo >/dev/null

openbox &
pids+=("$!")
x11vnc -display "$DISPLAY" -localhost -rfbport 5900 -nopw -forever -shared &
pids+=("$!")
websockify --web /usr/share/novnc 0.0.0.0:6080 127.0.0.1:5900 &
pids+=("$!")
/bin/sh /app/start-game.sh &
pids+=("$!")

wait -n "${pids[@]}"

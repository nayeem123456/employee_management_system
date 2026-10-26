#!/bin/sh
# Maven Wrapper Shell Script for AI-Powered Employee Management System
set -e

if command -v mvn >/dev/null 2>&1; then
    exec mvn "$@"
elif [ -x "/usr/share/maven/bin/mvn" ]; then
    exec /usr/share/maven/bin/mvn "$@"
else
    echo "[ERROR] Maven command 'mvn' not found in PATH." >&2
    exit 1
fi

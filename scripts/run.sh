#!/bin/bash
set -e

# Defining directories
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC_DIR="$PROJECT_ROOT/src/main/java"
OUT_DIR="$PROJECT_ROOT/out"
MAIN_CLASS="com.jhanvi857.coreHTTP.server.HttpServer"

# Cleaning
echo "Cleaning output directory..."
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# Compiling
echo "Compiling source files..."
# Creating list of source files to avoid command line length limits
find "$SRC_DIR" -name "*.java" > sources.txt
javac -d "$OUT_DIR" -sourcepath "$SRC_DIR" @sources.txt
rm sources.txt

# Running
echo "Starting Server..."
java -cp "$OUT_DIR" $MAIN_CLASS
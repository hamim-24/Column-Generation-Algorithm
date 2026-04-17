#!/bin/bash

CPLEX_JAR="/Users/Inz_mac/Applications/CPLEX_Studio_Community2212/cplex/lib/cplex.jar"
CPLEX_LIB_PATH="/Users/Inz_mac/Applications/CPLEX_Studio_Community2212/cplex/bin/arm64_osx"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR" || exit 1

if [ ! -f "$CPLEX_JAR" ]; then
    echo "Error: cplex.jar not found at: $CPLEX_JAR"
    echo "Please edit this script and update the CPLEX_JAR path."
    exit 1
fi

if [ ! -d "$CPLEX_LIB_PATH" ]; then
    echo "Warning: CPLEX library path not found at: $CPLEX_LIB_PATH"
    echo "The application may fail if native libraries cannot be loaded."
fi

mkdir -p bin bin/csp bin/vrp

echo "Compiling root Java menu launcher..."
javac -d bin -cp ".:$CPLEX_JAR" src/Main.java
if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi

echo "Compiling CSP sources..."
find CSP/src -name "*.java" | sort | xargs javac -d bin/csp -cp ".:$CPLEX_JAR"
if [ $? -ne 0 ]; then
    echo "CSP compilation failed."
    exit 1
fi

echo "Compiling VRP sources..."
find VRP/src -name "*.java" | sort | xargs javac -d bin/vrp -cp ".:$CPLEX_JAR"
if [ $? -ne 0 ]; then
    echo "VRP compilation failed."
    exit 1
fi

echo "Running project menu..."
java --enable-native-access=ALL-UNNAMED -cp "bin:$CPLEX_JAR" -Djava.library.path="$CPLEX_LIB_PATH" Main "$CPLEX_JAR" "$CPLEX_LIB_PATH"

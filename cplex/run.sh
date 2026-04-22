#!/bin/bash

CPLEX_JAR="/Users/Inz_mac/Applications/CPLEX_Studio_Community2212/cplex/lib/cplex.jar"
CPLEX_LIB_PATH="/Users/Inz_mac/Applications/CPLEX_Studio_Community2212/cplex/bin/arm64_osx"

# it will find the run.sh file path and cd to it
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

echo "Compiling CSP sources..."
# find all .java file and sort and pass these as arg to javac
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

# ./run.sh csp
if [ "$1" == "csp" ]; then
    echo "Launching CSP..."
    cd CSP || exit 1
    java --enable-native-access=ALL-UNNAMED -cp "../bin/csp:$CPLEX_JAR" -Djava.library.path="$CPLEX_LIB_PATH" CSPMain

# ./run.sh vrp
elif [ "$1" == "vrp" ]; then
    echo "Launching VRP..."
    cd VRP || exit 1
    java --enable-native-access=ALL-UNNAMED -cp "../bin/vrp:$CPLEX_JAR" -Djava.library.path="$CPLEX_LIB_PATH" VRPMain

# ./run.sh
else
    while true; do
        echo ""
        echo "=== Column Generation Project Menu ==="
        echo "  1) CSP"
        echo "  2) VRP"
        echo "  3) Exit"
        echo -n "Enter choice: "
        read choice
        case $choice in
            1)
                echo "Launching CSP..."
                cd CSP || exit 1
                java --enable-native-access=ALL-UNNAMED -cp "../bin/csp:$CPLEX_JAR" -Djava.library.path="$CPLEX_LIB_PATH" CSPMain
                cd ..
                # exit 0
                ;;
            2)
                echo "Launching VRP..."
                cd VRP || exit 1
                java --enable-native-access=ALL-UNNAMED -cp "../bin/vrp:$CPLEX_JAR" -Djava.library.path="$CPLEX_LIB_PATH" VRPMain
                cd ..
                # exit 0
                ;;
            3)
                echo "Exiting..."
                exit 0
                ;;
            *)
                echo "Invalid choice. Please enter 1, 2 or 3."
                ;;
        esac
    done
fi

#!/bin/bash
echo "Compiling Java source files..."
mkdir -p bin

javac -d bin src/com/ezbattlemap/dualscreen/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo ""
    echo "To run the application, use: ./run.sh"
else
    echo "Compilation failed!"
fi

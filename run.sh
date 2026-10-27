#!/bin/bash
# Compile and run (Mac / Linux). Needs JDK 8+.
cd "$(dirname "$0")" || exit 1
mkdir -p bin
javac -encoding UTF-8 -sourcepath src -cp lib/sqlite-jdbc.jar -d bin src/Main.java && java -cp "bin:lib/sqlite-jdbc.jar" Main

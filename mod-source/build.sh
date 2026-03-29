#!/bin/bash
# Nexus Absolu Mod - Build Script
# Usage: bash build.sh

cd /c/Dev/NexusAbsoluMod

FORGE="C:/Users/lamar/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.12.2-14.23.5.2860_mapped_snapshot_20171003-1.12/forge-1.12.2-14.23.5.2860_mapped_snapshot_20171003-1.12.jar"
LOG4J_API="C:/Users/lamar/.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-api/2.8.1/e801d13612e22cad62a3f4f3fe7fdbe6334a8e72/log4j-api-2.8.1.jar"
LOG4J_CORE="C:/Users/lamar/.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-core/2.8.1/4ac28ff2f1ddf05dae3043a190451e8c46b73c31/log4j-core-2.8.1.jar"
JSR="C:/Users/lamar/.gradle/caches/modules-2/files-2.1/com.google.code.findbugs/jsr305/3.0.1/f7be08ec23c21485b9b5a1cf1654c2ec8c58168d/jsr305-3.0.1.jar"
LWJGL="C:/Users/lamar/.gradle/caches/modules-2/files-2.1/org.lwjgl.lwjgl/lwjgl/2.9.4-nightly-20150209/697517568c68e78ae0b4544145af031c81082dfe/lwjgl-2.9.4-nightly-20150209.jar"
GUAVA="C:/Users/lamar/.gradle/caches/modules-2/files-2.1/com.google.guava/guava/21.0/3a3d111be1be1b745edfa7d91678a12d7ed38709/guava-21.0.jar"

CP="$FORGE;$LOG4J_API;$LOG4J_CORE;$JSR;$LWJGL;$GUAVA"

echo "=== Nexus Absolu Build ==="

# Step 1: Pull latest from git
echo "[1/5] Git pull..."
cd "/c/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu"
git pull

# Step 2: Copy sources
echo "[2/5] Copying sources..."
cp -r mod-source/src/* /c/Dev/NexusAbsoluMod/src/
rm -rf /c/Dev/NexusAbsoluMod/src/main/java/com/example

# Step 3: Compile
echo "[3/5] Compiling..."
cd /c/Dev/NexusAbsoluMod
rm -rf build/classes build/libs
mkdir -p build/classes build/libs
find src/main/java -name "*.java" > /tmp/sources.txt
javac -d build/classes -cp "$CP" -source 1.8 -target 1.8 @/tmp/sources.txt 2>&1
if [ $? -ne 0 ]; then
    echo "COMPILE FAILED!"
    exit 1
fi

# Step 4: Package JAR
echo "[4/5] Packaging JAR..."
cp -r src/main/resources/* build/classes/
cd build/classes
jar cf ../libs/NexusAbsolu-1.0.0.jar .
cd ../..

# Step 5: Deploy
echo "[5/5] Deploying to modpack..."
cp build/libs/NexusAbsolu-1.0.0.jar "/c/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu/mods/"

echo "=== BUILD SUCCESSFUL ==="
echo "Launch the game!"

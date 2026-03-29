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
JEI="C:/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu/mods/jei_1.12.2-4.16.1.301.jar"
SPECIALSOURCE="C:/Users/lamar/.gradle/caches/forge_gradle/maven_downloader/net/md-5/SpecialSource/1.8.3/SpecialSource-1.8.3-shaded.jar"
SRG="C:/Users/lamar/.gradle/caches/minecraft/de/oceanlabs/mcp/mcp_snapshot/20171003/1.12.2/srgs/mcp-srg.srg"

CP="$FORGE;$LOG4J_API;$LOG4J_CORE;$JSR;$LWJGL;$GUAVA;$JEI"

echo "=== Nexus Absolu Build ==="

# Step 1: Pull latest from git
echo "[1/6] Git pull..."
cd "/c/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu"
git pull

# Step 2: Copy sources
echo "[2/6] Copying sources..."
cp -r mod-source/src/* /c/Dev/NexusAbsoluMod/src/
cp mod-source/build.gradle /c/Dev/NexusAbsoluMod/build.gradle
rm -rf /c/Dev/NexusAbsoluMod/src/main/java/com/example

# Read version from build.gradle
VERSION=$(grep 'version = ' /c/Dev/NexusAbsoluMod/build.gradle | head -1 | sed 's/.*"\(.*\)".*/\1/')
echo "Version: $VERSION"

# Step 3: Compile
echo "[3/6] Compiling..."
cd /c/Dev/NexusAbsoluMod
rm -rf build/classes build/libs
mkdir -p build/classes build/libs
find src/main/java -name "*.java" > /tmp/sources.txt
javac -d build/classes -cp "$CP" -source 1.8 -target 1.8 @/tmp/sources.txt 2>&1
if [ $? -ne 0 ]; then
    echo "COMPILE FAILED!"
    exit 1
fi

# Step 4: Package dev JAR
echo "[4/6] Packaging..."
cp -r src/main/resources/* build/classes/
cd build/classes
jar cf ../libs/NexusAbsolu-$VERSION-dev.jar .
cd ../..

# Step 5: Reobfuscate (MCP names -> SRG names)
echo "[5/6] Reobfuscating..."
java -cp "$SPECIALSOURCE;$FORGE" net.md_5.specialsource.SpecialSource --in-jar build/libs/NexusAbsolu-$VERSION-dev.jar --out-jar build/libs/NexusAbsolu-$VERSION.jar --srg-in "$SRG" -l 2>&1
if [ $? -ne 0 ]; then
    echo "REOBF FAILED!"
    exit 1
fi

# Step 6: Deploy
echo "[6/6] Deploying v$VERSION to modpack..."
rm -f "/c/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu/mods/"NexusAbsolu-*.jar
cp build/libs/NexusAbsolu-$VERSION.jar "/c/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu/mods/"

echo "=== BUILD SUCCESSFUL ==="
echo "Launch the game!"

#!/bin/bash
# Nexus Absolu - Sonde classpath Modular Machinery
# Usage : bash check-mm-classpath.sh
#
# Verifie, avant d'ecrire une seule ligne du moteur de machines, que javac
# arrive bien a resoudre l'API de Modular Machinery depuis le jar reobfusque
# du modpack. Ne touche a rien : compile un fichier jetable dans /tmp.

set -u

MODS_DIR="C:/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu/mods"
FORGE="C:/Users/lamar/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.12.2-14.23.5.2860_mapped_snapshot_20171003-1.12/forge-1.12.2-14.23.5.2860_mapped_snapshot_20171003-1.12.jar"
GUAVA="C:/Users/lamar/.gradle/caches/modules-2/files-2.1/com.google.guava/guava/21.0/3a3d111be1be1b745edfa7d91678a12d7ed38709/guava-21.0.jar"
JSR="C:/Users/lamar/.gradle/caches/modules-2/files-2.1/com.google.code.findbugs/jsr305/3.0.1/f7be08ec23c21485b9b5a1cf1654c2ec8c58168d/jsr305-3.0.1.jar"
LOG4J_API="C:/Users/lamar/.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-api/2.8.1/e801d13612e22cad62a3f4f3fe7fdbe6334a8e72/log4j-api-2.8.1.jar"

MM=$(find "$MODS_DIR" -maxdepth 1 -iname "modularmachinery-*.jar" 2>/dev/null | head -1)
if [ -z "$MM" ]; then
    echo "[X] jar Modular Machinery introuvable dans $MODS_DIR"
    exit 1
fi
echo "[1/3] jar trouve : $(basename "$MM")"

echo "[2/3] classes attendues presentes dans le jar :"
MISSING=0
for c in \
    hellfirepvp/modularmachinery/common/machine/MachineRegistry.class \
    hellfirepvp/modularmachinery/common/machine/DynamicMachine.class \
    hellfirepvp/modularmachinery/common/machine/TaggedPositionBlockArray.class \
    hellfirepvp/modularmachinery/common/tiles/TileMachineController.class \
    hellfirepvp/modularmachinery/common/block/BlockController.class \
    hellfirepvp/modularmachinery/common/crafting/helper/RecipeCraftingContext.class ; do
    if unzip -l "$MM" "$c" >/dev/null 2>&1; then
        echo "    ok   $c"
    else
        echo "    MANQUE $c"
        MISSING=1
    fi
done
[ "$MISSING" -eq 1 ] && { echo "[X] jar incomplet ou version inattendue"; exit 1; }

echo "[3/3] compilation d'une sonde contre l'API MM..."
PROBE=/tmp/nexus_mm_probe
rm -rf "$PROBE" && mkdir -p "$PROBE/out"
cat > "$PROBE/MMProbe.java" <<'JAVA'
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import hellfirepvp.modularmachinery.common.machine.TaggedPositionBlockArray;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MMProbe {
    // Types MM seuls
    static DynamicMachine byName(ResourceLocation rl) {
        return MachineRegistry.getRegistry().getMachine(rl);
    }
    // Type MM + types Minecraft melanges dans la meme signature :
    // c'est ce cas qui casse si le mapping du jar reobf est incompatible.
    static boolean matches(TaggedPositionBlockArray pattern, World world, BlockPos pos) {
        return pattern.matches(world, pos, true, null);
    }
    // Lecture d'un etat du controleur MM
    static boolean crafting(TileMachineController ctrl) {
        return ctrl.getCraftingStatus().isCrafting();
    }
}
JAVA

CP="$FORGE;$LOG4J_API;$JSR;$GUAVA;$MM"
javac -d "$PROBE/out" -cp "$CP" -source 1.8 -target 1.8 -nowarn "$PROBE/MMProbe.java" 2>&1
RC=$?
echo
if [ $RC -eq 0 ]; then
    echo "=== OK : l'API MM se compile depuis le jar du modpack ==="
    echo "build.sh peut compiler le moteur de machines."
else
    echo "=== ECHEC : voir les erreurs javac ci-dessus ==="
    echo "Si l'erreur porte sur des methodes 'func_XXXXX', le jar reobf n'expose"
    echo "pas les noms MCP : il faudra deobfusquer le jar MM avec SpecialSource"
    echo "et le mapping inverse avant de le mettre au classpath."
fi
exit $RC

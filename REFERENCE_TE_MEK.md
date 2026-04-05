# Reference: Thermal Expansion + Mekanism Patterns for Nexus Absolu
## Study from CoFH/ThermalExpansion-1.12-Legacy + mekanism/Mekanism 1.12

---

## TABLE OF CONTENTS
1. [Machine Architecture](#1-machine-architecture)
2. [Processing Loop](#2-processing-loop)
3. [Side Configuration](#3-side-configuration)
4. [GUI System](#4-gui-system)
5. [JEI Integration](#5-jei-integration)
6. [Energy System](#6-energy-system)
7. [Fluid Handling](#7-fluid-handling)
8. [Item Handling & Slots](#8-item-handling--slots)
9. [NBT & Network Sync](#9-nbt--network-sync)
10. [Recipe System](#10-recipe-system)
11. [Sound System](#11-sound-system)
12. [Registration Patterns](#12-registration-patterns)
13. [Block & Facing](#13-block--facing)
14. [Textures & Rendering](#14-textures--rendering)
15. [Nexus Absolu Action Items](#15-nexus-absolu-action-items)

---

## 1. MACHINE ARCHITECTURE

### Thermal Expansion Hierarchy
```
TileEntity (vanilla)
  └─ TileTEBase (NBT, security, redstone control, side cache)
       └─ TileRSPowered (redstone signal handling)
            └─ TilePowered (EnergyStorage, capabilities, auto I/O)
                 └─ TileMachineBase (processing loop, augments, upgrades)
                      ├─ TileSmelter (Induction Smelter)
                      ├─ TilePulverizer
                      ├─ TileFurnace
                      ├─ TileSawmill
                      ├─ TileBrewer (fluid I/O)
                      ├─ TileCrucible
                      ├─ TileTransposer
                      └─ ... (13 machines total)
```

**Key insight**: TE puts ALL machine logic in the base class. Concrete machines only implement:
- `canStart()` — checks if recipe inputs are present
- `hasValidInput()` — validates current state
- `processStart()` — sets processMax/processRem
- `processFinish()` — consumes inputs, produces outputs
- `getRecipe()` / `clearRecipe()` — recipe cache
- `transferInput()` / `transferOutput()` — auto-pull/push

### Mekanism Hierarchy
```
TileEntity (vanilla)
  └─ TileEntityBasicBlock (facing, redstone, security, components)
       └─ TileEntityContainerBlock (inventory, ISidedInventory)
            └─ TileEntityElectricBlock (energy storage)
                 └─ TileEntityEffectsBlock (sound, particles)
                      └─ TileEntityMachine (active state, upgrades)
                           └─ TileEntityOperationalMachine (operatingTicks, ticksRequired)
                                └─ TileEntityUpgradeableMachine (recipes, cachedRecipe)
                                     └─ TileEntityBasicMachine (GUI texture ref)
                                          └─ TileEntityElectricMachine (1 item in, 1 item out)
                                               ├─ TileEntityEnrichmentChamber
                                               ├─ TileEntityCrusher
                                               └─ ...
```

**Key insight**: Mekanism's hierarchy is deeper (8 levels vs TE's 5) but each level is thinner.
The concrete machine class (TileEntityEnrichmentChamber) has ZERO extra logic — everything is in the hierarchy.

### What Nexus Absolu Does
Our hierarchy is flat:
```
TileEntity → TileMachineHumaine (everything in one class)
```
This works for 2 machines but won't scale. Consider extracting a `TileNexusMachine` base.

---

## 2. PROCESSING LOOP

### TE Pattern (TileMachineBase.update)
```java
void update() {
    boolean curActive = isActive;
    
    if (isActive) {
        processTick();                    // drain energy, reduce processRem
        if (canFinish()) {                // processRem <= 0
            processFinish();              // consume inputs, produce outputs
            transferOutput();             // auto-push to neighbors
            transferInput();              // auto-pull from neighbors
            energyStorage.modifyEnergyStored(-processRem); // leftover energy
            if (!canStart()) {
                processOff();             // no more inputs
            } else {
                processStart();           // chain next recipe
            }
        } else if (energyStorage.getEnergyStored() <= 0) {
            processOff();                 // out of energy
        }
    } else if (redstoneControlOrDisable()) {
        if (timeCheck()) {                // every 8 ticks
            transferOutput();
            transferInput();
        }
        if (timeCheckEighth() && canStart()) {  // every tick
            processStart();
            processTick();
            isActive = true;
        }
    }
    updateIfChanged(curActive);           // send packet if state changed
    chargeEnergy();                       // charge from battery slot
}
```

**Critical details:**
- `processRem` counts DOWN from `processMax` to 0 (energy remaining, not ticks)
- `processTick()` drains variable energy per tick based on stored energy (dynamic speed)
- Machine speed scales with energy buffer level (`calcEnergy()`)
- `transferInput/Output` only run every 8 ticks when idle (performance)
- State changes trigger client packet (`sendTilePacket`)
- `wasActive` + `TimeTracker` prevent flickering (delay off-animation by `tileUpdateDelay`)

### TE calcEnergy — Dynamic Speed
```java
protected int calcEnergy() {
    if (energyStorage >= maxPowerLevel) return maxPower;     // full speed
    if (energyStorage < minPowerLevel)  return minPower;     // minimum speed
    return energyStorage / energyRamp;                       // proportional
}
```
This means: the more RF stored, the faster the machine runs. Brilliant UX.

### Mekanism Pattern (TileEntityElectricMachine.onUpdate)
```java
void onUpdate() {
    super.onUpdate();
    if (!world.isRemote) {
        ChargeUtils.discharge(1, this);       // discharge battery in slot 1
        RECIPE recipe = getRecipe();
        if (canOperate(recipe) && canFunction() && getEnergy() >= energyPerTick) {
            setActive(true);
            electricityStored -= energyPerTick;
            if ((operatingTicks + 1) < ticksRequired) {
                operatingTicks++;             // counting UP in ticks
            } else {
                operate(recipe);              // consume + produce
                operatingTicks = 0;
            }
        } else if (prevEnergy >= getEnergy()) {
            setActive(false);
        }
        if (!canOperate(recipe)) operatingTicks = 0;
        prevEnergy = getEnergy();
    }
}
```

**Key differences from TE:**
- Counts ticks UP (simpler but no dynamic speed)
- Fixed energy per tick (no speed scaling)
- Recipe checked every tick (cached with `cachedRecipe`)
- `prevEnergy >= getEnergy()` check prevents instant deactivation when receiving energy

### Nexus Recommendation
Use TE's pattern for new machines:
- `processRem` counting down = natural progress display
- Dynamic speed from energy buffer = player incentive to build good power
- `timeCheck()` gating for idle transfer = better TPS

---

## 3. SIDE CONFIGURATION

### TE Pattern
```java
// Static per machine type — defined once in initialize()
SIDE_CONFIGS[TYPE] = new SideConfig();
SIDE_CONFIGS[TYPE].numConfig = 9;    // number of possible states per face
SIDE_CONFIGS[TYPE].slotGroups = new int[][] {
    {},           // NONE
    { 0, 1 },    // INPUT_ALL
    { 2 },       // OUTPUT_PRIMARY
    { 3 },       // OUTPUT_SECONDARY
    { 2, 3 },    // OUTPUT_ALL
    { 0 },       // INPUT_PRIMARY
    { 1 },       // INPUT_SECONDARY
    { 0,1,2,3 }, // OPEN
    { 0,1,2,3 }  // OMNI
};
SIDE_CONFIGS[TYPE].sideTypes = new int[] {
    NONE, INPUT_ALL, OUTPUT_PRIMARY, OUTPUT_SECONDARY,
    OUTPUT_ALL, INPUT_PRIMARY, INPUT_SECONDARY, OPEN, OMNI
};
SIDE_CONFIGS[TYPE].defaultSides = new byte[] { 3, 1, 2, 2, 2, 2 };
// Bottom=OUTPUT_SEC, Top=INPUT_ALL, N/S/E/W=OUTPUT_PRI

// Per-instance: sideCache[6] stores which config index each face uses
// Clicking a face cycles through sideTypes[]
```

**TE uses overlay textures** to show side config on the block model (TETextures.CONFIG[sideType]).
Each config type has a distinct color overlay visible on the block in-world.

### Mekanism Pattern
```java
// Per-instance component
configComponent = new TileComponentConfig(this, TransmissionType.ITEM, TransmissionType.ENERGY);

// Multiple SideData entries per transmission type
configComponent.addOutput(TransmissionType.ITEM, new SideData("None", GREY, EMPTY));
configComponent.addOutput(TransmissionType.ITEM, new SideData("Input", DARK_RED, new int[]{0}));
configComponent.addOutput(TransmissionType.ITEM, new SideData("Output", DARK_BLUE, new int[]{2}));
configComponent.addOutput(TransmissionType.ITEM, new SideData("Energy", DARK_GREEN, new int[]{1}));

// Default face assignments
configComponent.setConfig(TransmissionType.ITEM, new byte[]{3, 1, 0, 0, 0, 2});
// Bottom=Energy, Top=Input, Front/Back/Left/Right=None/None/None/Output

// Ejector component handles auto-output
ejectorComponent = new TileComponentEjector(this);
ejectorComponent.setOutputData(TransmissionType.ITEM, configComponent.getOutputs(TransmissionType.ITEM).get(2));
```

**Mekanism opens a SEPARATE GUI** for side config (GuiSideConfiguration) via network packet.
It shows a 3D cross layout (Top/Bottom/N/S/E/W) with colored buttons.
Color = SideData color (EnumColor enum).

### Nexus SideConfig Comparison
Our system (SideConfig.java) uses bitpacking:
- 4 types (food/water/energy/output) x 6 faces = 24 bits
- Plus eject/autopull per type = 8 more bits
This is simpler than both TE and Mek but works for our needs.

**Improvement**: Add in-world overlay like TE (colored faces on the block).

---

## 4. GUI SYSTEM

### TE GUI Pattern
TE relies heavily on CoFH Core's GUI framework:
- Background = `getGuiLocation()` returns a PNG texture (ResourceLocation)
- CoFH provides: `ElementEnergyStored`, `ElementFluidTank`, `ElementDualScaled` (progress/speed)
- Each GUI creates elements in constructor and they self-render
- Click handling delegated to elements

### Mekanism GUI Pattern (RECOMMENDED)
```java
// Constructor — add all GUI elements declaratively
public GuiElectricMachine(...) {
    super(tile, container);
    addGuiElement(new GuiRedstoneControl(this, tileEntity, resource));
    addGuiElement(new GuiUpgradeTab(this, tileEntity, resource));
    addGuiElement(new GuiSecurityTab(this, tileEntity, resource));
    addGuiElement(new GuiSideConfigurationTab(this, tileEntity, resource));
    addGuiElement(new GuiPowerBar(this, tileEntity, resource, 164, 15));
    addGuiElement(new GuiEnergyInfo(() -> Arrays.asList(...), this, resource));
    addGuiElement(new GuiSlot(SlotType.INPUT, this, resource, 55, 16));
    addGuiElement(new GuiSlot(SlotType.OUTPUT_LARGE, this, resource, 111, 30));
    addGuiElement(new GuiProgress(progressHandler, ProgressBar.BLUE, this, resource, 77, 37));
}

// Background — just draw the PNG, elements self-render
protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
    resetColor();
    bindTexture(getGuiLocation());
    drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    // Elements render themselves via forEach loop in base class
    guiElements.forEach(e -> e.renderBackground(xAxis, yAxis, guiLeft, guiTop));
}

// Foreground — elements render their own tooltips
protected void drawGuiContainerForegroundLayer(int mx, int my) {
    fontRenderer.drawString(title, ...);
    fontRenderer.drawString("Inventory", ...);
    guiElements.forEach(e -> e.renderForeground(xAxis, yAxis));
}
```

### GuiElement Base Class
```java
abstract class GuiElement {
    ResourceLocation RESOURCE;     // element's own texture sheet (256x256)
    IGuiWrapper guiObj;            // parent GUI reference
    ResourceLocation defaultLocation; // main GUI texture to rebind after

    abstract void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight);
    abstract void renderForeground(int xAxis, int yAxis);  // tooltips here
    abstract void mouseClicked(int xAxis, int yAxis, int button);
}
```

### GuiProgress Element
Each progress bar type is defined in an enum with UV coordinates:
```java
enum ProgressBar {
    BLUE(28, 11, 0, 0),
    YELLOW(28, 11, 0, 11),
    RED(28, 11, 0, 22),
    // ...
    LARGE_RIGHT(52, 10, 128, 0),
    SMALL_LEFT(32, 10, 128, 40);
    
    int width, height, textureX, textureY;
}
```
Rendering: bind GuiProgress.png texture, draw background sprite, then draw fill sprite clipped to progress ratio.

### GuiPowerBar Element
- Reads `tileEntity.getEnergy() / getMaxEnergy()` as ratio
- Draws from a 6x56 texture sheet (empty state + filled state side by side)
- Fill draws from bottom up: `height - displayInt` offset
- Tooltip: formatted energy string on hover

### GuiGauge (Fluid/Gas Gauge)
- Multiple sizes via Type enum (STANDARD, WIDE, SMALL)
- Draws the actual fluid texture from TextureMap (block atlas)
- Tiles the fluid sprite vertically to fill the gauge height
- Overlay draws level lines on top
- Tooltip: fluid name + amount

### Nexus Current State vs Pro Pattern
We already use PNG backgrounds (good). What's missing:
- **No element system** — each GUI draws everything manually
- **No reusable components** — PowerBar, FluidTank, Progress duplicated across GUIs
- **Consider**: Create NexusGuiElement base + NexusPowerBar, NexusFluidGauge, NexusProgress

---

## 5. JEI INTEGRATION

### TE JEI Pattern (Drawables Singleton)
TE creates a centralized `Drawables` singleton with ALL JEI sprites:
```java
class Drawables {
    static ResourceLocation JEI_TEXTURE = new ResourceLocation("thermalexpansion:textures/gui/jei_handler.png");
    
    IDrawableStatic[] slot;          // regular, output, double
    IDrawableStatic[] tank;          // standard, thin, short
    IDrawableStatic[] tankOverlay;   // small overlay, large overlay
    IDrawableStatic[] progressRight; // arrow, fluid arrow, drop
    IDrawableStatic[] progressRightFill;  // filled versions
    IDrawableStatic[] scale;         // flame, bubble, crush, saw...
    IDrawableStatic[] scaleFill;
    IDrawableStatic energyEmpty, energyFill;
}
```

All TE JEI categories share this one 256x256 texture. Very efficient.

### TE Category Background
```java
// Uses the ACTUAL machine GUI texture as JEI background!
background = guiHelper.createDrawable(GuiSmelter.TEXTURE, 26, 11, 124, 62, 0, 0, 16, 24);
// This crops a region from the real GUI PNG and adds padding

// Then overlays animated elements
progress = Drawables.getDrawables(guiHelper).getProgressFill(PROGRESS_ARROW);
progressAnimated = guiHelper.createAnimatedDrawable(progress, 200, LEFT, false);
speed = Drawables.getDrawables(guiHelper).getScaleFill(SCALE_FLAME);
speedAnimated = guiHelper.createAnimatedDrawable(speed, 400, TOP, true);
```

### TE Recipe Click Area
```java
registry.addRecipeClickArea(GuiSmelter.class, 79, 34, 24, 16, 
    RecipeUidsTE.SMELTER, RecipeUidsTE.SMELTER_PYROTHEUM);
```
This makes the arrow area in the real GUI clickable to see JEI recipes. **We should add this.**

### Mekanism JEI: Not in the main repo
Mekanism's JEI integration is handled externally. Not applicable as reference.

### JEI Fluid Rendering (from JEI source)
```java
// In category setRecipe():
IGuiFluidStackGroup fluids = layout.getFluidStacks();
fluids.init(slotIndex, isInput, x, y, width, height, capacityMb, showCapacity, overlay);
fluids.set(ingredients);

// In wrapper getIngredients():
ingredients.setInput(VanillaTypes.FLUID, new FluidStack(fluid, amount));
ingredients.setOutput(VanillaTypes.FLUID, new FluidStack(fluid, amount));
```

JEI renders fluids using the block atlas texture (same as in-world) with proper tiling.
The `overlay` parameter can be used for tank level lines (TE uses tankOverlay drawables).

### JEI IDrawableAnimated
```java
// Create static sprite
IDrawableStatic arrowStatic = guiHelper.createDrawable(texture, u, v, w, h);

// Wrap in animated version
IDrawableAnimated arrow = guiHelper.createAnimatedDrawable(
    arrowStatic,      // the sprite to reveal over time
    ticksPerCycle,     // how long one animation cycle takes (200 = 10 seconds)
    StartDirection.LEFT, // reveal from left to right
    false              // false = reveal, true = hide
);

// Alternative via builder (more control)
IDrawableAnimated arrow = guiHelper.drawableBuilder(texture, u, v, w, h)
    .buildAnimated(ticksPerCycle, StartDirection.LEFT, false);

// In category drawExtras():
arrow.draw(minecraft, x, y);  // self-animating, no tick tracking needed
```

StartDirection options: `TOP`, `BOTTOM`, `LEFT`, `RIGHT`

### Nexus JEI Improvements Applied (v1.0.111)
- Custom PNG texture sheet (jei_nexus.png) with category backgrounds + arrow sprites
- Animated progress arrow via IDrawableAnimated
- Fluid tanks via IGuiFluidStackGroup (water, diarrhee)
- **Still missing**: Recipe click areas in machine GUIs, energy display drawables

---

## 6. ENERGY SYSTEM

### TE EnergyStorage
```java
// CoFH's EnergyStorage (RF API)
energyStorage = new EnergyStorage(maxEnergy, maxTransfer);
energyStorage.modifyEnergyStored(-amount);  // direct modify
energyStorage.receiveEnergy(amount, simulate);
energyStorage.extractEnergy(amount, simulate);
```

TE machines have TWO energy thresholds:
- `minPowerLevel` = minimum stored energy to run at minimum speed
- `maxPowerLevel` = energy level for maximum speed
- Between them, speed scales linearly

### Mekanism Energy
```java
// Mekanism uses its own energy unit (Joules) with conversion
double electricityStored;
double BASE_ENERGY_PER_TICK;
double energyPerTick; // modified by speed upgrades

// Upgrade scaling
energyPerTick = BASE * (1 + upgradeCount * multiplier);
ticksRequired = BASE_TICKS / (1 + upgradeCount * speedMultiplier);
```

### Nexus Energy
Our machines use Forge Energy (compatible with RF):
```java
EnergyStorageNexus energy = new EnergyStorageNexus(capacity, maxReceive);
```
We check `energy.getEnergyStored() >= RF_PER_TICK` and drain fixed amount.
**Improvement**: Add dynamic speed like TE's `calcEnergy()`.

---

## 7. FLUID HANDLING

### TE FluidTankCore
```java
FluidTankCore tank = new FluidTankCore(capacity);
tank.setLock(TFFluids.fluidPyrotheum);  // lock to specific fluid
tank.fill(fluidStack, doFill);
tank.drain(amount, doDrain);
tank.getFluidAmount();
```

TE exposes fluid capabilities per-face using side config:
```java
// In hasCapability/getCapability:
if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
    return sideConfig.allowsFluid(facing);
}
```

### Mekanism Gas System
Mekanism has its own "Gas" system parallel to fluids:
```java
GasTank gasTank = new GasTank(capacity);
gasTank.receive(gasStack, doTransfer);
gasTank.draw(amount, doTransfer);
```
Gases render with their own texture in the GUI gauge.

### Nexus Fluid Handling
Our machines use vanilla FluidTank:
```java
FluidTank waterTank = new FluidTank(4000);
FluidTank outputTank = new FluidTank(4000);
```
Side config controls which faces expose which tank.
Bucket I/O handled manually in `tryFillBucket()`.

**Improvement**: Consider FluidTankCore-style locking for output tanks (prevent wrong fluid insertion).

---

## 8. ITEM HANDLING & SLOTS

### TE Slot Management
```java
// SlotConfig defines per-slot permissions
SLOT_CONFIGS[TYPE] = new SlotConfig();
SLOT_CONFIGS[TYPE].allowInsertionSlot = new boolean[] { true, true, false, false, false };
SLOT_CONFIGS[TYPE].allowExtractionSlot = new boolean[] { false, false, true, true, false };
```

TE's `transferInput()` iterates faces, checks side config, pulls items from neighbors.
`transferOutput()` pushes to neighbors. Both use round-robin trackers:
```java
for (int i = inputTracker + 1; i <= inputTracker + 6; i++) {
    side = i % 6;
    if (isPrimaryInput(sideConfig.sideTypes[sideCache[side]])) {
        if (extractItem(0, ITEM_TRANSFER[level], EnumFacing.VALUES[side])) {
            inputTracker = side;
            break;
        }
    }
}
```

### Mekanism Slot Validation
```java
public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot == OUTPUT) return false;           // never insert to output
    if (slot == UPGRADE) return isUpgrade(stack); // upgrade items only
    if (slot == INPUT) return isInRecipe(stack);  // recipe-valid items only
    if (slot == ENERGY) return canBeDischarged(stack); // batteries
    return false;
}
```

### Container enchantItem Trick (Used by Nexus)
Both TE and Mek use proper network packets for GUI actions.
We use `enchantItem()` as a hack for button presses. This works but:
- Limited to int payload (the enchant ID)
- No validation on server side
- Should eventually be replaced with custom packets

---

## 9. NBT & NETWORK SYNC

### TE Pattern (3 types of data transfer)
```java
// 1. Full NBT (save/load)
writeToNBT(NBTTagCompound nbt) {
    nbt.setInteger("ProcMax", processMax);
    nbt.setInteger("ProcRem", processRem);
}

// 2. GUI sync packet (only GUI-relevant data, frequent)
getGuiPacket() → payload.addInt(processMax); payload.addInt(processRem);
handleGuiPacket() → processMax = payload.getInt();

// 3. Tile packet (block state changes, infrequent)
// Sent when isActive changes, contains visual state only
```

**Critical**: TE separates GUI sync from tile sync. GUI packets are sent only when GUI is open
(triggered by Container.detectAndSendChanges). Tile packets are sent on state change (active/inactive).

### Mekanism Pattern
```java
// Uses TileNetworkList for ordered packet building
TileNetworkList data = new TileNetworkList();
data.add(operatingTicks);
data.add(isActive);
data.add(facing);
// ...
```

### Nexus Pattern
We use Container fields (getField/setField) for GUI sync:
```java
// Container detectAndSendChanges() calls getField(0..9)
// Client Container updateProgressBar() receives updates
```
This is vanilla-compatible and works well for simple data.

---

## 10. RECIPE SYSTEM

### TE RecipeManager Pattern
```java
class SmelterManager {
    static Map<List<ComparableItemStackSmelter>, SmelterRecipe> recipeMap;
    
    static SmelterRecipe getRecipe(ItemStack input1, ItemStack input2) {
        return recipeMap.get(Arrays.asList(
            new ComparableItemStackSmelter(input1),
            new ComparableItemStackSmelter(input2)));
    }
    
    static void addRecipe(int energy, ItemStack primary, ItemStack secondary,
                           ItemStack output, ItemStack secondaryOutput, int chance) {
        recipeMap.put(key, new SmelterRecipe(primary, secondary, output, secondaryOutput, chance, energy));
    }
}
```

Each recipe stores: inputs, primary output, secondary output (with chance), total energy cost.
CraftTweaker hooks into `addRecipe` via ZenScript.

### Mekanism Recipe Pattern
```java
// Generic recipe with typed Input/Output
abstract class MachineRecipe<INPUT, OUTPUT> {
    INPUT input;
    OUTPUT output;
    
    abstract boolean canOperate(NonNullList<ItemStack> inventory, int inSlot, int outSlot);
    abstract void operate(NonNullList<ItemStack> inventory, int inSlot, int outSlot);
}

// Concrete: BasicMachineRecipe (1 item in, 1 item out)
// Cached per-tile:
RECIPE cachedRecipe;
RECIPE getRecipe() {
    INPUT input = getInput();
    if (cachedRecipe == null || !input.testEquality(cachedRecipe.getInput())) {
        cachedRecipe = RecipeHandler.getRecipe(input, getRecipes());
    }
    return cachedRecipe;
}
```

**Key**: Recipe caching per-tile avoids map lookups every tick.

### Nexus Recipe Approach
Our machines have hardcoded logic (no recipe registry):
- Diarh33: `food instanceof ItemFood` → diarrhee
- KRDA: signalum + diarrhee → signalhee
- Condenseur: slot-based recipe from CondenseurRecipes registry

For future machines, consider a simple recipe registry like TE.

---

## 11. SOUND SYSTEM

### TE Machine Sounds
```java
// Defined in TESounds
public static SoundEvent machineSmelter;

// In TileSmelter
@Override
protected SoundEvent getSoundEvent() {
    return TESounds.machineSmelter;
}
```
TE's sound plays via the effects block layer (TileEntityEffectsBlock equivalent).
Sound loops while `isActive`, stops on `processOff()`.

### Mekanism Machine Sounds
```java
// TileEntityEffectsBlock handles sound
String soundPath;  // e.g. "machine.enrichment"

// Client-side sound is played via SoundHandler
// Loops while machine is active
// Stops when setActive(false)
```

### Nexus Sound Implementation (v1.0.103)
We play sounds directly in `update()`:
```java
if (gurgleCooldown <= 0) {
    world.playSound(null, pos, ModSounds.STOMACH_GURGLE, BLOCKS, 0.2F, pitch);
    gurgleCooldown = 50 + rand.nextInt(20);
}
```
This is functional but could be improved with a looping SoundInstance for smoother playback.

---

## 12. REGISTRATION PATTERNS

### TE Registration
```java
// In machine initialize():
GameRegistry.registerTileEntity(TileSmelter.class, "thermalexpansion:machine_smelter");

// Block registration uses metadata subtypes (one block class, 13 machines)
BlockMachine has Type enum with metadata values
```

### Mekanism Registration
```java
// Uses MachineType enum that maps to tile classes
enum MachineType {
    ENRICHMENT_CHAMBER(0, TileEntityEnrichmentChamber.class, ...),
    CRUSHER(3, TileEntityCrusher.class, ...),
    // ...
}
```

### Nexus Registration
We register each machine separately in ModBlocks + CommonProxy:
```java
public static final Block MACHINE_HUMAINE = new BlockMachineHumaine();
// CommonProxy.preInit: GameRegistry.registerTileEntity(...)
```
This is fine for our scale.

---

## 13. BLOCK & FACING

### TE Block Pattern
- One BlockMachine class for all machine types (metadata subtypes 0-15)
- `IBlockState` with `FACING` property (EnumFacing.HORIZONTALS)
- `onBlockPlacedBy` sets facing from player look direction
- Block model references different textures based on metadata + active state

### Mekanism Block Pattern
- Similar: one BlockMachine with MachineType metadata
- BlockStateMachine handles all states
- `facing` stored as byte in TileEntity, not in blockstate

### Nexus Pattern
We use separate Block classes per machine with `FACING` property.
This is simpler and works well.

---

## 14. TEXTURES & RENDERING

### TE Machine Textures
```java
// 2-pass rendering:
@Override
public int getNumPasses() { return 2; }

@Override
public TextureAtlasSprite getTexture(int side, int pass) {
    if (pass == 0) {  // Base texture
        return side == facing 
            ? (isActive ? MACHINE_ACTIVE[type] : MACHINE_FACE[type])
            : MACHINE_SIDE;
    } else {  // Config overlay (pass 1)
        return TETextures.CONFIG[sideConfig.sideTypes[sideCache[side]]];
    }
}
```

Pass 1 draws the base machine texture.
Pass 2 draws a colored overlay showing the side configuration (blue=input, orange=output, etc).
This is visible in-world without opening the GUI.

### Mekanism Machine Textures
Mekanism uses TESR (TileEntitySpecialRenderer) for some machines but standard block models for most.
Active/inactive states use different textures registered in blockstates JSON.

### Nexus Textures
We have 16x16 animated front faces (mcmeta). 
**Improvement**: Add config overlay pass like TE to show side config in-world.

---

## 15. NEXUS ABSOLU ACTION ITEMS

Based on studying both codebases, prioritized improvements:

### HIGH PRIORITY (Next Session)
1. **Recipe click areas in machine GUIs** — `registry.addRecipeClickArea(GuiMachineHumaine.class, px, py, w, h, Diarh33Category.UID)` so players can click the progress bar to see JEI recipes
2. **JEI energy display** — Add energy bar drawable to JEI categories (like TE's energyEmpty/energyFill)
3. **TileMachineBase extraction** — Common base class for Diarh33/KRDA with shared processing loop, energy, side config, sounds, NBT

### MEDIUM PRIORITY
4. **Dynamic speed scaling** — Port TE's `calcEnergy()` pattern: machines run faster with more stored energy
5. **Reusable GUI elements** — Create NexusGuiElement, NexusPowerBar, NexusFluidGauge, NexusProgress classes
6. **Recipe registry** — Simple HashMap-based recipe system instead of hardcoded logic
7. **In-world side config overlay** — Second render pass showing colored faces like TE
8. **Better auto I/O** — Round-robin face iteration with tracker like TE (avoids hammering one face)

### LOW PRIORITY (Polish)
9. **Augment/upgrade system** — Speed upgrades, energy efficiency upgrades
10. **Looping machine sounds** — Client-side ISound instance that loops while active (smoother than periodic playSound)
11. **Custom network packets** — Replace enchantItem hack with proper PacketBuffer-based packets
12. **GUI animation** — Pulsing borders, scanline effects, particle overlays during processing

### CODE SNIPPETS FOR QUICK REFERENCE

#### Recipe Click Area (add to NexusJEIPlugin.register)
```java
registry.addRecipeClickArea(GuiMachineHumaine.class, 100, 34, 46, 42, Diarh33Category.UID);
registry.addRecipeClickArea(GuiMachineKRDA.class, 90, 34, 56, 42, KRDACategory.UID);
```

#### Dynamic Speed Pattern
```java
protected int calcEnergy() {
    int stored = energy.getEnergyStored();
    int max = energy.getMaxEnergyStored();
    if (stored >= max * 3/4) return maxPower;
    if (stored <= max / 8) return minPower;
    return stored * maxPower / (max * 3/4);
}
```

#### TileMachineBase Template
```java
public abstract class TileNexusMachine extends TileEntity implements ITickable, IInventory {
    protected EnergyStorageNexus energy;
    protected SideConfig sideConfig;
    protected int processMax, processRem;
    protected boolean isActive;
    
    // Subclasses implement:
    protected abstract boolean canStart();
    protected abstract void processFinish();
    protected abstract int getProcessEnergy();
    
    @Override
    public void update() {
        if (world.isRemote) return;
        boolean wasActive = isActive;
        if (isActive) {
            int drain = calcEnergy();
            energy.drainInternal(drain);
            processRem -= drain;
            if (processRem <= 0) {
                processFinish();
                if (canStart()) processStart();
                else isActive = false;
            }
        } else if (canStart()) {
            processStart();
            isActive = true;
        }
        if (wasActive != isActive) markDirty();
    }
}
```

---

## FILE LOCATIONS (for reference)

### Thermal Expansion (cloned to /home/claude/te-legacy)
- `block/machine/TileMachineBase.java` — 706 lines, core processing loop
- `block/machine/TileSmelter.java` — 625 lines, Induction Smelter
- `gui/client/machine/GuiSmelter.java` — machine GUI
- `plugins/jei/Drawables.java` — JEI sprite singleton
- `plugins/jei/machine/smelter/` — JEI category + wrapper

### Mekanism (cloned to /home/claude/mek-12)
- `tile/prefab/TileEntityElectricMachine.java` — 159 lines, base machine
- `tile/component/TileComponentConfig.java` — 248 lines, side config
- `tile/component/TileComponentEjector.java` — 263 lines, auto-output
- `client/gui/GuiMekanism.java` — 217 lines, base GUI with element system
- `client/gui/element/GuiElement.java` — base element class
- `client/gui/element/GuiProgress.java` — progress bar with enum types
- `client/gui/element/GuiPowerBar.java` — energy bar with tooltip
- `client/gui/element/gauge/GuiGauge.java` — 183 lines, fluid/gas rendering
- `client/gui/GuiSideConfiguration.java` — 189 lines, config popup

### JEI (cloned to /home/claude/jei-src)
- `api/IGuiHelper.java` — drawable creation API
- `api/gui/IGuiFluidStackGroup.java` — fluid rendering in recipes
- `plugins/vanilla/furnace/FurnaceSmeltingCategory.java` — reference category
- `gui/elements/DrawableAnimated.java` — animation implementation

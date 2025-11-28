package com.Pink_Cats.createfastschematiccannon.mixin;

import com.Pink_Cats.createfastschematiccannon.Config;
import com.Pink_Cats.createfastschematiccannon.Createfastschematiccannon;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonInventory;
import com.simibubi.create.content.schematics.cannon.SchematicannonMenu;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.simibubi.create.infrastructure.config.CSchematics;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.MAX_ANCHOR_DISTANCE;
import static com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.NEIGHBOUR_CHECKING;

import java.util.List;

@Mixin(value = SchematicannonBlockEntity.class,remap = false)
public class SchematicCannonBlockEntityMixin extends SmartBlockEntity implements MenuProvider {

    @Unique
    public boolean createfastschematiccannon$MissingTick =true;
    @Unique
    public int createfastschematiccannon$MissingCount = 0;

    @Shadow
    public SchematicannonBlockEntity.State state;

    @Shadow
    public int neighbourCheckCooldown;

    public SchematicCannonBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    public void findInventories() {}

    @Shadow
    public boolean firstRenderTick;

    @Shadow
    public BlockPos previousTarget;

    @Shadow
    public int blocksToPlace;

    @Shadow
    public SchematicPrinter printer;

    @Shadow
    protected void tickFlyingBlocks() {}

    @Shadow
    protected void tickPaperPrinter() {}

    @Shadow
    public boolean sendUpdate;

    @Shadow
    private int skipsLeft;

    @Shadow
    public int blocksPlaced;

    @Shadow
    private boolean blockSkipped;

    @Shadow
    public float schematicProgress;

    @Shadow
    protected void refillFuelIfPossible() {}

    @Shadow
    protected void tickPrinter() {}




    @Inject(method = "tick" ,at=@At("HEAD" ),cancellable = true,remap = false)
    public void tick(CallbackInfo ci){
        //if (createfastschematiccannon$MissingCount !=0){

        //}


        if (createfastschematiccannon$MissingCount > (Config.lazyTick)){
            createfastschematiccannon$MissingTick = false;
            createfastschematiccannon$MissingCount = 0;
        }

        if (createfastschematiccannon$MissingTick){
            createfastschematiccannon$MissingCount +=1;
            ci.cancel();
            return;

        }
        super.tick();
        for (int x = 0; x < Config.SchematicSpeedupPerTick; x++) {
            if (!createfastschematiccannon$MissingTick){

                if (state != SchematicannonBlockEntity.State.STOPPED && neighbourCheckCooldown-- <= 0) {
                    neighbourCheckCooldown = NEIGHBOUR_CHECKING;
                    findInventories();
                }


                firstRenderTick = true;
                previousTarget = printer.getCurrentTarget();
                tickFlyingBlocks();

                if (level.isClientSide)
                {ci.cancel();
                    return;}

                // Update Fuel and Paper
                tickPaperPrinter();
                refillFuelIfPossible();

                // Update Printer
                skipsLeft = 1000;
                blockSkipped = true;
                while (blockSkipped && skipsLeft-- > 0) {
                    tickPrinter();
                }

                schematicProgress = 0;
                if (blocksToPlace > 0)
                    schematicProgress = (float) blocksPlaced / blocksToPlace;

                // Update Client block entity
                if (sendUpdate) {
                    sendUpdate = false;
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 6);
                }
            }
        }
        ci.cancel();
    }

    /**
     * @author PinkCats
     * @reason for crush
     */
    @Overwrite
    public void addBehaviours(List<BlockEntityBehaviour> list) {}

    /**
     * @author PinkCats
     * @reason for crush
     */
    @Overwrite
    public Component getDisplayName() {
        return CreateLang.translateDirect("gui.schematicannon.title");
    }

    /**
     * @author PinkCats
     * @reason for crush
     */
    @Overwrite
    public AbstractContainerMenu createMenu (int id, Inventory inv, Player player) {
        return SchematicannonMenu.create(id, inv, (SchematicannonBlockEntity) (Object) this);
    }


    @Shadow
    public SchematicannonInventory inventory;

    @Shadow
    public String statusMsg;


    @Shadow
    protected void resetPrinter() {}

    @Shadow
    public boolean positionNotLoaded;

    @Shadow
    public ItemStack missingItem;

    @Shadow
    private int printerCooldown;

    @Shadow
    public int remainingFuel;

    @Shadow
    public boolean hasCreativeCrate;

    @Shadow
    protected void launchEntity(BlockPos target, ItemStack stack, Entity entity) {}

    @Shadow
    public CSchematics config() {
        return AllConfigs.server().schematics;
    }

    @Shadow
    public void finishedPrinting() {}

    @Shadow
    protected void launchBlockOrBelt(BlockPos target, ItemStack icon, BlockState blockState, BlockEntity blockEntity) {}

    @Shadow
    protected boolean shouldPlace(BlockPos pos, BlockState state, BlockEntity be, BlockState toReplace,
                                  BlockState toReplaceOther, boolean isNormalCube) {return false;}

    @Shadow
    protected boolean grabItemsFromAttachedInventories(ItemRequirement.StackRequirement required, boolean simulate) {return false;}

    @Shadow
    public boolean skipMissing;


    @Shadow
    protected void initializePrinter(ItemStack blueprint) {}


    @Unique
    private boolean createfastschematiccannon$IsNotLoad = false;

    @Inject(method = "initializePrinter", at = @At("HEAD"), cancellable = true)
    protected void initializePrinterInject(ItemStack blueprint, CallbackInfo ci) {

        //System.out.println(createfastschematiccannon$IsNotLoad);


        if (blueprint.isEmpty())
            createfastschematiccannon$IsNotLoad = false;


        if (createfastschematiccannon$IsNotLoad) {
            statusMsg = "targetOutsideRange";
            ci.cancel();
            return;
        }

        if (!blueprint.hasTag()) {
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "schematicInvalid";
            sendUpdate = true;
            ci.cancel();
            return;
        }

        if (!blueprint.getTag()
                .getBoolean("Deployed")) {
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "schematicNotPlaced";
            sendUpdate = true;
            ci.cancel();
            return;
        }

        // Load blocks into reader
        printer.loadSchematic(blueprint, level, true);
        if (printer.isErrored()) {
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "schematicErrored";
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get()));
            printer.resetSchematic();
            sendUpdate = true;
            ci.cancel();
            return;
        }

        if (printer.isWorldEmpty()) {
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "schematicExpired";
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            inventory.setStackInSlot(1, new ItemStack(AllItems.EMPTY_SCHEMATIC.get()));
            printer.resetSchematic();
            sendUpdate = true;
            ci.cancel();
            return;
        }

        if (!printer.getAnchor()
                .closerThan(getBlockPos(), MAX_ANCHOR_DISTANCE)) {
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "targetOutsideRange";
            createfastschematiccannon$IsNotLoad = true;
            printer.resetSchematic();
            sendUpdate = true;
            ci.cancel();
            return;
        }

        state = SchematicannonBlockEntity.State.PAUSED;
        statusMsg = "ready";
        updateChecklist();
        sendUpdate = true;
        blocksToPlace += blocksPlaced;
        ci.cancel();
    }



    @Shadow
    public void updateChecklist() {}





    @Inject(method = "tickPrinter", at = @At("HEAD"), cancellable = true)
    protected void injectTickPrinter(CallbackInfo info) {

        ItemStack blueprint = inventory.getStackInSlot(0);
        blockSkipped = false;

        if (blueprint.isEmpty() && !statusMsg.equals("idle") && inventory.getStackInSlot(1)
                .isEmpty()) {
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "idle";
            sendUpdate = true;
            info.cancel(); // 取消原始方法的执行
            return;
        }

        // Skip if not Active
        if (state == SchematicannonBlockEntity.State.STOPPED) {
            if (printer.isLoaded()) {
                resetPrinter();
            }
            info.cancel(); // 取消原始方法的执行
            return;
        }

        if (state == SchematicannonBlockEntity.State.PAUSED && !positionNotLoaded && missingItem == null && remainingFuel > 0) {
            info.cancel(); // 取消原始方法的执行
            return;
        }

        // Initialize Printer
        if (!printer.isLoaded()) {
            initializePrinter(blueprint);
            info.cancel(); // 取消原始方法的执行
            return;
        }

        // Cooldown from last shot
        if (printerCooldown > 0) {
            printerCooldown--;
            info.cancel(); // 取消原始方法的执行
            return;
        }

        // Check Fuel
        if (remainingFuel <= 0 && !hasCreativeCrate) {
            refillFuelIfPossible();
            if (remainingFuel <= 0) {
                state = SchematicannonBlockEntity.State.PAUSED;
                statusMsg = "noGunpowder";
                createfastschematiccannon$MissingTick = true;

                sendUpdate = true;
                info.cancel(); // 取消原始方法的执行
                return;
            }
        }

        if (hasCreativeCrate) {
            remainingFuel = 0;
            if (missingItem != null) {
                missingItem = null;
                state = SchematicannonBlockEntity.State.RUNNING;
            }
        }

        // Update Target
        if (missingItem == null && !positionNotLoaded) {
            if (!printer.advanceCurrentPos()) {
                finishedPrinting();
                info.cancel(); // 取消原始方法的执行
                return;
            }
            sendUpdate = true;
        }

        // Check block
        if (!getLevel().isLoaded(printer.getCurrentTarget())) {
            positionNotLoaded = true;
            statusMsg = "targetNotLoaded";
            state = SchematicannonBlockEntity.State.PAUSED;
            info.cancel(); // 取消原始方法的执行
            return;
        } else {
            if (positionNotLoaded) {
                positionNotLoaded = false;
                state = SchematicannonBlockEntity.State.RUNNING;
            }
        }

        // Get item requirement
        ItemRequirement requirement = printer.getCurrentRequirement();
        if (requirement.isInvalid() || !printer.shouldPlaceCurrent(level, this::shouldPlace)) {
            sendUpdate = !statusMsg.equals("searching");
            statusMsg = "searching";
            blockSkipped = true;
            info.cancel();
            return;
        }

        // Find item
        List<ItemRequirement.StackRequirement> requiredItems = requirement.getRequiredItems();
        if (!requirement.isEmpty()) {
            for (ItemRequirement.StackRequirement required : requiredItems) {
                if (!grabItemsFromAttachedInventories(required, true)) {
                    if (skipMissing) {
                        statusMsg = "skipping";
                        blockSkipped = true;
                        if (missingItem != null) {
                            missingItem = null;
                            state = SchematicannonBlockEntity.State.RUNNING;
                        }
                        info.cancel();
                        return;
                    }

                    missingItem = required.stack;
                    state = SchematicannonBlockEntity.State.PAUSED;
                    statusMsg = "missingBlock";
                    createfastschematiccannon$MissingTick = true;
                    info.cancel();
                    return;
                }
            }

            for (ItemRequirement.StackRequirement required : requiredItems) {
                grabItemsFromAttachedInventories(required, false);
            }
        }

        state = SchematicannonBlockEntity.State.RUNNING;
        ItemStack icon = requirement.isEmpty() || requiredItems.isEmpty() ? ItemStack.EMPTY : requiredItems.get(0).stack;
        printer.handleCurrentTarget(
                (target, blockState, blockEntity) -> {


                    if (level != null) {
                        BlockState targetstate = level.getBlockState(target);
                        String targetstring = targetstate.getBlock().
                                toString().replaceAll("Block\\{(.*?)\\}", "$1");
                        boolean IsUnBreakable = Config.blocks_unbreak.stream().anyMatch(targetstring::equals);
                        if (IsUnBreakable) {
                            if (Config.enable_debug){
                                Createfastschematiccannon.LOGGER.info("Block [{}] in [{},{},{}] is forbid by CFC",targetstring,target.getX(),target.getY(),target.getZ());
                            }
                            info.cancel();
                            return;
                        }
                    }

            statusMsg = blockState.getBlock() != Blocks.AIR ? "placing" : "clearing";
            launchBlockOrBelt(target, icon, blockState, blockEntity);
        }, (target, entity) -> {
            // Launch entity
            statusMsg = "placing";
            launchEntity(target, icon, entity);
        });

        printerCooldown = config().schematicannonDelay.get();
        remainingFuel -= 1;
        sendUpdate = true;
        missingItem = null;
    }

    public boolean isNotLoad() {
        return createfastschematiccannon$IsNotLoad;
    }
}

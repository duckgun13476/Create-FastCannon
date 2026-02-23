package com.Pink_Cats.createfastschematiccannon.mixin;

import com.Pink_Cats.createfastschematiccannon.Config;
import com.Pink_Cats.createfastschematiccannon.Createfastschematiccannon;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonInventory;
import com.simibubi.create.content.schematics.cannon.SchematicannonMenu;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.Pink_Cats.createfastschematiccannon.Config.enable_gunpowder_blocks_compat;
import static com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.MAX_ANCHOR_DISTANCE;
import static com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.NEIGHBOUR_CHECKING;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Mixin(value = SchematicannonBlockEntity.class,remap = false)
public abstract class SchematicCannonBlockEntityMixin extends SmartBlockEntity implements MenuProvider {

    public SchematicCannonBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique public boolean createfastschematiccannon$MissingTick =true;
    @Unique public int createfastschematiccannon$MissingCount = 0;

    @Shadow public SchematicannonBlockEntity.State state;
    @Shadow public int neighbourCheckCooldown;
    @Shadow public abstract void findInventories();
    @Shadow public boolean firstRenderTick;
    @Shadow public BlockPos previousTarget;
    @Shadow public int blocksToPlace;
    @Shadow public SchematicPrinter printer;
    @Shadow protected abstract void tickFlyingBlocks();
    @Shadow protected abstract void tickPaperPrinter();
    @Shadow public boolean sendUpdate;
    @Shadow private int skipsLeft;
    @Shadow public int blocksPlaced;
    @Shadow private boolean blockSkipped;
    @Shadow public float schematicProgress;
    @Shadow protected abstract void refillFuelIfPossible();
    @Shadow protected abstract void tickPrinter();


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


    @Shadow public SchematicannonInventory inventory;
    @Shadow public String statusMsg;
    @Shadow public int remainingFuel;
    @Shadow public boolean hasCreativeCrate;

    @Unique private boolean createfastschematiccannon$IsNotLoad = false;

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

        if (!blueprint.has(AllDataComponents.SCHEMATIC_ANCHOR)) {
            state = SchematicannonBlockEntity.State.STOPPED;
            statusMsg = "schematicInvalid";
            sendUpdate = true;
            ci.cancel();
            return;
        }

        if (!blueprint.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false)) {
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


    @Shadow public abstract void updateChecklist();

    @Inject(
            method = "tickPrinter",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/schematics/cannon/SchematicannonBlockEntity;refillFuelIfPossible()V",
                    shift =  At.Shift.AFTER
            ),
            remap = false
    )
    protected void activateTickDelay(CallbackInfo info) {
        if (remainingFuel <= 0) {
            createfastschematiccannon$MissingTick = true;
        }
    }


    @Inject(
            method = "tickPrinter",
            at = @At(
                    value = "FIELD",
                    target = "Lcom/simibubi/create/content/schematics/cannon/SchematicannonBlockEntity;statusMsg:Ljava/lang/String;",
                    shift =  At.Shift.AFTER,
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 5
            ),
            remap = false
    )
    protected void injectTickPrinterFuelCheck(CallbackInfo info) {
        createfastschematiccannon$MissingTick = true;
    }


    @Inject(
            method = "tickPrinter",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lcom/simibubi/create/content/schematics/SchematicPrinter;shouldPlaceCurrent(Lnet/minecraft/world/level/Level;Lcom/simibubi/create/content/schematics/SchematicPrinter$PlacementPredicate;)Z"
            ),
            remap = false
    )
    protected void injectTickPrinter(CallbackInfo info) {
    }

    @Redirect(
            method = "tickPrinter",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/schematics/SchematicPrinter;handleCurrentTarget(Lcom/simibubi/create/content/schematics/SchematicPrinter$BlockTargetHandler;Lcom/simibubi/create/content/schematics/SchematicPrinter$EntityTargetHandler;)V"
            ),
            remap = false)
    protected void redirectHandleCurrentTarget(
            SchematicPrinter instance,
            SchematicPrinter.BlockTargetHandler originalBlockHandler,
            SchematicPrinter.EntityTargetHandler originalEntityHandler) {

        SchematicPrinter.BlockTargetHandler wrappedBlockHandler = (target, blockState, blockEntity) -> {

            if (level != null) {
                BlockState TargetState = level.getBlockState(target);
                String targetstring = TargetState.getBlock()
                        .toString().replaceAll("Block\\{(.*?)\\}", "$1");
                boolean IsUnBreakable = Config.blocks_unbreak.stream().anyMatch(targetstring::equals);
                if (IsUnBreakable) {
                    if (Config.enable_debug) {
                        Createfastschematiccannon.LOGGER.info(
                                "Block [{}] in [{},{},{}] is forbid by CFC",
                                targetstring, target.getX(), target.getY(), target.getZ()
                        );
                    }
                    this.blockSkipped = true;
                    this.statusMsg = "searching";
                    return;
                }
            }

            originalBlockHandler.handle(target, blockState, blockEntity);
        };

        // Vanilla handleCurrentTarget method
        instance.handleCurrentTarget(wrappedBlockHandler, originalEntityHandler);
    }



    @Shadow public abstract int getShotsPerGunpowder();
    @Shadow public LinkedHashSet<IItemHandler> attachedInventories;

    @Unique private static final List<String> createfastschematiccannon$charge;

    static {
        createfastschematiccannon$charge = new ArrayList<>();
        createfastschematiccannon$charge.add("block.cratedelight.gunpowder_bag");
    }

    @Inject(method = "refillFuelIfPossible" ,at=@At("HEAD" ),cancellable = true,remap = false)
    protected void refillFuelIfPossible(CallbackInfo ci) {

        if (!enable_gunpowder_blocks_compat)
            return;

        if (hasCreativeCrate) {
            ci.cancel();
            return;
        }
        if (remainingFuel > getShotsPerGunpowder()) {
            remainingFuel = getShotsPerGunpowder();
            sendUpdate = true;
            ci.cancel();
            return;
        }

        if (remainingFuel > 0) {
            ci.cancel();
            return;
        }

        if (!inventory.getStackInSlot(4)
                .isEmpty())
            inventory.getStackInSlot(4)
                    .shrink(1);
        else {
            boolean externalGunpowderFound = false;
            for (IItemHandler cap : attachedInventories) {
                IItemHandler itemHandler = cap;
                ItemStack cache = new ItemStack(Items.GUNPOWDER,0);
                boolean HasCharged = false;
                for (int i = 0; i < itemHandler.getSlots(); i++) {
                    ItemStack stack = itemHandler.getStackInSlot(i);
                    if (stack.getCount() > 0){
                        String id = stack.getItem().getDescriptionId();
                        if (createfastschematiccannon$charge.contains(id)){
                            ItemStack extracted = itemHandler.extractItem(i, 1, false);
                            if (!extracted.isEmpty()) {
                                cache.setCount(9);
                                HasCharged = true;
                                break;
                            }
                        }
                    }
                }

                if (HasCharged) {
                    ItemStack slot4Stack = inventory.getStackInSlot(4);
                    if (slot4Stack.isEmpty()) {
                        slot4Stack = cache.copy();
                        inventory.setStackInSlot(4, slot4Stack);
                    } else {
                        slot4Stack.grow(cache.getCount());
                        inventory.setStackInSlot(4, slot4Stack);
                    }
                }


                if (ItemHelper.extract(itemHandler, stack -> inventory.isItemValid(4, stack), 1, false)
                        .isEmpty())
                    continue;
                externalGunpowderFound = true;
                break;
            }
            if (!externalGunpowderFound) {
                ci.cancel();
                return;
            }
        }

        remainingFuel += getShotsPerGunpowder();
        if (statusMsg.equals("noGunpowder")) {
            if (blocksPlaced > 0)
                state = SchematicannonBlockEntity.State.RUNNING;
            statusMsg = "ready";
        }
        sendUpdate = true;
        ci.cancel();
    }
}

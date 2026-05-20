package de.tomalbrc.filament.recipe;

import de.tomalbrc.filament.util.ExecuteUtil;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public class StationGui extends SimpleGui {
    private final StationDef station;
    private final StationBlockEntity blockEntity;
    private Runnable onClose;
    private int outputSlotIndex = -1;
    private ItemStack lastPending = ItemStack.EMPTY;

    public StationGui(ServerPlayer player, StationDef station, StationBlockEntity blockEntity) {
        super(station.menuType(), player, false);
        this.station = station;
        this.blockEntity = blockEntity;

        var s = this.getSize();
        for (int i = 0; i < s; i++) {
            setSlot(i, GuiElementBuilder.from(station.backgroundItemStack()).hideTooltip().model(Items.AIR));
        }

        station.decorations().ifPresent(map -> {
            map.forEach((key, value) -> {
                var builder = new GuiElementBuilder(value.item().create());
                value.command().ifPresent(command -> {
                    builder.setCallback(() -> {
                        ExecuteUtil.asPlayer(player, player.position(), command);
                    });
                });
                setSlot(key, builder);
            });
        });

        setupSlots();
    }

    private void setupSlots() {
        for (StationDef.SlotDef slotDef : station.slots()) {
            int slotIndex = slotDef.slotIndex();
            boolean isOutput = slotDef.role() == StationDef.SlotRole.OUTPUT;

            if (isOutput) {
                outputSlotIndex = slotIndex;
                createOutputSlot();
            } else {
                this.setSlot(slotIndex, new Slot(blockEntity.getInventory(), slotIndex, 0, 0));
            }
        }
    }

    private void createOutputSlot() {
        if (outputSlotIndex == -1) return;

        if (blockEntity.getDef().processingTime() > 0) {
            this.setSlot(outputSlotIndex, new Slot(blockEntity.getInventory(), outputSlotIndex, 0, 0) {
                @Override
                public boolean mayPlace(@NonNull ItemStack stack) {
                    return false;
                }

                @Override
                public @NonNull ItemStack getItem() {
                    var item = super.getItem();
                    if (item.isEmpty()) {
                        var stack = Items.PAPER.getDefaultInstance();
                        stack.set(DataComponents.ITEM_MODEL, Items.AIR.components().get(DataComponents.ITEM_MODEL));
                        stack.set(DataComponents.MAX_STACK_SIZE, 1);
                        stack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
                        return stack;
                    }

                    return item;
                }
            });
        }
        else {
            var out = blockEntity.getPendingOutput();
            if (out.isEmpty()) {
                out = Items.PAPER.getDefaultInstance();
                out.set(DataComponents.ITEM_MODEL, Items.AIR.components().get(DataComponents.ITEM_MODEL));
                out.set(DataComponents.MAX_STACK_SIZE, 1);
                out.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
            }

            this.setSlot(outputSlotIndex, out, (_, clickType, _, _) -> {
                ServerPlayer player = getPlayer();
                if (blockEntity.getPendingOutput().isEmpty()) {
                    return;
                }

                if (clickType.isLeft) {
                    if (clickType.shift) {
                        blockEntity.tryCraftAll(player);
                        createOutputSlot();
                    } else {
                        ItemStack result = blockEntity.tryCraftOne();
                        if (!result.isEmpty()) {
                            ItemStack cursor = player.containerMenu.getCarried();
                            if (cursor.isEmpty()) {
                                player.containerMenu.setCarried(result);
                            } else if (ItemStack.isSameItemSameComponents(cursor, result) && cursor.getCount() + result.getCount() <= cursor.getMaxStackSize()) {
                                cursor.grow(result.getCount());
                                player.containerMenu.setCarried(cursor);
                            }

                            createOutputSlot();
                        }
                    }
                } else if (clickType.isRight) {
                    ItemStack result = blockEntity.tryCraftOne();
                    if (!result.isEmpty()) {
                        ItemStack cursor = player.containerMenu.getCarried();
                        if (cursor.isEmpty()) {
                            player.containerMenu.setCarried(result);
                        } else if (ItemStack.isSameItemSameComponents(cursor, result) &&
                                cursor.getCount() + 1 <= cursor.getMaxStackSize()) {
                            cursor.grow(1);
                            player.containerMenu.setCarried(cursor);
                        } else {
                            if (!player.getInventory().add(result)) {
                                player.drop(result, false);
                            }
                        }
                        createOutputSlot();
                    }
                }
            });
        }
    }



    @Override
    public void onTick() {
        super.onTick();

        if (outputSlotIndex != -1) {
            ItemStack currentPending = blockEntity.getPendingOutput();
            if (currentPending != lastPending) {
                lastPending = currentPending;
                createOutputSlot();
            }
        }

        if (blockEntity.getDef() != null && (blockEntity.getDef().menuType() == MenuType.FURNACE || blockEntity.getDef().menuType() == MenuType.BLAST_FURNACE || blockEntity.getDef().menuType() == MenuType.SMOKER)) {
            int cook = blockEntity.getCookProgress();
            int burn = blockEntity.getBurnProgress();
            player.connection.send(new ClientboundContainerSetDataPacket(getSyncId(), 0, burn));
            player.connection.send(new ClientboundContainerSetDataPacket(getSyncId(), 1, 100));
            player.connection.send(new ClientboundContainerSetDataPacket(getSyncId(), 2, cook));
            player.connection.send(new ClientboundContainerSetDataPacket(getSyncId(), 3, 100));
        }
    }

    @Override
    public void onPlayerClose(boolean success) {

        if (onClose != null) onClose.run();
        super.onPlayerClose(success);

        if (!blockEntity.getDef().persistent()) {
            giveOrDrop(player, blockEntity.getInventory().getItems(), false);
        }
    }

    public static void giveOrDrop(ServerPlayer player, Collection<ItemStack> items, boolean playSound) {
        for (ItemStack itemStack : items) {
            if (player.addItem(itemStack)) {
                if (playSound) player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
                continue;
            }

            ItemEntity drop = player.drop(itemStack, false);
            if (drop == null)
                continue;

            drop.setNoPickUpDelay();
        }
    }

    public void open(Runnable onClose) {
        this.onClose = onClose;
        this.open();
    }
}
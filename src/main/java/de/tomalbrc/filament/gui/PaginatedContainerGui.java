package de.tomalbrc.filament.gui;

import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.Util;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PaginatedContainerGui extends SimpleGui {
    GuiElementBuilder empty = GuiElementBuilder.from(Items.LIGHT_GRAY_STAINED_GLASS_PANE.getDefaultInstance())
            .hideDefaultTooltip()
            .setComponent(DataComponents.MAX_STACK_SIZE, 1)
            .hideTooltip();
    // TODO: 1.21.1
            //.model(FilamentConfig.getInstance().addCustomMenuAssets ? Util.id("blank_gui") : Items.AIR.components().get(DataComponents.ITEM_MODEL));

    private final Container container;
    private final boolean fromBackpack;
    private int currentPage = 0;

    public PaginatedContainerGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots, Container container, boolean fromBackpack) {
        super(type, player, manipulatePlayerSlots);
        this.container = container;
        this.fromBackpack = fromBackpack;
        populateFromContainer(currentPage);
    }

    private int slotsPerPage() {
        int w = this.getWidth();
        int h = this.getHeight() - 1;
        return w*h;
    }

    private void populateButtons() {
        int pages = Math.max(0, (this.container.getContainerSize() - 1) / slotsPerPage());
        for (int i = 0; i < getWidth(); i++) {
            this.setSlot(GuiHelpers.posToIndex(i, getHeight() - 1, getHeight(), getWidth()), empty.build());
        }

        // prev
        int prevIdx = GuiHelpers.posToIndex(3, getHeight() - 1, getHeight(), getWidth());
        if (currentPage > 0) this.setSlot(
                prevIdx,
                GuiElementBuilder.from(Items.ARROW.getDefaultInstance())
                        .hideDefaultTooltip()
                        .setName(Component.translatable("spectatorMenu.previous_page"))
                        .addLoreLine(Component.translatable("book.pageIndicator", Component.literal(String.valueOf(currentPage+1)), Component.literal(String.valueOf(pages+1))).withStyle(ChatFormatting.DARK_GRAY))
                        //.model(Util.id("previous_gui"))
                        .setCallback(() -> {
                            Util.clickSound(player);
                            if (currentPage > 0) {
                                populateFromContainer(currentPage - 1);
                            }
                        })
        );

        // next
        int nextIdx = GuiHelpers.posToIndex(getWidth()-4, getHeight() - 1, getHeight(), getWidth());
        if (currentPage < pages) this.setSlot(
                nextIdx,
                GuiElementBuilder.from(Items.ARROW.getDefaultInstance())
                        .hideDefaultTooltip()
                        .setName(Component.translatable("spectatorMenu.next_page"))
                        .addLoreLine(Component.translatable("book.pageIndicator", Component.literal(String.valueOf(currentPage+1)), Component.literal(String.valueOf(pages+1))).withStyle(ChatFormatting.DARK_GRAY))
                        //.model(Util.id("next_gui"))
                        .setCallback(() -> {
                            Util.clickSound(player);
                            int slotsPerPage = getWidth() * (getHeight() - 1);
                            int maxPage = Math.max(0, (container.getContainerSize() - 1) / slotsPerPage);
                            if (currentPage < maxPage) {
                                populateFromContainer(currentPage + 1);
                            }
                        })
        );
    }

    private void populateFromContainer(int page) {
        boolean hasPages = getHeight() > 1 && container.getContainerSize() > getWidth()*getHeight();

        int w = this.getWidth();
        int h = this.getHeight() - (hasPages ? 1 : 0); // bottom row for buttons

        int containerSize = this.container.getContainerSize();
        int slotsPerPage = slotsPerPage();
        int startIndex = page * slotsPerPage;

        if (startIndex >= containerSize) {
            currentPage = Math.max(0, (containerSize - 1) / slotsPerPage);
            startIndex = currentPage * slotsPerPage;
        } else if (page < 0) {
            currentPage = 0;
            startIndex = 0;
        } else {
            currentPage = page;
        }

        int xOffset = 0;
        boolean singleRowMode = h == 1 && containerSize <= w;
        if (singleRowMode) {
            xOffset = (w - containerSize) / 2;
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int slotInGui = GuiHelpers.posToIndex(x, y, h, w);

                int slotInContainer;
                if (singleRowMode) {
                    int adjustedX = x - xOffset;
                    slotInContainer = (adjustedX >= 0 && adjustedX < containerSize) ? adjustedX : -1;
                } else {
                    slotInContainer = startIndex + (y * w + x);
                }

                if (slotInContainer >= 0 && slotInContainer < containerSize) {
                    this.setSlotRedirect(slotInGui, createSlot(container, slotInContainer));
                } else {
                    this.setSlot(slotInGui, empty.build());
                }
            }
        }

        if (hasPages) populateButtons();
    }

    public void setScreenHandler(VirtualChestMenu virtualChestMenu) {
        screenHandler = virtualChestMenu;
    }

    public Container getContainer() {
        return container;
    }

    public Slot createSlot(Container containerx, int slotInContainer) {
        return new LimitingContainerSlot(containerx, slotInContainer, 0, 0, 0, this.fromBackpack) {
            @Override
            public int getMaxStackSize(ItemStack itemStack) {
                return containerx instanceof FilamentContainer filamentContainer ? filamentContainer.getMaxStackSize(slotInContainer) : containerx.getMaxStackSize(itemStack);
            }
        };
    }
}

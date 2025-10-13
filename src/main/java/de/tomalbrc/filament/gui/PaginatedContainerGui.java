package de.tomalbrc.filament.gui;

import de.tomalbrc.filament.util.FilamentContainer;
import de.tomalbrc.filament.util.Util;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PaginatedContainerGui extends SimpleGui {
    GuiElementBuilder empty = GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
            .hideDefaultTooltip()
            .hideTooltip()
            .model(Util.id("blank_gui"));

    private final Container container;
    private int currentPage = 0;

    public PaginatedContainerGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots, Container container) {
        super(type, player, manipulatePlayerSlots);

        this.container = container;

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
                GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
                        .hideDefaultTooltip()
                        .setName(Component.translatable("book.page_button.previous"))
                        .addLoreLine(Component.translatable("book.pageIndicator", Component.literal(String.valueOf(currentPage+1)), Component.literal(String.valueOf(pages+1))).withStyle(ChatFormatting.DARK_GRAY))
                        .model(Util.id("previous_gui"))
                        .setCallback(() -> {
                            if (currentPage > 0) {
                                populateFromContainer(currentPage - 1);
                            }
                        })
        );

        // next
        int nextIdx = GuiHelpers.posToIndex(getWidth()-4, getHeight() - 1, getHeight(), getWidth());
        if (currentPage < pages) this.setSlot(
                nextIdx,
                GuiElementBuilder.from(Items.PAPER.getDefaultInstance())
                        .hideDefaultTooltip()
                        .setName(Component.translatable("book.page_button.next"))
                        .addLoreLine(Component.translatable("book.pageIndicator", Component.literal(String.valueOf(currentPage+1)), Component.literal(String.valueOf(pages+1))).withStyle(ChatFormatting.DARK_GRAY))
                        .model(Util.id("next_gui"))
                        .setCallback(() -> {
                            int slotsPerPage = getWidth() * (getHeight() - 1);
                            int maxPage = Math.max(0, (container.getContainerSize() - 1) / slotsPerPage);
                            if (currentPage < maxPage) {
                                populateFromContainer(currentPage + 1);
                            }
                        })
        );
    }

    private void populateFromContainer(int page) {
        int w = this.getWidth();
        int h = this.getHeight() - (getHeight() == 1 ? 0 : 1); // bottom row for buttons

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

        if (getHeight() > 1) populateButtons();
    }

    public void setScreenHandler(VirtualChestMenu virtualChestMenu) {
        screenHandler = virtualChestMenu;
    }

    public Container getContainer() {
        return container;
    }

    public static Slot createSlot(Container container, int slotInContainer) {
        return new Slot(container, slotInContainer, 0, 0) {
            @Override
            public int getMaxStackSize(ItemStack itemStack) {
                return container instanceof FilamentContainer filamentContainer ? filamentContainer.getMaxStackSize(slotInContainer) : container.getMaxStackSize(itemStack);
            }

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                if (FilamentContainer.isPickUpContainer(container))
                    return itemStack.getItem().canFitInsideContainerItems();

                return super.mayPlace(itemStack);
            }
        };
    }
}

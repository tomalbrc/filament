package de.tomalbrc.filament.recipe;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class StationBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer,
        StackedContentsCompatible,
        RecipeCraftingHolder {
    private final SimpleContainer inventory;
    private int progress = 0;
    private int burnTime = 0;
    private int fuelTime = 0;
    private ItemStack currentFuel = ItemStack.EMPTY;
    private StationRecipe currentRecipe = null;
    private final Identifier stationId;
    private final StationDef def;
    private final Set<Integer> fuelSlots;
    private ItemStack pendingOutput = ItemStack.EMPTY;

    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap<>();
    private final Map<Integer, ItemStack> pendingOutputs = new Int2ObjectOpenHashMap<>();

    public StationBlockEntity(BlockPos pos, BlockState state, Identifier stationId) {
        super(WorkstationBehaviour.BLOCK_ENTITY_TYPES.get(state.getBlock()), pos, state);
        this.stationId = stationId;
        this.def = Workstations.get(stationId);
        int slotCount = def.slots().stream().mapToInt(StationDef.SlotDef::slotIndex).max().orElse(0) + 1;
        this.inventory = new SimpleContainer(slotCount);
        this.fuelSlots = def.slots().stream()
                .filter(s -> s.role() == StationDef.SlotRole.FUEL)
                .map(StationDef.SlotDef::slotIndex)
                .collect(Collectors.toSet());
    }

    public ItemStack getPendingOutputForSlot(int slotIndex) {
        return pendingOutputs.getOrDefault(slotIndex, ItemStack.EMPTY);
    }

    public StationDef getDef() {
        return def;
    }

    public void tick() {
        if (level == null || level.isClientSide()) return;

        boolean changed = false;
        boolean wasLit = fuelTime > 0;
        BlockState state = getBlockState();

        boolean hasFuelSlot = !fuelSlots.isEmpty();
        ItemStack virtualFuel = (burnTime > 0 && !currentFuel.isEmpty()) ? currentFuel : ItemStack.EMPTY;
        StationRecipeInput input = new StationRecipeInput(inventory, fuelSlots, virtualFuel);

        StationRecipe match = ((ServerLevel) level).recipeAccess()
                .getRecipeFor(Workstations.STATION_RECIPE_TYPE, input, level)
                .filter(r -> r.value().stationId().equals(stationId))
                .map(RecipeHolder::value)
                .orElse(null);

        if (currentRecipe != null && (!currentRecipe.matches(input, level) || !canPlaceOutputs(currentRecipe))) {
            currentRecipe = null;
            progress = 0;
            pendingOutput = ItemStack.EMPTY;
        }

        if (currentRecipe == null && match != null && canPlaceOutputs(match)) {
            currentRecipe = match;
            progress = 0;

            // only for instant stations
            if (match.processingTime() == 0) {
                pendingOutputs.clear();
                for (var entry : match.results().entrySet()) {
                    pendingOutputs.put(entry.getKey(), entry.getValue().create());
                }

                pendingOutput = pendingOutputs.values().iterator().next();
            } else {
                pendingOutputs.clear();
                pendingOutput = ItemStack.EMPTY;
            }

            changed = true;
        }

        if (hasFuelSlot) {
            if (burnTime > 0) burnTime--;
            if (burnTime <= 0) {
                Optional<Integer> fuelSlot = fuelSlots.stream().findFirst();
                if (fuelSlot.isPresent() && currentRecipe != null) {
                    ItemStack fuelStack = inventory.getItem(fuelSlot.get());
                    int burnTicks = level.fuelValues().burnDuration(fuelStack);
                    if (burnTicks > 0) {
                        fuelTime = burnTicks;
                        burnTime = burnTicks;
                        currentFuel = fuelStack.copy();
                        fuelStack.shrink(1);
                        changed = true;
                    } else {
                        currentFuel = ItemStack.EMPTY;
                        fuelTime = 0;
                    }
                }
            }
        }

        if (currentRecipe != null && currentRecipe.processingTime() > 0) {
            boolean canProgress = (!hasFuelSlot || burnTime > 0)
                    && currentRecipe.matches(input, level)
                    && canPlaceOutputs(currentRecipe);
            if (canProgress) {
                progress++;
                if (progress >= currentRecipe.processingTime()) {
                    craft(currentRecipe, input);
                    progress = 0;
                    pendingOutput = ItemStack.EMPTY;
                    currentRecipe = null;
                    changed = true;
                }
            } else {
                progress = 0;

                if (!pendingOutput.isEmpty()) {
                    pendingOutput = ItemStack.EMPTY;
                    changed = true;
                }
            }
        }

        if (burnTime <= 0 && hasFuelSlot && fuelTime != 0) {
            fuelTime = 0;
            currentFuel = ItemStack.EMPTY;
        }

        var isLit = fuelTime > 0;
        if (wasLit != isLit && state.hasProperty(BlockStateProperties.LIT)) {
            changed = true;
            state = state.setValue(BlockStateProperties.LIT, isLit);
            level.setBlock(this.worldPosition, state, 3);
        }

        if (changed) {
            setChanged(level, worldPosition, state);
        }
    }

    public ItemStack tryCraftOne() {
        if (level == null || level.isClientSide()) return ItemStack.EMPTY;
        if (currentRecipe == null || currentRecipe.processingTime() != 0) return ItemStack.EMPTY;
        StationRecipeInput input = new StationRecipeInput(inventory, fuelSlots, ItemStack.EMPTY);
        if (!currentRecipe.matches(input, level) || !canPlaceOutputs(currentRecipe)) return ItemStack.EMPTY;

        craft(currentRecipe, input);
        setChanged();

        return currentRecipe.results().values().iterator().next().create();
    }

    public int tryCraftAll(ServerPlayer player) {
        if (level == null || level.isClientSide()) return 0;
        if (currentRecipe == null || currentRecipe.processingTime() != 0) return 0;

        int crafted = 0;
        Inventory inv = player.getInventory();

        while (true) {
            StationRecipeInput input = new StationRecipeInput(inventory, fuelSlots, ItemStack.EMPTY);
            if (!currentRecipe.matches(input, level) || !canPlaceOutputs(currentRecipe)) break;

            List<ItemStack> results = currentRecipe.results().values().stream()
                    .map(ItemStackTemplate::create)
                    .toList();
            boolean enoughSpace = true;
            for (ItemStack result : results) {
                int need = result.getCount();
                int available = 0;

                for (int i = 0; i < 36; i++) {
                    ItemStack stack = inv.getItem(i);
                    if (stack.isEmpty()) {
                        available += result.getMaxStackSize();
                    } else if (ItemStack.isSameItemSameComponents(stack, result)) {
                        available += stack.getMaxStackSize() - stack.getCount();
                    }
                    if (available >= need) break;
                }
                if (available < need) {
                    enoughSpace = false;
                    break;
                }
            }
            if (!enoughSpace) break;

            craft(currentRecipe, input);

            for (var entry : currentRecipe.results().entrySet()) {
                int slot = entry.getKey();
                ItemStack stack = inventory.getItem(slot);
                if (!stack.isEmpty()) {
                    inv.add(stack.copy());
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
            }
            crafted++;
            setChanged();
        }
        return crafted;
    }

    public void refreshPendingOutput() {
        if (level == null || level.isClientSide()) return;

        if (currentRecipe == null) {
            pendingOutputs.clear();
            pendingOutput = ItemStack.EMPTY;
            return;
        }

        if (currentRecipe.processingTime() == 0) {
            pendingOutputs.clear();

            for (var entry : currentRecipe.results().entrySet()) {
                pendingOutputs.put(entry.getKey(), entry.getValue().create());
            }
            pendingOutput = pendingOutputs.values().iterator().next();
        } else {
            pendingOutputs.clear();
            pendingOutput = ItemStack.EMPTY;
        }
    }

    private boolean canPlaceOutputs(StationRecipe recipe) {
        for (var entry : recipe.results().entrySet()) {
            int slot = entry.getKey();
            ItemStack result = entry.getValue().create();
            ItemStack existing = inventory.getItem(slot);
            if (existing.isEmpty()) continue;
            if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
            if (existing.getCount() + result.getCount() > existing.getMaxStackSize()) return false;
        }
        return true;
    }

    private void craft(StationRecipe recipe, StationRecipeInput input) {
        for (var entry : recipe.results().entrySet()) {
            int slot = entry.getKey();
            ItemStack result = entry.getValue().create();
            ItemStack existing = inventory.getItem(slot);
            if (existing.isEmpty())
                inventory.setItem(slot, result);
            else
                existing.grow(result.getCount());
        }

        if (recipe.isShapeless()) {
            // shapeless
            List<Ingredient> remaining = new ArrayList<>(recipe.shapelessIngredients());

            for (Integer slot : def.slots().stream()
                    .filter(s -> s.role() == StationDef.SlotRole.INPUT)
                    .map(StationDef.SlotDef::slotIndex)
                    .sorted()
                    .toList()) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty()) continue;
                for (Iterator<Ingredient> it = remaining.iterator(); it.hasNext(); ) {
                    Ingredient ing = it.next();
                    if (ing.test(stack)) {
                        stack.shrink(1);
                        it.remove();
                        break;
                    }
                }
                if (remaining.isEmpty()) break;
            }
        } else if (recipe.isShaped()) {
            Map<Integer, Ingredient> consumptionMap = recipe.getResolvedIngredients(input, level);
            for (int slot : consumptionMap.keySet()) {
                Optional<StationDef.SlotRole> role = def.getSlotRole(slot);
                if (role.isPresent() && role.get() == StationDef.SlotRole.INPUT) {
                    inventory.getItem(slot).shrink(1);
                }
            }
        } else {
            // flat/positional
            for (var entry : recipe.ingredients().entrySet()) {
                int slot = entry.getKey();
                Optional<StationDef.SlotRole> role = def.getSlotRole(slot);
                if (role.isPresent() && role.get() == StationDef.SlotRole.INPUT) {
                    inventory.getItem(slot).shrink(1);
                }
            }
        }

        pendingOutputs.clear();
        pendingOutput = ItemStack.EMPTY;

        if (recipe.processingTime() == 0) {
            refreshPendingOutput();
        }
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Progress", progress);
        output.putInt("BurnTime", burnTime);
        output.putInt("FuelTime", fuelTime);
        if (!currentFuel.isEmpty()) output.store("CurrentFuel", ItemStack.CODEC, currentFuel);
        if (!pendingOutput.isEmpty()) output.store("PendingOutput", ItemStack.CODEC, pendingOutput);
        if (def.persistent()) inventory.storeAsItemList(output.list("Inventory", ItemStack.CODEC));
    }

    @Override
    protected @NonNull Component getDefaultName() {
        return Component.literal("Workstation");
    }

    @Override
    protected @NonNull NonNullList<ItemStack> getItems() {
        return inventory.getItems();
    }

    @Override
    protected void setItems(@NonNull NonNullList<ItemStack> items) {
        inventory.getItems().clear();
        inventory.getItems().addAll(items);
    }

    @Override
    protected @NonNull AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inventory) {
        return null;
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        input.getInt("Progress").ifPresent(x -> progress = x);
        input.getInt("BurnTime").ifPresent(x -> burnTime = x);
        input.getInt("FuelTime").ifPresent(x -> fuelTime = x);
        currentFuel = input.read("CurrentFuel", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        pendingOutput = input.read("PendingOutput", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        if (def.persistent()) inventory.fromItemList(input.listOrEmpty("Inventory", ItemStack.CODEC));
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public int getCookProgress() {
        if (currentRecipe == null) return 0;
        int required = currentRecipe.processingTime();
        if (required <= 0) return 0;
        return Math.min(100, progress * 100 / required);
    }

    public int getBurnProgress() {
        if (fuelTime <= 0) return 0;
        return Math.min(100, burnTime * 100 / fuelTime);
    }

    public ItemStack getPendingOutput() {
        return pendingOutput;
    }

    @Override
    public int @NonNull [] getSlotsForFace(@NonNull Direction direction) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NonNull ItemStack itemStack, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NonNull ItemStack itemStack, @NonNull Direction direction) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return inventory.getContainerSize();
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeUsed) {
        if (recipeUsed != null)
            this.recipesUsed.addTo(recipeUsed.id(), 1);
    }

    @Override
    public @Nullable RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void fillStackedContents(@NonNull StackedItemContents contents) {
        if (this.inventory instanceof StackedContentsCompatible stackedContentsCompatible) {
            stackedContentsCompatible.fillStackedContents(contents);
        }
    }
}
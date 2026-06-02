package de.tomalbrc.filament.api.behaviour;

import de.tomalbrc.filament.decoration.block.entity.DecorationEntity;
import de.tomalbrc.filament.decoration.holder.FilamentDecorationHolder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface DecorationEntityBehaviour<T> extends Behaviour<T> {
    default void init(DecorationEntity entity) {}

    @Nullable
    default FilamentDecorationHolder createHolder(DecorationEntity entity) { return null; }

    default void onHolderAttach(DecorationEntity entity, FilamentDecorationHolder holder) {}

    default InteractionResult interact(ServerPlayer player, InteractionHand hand, Vec3 location, DecorationEntity entity) {
        return InteractionResult.PASS;
    }

    default void read(ValueInput input, DecorationEntity entity) {}

    default void write(ValueOutput output, DecorationEntity entity) {}

    default void destroy(DecorationEntity entity, boolean dropItem) {}

    default void modifyDrop(DecorationEntity entity, ItemStack itemStack) {}

    default ItemStack visualItemStack(DecorationEntity entity, ItemStack adjusted, @Nullable net.minecraft.world.level.block.state.BlockState blockState) {
        return adjusted;
    }

    default void applyImplicitComponents(DecorationEntity entity, DataComponentGetter componentGetter) {}

    default void collectImplicitComponents(DecorationEntity entity, DataComponentMap.Builder builder) {}

    default void removeComponentsFromTag(DecorationEntity entity, ValueOutput output) {}
}
package de.tomalbrc.filament.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import de.tomalbrc.filament.decoration.DecorationBlockEntity;
import de.tomalbrc.filament.decoration.DecorationBlock;

public class PickCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> pickNode = Commands
                .literal("pick")
                .executes(PickCommand::execute)
                .build();

        dispatcher.getRoot().addChild(pickNode);
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        if (context.getSource() != null && context.getSource().getPlayer() != null) {
            ServerPlayer player = context.getSource().getPlayer();

            for (int i = 1; i <= 8; i++) {
                Vec3 pos = player.getEyePosition().add(player.getViewVector(1).normalize().scale(i*0.5f));
                BlockPos blockPos = BlockPos.containing(pos);

                BlockState state = player.level().getBlockState(blockPos);
                if (state.getBlock() instanceof DecorationBlock) {
                    if (player.level().getBlockEntity(blockPos) instanceof DecorationBlockEntity decorationBlockEntity) {
                        ItemStack item = decorationBlockEntity.getItem().copy();

                        if (player.isCreative()) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, item);
                        } else {
                            int slot = player.getInventory().findSlotMatchingItem(item);
                            if (slot != -1) {
                                player.getInventory().pickSlot(slot);
                            }
                        }

                        // eh, may actually not be successful, but it's not like this return value changes anything..?
                        return Command.SINGLE_SUCCESS;
                    }
                } else if (!state.isAir() && !state.liquid()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, state.getBlock().asItem().getDefaultInstance());
                    return Command.SINGLE_SUCCESS;
                }
            }
        }
        return 0;
    }
}

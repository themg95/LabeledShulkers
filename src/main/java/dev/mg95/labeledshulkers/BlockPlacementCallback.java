package dev.mg95.labeledshulkers;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockPlacementCallback {
    Event<BlockPlacementCallback> EVENT = EventFactory.createArrayBacked(BlockPlacementCallback.class,
            (listeners) -> (pos, world, player, stack, state) -> {
                for (BlockPlacementCallback listener : listeners) {
                    listener.place(pos, world, player, stack, state);
                }
            });

    void place(BlockPos pos, World world, PlayerEntity player, ItemStack stack, BlockState state);
}

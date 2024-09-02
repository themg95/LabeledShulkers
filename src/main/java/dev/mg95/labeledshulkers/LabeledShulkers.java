package dev.mg95.labeledshulkers;

import dev.mg95.labeledshulkers.mixin.DisplayEntityAccess;
import dev.mg95.labeledshulkers.interfaces.IDisplayEntityMixin;
import dev.mg95.labeledshulkers.mixin.TextDisplayEntityAccess;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

import static net.minecraft.item.Items.*;
import static net.minecraft.server.command.CommandManager.literal;

public class LabeledShulkers implements ModInitializer {
    public LSConfig config = new LSConfig();
    public Map<ServerPlayerEntity, DisplayEntity.TextDisplayEntity> displays = new HashMap<>();
    public static final List<Item> SHULKERS = List.of(
            SHULKER_BOX,
            WHITE_SHULKER_BOX,
            ORANGE_SHULKER_BOX,
            MAGENTA_SHULKER_BOX,
            LIGHT_BLUE_SHULKER_BOX,
            YELLOW_SHULKER_BOX,
            LIME_SHULKER_BOX,
            PINK_SHULKER_BOX,
            GRAY_SHULKER_BOX,
            LIGHT_GRAY_SHULKER_BOX,
            CYAN_SHULKER_BOX,
            PURPLE_SHULKER_BOX,
            BLUE_SHULKER_BOX,
            BROWN_SHULKER_BOX,
            GREEN_SHULKER_BOX,
            RED_SHULKER_BOX,
            BLACK_SHULKER_BOX
    );

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                var raycast = player.raycast(player.getBlockInteractionRange(), 1, false);
                if (!(raycast instanceof BlockHitResult result)) continue;

                var block = player.getWorld().getBlockState(result.getBlockPos()).getBlock();

                var previousAttachment = displays.get(player);
                if (previousAttachment != null) {
                    var previousPos = previousAttachment.getPos();
                    if (result.getBlockPos().equals(new BlockPos((int) (previousPos.x - 0.5), (int) (previousPos.y - 0.75), (int) (previousPos.z - 0.5))))
                        continue;
                    if (!(block instanceof ShulkerBoxBlock)) removePlayer(player);
                }

                if (!(block instanceof ShulkerBoxBlock)) continue;

                var be = player.getWorld().getBlockEntity(result.getBlockPos());

                if (!(be instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity)) continue;

                if (!shulkerBoxBlockEntity.hasCustomName() && !config.showNonCustomNames) {
                    if (displays.containsKey(player)) removePlayer(player);
                    continue;
                }

                if (displays.containsKey(player)) removePlayer(player);

                var display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, player.getWorld());
                display.setPosition(result.getBlockPos().toCenterPos().add(0, .75, 0));

                //((TextDisplayEntityAccessor) display).setText(shulkerBox.getName());

                var nbt = new NbtCompound();
                nbt.putString("text", shulkerBoxBlockEntity.getDisplayName().toString());


                final var TEXT = ((TextDisplayEntityAccess) display).getTEXT();
                final var BILLBOARD = ((DisplayEntityAccess) display).getBILLBOARD();
                var dataTracker = display.getDataTracker();
                dataTracker.set(TEXT, shulkerBoxBlockEntity.getDisplayName());
                player.networkHandler.sendPacket(new EntitySpawnS2CPacket(
                        display.getId(),
                        display.getUuid(),
                        display.getPos().getX(),
                        display.getPos().getY(),
                        display.getPos().getZ(),
                        display.getPitch(),
                        display.getYaw(),
                        display.getType(),
                        1,
                        Vec3d.ZERO,
                        display.getHeadYaw()
                ));
                player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(display.getId(), List.of(
                        DataTracker.SerializedEntry.of(TEXT, shulkerBoxBlockEntity.getDisplayName()),
                        DataTracker.SerializedEntry.of(BILLBOARD, (byte) DisplayEntity.BillboardMode.CENTER.ordinal())
                )));

                //player.getServerWorld().spawnEntity(display);

                //holder.startWatching(player);
                displays.put(player, display);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            var player = handler.getPlayer();
            displays.remove(player);
        });

        if (config.enableHolograms) {
            BlockPlacementCallback.EVENT.register(((pos, world, player, stack, state) -> {
                if (!config.enableHolograms) return;

                if (!(state.getBlock() instanceof ShulkerBoxBlock shulkerBoxBlock)) return;

                try {
                    if (!stack.getTooltip(Item.TooltipContext.DEFAULT, player, TooltipType.BASIC).get(1).getString().equals("Hologram"))
                        return;

                } catch (IndexOutOfBoundsException ignored) {
                    return;
                }

                var be = world.getBlockEntity(pos);

                if (!(be instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity)) return;


                var display = new DisplayEntity.TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
                display.setPosition(pos.toCenterPos().add(0, .75, 0));

                final var TEXT = ((TextDisplayEntityAccess) display).getTEXT();
                final var BILLBOARD = ((DisplayEntityAccess) display).getBILLBOARD();

                display.getDataTracker().set(TEXT, stack.getName());
                display.getDataTracker().set(BILLBOARD, (byte) DisplayEntity.BillboardMode.CENTER.ordinal());

                ((IDisplayEntityMixin) display).setHologram(true);

                world.spawnEntity(display);
            }));

            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("set_hologram").executes(context -> {
                if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
                    context.getSource().sendError(Text.literal("Only players can execute this command!"));
                    return 0;
                }

                if (!config.enableHolograms) {
                    context.getSource().sendError(Text.literal("Holograms are disabled!"));
                    return 0;
                }

                var stack = player.getMainHandStack();

                if (stack.isEmpty() || !SHULKERS.contains(stack.getItem())) {
                    context.getSource().sendError(Text.literal("Not a shulker box!"));
                    return 0;
                }

                var lore = new ArrayList<>(stack.get(DataComponentTypes.LORE).lines());

                var text = Text.literal("Hologram");
                if (!lore.isEmpty() && Objects.equals(lore.getFirst(), text)) {
                    context.getSource().sendError(Text.literal("Already a hologram!"));
                    return 0;
                }


                if (lore.isEmpty()) {
                    lore.add(text);
                } else {
                    lore.addFirst(text);
                }

                stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

                context.getSource().sendMessage(Text.literal("Set item as hologram."));

                return 1;
            })));
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("unset_hologram").executes(context -> {
            if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)) {
                context.getSource().sendError(Text.literal("Only players can execute this command!"));
                return 0;
            }

            var stack = player.getMainHandStack();

            if (stack.isEmpty()) {
                context.getSource().sendError(Text.literal("You aren't holding anything..."));
                return 0;
            }

            var lore = new ArrayList<>(stack.get(DataComponentTypes.LORE).lines());

            var text = Text.literal("Hologram");

            if (stack.isEmpty() || lore.isEmpty() || !Objects.equals(lore.getFirst(), text)) {
                context.getSource().sendError(Text.literal("Isn't a hologram!"));
                return 0;
            }

            lore.removeFirst();

            stack.set(DataComponentTypes.LORE, new LoreComponent(lore));

            context.getSource().sendMessage(Text.literal("Unset item as hologram."));

            return 1;
        })));


    }


    public void removePlayer(ServerPlayerEntity player) {
        var display = displays.get(player);
        player.networkHandler.sendPacket(new EntitiesDestroyS2CPacket(display.getId()));
        displays.remove(player);
    }
}

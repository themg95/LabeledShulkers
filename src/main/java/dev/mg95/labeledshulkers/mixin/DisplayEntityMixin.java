package dev.mg95.labeledshulkers.mixin;

import dev.mg95.labeledshulkers.interfaces.IDisplayEntityMixin;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisplayEntity.class)
public class DisplayEntityMixin implements IDisplayEntityMixin {
    // based partly on https://github.com/FaeWulf/Diversity/blob/1.21/common/src/main/java/xyz/faewulf/diversity/mixin/shulkerBoxLabel/DisplayEntityMixins.java
    // for parts that were based on Diversity, its license applies.

    @Unique
    public boolean hologram = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (!this.hologram) return;

        if (!((Object) this instanceof DisplayEntity.TextDisplayEntity textDisplay)) return;

        var pos = textDisplay.getBlockPos().down(1);
        var be = textDisplay.getWorld().getBlockEntity(pos);

        if (!(be instanceof ShulkerBoxBlockEntity)) {
            textDisplay.discard();
        }

    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("labeledshulkers:hologram", this.hologram);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("labeledshulkers:hologram")) {
            this.hologram = nbt.getBoolean("labeledshulkers:hologram");
        }
    }

    @Unique
    public boolean getHologram() {
        return this.hologram;
    }

    @Unique
    public void setHologram(boolean hologram) {
        this.hologram = hologram;
    }

}

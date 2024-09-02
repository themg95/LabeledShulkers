package dev.mg95.labeledshulkers.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisplayEntity.class)
public interface DisplayEntityAccess {
    @Accessor("BILLBOARD")
    TrackedData<Byte> getBILLBOARD();
}

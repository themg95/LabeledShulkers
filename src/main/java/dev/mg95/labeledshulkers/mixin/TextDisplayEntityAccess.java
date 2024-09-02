package dev.mg95.labeledshulkers.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DisplayEntity.TextDisplayEntity.class)
public interface TextDisplayEntityAccess {
    @Accessor("textLines")
    void setTextLines(DisplayEntity.TextDisplayEntity.TextLines textLines);

    @Accessor("TEXT")
    TrackedData<Text> getTEXT();

    @Accessor("LEFT_ALIGNMENT_FLAG")
    public static byte getLEFT_ALIGNMENT_FLAG() {
        throw new AssertionError();
    }
}

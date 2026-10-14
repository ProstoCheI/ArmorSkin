package dev.playersarmor.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ArmorStandEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorStandEntityModel.class)
public interface ArmorStandModelAccessor {
    @Accessor("rightBodyStick")
    ModelPart playersarmor$getRightBodyStick();

    @Accessor("leftBodyStick")
    ModelPart playersarmor$getLeftBodyStick();

    @Accessor("shoulderStick")
    ModelPart playersarmor$getShoulderStick();
}

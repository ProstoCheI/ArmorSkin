package dev.armorskin.mixin;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ArmorStandEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorStandEntityModel.class)
public interface ArmorStandModelAccessor {
    @Accessor("rightBodyStick")
    ModelPart armorskin$getRightBodyStick();

    @Accessor("leftBodyStick")
    ModelPart armorskin$getLeftBodyStick();

    @Accessor("shoulderStick")
    ModelPart armorskin$getShoulderStick();
}

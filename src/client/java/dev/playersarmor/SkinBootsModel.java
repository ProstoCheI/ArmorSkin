package dev.playersarmor;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public final class SkinBootsModel<T extends LivingEntity> extends BipedEntityModel<T> {
    public SkinBootsModel() {
        super(createRoot());
    }

    private static ModelPart createRoot() {
        ModelPart template = BipedEntityModel.getModelData(Dilation.NONE, 0.0F)
                .getRoot().createPart(64, 64);
        return new ModelPart(List.of(), Map.of(
                "head", template.getChild("head"),
                "hat", template.getChild("hat"),
                "body", template.getChild("body"),
                "left_arm", template.getChild("left_arm"),
                "right_arm", template.getChild("right_arm"),
                "left_leg", createFoot(16, 48, 0, 48),
                "right_leg", createFoot(0, 16, 0, 32)));
    }

    private static ModelPart createFoot(int textureU, int textureV, int overlayU, int overlayV) {
        ModelPart overlay = new ModelPart(createCuboids(overlayU, overlayV, 1.2F), Map.of());
        return new ModelPart(createCuboids(textureU, textureV, 0.95F), Map.of("overlay", overlay));
    }

    private static List<ModelPart.Cuboid> createCuboids(int textureU, int textureV, float dilation) {
        // Crop side UVs to the bottom four rows, keeping the original foot cap UVs.
        ModelPart.Cuboid sides = new ModelPart.Cuboid(textureU, textureV + 8,
                -2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F,
                dilation, dilation, dilation, false, 64.0F, 64.0F,
                EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        ModelPart.Cuboid caps = new ModelPart.Cuboid(textureU, textureV,
                -2.0F, 8.0F, -2.0F, 4.0F, 4.0F, 4.0F,
                dilation, dilation, dilation, false, 64.0F, 64.0F,
                EnumSet.of(Direction.UP, Direction.DOWN));
        return List.of(sides, caps);
    }
}

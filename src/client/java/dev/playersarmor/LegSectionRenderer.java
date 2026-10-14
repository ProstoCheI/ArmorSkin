package dev.playersarmor;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class LegSectionRenderer {
    private LegSectionRenderer() {
    }

    public static void renderUpper(BipedEntityModel<?> model, MatrixStack matrices, VertexConsumer vertices,
                                   int light, int overlay, int color, float bootDilation) {
        renderSection(model, matrices, vertices, light, overlay, color, 8.0F - bootDilation, true);
    }

    public static void renderLower(BipedEntityModel<?> model, MatrixStack matrices, VertexConsumer vertices,
                                   int light, int overlay, int color) {
        renderSection(model, matrices, vertices, light, overlay, color, 8.0F, false);
    }

    private static void renderSection(BipedEntityModel<?> model, MatrixStack matrices, VertexConsumer vertices,
                                      int light, int overlay, int color, float cutoff, boolean upper) {
        matrices.push();
        try {
            if (model.child) {
                matrices.scale(0.5F, 0.5F, 0.5F);
                matrices.translate(0.0F, 1.5F, 0.0F);
            }

            renderPart(model.leftLeg, matrices, vertices, light, overlay, color, cutoff, upper);
            renderPart(model.rightLeg, matrices, vertices, light, overlay, color, cutoff, upper);
            if (model instanceof PlayerEntityModel<?> player) {
                renderPart(player.leftPants, matrices, vertices, light, overlay, color, cutoff - 0.25F, upper);
                renderPart(player.rightPants, matrices, vertices, light, overlay, color, cutoff - 0.25F, upper);
            }
        } finally {
            matrices.pop();
        }
    }

    private static void renderPart(ModelPart part, MatrixStack matrices, VertexConsumer vertices,
                                   int light, int overlay, int color, float cutoff, boolean upper) {
        if (!part.visible || part.hidden) {
            return;
        }

        part.forEachCuboid(matrices, (entry, path, index, cuboid) -> {
            VertexConsumer clipped = new ClippingConsumer(vertices, entry.getPositionMatrix(), cutoff / 16.0F, upper);
            cuboid.renderCuboid(entry, clipped, light, overlay, color);
        });
    }

    private record Vertex(float x, float y, float z, float u, float v, float localY) {
        Vertex interpolate(Vertex other, float t) {
            return new Vertex(x + (other.x - x) * t, y + (other.y - y) * t,
                    z + (other.z - z) * t, u + (other.u - u) * t,
                    v + (other.v - v) * t, localY + (other.localY - localY) * t);
        }
    }

    private static final class ClippingConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final Matrix4f inverse;
        private final float cutoff;
        private final boolean upper;
        private final Vertex[] quad = new Vertex[4];
        private int count;

        private ClippingConsumer(VertexConsumer delegate, Matrix4f transform, float cutoff, boolean upper) {
            this.delegate = delegate;
            this.inverse = new Matrix4f(transform).invert();
            this.cutoff = cutoff;
            this.upper = upper;
        }

        @Override
        public void vertex(float x, float y, float z, int color, float u, float v,
                           int overlay, int light, float normalX, float normalY, float normalZ) {
            float localY = inverse.m01() * x + inverse.m11() * y + inverse.m21() * z + inverse.m31();
            quad[count++] = new Vertex(x, y, z, u, v, localY);
            if (count != 4) {
                return;
            }
            count = 0;

            // Clip each face in leg-local coordinates so rotations and UVs stay intact.
            List<Vertex> clipped = new ArrayList<>(4);
            Vertex previous = quad[3];
            boolean previousInside = isInside(previous);
            for (Vertex current : quad) {
                boolean inside = isInside(current);
                if (inside != previousInside) {
                    float t = (cutoff - previous.localY) / (current.localY - previous.localY);
                    clipped.add(previous.interpolate(current, t));
                }
                if (inside) {
                    clipped.add(current);
                }
                previous = current;
                previousInside = inside;
            }

            if (clipped.size() == 4) {
                for (Vertex vertex : clipped) {
                    emit(vertex, color, overlay, light, normalX, normalY, normalZ);
                }
            } else {
                for (int i = 1; i + 1 < clipped.size(); i++) {
                    emit(clipped.get(0), color, overlay, light, normalX, normalY, normalZ);
                    emit(clipped.get(i), color, overlay, light, normalX, normalY, normalZ);
                    emit(clipped.get(i + 1), color, overlay, light, normalX, normalY, normalZ);
                    emit(clipped.get(i + 1), color, overlay, light, normalX, normalY, normalZ);
                }
            }
        }

        private boolean isInside(Vertex vertex) {
            return upper ? vertex.localY <= cutoff : vertex.localY >= cutoff;
        }

        private void emit(Vertex vertex, int color, int overlay, int light,
                          float normalX, float normalY, float normalZ) {
            delegate.vertex(vertex.x, vertex.y, vertex.z, color, vertex.u, vertex.v,
                    overlay, light, normalX, normalY, normalZ);
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            delegate.texture(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            delegate.overlay(u, v);
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            delegate.light(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }
    }
}

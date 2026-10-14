package dev.playersarmor;

import net.minecraft.client.model.ModelPart;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ModelPartVisibility implements AutoCloseable {
    private final Map<ModelPart, Boolean> originalHidden = new IdentityHashMap<>();

    public void hide(ModelPart... parts) {
        for (ModelPart part : parts) {
            originalHidden.putIfAbsent(part, part.hidden);
            part.hidden = true;
        }
    }

    @Override
    public void close() {
        originalHidden.forEach((part, hidden) -> part.hidden = hidden);
        originalHidden.clear();
    }
}

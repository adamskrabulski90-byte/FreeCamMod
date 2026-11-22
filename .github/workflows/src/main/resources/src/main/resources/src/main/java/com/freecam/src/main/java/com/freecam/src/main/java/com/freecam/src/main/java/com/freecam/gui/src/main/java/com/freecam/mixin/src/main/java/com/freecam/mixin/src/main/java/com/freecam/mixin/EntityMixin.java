package com.freecam.mixin;

import com.freecam.FreecamConfig;
import com.freecam.FreecamState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    private boolean freecam$isLocalPlayer() {
        return (Object) this == MinecraftClient.getInstance().player;
    }

    // Mysz obraca kamera. Rotation wylaczone: glowa postaci stoi w miejscu.
    // Rotation wlaczone: postac obraca sie razem z kamera.
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void freecam$look(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (!FreecamState.enabled || !freecam$isLocalPlayer()) {
            return;
        }

        FreecamState.rotate(cursorDeltaX, cursorDeltaY);

        if (!FreecamConfig.INSTANCE.rotation) {
            ci.cancel();
        }
    }

    // Cancel move - fizyczna postac zamrozona w miejscu
    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void freecam$move(MovementType type, Vec3d movement, CallbackInfo ci) {
        if (FreecamState.enabled && FreecamConfig.INSTANCE.cancelMove && freecam$isLocalPlayer()) {
            ((Entity) (Object) this).setVelocity(Vec3d.ZERO);
            ci.cancel();
        }
    }
}

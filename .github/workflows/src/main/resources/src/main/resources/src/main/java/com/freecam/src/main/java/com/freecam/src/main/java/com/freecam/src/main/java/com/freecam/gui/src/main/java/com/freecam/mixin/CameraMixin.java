package com.freecam.mixin;

import com.freecam.FreecamState;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private boolean thirdPerson;

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At("TAIL"))
    private void freecam$update(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!FreecamState.enabled) {
            return;
        }

        // true = model postaci jest rysowany, a reka w pierwszej osobie ukryta
        this.thirdPerson = true;

        this.setRotation(FreecamState.yaw, FreecamState.pitch);
        this.setPos(
                FreecamState.lerpX(tickDelta),
                FreecamState.lerpY(tickDelta),
                FreecamState.lerpZ(tickDelta)
        );
    }
}

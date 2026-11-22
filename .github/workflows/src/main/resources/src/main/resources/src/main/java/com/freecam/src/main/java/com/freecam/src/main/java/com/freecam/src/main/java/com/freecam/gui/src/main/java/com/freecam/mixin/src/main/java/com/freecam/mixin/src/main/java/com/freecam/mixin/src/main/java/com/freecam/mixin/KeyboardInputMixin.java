package com.freecam.mixin;

import com.freecam.FreecamConfig;
import com.freecam.FreecamState;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {

    // Podczas Freecam klawisze nie ruszaja fizycznej postaci.
    // Przy Arrow move: WASD dalej steruje postacia, ale Spacja i Shift nie (te ruszaja kamera).
    @Inject(method = "tick", at = @At("TAIL"))
    private void freecam$tick(CallbackInfo ci) {
        if (!FreecamState.enabled) {
            return;
        }

        if (FreecamConfig.INSTANCE.arrowMove) {
            PlayerInput p = this.playerInput;
            this.playerInput = new PlayerInput(p.forward(), p.backward(), p.left(), p.right(), false, false, p.sprint());
        } else {
            this.playerInput = PlayerInput.DEFAULT;
            this.movementVector = Vec2f.ZERO;
        }
    }
}

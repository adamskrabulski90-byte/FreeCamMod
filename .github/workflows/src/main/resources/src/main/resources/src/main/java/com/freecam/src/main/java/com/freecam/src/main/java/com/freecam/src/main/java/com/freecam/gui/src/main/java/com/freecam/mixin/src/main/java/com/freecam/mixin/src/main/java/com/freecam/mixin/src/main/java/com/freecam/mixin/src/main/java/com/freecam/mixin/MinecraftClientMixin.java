package com.freecam.mixin;

import com.freecam.FreecamConfig;
import com.freecam.FreecamState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public GameOptions options;

    // Cancel interact - zjadamy klikniecia LPM / PPM zanim gra je obsluzy:
    // brak niszczenia, stawiania blokow, bicia i uzywania przedmiotow
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void freecam$blockInteract(CallbackInfo ci) {
        if (!FreecamState.enabled || !FreecamConfig.INSTANCE.cancelInteract) {
            return;
        }

        this.options.attackKey.setPressed(false);
        this.options.useKey.setPressed(false);

        while (this.options.attackKey.wasPressed()) {
            // zuzycie zakolejkowanych klikniec
        }
        while (this.options.useKey.wasPressed()) {
            // zuzycie zakolejkowanych klikniec
        }
    }
}

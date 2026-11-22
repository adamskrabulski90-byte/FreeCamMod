package com.freecam.mixin;

import com.freecam.FreecamConfig;
import com.freecam.FreecamState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    // Cancel move - blokuje pakiety ruchu wysylane na serwer
    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void freecam$sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (FreecamState.enabled
                && FreecamConfig.INSTANCE.cancelMove
                && packet instanceof PlayerMoveC2SPacket) {
            ci.cancel();
        }
    }
}

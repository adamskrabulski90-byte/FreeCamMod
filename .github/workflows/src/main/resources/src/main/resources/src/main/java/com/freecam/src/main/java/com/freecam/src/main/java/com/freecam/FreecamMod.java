package com.freecam;

import com.freecam.gui.ClickGuiScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class FreecamMod implements ClientModInitializer {
    private static boolean wasGuiDown = false;
    private static boolean wasBindDown = false;

    @Override
    public void onInitializeClient() {
        FreecamConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(FreecamMod::onTick);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FreecamState.disable());

        WorldRenderEvents.LAST.register(FreecamMod::renderTracers);
    }

    private static void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) {
            FreecamState.disable();
            return;
        }

        long window = mc.getWindow().getHandle();

        // Gdy jest otwarty jakis ekran (menu, czat, ClickGUI) - nie reagujemy na klawisze
        if (mc.currentScreen != null) {
            wasGuiDown = true;
            wasBindDown = true;
            FreecamState.tick(mc);
            return;
        }

        // Prawy Shift - otwarcie ClickGUI
        boolean guiDown = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        if (guiDown && !wasGuiDown) {
            mc.setScreen(new ClickGuiScreen());
        }
        wasGuiDown = guiDown;

        // Klawisz Bind - wlaczanie / wylaczanie Freecam
        int bind = FreecamConfig.INSTANCE.bindKey;
        boolean bindDown = bind > 0 && InputUtil.isKeyPressed(window, bind);
        if (bindDown && !wasBindDown) {
            FreecamState.toggle();
        }
        wasBindDown = bindDown;

        FreecamState.tick(mc);
    }

    // Player tracing - cienka linia od kamery do postaci gracza i innych graczy w poblizu
    private static void renderTracers(WorldRenderContext context) {
        if (!FreecamState.enabled || !FreecamConfig.INSTANCE.playerTracing) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null || mc.player == null) {
            return;
        }

        VertexConsumerProvider consumers = context.consumers();
        MatrixStack matrices = context.matrixStack();
        if (consumers == null || matrices == null) {
            return;
        }

        float delta = context.tickCounter().getTickDelta(true);
        Vec3d cam = context.camera().getPos();

        // punkt startu linii: tuz przed kamera (wzgledem kamery)
        double yawRad = Math.toRadians(FreecamState.yaw);
        double pitchRad = Math.toRadians(FreecamState.pitch);
        Vec3d look = new Vec3d(
                -Math.sin(yawRad) * Math.cos(pitchRad),
                -Math.sin(pitchRad),
                Math.cos(yawRad) * Math.cos(pitchRad)
        );
        Vec3d start = look.multiply(0.4);

        MatrixStack.Entry entry = matrices.peek();
        VertexConsumer vc = consumers.getBuffer(RenderLayer.getLines());

        // fizyczna postac gracza - pomaranczowa linia
        Vec3d self = mc.player.getLerpedPos(delta).add(0.0, mc.player.getHeight() * 0.5, 0.0);
        drawLine(vc, entry, start, self.subtract(cam), 255, 170, 0);

        // inni gracze w poblizu - biala linia
        for (AbstractClientPlayerEntity other : mc.world.getPlayers()) {
            if (other == mc.player) {
                continue;
            }
            if (other.squaredDistanceTo(cam.x, cam.y, cam.z) > 64.0 * 64.0) {
                continue;
            }
            Vec3d pos = other.getLerpedPos(delta).add(0.0, other.getHeight() * 0.5, 0.0);
            drawLine(vc, entry, start, pos.subtract(cam), 255, 255, 255);
        }

        if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw(RenderLayer.getLines());
        }
    }

    private static void drawLine(VertexConsumer vc, MatrixStack.Entry entry, Vec3d from, Vec3d to, int r, int g, int b) {
        Vec3d d = to.subtract(from);
        double len = d.length();
        if (len < 1.0E-4) {
            return;
        }
        float nx = (float) (d.x / len);
        float ny = (float) (d.y / len);
        float nz = (float) (d.z / len);

        vc.vertex(entry, (float) from.x, (float) from.y, (float) from.z).color(r, g, b, 255).normal(entry, nx, ny, nz);
        vc.vertex(entry, (float) to.x, (float) to.y, (float) to.z).color(r, g, b, 255).normal(entry, nx, ny, nz);
    }
}

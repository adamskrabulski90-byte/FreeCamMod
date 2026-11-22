package com.freecam;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class FreecamState {
    public static boolean enabled = false;

    // pozycja kamery (aktualna i z poprzedniego ticku - do plynnej interpolacji)
    public static double x, y, z;
    public static double prevX, prevY, prevZ;

    // obrot kamery
    public static float yaw, pitch;

    private static float lastHealth;
    private static int lastHurtTime;

    public static void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    public static void enable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) {
            return;
        }

        Vec3d pos = player.getEyePos();
        x = prevX = pos.x;
        y = prevY = pos.y;
        z = prevZ = pos.z;
        yaw = player.getYaw();
        pitch = player.getPitch();

        lastHealth = player.getHealth();
        lastHurtTime = player.hurtTime;

        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    // Obrot kamery myszka (te same proporcje co w Minecrafcie)
    public static void rotate(double deltaX, double deltaY) {
        pitch = MathHelper.clamp(pitch + (float) deltaY * 0.15F, -90.0F, 90.0F);
        yaw += (float) deltaX * 0.15F;
    }

    public static double lerpX(float delta) {
        return MathHelper.lerp(delta, prevX, x);
    }

    public static double lerpY(float delta) {
        return MathHelper.lerp(delta, prevY, y);
    }

    public static double lerpZ(float delta) {
        return MathHelper.lerp(delta, prevZ, z);
    }

    // Wywolywane co tick klienta
    public static void tick(MinecraftClient mc) {
        if (!enabled) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) {
            enabled = false;
            return;
        }

        FreecamConfig cfg = FreecamConfig.INSTANCE;

        // Disable on death
        if (cfg.disableOnDeath && player.getHealth() <= 0.0F) {
            disable();
            return;
        }

        // Disable on damage
        if (cfg.disableOnDamage && (player.hurtTime > lastHurtTime || player.getHealth() < lastHealth)) {
            disable();
            return;
        }
        lastHealth = player.getHealth();
        lastHurtTime = player.hurtTime;

        prevX = x;
        prevY = y;
        prevZ = z;

        // Gdy jest otwarte jakies menu (np. ClickGUI), kamera stoi
        if (mc.currentScreen != null) {
            return;
        }

        GameOptions o = mc.options;
        long window = mc.getWindow().getHandle();

        boolean fw, bk, lt, rt;
        if (cfg.arrowMove) {
            fw = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_UP);
            bk = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_DOWN);
            lt = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT);
            rt = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT);
        } else {
            fw = o.forwardKey.isPressed();
            bk = o.backKey.isPressed();
            lt = o.leftKey.isPressed();
            rt = o.rightKey.isPressed();
        }
        boolean up = o.jumpKey.isPressed();
        boolean down = o.sneakKey.isPressed();

        double forward = (fw ? 1 : 0) - (bk ? 1 : 0);
        double strafe = (lt ? 1 : 0) - (rt ? 1 : 0); // dodatnie = w lewo

        double length = Math.sqrt(forward * forward + strafe

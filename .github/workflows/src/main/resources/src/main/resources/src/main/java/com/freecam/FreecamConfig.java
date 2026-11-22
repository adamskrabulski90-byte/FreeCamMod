package com.freecam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FreecamConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("freecam.json");

    public static FreecamConfig INSTANCE = new FreecamConfig();

    public double hSpeed = 0.632;
    public double vSpeed = 0.592;
    public boolean rotation = false;
    public boolean arrowMove = false;
    public boolean cancelMove = false;
    public boolean cancelInteract = false;
    public boolean disableOnDamage = false;
    public boolean disableOnDeath = false;
    public boolean playerTracing = false;
    public int bindKey = GLFW.GLFW_KEY_P;

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                FreecamConfig loaded = GSON.fromJson(Files.readString(PATH), FreecamConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                }
            }
        } catch (Exception e) {
            System.err.println("[Freecam] Nie udalo sie wczytac configu: " + e.getMessage());
        }
        INSTANCE.hSpeed = clamp(INSTANCE.hSpeed);
        INSTANCE.vSpeed = clamp(INSTANCE.vSpeed);
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            System.err.println("[Freecam] Nie udalo sie zapisac configu: " + e.getMessage());
        }
    }

    private static double clamp(double v) {
        return Math.max(0.1, Math.min(3.0, v));
    }
}

package com.freecam.gui;

import com.freecam.FreecamConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ClickGuiScreen extends Screen {
    // wymiary
    private static final int PANEL_W = 150;
    private static final int TITLE_W = 150;
    private static final int TITLE_H = 24;
    private static final int PAD = 6;
    private static final int SLIDER_ROW = 28;
    private static final int TOGGLE_ROW = 17;
    private static final int BIND_H = 16;
    private static final int PANEL_H = PAD + 2 * SLIDER_ROW + 7 * TOGGLE_ROW + 4 + BIND_H + PAD;

    // kolory
    private static final int BG = 0xAA101010;
    private static final int ORANGE = 0xFFFF8800;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF808080;
    private static final int GRAY_HOVER = 0xFF959595;
    private static final int TRACK = 0xFFC8C8C8;
    private static final int KNOB = 0xFF3F6FFF;

    private static class Toggle {
        final String label;
        final BooleanSupplier getter;
        final Consumer<Boolean> setter;

        Toggle(String label, BooleanSupplier getter, Consumer<Boolean> setter) {
            this.label = label;
            this.getter = getter;
            this.setter = setter;
        }
    }

    private final List<Toggle> toggles;

    private int px, py;
    private int titleX, titleY;
    private int dragging = -1; // 0 = HSpeed, 1 = VSpeed
    private boolean listeningForBind = false;

    public ClickGuiScreen() {
        super(Text.literal("Freecam"));
        FreecamConfig c = FreecamConfig.INSTANCE;
        toggles = List.of(
                new Toggle("Rotation", () -> c.rotation, v -> c.rotation = v),
                new Toggle("Arrow move", () -> c.arrowMove, v -> c.arrowMove = v),
                new Toggle("Cancel move", () -> c.cancelMove, v -> c.cancelMove = v),
                new Toggle("Cancel interact", () -> c.cancelInteract, v -> c.cancelInteract = v),
                new Toggle("Disable on damage", () -> c.disableOnDamage, v -> c.disableOnDamage = v),
                new Toggle("Disable on death", () -> c.disableOnDeath, v -> c.disableOnDeath = v),
                new Toggle("Player tracing", () -> c.playerTracing, v -> c.playerTracing = v)
        );
    }

    @Override
    protected void init() {
        int total = TITLE_W + PANEL_W;
        titleX = (this.width - total) / 2;
        titleY = (this.height - PANEL_H) / 2;
        px = titleX + TITLE_W;
        py = titleY;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    // bez rozmycia i ciemnego tla - gra ma byc widoczna jak na screenie
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void removed() {
        FreecamConfig.save();
    }

    // ---------- pozycje ----------

    private int sliderY(int i) {
        return py + PAD + i * SLIDER_ROW;
    }

    private int toggleY(int i) {
        return py + PAD + 2 * SLIDER_ROW + i * TOGGLE_ROW;
    }

    private int bindY() {
        return py + PANEL_H - PAD - BIND_H;
    }

    private int trackX0() {
        return px + PAD + 3;
    }

    private int trackX1() {
        return px + PANEL_W - PAD - 3;
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private double getSlider(int i) {
        return i == 0 ? FreecamConfig.INSTANCE.hSpeed : FreecamConfig.INSTANCE.vSpeed;
    }

    private void setSliderFromMouse(int i, double mouseX) {
        double t = (mouseX - trackX0()) / (double) (trackX1() - trackX0());
        t = Math.max(0.0, Math.min(1.0, t));
        double value = 0.1 + t * 2.9;
        value = Math.round(value * 1000.0) / 1000.0;
        if (i == 0) {
            FreecamConfig.INSTANCE.hSpeed = value;
        } else {
            FreecamConfig.INSTANCE.vSpeed = value;
        }
    }

    private String keyName(int key) {
        if (key <= 0) {
            return "NONE";
        }
        return InputUtil.fromKeyCode(key, 0).getLocalizedText().getString().toUpperCase(Locale.ROOT);
    }

    // ---------- rysowanie ----------

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // okienko z tytulem
        ctx.fill(titleX, titleY, titleX + TITLE_W, titleY + TITLE_H, BG);
        ctx.drawBorder(titleX, titleY, TITLE_W, TITLE_H, ORANGE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Freecam", titleX + TITLE_W / 2, titleY + (TITLE_H - 8) / 2, WHITE);

        // glowny panel
        ctx.fill(px, py, px + PANEL_W, py + PANEL_H, BG);
        ctx.drawBorder(px, py,

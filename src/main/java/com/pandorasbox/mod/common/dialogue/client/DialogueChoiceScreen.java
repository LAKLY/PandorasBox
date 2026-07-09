package com.pandorasbox.mod.common.dialogue.client;

import com.pandorasbox.mod.common.dialogue.api.DialogueChoice;
import com.pandorasbox.mod.common.dialogue.api.DialogueType;
import com.pandorasbox.mod.common.dialogue.network.C2SDialogueChoicePacket;
import com.pandorasbox.mod.common.dialogue.network.S2CDialoguePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DialogueChoiceScreen extends Screen {

    private final S2CDialoguePacket packet;
    private float alpha = 0.0f;
    private float buttonOffset = 0.0f;
    private int tickCounter = 0;
    private final List<Button> choiceButtons = new ArrayList<>();
    private List<String> wrappedLines = new ArrayList<>();
    private int textHeight = 0;

    private static final int COLOR_NARRATOR = 0xFFAA00;
    private static final int COLOR_AMBIENT = 0xAA66FF;
    private static final int COLOR_ACTION = 0xFF4444;
    private static final int COLOR_SPEAKER = 0x66CCFF;

    public DialogueChoiceScreen(S2CDialoguePacket packet) {
        super(Component.literal(""));
        this.packet = packet;
        wrapText();
    }

    private void wrapText() {
        Minecraft mc = Minecraft.getInstance();
        String fullText = packet.speaker() + ": " + packet.text().getString();
        int maxWidth = this.width - 120;
        if (maxWidth < 200) maxWidth = 200;

        this.wrappedLines = mc.font.getSplitter().splitLines(
                        Component.literal(fullText),
                        maxWidth,
                        Style.EMPTY
                ).stream()
                .map(ft -> ft.getString())
                .toList();

        this.textHeight = this.wrappedLines.size() * (mc.font.lineHeight + 2);
    }

    @Override
    protected void init() {
        super.init();
        this.tickCounter = 0;
        this.alpha = 0.0f;
        this.buttonOffset = 30.0f;

        int buttonWidth = 220;
        int buttonHeight = 24;
        int totalButtons = packet.choices().size();

        int textY = this.height / 2 - 30 - this.textHeight / 2;
        int startY = textY + this.textHeight + 30;

        for (int i = 0; i < totalButtons; i++) {
            DialogueChoice choice = packet.choices().get(i);
            int yPos = startY + i * (buttonHeight + 8);
            Button btn = Button.builder(choice.text(), (btnWidget) -> {
                        PacketDistributor.sendToServer(new C2SDialogueChoicePacket(packet.treeId(), choice.id()));
                        this.onClose();
                    })
                    .bounds(this.width / 2 - buttonWidth / 2, yPos, buttonWidth, buttonHeight)
                    .build();
            this.choiceButtons.add(btn);
            this.addRenderableWidget(btn);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.tickCounter++;
        if (this.alpha < 1.0f) {
            this.alpha = Math.min(1.0f, this.alpha + 0.08f);
        }
        if (this.buttonOffset > 0.0f) {
            this.buttonOffset = Math.max(0.0f, this.buttonOffset - 2.0f);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Рисуем только наш кастомный фон — без блюра и без вызова super.render()
        this.renderCustomBackground(graphics);

        float animAlpha = Math.min(1.0f, this.alpha + partialTick * 0.05f);
        int alphaInt = (int) (animAlpha * 255);

        // Текст диалога
        int textY = this.height / 2 - 30 - this.textHeight / 2;
        drawDialogueText(graphics, textY, alphaInt);

        // Кнопки с анимацией
        float offset = this.buttonOffset - partialTick * 2.0f;
        if (offset < 0) offset = 0;

        for (int i = 0; i < this.choiceButtons.size(); i++) {
            Button btn = this.choiceButtons.get(i);
            int originalY = btn.getY();
            int offsetY = (int) (originalY + offset);
            btn.setY(offsetY);
            btn.visible = true;
            btn.setAlpha(animAlpha);
        }

        // Отрисовываем кнопки (без супер-рендера, чтобы не было лишнего фона)
        for (Button btn : this.choiceButtons) {
            btn.render(graphics, mouseX, mouseY, partialTick);
        }

        // Сбрасываем позиции кнопок для следующего кадра
        int startY = this.height / 2 - 30 - this.textHeight / 2 + this.textHeight + 30;
        for (int i = 0; i < this.choiceButtons.size(); i++) {
            Button btn = this.choiceButtons.get(i);
            int originalY = startY + i * (24 + 8);
            btn.setY(originalY);
        }
    }

    private void renderCustomBackground(GuiGraphics graphics) {
        int width = this.width;
        int height = this.height;

        // Полупрозрачный тёмный фон (без блюра)
        int bgAlpha = (int) (0.75f * 255);
        int color = (bgAlpha << 24) | 0x000000;
        graphics.fill(0, 0, width, height, color);

        // Акцентная рамка по краям
        int border = 40;
        int innerColor = (int) (0.85f * 255) << 24 | 0x111122;
        graphics.fill(border, border, width - border, height - border, innerColor);

        // Рамка цветом в зависимости от типа диалога
        int borderColor = getTypeColor() | 0x88000000;
        graphics.fill(border, border, width - border, border + 2, borderColor);
        graphics.fill(border, height - border - 2, width - border, height - border, borderColor);
        graphics.fill(border, border, border + 2, height - border, borderColor);
        graphics.fill(width - border - 2, border, width - border, height - border, borderColor);
    }

    private int getTypeColor() {
        DialogueType type = packet.dialogueType();
        if (type == null) return COLOR_AMBIENT;
        return switch (type) {
            case NARRATOR -> COLOR_NARRATOR;
            case ACTION -> COLOR_ACTION;
            default -> COLOR_AMBIENT;
        };
    }

    private void drawDialogueText(GuiGraphics graphics, int y, int alphaInt) {
        Minecraft mc = Minecraft.getInstance();

        int textColor = getTypeColor();
        int speakerColor = COLOR_SPEAKER;

        for (String line : wrappedLines) {
            int colonIndex = line.indexOf(":");
            int x = (this.width - mc.font.width(line)) / 2;
            if (x < 20) x = 20;

            if (colonIndex > 0 && colonIndex < line.length() - 1) {
                String speakerPart = line.substring(0, colonIndex + 1);
                String textPart = line.substring(colonIndex + 1);
                graphics.drawString(mc.font, speakerPart, x, y, speakerColor | (alphaInt << 24), false);
                graphics.drawString(mc.font, textPart, x + mc.font.width(speakerPart), y, textColor | (alphaInt << 24), false);
            } else {
                graphics.drawString(mc.font, line, x, y, textColor | (alphaInt << 24), false);
            }
            y += mc.font.lineHeight + 2;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false; // не ставим игру на паузу, чтобы избежать блюра
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        this.wrapText();
        this.rebuildWidgets();
    }
}
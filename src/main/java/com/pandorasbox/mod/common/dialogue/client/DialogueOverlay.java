package com.pandorasbox.mod.common.dialogue.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pandorasbox.mod.common.dialogue.api.DialogueType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DialogueOverlay implements LayeredDraw.Layer {

    private static final int COLOR_NARRATOR = 0xFFAA00;
    private static final int COLOR_AMBIENT = 0xAA66FF;
    private static final int COLOR_ACTION = 0xFF4444;
    private static final int COLOR_SPEAKER = 0x66CCFF;

    @Override
    public void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        if (ClientDialogueHandler.currentDialogue == null) {
            // Если нет диалога, но alpha > 0 (редкий случай), дорисовываем исчезание
            if (ClientDialogueHandler.alpha <= 0.01f) return;
        }

        Minecraft mc = Minecraft.getInstance();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        if (ClientDialogueHandler.currentDialogue == null) return;

        Component text = ClientDialogueHandler.currentDialogue.text();
        String speaker = ClientDialogueHandler.currentDialogue.speaker();
        DialogueType type = ClientDialogueHandler.currentDialogue.dialogueType();
        float alpha = ClientDialogueHandler.alpha;

        if (text.getString().isEmpty() || alpha <= 0.01f) return;

        int textAlpha = ((int) (alpha * 255) << 24);

        int textColor;
        if (type == null) textColor = COLOR_AMBIENT;
        else textColor = switch (type) {
            case NARRATOR -> COLOR_NARRATOR;
            case ACTION -> COLOR_ACTION;
            default -> COLOR_AMBIENT;
        };

        String fullText = speaker + ": " + text.getString();

        int maxWidth = width - 80;
        List<String> lines = mc.font.getSplitter().splitLines(
                        Component.literal(fullText),
                        maxWidth,
                        Style.EMPTY
                ).stream()
                .map(ft -> ft.getString())
                .toList();

        int lineHeight = mc.font.lineHeight + 2;
        int totalHeight = lines.size() * lineHeight;
        int startY = height - 80 - totalHeight / 2;

        int maxLineWidth = 0;
        for (String line : lines) {
            maxLineWidth = Math.max(maxLineWidth, mc.font.width(line));
        }
        int backgroundX = (width / 2) - (maxLineWidth / 2) - 15;
        int backgroundY = startY - 5;

        RenderSystem.enableBlend();

        // Фон с плавной прозрачностью
        int bgAlpha = (int) (0.7f * alpha * 255);
        int bgColor = (bgAlpha << 24) | 0x000000;
        graphics.fill(
                backgroundX,
                backgroundY,
                backgroundX + maxLineWidth + 30,
                backgroundY + totalHeight + 10,
                bgColor
        );

        int y = startY;
        for (String line : lines) {
            int colonIndex = line.indexOf(":");
            int x = (width / 2) - (mc.font.width(line) / 2);

            if (colonIndex > 0 && colonIndex < line.length() - 1) {
                String speakerPart = line.substring(0, colonIndex + 1);
                String textPart = line.substring(colonIndex + 1);
                graphics.drawString(mc.font, speakerPart, x, y, COLOR_SPEAKER | textAlpha, true);
                graphics.drawString(mc.font, textPart, x + mc.font.width(speakerPart), y, textColor | textAlpha, true);
            } else {
                graphics.drawString(mc.font, line, x, y, textColor | textAlpha, true);
            }
            y += lineHeight;
        }

        RenderSystem.disableBlend();
    }
}
package com.pandorasbox.mod.client.screen;

import com.pandorasbox.mod.common.quest.Quest;
import com.pandorasbox.mod.common.quest.QuestManager;
import com.pandorasbox.mod.common.quest.QuestTask;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class QuestLogScreen extends Screen {

    private final Player player;
    private List<Quest> activeQuests;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private final int entryHeight = 40;
    private final int taskLineHeight = 10;

    public QuestLogScreen(Player player) {
        super(Component.translatable("screen.pandorasbox.quest_log"));
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        refreshQuests();
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.pandorasbox.close"),
                        btn -> this.onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
        this.addRenderableWidget(Button.builder(
                        Component.literal("Refresh"),
                        btn -> refreshQuests())
                .bounds(this.width / 2 + 60, this.height - 30, 60, 20)
                .build());
        updateScroll();
    }

    private void refreshQuests() {
        this.activeQuests = QuestManager.getInstance().getActiveQuests(player);
        updateScroll();
    }

    private void updateScroll() {
        int totalHeight = activeQuests.size() * entryHeight;
        int visibleHeight = this.height - 80;
        maxScroll = Math.max(0, totalHeight - visibleHeight);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Пустой метод, чтобы стандартный фон не рисовался
        // Мы нарисуем свой фон в render()
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Рисуем свой полупрозрачный фон
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        // Заголовок
        graphics.drawCenteredString(this.font,
                Component.translatable("screen.pandorasbox.quest_log"),
                this.width / 2, 15, 0xFFFFFF);

        int yStart = 40;
        int visibleHeight = this.height - 80;

        graphics.enableScissor(10, yStart, this.width - 10, yStart + visibleHeight);

        int currentY = yStart - scrollOffset;
        for (Quest quest : activeQuests) {
            if (currentY + entryHeight > yStart && currentY < yStart + visibleHeight) {
                drawQuestEntry(graphics, quest, 20, currentY, this.width - 40);
            }
            currentY += entryHeight;
        }

        graphics.disableScissor();

        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(20, visibleHeight * visibleHeight / (activeQuests.size() * entryHeight));
            int scrollBarY = yStart + (int) ((float) scrollOffset / maxScroll * (visibleHeight - scrollBarHeight));
            graphics.fill(this.width - 8, scrollBarY, this.width - 4, scrollBarY + scrollBarHeight, 0xAAFFFFFF);
        }

        // Вызываем super.render() чтобы нарисовать кнопки (они будут поверх фона)
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawQuestEntry(GuiGraphics graphics, Quest quest, int x, int y, int width) {
        graphics.fill(x, y, x + width, y + entryHeight, 0x44000000);
        graphics.drawString(this.font, quest.getTitle(), x + 4, y + 2, 0xFFFF55, false);
        graphics.drawString(this.font, quest.getDescription(), x + 4, y + 14, 0xCCCCCC, false);

        String progress = "Progress: " + quest.getProgress() + "/" + quest.getTotalProgress();
        graphics.drawString(this.font, progress, x + width - 120, y + 2, 0xAAAAAA, false);

        int taskY = y + 26;
        for (QuestTask task : quest.getTasks()) {
            String taskDesc = task.getDescription() + " (" + task.getProgress() + "/" + task.getRequired() + ")";
            int color = task.isCompleted() ? 0x55FF55 : 0xFF5555;
            graphics.drawString(this.font, taskDesc, x + 8, taskY, color, false);
            taskY += taskLineHeight;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 15));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
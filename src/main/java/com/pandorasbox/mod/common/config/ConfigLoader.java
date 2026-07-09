package com.pandorasbox.mod.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.dialogue.api.DialogueChoice;
import com.pandorasbox.mod.common.dialogue.api.DialogueNode;
import com.pandorasbox.mod.common.dialogue.api.DialogueTree;
import com.pandorasbox.mod.common.dialogue.api.DialogueType;
import com.pandorasbox.mod.common.dialogue.server.DialogueManager;
import com.pandorasbox.mod.common.quest.Quest;
import com.pandorasbox.mod.common.quest.QuestChain;
import com.pandorasbox.mod.common.quest.QuestManager;
import com.pandorasbox.mod.common.quest.QuestTask;
import com.pandorasbox.mod.common.quest.tasks.*;
import com.pandorasbox.mod.common.reward.Reward;
import com.pandorasbox.mod.common.reward.RewardHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

public class ConfigLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Random RANDOM = new Random();

    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("pandoras_box");
    private static final Path QUESTS_DIR = CONFIG_DIR.resolve("quests");
    private static final Path EVENTS_DIR = CONFIG_DIR.resolve("events");
    private static final Path DIALOGUES_DIR = CONFIG_DIR.resolve("dialogues");
    private static final Path SETTINGS_FILE = CONFIG_DIR.resolve("settings.json");

    private static final Map<String, Function<JsonObject, QuestTask>> TASK_PARSERS = new HashMap<>();
    private static final List<String> RANDOM_PHRASES = new ArrayList<>();

    // Настройки из settings.json
    private static String initialDialogue = "introduction";
    private static String initialQuest = "awakening";
    private static List<String> deathDialogues = List.of("death_comment_1", "death_comment_2", "death_comment_3");

    static {
        registerTaskType("kill_entity", ConfigLoader::parseKillEntityTask);
        registerTaskType("break_block", ConfigLoader::parseBlockBreakTask);
        registerTaskType("location", ConfigLoader::parseLocationTask);
        registerTaskType("dialogue_choice", ConfigLoader::parseDialogueChoiceTask);
        registerTaskType("craft_item", ConfigLoader::parseCraftItemTask);
        registerTaskType("use_item", ConfigLoader::parseUseItemTask);
        registerTaskType("reach_level", ConfigLoader::parseReachLevelTask);
        registerTaskType("interact_block", ConfigLoader::parseInteractBlockTask);
    }

    private ConfigLoader() {}

    public static void registerTaskType(String typeId, Function<JsonObject, QuestTask> parser) {
        TASK_PARSERS.put(typeId, parser);
    }

    public static void ensureConfigDirectoriesExist() {
        try {
            Files.createDirectories(QUESTS_DIR);
            Files.createDirectories(EVENTS_DIR);
            Files.createDirectories(DIALOGUES_DIR);
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to create Pandora's Box config directories", e);
        }
    }

    public static void copyDefaultConfigsIfMissing() {
        boolean hasQuests = false;
        if (Files.exists(QUESTS_DIR)) {
            try (Stream<Path> paths = Files.list(QUESTS_DIR)) {
                hasQuests = paths.anyMatch(p -> p.toString().endsWith(".json"));
            } catch (IOException ignored) {}
        }
        if (hasQuests) {
            PandorasBoxMod.LOGGER.info("Config files already exist, skipping default copy.");
            return;
        }
        PandorasBoxMod.LOGGER.info("No quest configs found, creating placeholder.");
        try {
            Files.createDirectories(QUESTS_DIR);
            Files.createDirectories(EVENTS_DIR);
            Files.createDirectories(DIALOGUES_DIR);
            Path marker = QUESTS_DIR.resolve(".generated");
            if (!Files.exists(marker)) {
                Files.createFile(marker);
            }
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to create default config marker", e);
        }
    }

    // ---------- ЗАГРУЗКА НАСТРОЕК ----------
    public static void loadSettings() {
        if (!Files.exists(SETTINGS_FILE)) {
            PandorasBoxMod.LOGGER.info("settings.json not found, using default settings.");
            return;
        }
        try (Reader reader = Files.newBufferedReader(SETTINGS_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("initialDialogue") && !json.get("initialDialogue").isJsonNull()) {
                initialDialogue = json.get("initialDialogue").getAsString();
            }
            if (json.has("initialQuest") && !json.get("initialQuest").isJsonNull()) {
                initialQuest = json.get("initialQuest").getAsString();
            }
            if (json.has("deathDialogues") && json.get("deathDialogues").isJsonArray()) {
                JsonArray arr = json.getAsJsonArray("deathDialogues");
                List<String> list = new ArrayList<>();
                for (JsonElement el : arr) {
                    list.add(el.getAsString());
                }
                if (!list.isEmpty()) {
                    deathDialogues = list;
                }
            }
            PandorasBoxMod.LOGGER.info("Loaded settings: initialDialogue={}, initialQuest={}, deathDialogues={}",
                    initialDialogue, initialQuest, deathDialogues);
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to load settings.json", e);
        }
    }

    public static String getInitialDialogue() { return initialDialogue; }
    public static String getInitialQuest() { return initialQuest; }
    public static List<String> getDeathDialogues() { return deathDialogues; }

    // ---------- ЗАГРУЗКА КВЕСТОВ ----------
    public static void loadAllQuests() {
        if (!Files.exists(QUESTS_DIR)) {
            PandorasBoxMod.LOGGER.info("Quest directory does not exist, skipping loading.");
            return;
        }
        int loaded = 0;
        try (Stream<Path> paths = Files.list(QUESTS_DIR)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                if (!path.toString().endsWith(".json")) continue;
                try (Reader reader = Files.newBufferedReader(path)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    if (json.has("chainId")) {
                        QuestChain chain = parseQuestChain(json);
                        QuestManager.getInstance().registerQuestChain(chain);
                        PandorasBoxMod.LOGGER.info("Registered quest chain: {}", chain.getChainId());
                        loaded++;
                    } else if (json.has("questId")) {
                        Quest quest = parseQuest(json);
                        QuestManager.getInstance().registerQuest(quest);
                        PandorasBoxMod.LOGGER.info("Registered quest: {}", quest.getQuestId());
                        loaded++;
                    } else {
                        PandorasBoxMod.LOGGER.warn("Unknown quest file format: {}", path);
                    }
                } catch (Exception e) {
                    PandorasBoxMod.LOGGER.error("Failed to load quest from {}", path, e);
                }
            }
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to list quest files", e);
        }
        PandorasBoxMod.LOGGER.info("Loaded {} quests/chains.", loaded);
    }

    private static QuestChain parseQuestChain(JsonObject json) {
        QuestChain chain = new QuestChain(json.get("chainId").getAsString());
        for (JsonElement el : json.getAsJsonArray("quests")) {
            chain.addQuest(parseQuest(el.getAsJsonObject()));
        }
        return chain;
    }

    // ---------- ЗАГРУЗКА ДИАЛОГОВ ИЗ CONFIG ----------
    public static void loadAllDialogues() {
        if (!Files.exists(DIALOGUES_DIR)) {
            PandorasBoxMod.LOGGER.info("Dialogues directory does not exist, skipping loading.");
            return;
        }
        int loaded = 0;
        try (Stream<Path> paths = Files.list(DIALOGUES_DIR)) {
            for (Path path : (Iterable<Path>) paths::iterator) {
                if (!path.toString().endsWith(".json")) continue;
                try (Reader reader = Files.newBufferedReader(path)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    DialogueTree tree = parseDialogueTree(json);
                    if (tree != null) {
                        DialogueManager.registerTree(tree);
                        loaded++;
                    }
                } catch (Exception e) {
                    PandorasBoxMod.LOGGER.error("Failed to load dialogue from {}", path, e);
                }
            }
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to list dialogue files", e);
        }
        PandorasBoxMod.LOGGER.info("Loaded {} dialogue trees.", loaded);
    }

    private static DialogueTree parseDialogueTree(JsonObject json) {
        try {
            String treeId = json.get("treeId").getAsString();
            String startNode = json.get("startNode").getAsString();
            JsonObject nodesJson = json.getAsJsonObject("nodes");

            Map<String, DialogueNode> nodes = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : nodesJson.entrySet()) {
                JsonObject nodeObj = entry.getValue().getAsJsonObject();
                String id = nodeObj.get("id").getAsString();
                String speaker = nodeObj.has("speaker") ? nodeObj.get("speaker").getAsString() : "Narrator";
                Component text = Component.literal(nodeObj.get("text").getAsString());
                int duration = nodeObj.has("duration") ? nodeObj.get("duration").getAsInt() : 4;
                DialogueType type = DialogueType.AMBIENT;
                try {
                    type = DialogueType.valueOf(nodeObj.get("type").getAsString());
                } catch (IllegalArgumentException | NullPointerException e) {
                    // оставляем AMBIENT
                }
                String nextNode = nodeObj.has("nextNode") && !nodeObj.get("nextNode").isJsonNull() ? nodeObj.get("nextNode").getAsString() : null;

                List<DialogueChoice> choices = new ArrayList<>();
                if (nodeObj.has("choices")) {
                    JsonArray choicesArr = nodeObj.getAsJsonArray("choices");
                    for (JsonElement cElem : choicesArr) {
                        JsonObject cObj = cElem.getAsJsonObject();
                        choices.add(new DialogueChoice(
                                cObj.get("id").getAsInt(),
                                Component.literal(cObj.get("text").getAsString()),
                                cObj.get("nextNode").getAsString(),
                                cObj.has("onSelectQuestTaskId") ? cObj.get("onSelectQuestTaskId").getAsString() : null
                        ));
                    }
                }
                nodes.put(id, new DialogueNode(id, speaker, text, duration, type, choices, nextNode));
            }
            return new DialogueTree(treeId, startNode, nodes);
        } catch (Exception e) {
            PandorasBoxMod.LOGGER.error("Failed to parse dialogue tree: {}", e.getMessage());
            return null;
        }
    }

    // ---------- СЛУЧАЙНЫЕ ФРАЗЫ ----------
    public static void loadRandomPhrases() {
        RANDOM_PHRASES.clear();
        Path path = CONFIG_DIR.resolve("random_phrases.json");
        if (!Files.exists(path)) {
            PandorasBoxMod.LOGGER.info("random_phrases.json not found, skipping random phrases.");
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray arr = json.getAsJsonArray("phrases");
            for (JsonElement el : arr) {
                RANDOM_PHRASES.add(el.getAsString());
            }
            PandorasBoxMod.LOGGER.info("Loaded {} random phrases.", RANDOM_PHRASES.size());
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to load random phrases", e);
        }
    }

    public static String getRandomPhrase() {
        if (RANDOM_PHRASES.isEmpty()) return null;
        return RANDOM_PHRASES.get(RANDOM.nextInt(RANDOM_PHRASES.size()));
    }

    // ---------- ЗАГРУЗКА ОДНОГО КВЕСТА (по id) ----------
    public static Quest loadQuest(String questId) {
        Path path = QUESTS_DIR.resolve(questId + ".json");
        if (!Files.exists(path)) return null;

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            return parseQuest(json);
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to load quest: {}", questId, e);
            return null;
        }
    }

    // ---------- ПАРСИНГ КВЕСТА ИЗ JSON ----------
    private static Quest parseQuest(JsonObject json) {
        String questId = json.get("questId").getAsString();
        String title = json.get("title").getAsString();
        String description = json.get("description").getAsString();

        Quest quest = new Quest(questId, title, description);
        quest.setRepeatable(json.has("repeatable") && json.get("repeatable").getAsBoolean());

        // Парсинг startDialogue
        if (json.has("startDialogue")) {
            quest.setStartDialogueId(json.get("startDialogue").getAsString());
        }

        if (json.has("tasks")) {
            for (JsonElement el : json.getAsJsonArray("tasks")) {
                QuestTask task = parseTask(el.getAsJsonObject());
                if (task != null) quest.addTask(task);
            }
        }

        if (json.has("rewards")) {
            for (JsonElement el : json.getAsJsonArray("rewards")) {
                JsonObject rewardJson = el.getAsJsonObject();
                String type = rewardJson.get("type").getAsString();
                Reward reward = RewardHandler.parseReward(type, rewardJson);
                quest.addReward(reward);
            }
        }

        if (json.has("onCompletionDialogue")) {
            quest.setOnCompletionDialogueId(json.get("onCompletionDialogue").getAsString());
        }
        if (json.has("nextQuestId")) {
            quest.setNextQuestId(json.get("nextQuestId").getAsString());
        }

        return quest;
    }

    private static QuestTask parseTask(JsonObject json) {
        String type = json.get("type").getAsString();
        Function<JsonObject, QuestTask> parser = TASK_PARSERS.get(type);
        if (parser == null) {
            PandorasBoxMod.LOGGER.warn("Unknown quest task type: {}", type);
            return null;
        }

        QuestTask task = parser.apply(json);
        task.setTaskId(json.has("taskId") ? json.get("taskId").getAsString() : type);
        task.setDescription(json.has("description") ? json.get("description").getAsString() : "");
        task.setRequired(json.has("required") ? json.get("required").getAsInt() : 1);
        return task;
    }

    // ----- Парсеры задач -----
    private static QuestTask parseKillEntityTask(JsonObject json) {
        KillEntityTask task = new KillEntityTask();
        task.setEntityType(json.get("entityType").getAsString());
        return task;
    }

    private static QuestTask parseBlockBreakTask(JsonObject json) {
        BlockBreakTask task = new BlockBreakTask();
        task.setBlockType(json.get("blockType").getAsString());
        return task;
    }

    private static QuestTask parseLocationTask(JsonObject json) {
        LocationTask task = new LocationTask();
        task.setLocation(new BlockPos(
                json.get("locationX").getAsInt(),
                json.get("locationY").getAsInt(),
                json.get("locationZ").getAsInt()
        ));
        task.setRadius(json.has("radius") ? json.get("radius").getAsInt() : 10);
        return task;
    }

    private static QuestTask parseDialogueChoiceTask(JsonObject json) {
        DialogueChoiceTask task = new DialogueChoiceTask();
        task.setRequiredChoiceId(json.has("requiredChoice") ? json.get("requiredChoice").getAsInt() : -1);
        return task;
    }

    private static QuestTask parseCraftItemTask(JsonObject json) {
        CraftItemTask task = new CraftItemTask();
        task.setItemType(json.get("itemType").getAsString());
        return task;
    }

    private static QuestTask parseUseItemTask(JsonObject json) {
        UseItemTask task = new UseItemTask();
        task.setItemType(json.get("itemType").getAsString());
        return task;
    }

    private static QuestTask parseReachLevelTask(JsonObject json) {
        ReachLevelTask task = new ReachLevelTask();
        task.setTargetLevel(json.get("targetLevel").getAsInt());
        return task;
    }

    private static QuestTask parseInteractBlockTask(JsonObject json) {
        InteractBlockTask task = new InteractBlockTask();
        task.setBlockType(json.get("blockType").getAsString());
        return task;
    }

}
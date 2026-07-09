//package com.pandorasbox.mod.common.dialogue.server;
//
//import com.google.gson.*;
//import com.pandorasbox.mod.PandorasBoxMod;
//import com.pandorasbox.mod.common.dialogue.api.DialogueChoice;
//import com.pandorasbox.mod.common.dialogue.api.DialogueNode;
//import com.pandorasbox.mod.common.dialogue.api.DialogueTree;
//import com.pandorasbox.mod.common.dialogue.api.DialogueType;
//import net.minecraft.network.chat.Component;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.packs.resources.ResourceManager;
//import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
//import net.minecraft.util.profiling.ProfilerFiller;
//import net.neoforged.bus.api.IEventBus;
//import net.neoforged.neoforge.event.AddReloadListenerEvent;
//
//import java.util.*;
//
//public class DialogueDataLoader extends SimpleJsonResourceReloadListener {
//    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
//    private static final Map<String, DialogueTree> DIALOGUES = new HashMap<>();
//
//    public DialogueDataLoader() {
//        // В NeoForge 1.21.1 конструктор принимает Gson и путь к папке в датапаке
//        super(GSON, "dialogues");
//    }
//
//    public static void init(IEventBus modBus, IEventBus forgeBus) {
//        forgeBus.addListener(DialogueDataLoader::onAddReloadListener);
//    }
//
//    private static void onAddReloadListener(AddReloadListenerEvent event) {
//        event.addListener(new DialogueDataLoader());
//    }
//
//    public static DialogueTree getTree(String id) {
//        return DIALOGUES.get(id);
//    }
//
//    @Override
//    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
//        DIALOGUES.clear();
//        object.forEach((location, element) -> {
//            try {
//                JsonObject json = element.getAsJsonObject();
//                String treeId = json.get("treeId").getAsString();
//                String startNode = json.get("startNode").getAsString();
//                JsonObject nodesJson = json.getAsJsonObject("nodes");
//
//                Map<String, DialogueNode> nodes = new HashMap<>();
//                for (Map.Entry<String, JsonElement> entry : nodesJson.entrySet()) {
//                    JsonObject nodeObj = entry.getValue().getAsJsonObject();
//                    String id = nodeObj.get("id").getAsString();
//                    String speaker = nodeObj.has("speaker") ? nodeObj.get("speaker").getAsString() : "Narrator";
//
//                    // Простое преобразование текста — используем Component.literal
//                    Component text = Component.literal(nodeObj.get("text").getAsString());
//
//                    int duration = nodeObj.has("duration") ? nodeObj.get("duration").getAsInt() : 4;
//                    DialogueType type = nodeObj.has("type") ? DialogueType.valueOf(nodeObj.get("type").getAsString()) : DialogueType.AMBIENT;
//                    String nextNode = nodeObj.has("nextNode") && !nodeObj.get("nextNode").isJsonNull() ? nodeObj.get("nextNode").getAsString() : null;
//
//                    List<DialogueChoice> choices = new ArrayList<>();
//                    if (nodeObj.has("choices")) {
//                        JsonArray choicesArr = nodeObj.getAsJsonArray("choices");
//                        for (JsonElement cElem : choicesArr) {
//                            JsonObject cObj = cElem.getAsJsonObject();
//                            choices.add(new DialogueChoice(
//                                    cObj.get("id").getAsInt(),
//                                    Component.literal(cObj.get("text").getAsString()),
//                                    cObj.get("nextNode").getAsString()
//                            ));
//                        }
//                    }
//                    nodes.put(id, new DialogueNode(id, speaker, text, duration, type, choices, nextNode));
//                }
//                DIALOGUES.put(treeId, new DialogueTree(treeId, startNode, nodes));
//                PandorasBoxMod.LOGGER.info("Loaded dialogue tree: {}", treeId);
//            } catch (Exception e) {
//                PandorasBoxMod.LOGGER.error("Failed to load dialogue data asset: {} - {}", location, e.getMessage());
//            }
//        });
//    }
//}
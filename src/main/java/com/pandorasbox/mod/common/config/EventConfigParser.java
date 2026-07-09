package com.pandorasbox.mod.common.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.event.ActionCommentaryEntry;
import com.pandorasbox.mod.common.event.ActionCommentaryHandler;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class EventConfigParser {

    private static final Path EVENTS_DIR = FMLPaths.CONFIGDIR.get()
            .resolve("pandoras_box").resolve("events");

    private EventConfigParser() {}

    /** Загружает все *.json файлы из config/pandoras_box/events/ и регистрирует записи. */
    public static void loadAllEventConfigs() {
        if (!Files.exists(EVENTS_DIR)) return;

        try (Stream<Path> paths = Files.list(EVENTS_DIR)) {
            paths.filter(p -> p.toString().endsWith(".json"))
                    .forEach(EventConfigParser::loadEventConfigFile);
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to list event config files", e);
        }
    }

    private static void loadEventConfigFile(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (!json.has("events")) return;

            for (JsonElement el : json.getAsJsonArray("events")) {
                JsonObject eventObj = el.getAsJsonObject();
                ActionCommentaryEntry entry = new ActionCommentaryEntry();
                entry.setEventId(eventObj.get("eventId").getAsString());
                entry.setTriggerType(eventObj.get("triggerType").getAsString());
                entry.setMatchValue(getOptionalString(eventObj, "entityType",
                        getOptionalString(eventObj, "blockType",
                                getOptionalString(eventObj, "dimension", null))));
                entry.setDialogueId(eventObj.get("dialogueId").getAsString());
                entry.setOneTime(eventObj.has("isOneTime") && eventObj.get("isOneTime").getAsBoolean());

                ActionCommentaryHandler.register(entry);
            }
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to load event config: {}", path, e);
        }
    }

    private static String getOptionalString(JsonObject json, String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }
}

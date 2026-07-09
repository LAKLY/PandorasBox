package com.pandorasbox.mod.common.config;

import com.pandorasbox.mod.PandorasBoxMod;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class DefaultConfigGenerator {

    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve("pandoras_box");
    private static final String DEFAULT_CONFIGS_ROOT = "/default_configs/";

    public static void generateDefaultConfigsIfMissing() {
        if (Files.exists(CONFIG_DIR) && hasAnyConfigFile()) {
            PandorasBoxMod.LOGGER.info("Config files already exist, skipping generation.");
            return;
        }

        PandorasBoxMod.LOGGER.info("Generating default config files...");
        try {
            Files.createDirectories(CONFIG_DIR);
            copyResource("settings.json", CONFIG_DIR.resolve("settings.json"));
            copyResource("random_phrases.json", CONFIG_DIR.resolve("random_phrases.json"));

            // quests
            Path questsDir = CONFIG_DIR.resolve("quests");
            Files.createDirectories(questsDir);
            copyResource("quests/awakening.json", questsDir.resolve("awakening.json"));
            copyResource("quests/first_trial.json", questsDir.resolve("first_trial.json"));

            // dialogues
            Path dialoguesDir = CONFIG_DIR.resolve("dialogues");
            Files.createDirectories(dialoguesDir);
            String[] dialogues = {
                    "introduction.json", "awakening_complete.json", "first_trial_complete.json",
                    "death_comment_1.json", "death_comment_2.json", "death_comment_3.json",
                    "first_stone_comment.json", "response_curious.json", "response_obedient.json",
                    "response_skeptical.json"
            };
            for (String dialog : dialogues) {
                copyResource("dialogues/" + dialog, dialoguesDir.resolve(dialog));
            }

            // events
            Path eventsDir = CONFIG_DIR.resolve("events");
            Files.createDirectories(eventsDir);
            copyResource("events/player_actions.json", eventsDir.resolve("player_actions.json"));

            PandorasBoxMod.LOGGER.info("Default config files generated successfully.");
        } catch (IOException e) {
            PandorasBoxMod.LOGGER.error("Failed to generate default config files", e);
        }
    }

    private static void copyResource(String resourcePath, Path targetPath) throws IOException {
        try (InputStream in = DefaultConfigGenerator.class.getResourceAsStream(DEFAULT_CONFIGS_ROOT + resourcePath)) {
            if (in == null) {
                PandorasBoxMod.LOGGER.warn("Default config resource not found: {}", resourcePath);
                return;
            }
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean hasAnyConfigFile() {
        try {
            Path questsDir = CONFIG_DIR.resolve("quests");
            if (Files.exists(questsDir)) {
                try (var stream = Files.list(questsDir)) {
                    return stream.anyMatch(p -> p.toString().endsWith(".json"));
                }
            }
        } catch (IOException ignored) {}
        return false;
    }
}
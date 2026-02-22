package com.nododiiiii.ponderer.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.nododiiiii.ponderer.Config;
import com.nododiiiii.ponderer.ponder.DslScene;
import com.nododiiiii.ponderer.ponder.LocalizedText;
import com.nododiiiii.ponderer.ponder.PondererClientCommands;
import com.nododiiiii.ponderer.ponder.SceneStore;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Orchestrator: assembles the prompt, calls the LLM provider, validates the result,
 * writes the scene JSON to disk and triggers reload.
 */
public class AiSceneGenerator {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalizedText.class, new LocalizedText.GsonAdapter())
        .setLenient()
        .create();
    private static final Gson GSON_PRETTY = new GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(LocalizedText.class, new LocalizedText.GsonAdapter())
        .create();

    /**
     * Generate a ponder scene from structure + user prompt.
     *
     * @param structurePaths  list of NBT file paths for the structures
     * @param carrierItemId   the item ID this scene is for (e.g. "minecraft:piston")
     * @param userPrompt      user's natural language prompt
     * @param referenceUrls   optional reference web page URLs
     * @param existingJson    existing JSON for adjustment mode (null for new generation)
     * @param onSuccess       callback on main thread with the saved file path
     * @param onError         callback on main thread with error message
     */
    public static void generate(List<Path> structurePaths, String carrierItemId, String userPrompt,
                                 List<String> referenceUrls, String existingJson,
                                 Consumer<String> onSuccess, Consumer<String> onError) {
        String apiKey = Config.AI_API_KEY.get().trim();
        if (apiKey.isEmpty()) {
            onError.accept("API key not configured. Set it in Settings > AI Configuration.");
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                // 1. Parse structures
                List<StructureDescriber.StructureInfo> structures = new ArrayList<>();
                List<String> structureNames = new ArrayList<>();
                List<String> allBlockTypes = new ArrayList<>();

                for (Path nbtPath : structurePaths) {
                    StructureDescriber.StructureInfo info = StructureDescriber.describe(nbtPath);
                    structures.add(info);
                    String name = nbtPath.getFileName().toString();
                    if (name.endsWith(".nbt")) name = name.substring(0, name.length() - 4);
                    structureNames.add("ponderer:" + name);
                    for (String bt : info.blockTypes()) {
                        if (!allBlockTypes.contains(bt)) allBlockTypes.add(bt);
                    }
                }

                // 2. Build name→ID mapping (relevant subset)
                String registryMapping = RegistryMapper.buildRelevantMapping(allBlockTypes);

                // 3. Fetch web pages (if any)
                List<LlmProvider.ContentBlock> webContent = new ArrayList<>();
                for (String url : referenceUrls) {
                    if (url == null || url.isBlank()) continue;
                    try {
                        WebPageFetcher.WebPageContent page = WebPageFetcher.fetch(url);
                        webContent.add(new LlmProvider.ContentBlock.Text(
                            "=== Reference web page: " + url + " ===\n" + page.text()));
                        for (WebPageFetcher.ImageData img : page.images()) {
                            webContent.add(new LlmProvider.ContentBlock.Image(img.base64(), img.mediaType()));
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Failed to fetch reference URL: {}", url, e);
                        webContent.add(new LlmProvider.ContentBlock.Text(
                            "(Failed to fetch " + url + ": " + e.getMessage() + ")"));
                    }
                }

                // 4. Build system prompt
                String systemPrompt = buildSystemPrompt();

                // 5. Build user content
                List<LlmProvider.ContentBlock> userContent = new ArrayList<>();

                // Structure descriptions
                StringBuilder structDesc = new StringBuilder();
                structDesc.append("=== Structure Information ===\n");
                structDesc.append("Structure pool:\n");
                for (int i = 0; i < structureNames.size(); i++) {
                    structDesc.append("  ").append(i + 1).append(". ").append(structureNames.get(i)).append("\n");
                }
                structDesc.append("\n");
                for (int i = 0; i < structures.size(); i++) {
                    structDesc.append("--- Structure: ").append(structureNames.get(i)).append(" ---\n");
                    structDesc.append(structures.get(i).textDescription()).append("\n");
                }
                userContent.add(new LlmProvider.ContentBlock.Text(structDesc.toString()));

                // Registry mapping
                userContent.add(new LlmProvider.ContentBlock.Text(registryMapping));

                // Web content
                userContent.addAll(webContent);

                // Existing JSON (adjustment mode)
                if (existingJson != null && !existingJson.isBlank()) {
                    userContent.add(new LlmProvider.ContentBlock.Text(
                        "=== Current scene JSON (modify this based on the instruction below) ===\n" + existingJson));
                }

                // User prompt
                userContent.add(new LlmProvider.ContentBlock.Text(
                    "=== User instruction ===\n" + userPrompt +
                    "\n\nTarget item ID: " + carrierItemId +
                    "\nStructures: " + String.join(", ", structureNames)));

                // 6. Call LLM
                String provider = Config.AI_PROVIDER.get();
                LlmProvider llm = "anthropic".equals(provider)
                    ? new AnthropicProvider() : new OpenAiCompatProvider();

                String response = llm.generate(systemPrompt, userContent,
                    Config.getEffectiveBaseUrl(), apiKey, Config.getEffectiveModel()).join();

                // 7. Extract JSON from response (strip markdown fences if present)
                String json = extractJson(response);

                // 8. Validate by parsing
                DslScene scene = GSON.fromJson(json, DslScene.class);
                if (scene == null || scene.id == null || scene.id.isBlank()) {
                    throw new RuntimeException("Generated scene has no ID");
                }

                // 9. Pretty-print and save
                String prettyJson = GSON_PRETTY.toJson(scene);
                Path sceneDir = SceneStore.getSceneDir();
                Files.createDirectories(sceneDir);
                String fileName = scene.id.replace(":", "/") + ".json";
                if (scene.id.startsWith("ponderer:")) {
                    fileName = scene.id.substring("ponderer:".length()) + ".json";
                }
                Path filePath = sceneDir.resolve(fileName);
                Files.createDirectories(filePath.getParent());
                Files.writeString(filePath, prettyJson, StandardCharsets.UTF_8);

                return filePath.toString();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }).thenAcceptAsync(filePath -> {
            PondererClientCommands.reloadLocal();
            onSuccess.accept(filePath);
        }, Minecraft.getInstance()).exceptionally(ex -> {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            LOGGER.error("AI scene generation failed", cause);
            String msg = cause.getMessage();
            if (msg == null) msg = cause.getClass().getSimpleName();
            String finalMsg = msg;
            Minecraft.getInstance().execute(() -> onError.accept(finalMsg));
            return null;
        });
    }

    /** Extract JSON object from LLM response, stripping markdown code fences if present. */
    private static String extractJson(String response) {
        String trimmed = response.trim();
        // Strip ```json ... ``` fences
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline > 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
            trimmed = trimmed.trim();
        }
        // Find the JSON object
        int braceStart = trimmed.indexOf('{');
        int braceEnd = trimmed.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return trimmed.substring(braceStart, braceEnd + 1);
        }
        return trimmed;
    }

    private static String buildSystemPrompt() {
        return """
            You are a Minecraft Ponder scene designer. You create JSON scene files for the Ponderer mod.

            A Ponder scene is an interactive tutorial that demonstrates game mechanics using a 3D structure.
            The scene is composed of sequential steps that show structures, add text annotations, manipulate blocks, spawn entities, and control the camera.

            ## JSON Schema

            Top-level fields:
            - "id": string (required) - Unique scene identifier, format "ponderer:<name>"
            - "items": string[] (required) - Item IDs that trigger this scene
            - "title": {"en_us": "...", "zh_cn": "..."} - Localized title
            - "structures": string[] - Structure pool references
            - "tags": string[] - Tags (usually empty)
            - "steps": [] - Leave empty, use scenes instead
            - "scenes": array of scene segments

            Each scene segment:
            - "id": string - Segment identifier
            - "title": {"en_us": "...", "zh_cn": "..."} - Segment title
            - "steps": array of step objects

            ## Step Types

            ### Structure & Overlay
            - show_structure: {structure: "ponderer:name", height?: int, attachKeyFrame?: bool}
            - idle: {duration: int (ticks, 20=1sec)}
            - text: {duration: int, text: {"en_us":"...", "zh_cn":"..."}, point: [x,y,z], color?: "green"|"blue"|"red"|"cyan"|"input", placeNearTarget?: bool, attachKeyFrame?: bool}
            - shared_text: {duration: int, key: "lang.key", point: [x,y,z], color?: string, placeNearTarget?: bool}
            - rotate_camera_y: {degrees: float, attachKeyFrame?: bool}
            - show_controls: {duration: int, point: [x,y,z], direction: "up"|"down"|"left"|"right", action: "left"|"right"|"scroll", item?: "item:id", whileSneaking?: bool, whileCTRL?: bool}
            - play_sound: {sound: "event.id", soundVolume?: float, pitch?: float, source?: "master"|"music"|...}
            - encapsulate_bounds: {bounds: [x,y,z]}

            ### Block Operations
            - set_block: {block: "block:id", blockProperties?: {"key":"val"}, blockPos: [x,y,z], blockPos2?: [x,y,z], spawnParticles?: bool}
            - destroy_block: {blockPos: [x,y,z], destroyParticles?: bool}
            - replace_blocks: {block: "block:id", blockPos: [x,y,z], blockPos2: [x,y,z], spawnParticles?: bool}
            - hide_section: {direction: "up"|"down"|"east"|"west"|"north"|"south", blockPos: [x,y,z], blockPos2: [x,y,z]}
            - show_section_and_merge: {direction: string, linkId: string, blockPos: [x,y,z], blockPos2: [x,y,z]}
            - toggle_redstone_power: {blockPos: [x,y,z], blockPos2: [x,y,z]}
            - modify_block_entity_nbt: {blockPos: [x,y,z], nbt: "SNBT string", reDrawBlocks?: bool}

            ### Entities & Effects
            - create_entity: {entity: "entity:id", pos: [x,y,z], yaw?: float, pitch?: float}
            - create_item_entity: {item: "item:id", pos: [x,y,z], motion?: [x,y,z], count?: int}
            - rotate_section: {duration: int, linkId: string, rotX?: float, rotY?: float, rotZ?: float}
            - move_section: {duration: int, linkId: string, offset: [x,y,z]}
            - indicate_redstone: {blockPos: [x,y,z]}
            - indicate_success: {blockPos: [x,y,z]}

            ## Coordinate System
            - X: east-west, Y: up-down, Z: north-south
            - [0,0,0] is the bottom-north-west corner of the structure
            - Text point coordinates are float (e.g. [2.5, 1.5, 2.5] for center of block)
            - Block positions are integer [x,y,z]
            - All coordinates must be within the structure bounds

            ## Guidelines
            1. Always start each scene segment with show_structure
            2. Use idle steps between content steps (20-40 ticks typical)
            3. Place text near relevant blocks using placeNearTarget: true
            4. Use attachKeyFrame: true on important steps for timeline markers
            5. Provide both en_us and zh_cn text where possible
            6. Keep scenes focused and concise (5-15 steps per segment)
            7. Use color to highlight importance: green=info, red=warning, input=interaction

            ## Important
            - Output ONLY valid JSON, no markdown fences, no explanation
            - The JSON must be a complete, valid DslScene object
            - Use the exact block/item/entity IDs from the registry mapping provided
            - Screenshots from tutorials are for understanding the workflow/steps only; use the NBT structure data for accurate block information
            """;
    }
}

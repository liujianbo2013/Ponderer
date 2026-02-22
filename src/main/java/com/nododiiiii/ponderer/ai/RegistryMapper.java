package com.nododiiiii.ponderer.ai;

import net.minecraft.core.registries.BuiltInRegistries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a display-name-to-registry-ID mapping from vanilla registries.
 * Used to help the LLM resolve localized names to proper IDs.
 */
public class RegistryMapper {

    /**
     * Build a mapping of display name → registry ID for blocks that appear in the structure.
     * Only includes blocks from the provided list to keep the prompt concise.
     */
    public static String buildRelevantMapping(List<String> structureBlockIds) {
        Map<String, String> mapping = new LinkedHashMap<>();

        // Add all blocks from the structure
        for (String blockId : structureBlockIds) {
            var loc = net.minecraft.resources.ResourceLocation.tryParse(blockId);
            if (loc == null) continue;
            BuiltInRegistries.BLOCK.getOptional(loc).ifPresent(block -> {
                String displayName = block.getName().getString();
                mapping.put(displayName, blockId);
            });
        }

        // Also add common items that might appear in ponder scenes
        BuiltInRegistries.ITEM.forEach(item -> {
            String id = BuiltInRegistries.ITEM.getKey(item).toString();
            String displayName = item.getDescription().getString();
            // Only add if not already present (block names take priority)
            mapping.putIfAbsent(displayName, id);
        });

        // Build text
        StringBuilder sb = new StringBuilder();
        sb.append("Display Name → Registry ID mapping:\n");
        int count = 0;
        for (var entry : mapping.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(" → ").append(entry.getValue()).append("\n");
            count++;
            // Limit to prevent excessive token usage
            if (count > 500) {
                sb.append("  ... (truncated, ").append(mapping.size() - 500).append(" more entries)\n");
                break;
            }
        }
        return sb.toString();
    }
}

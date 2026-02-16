package com.nododiiiii.ponderer.ui;

import com.nododiiiii.ponderer.ponder.DslScene;
import net.createmod.catnip.config.ui.HintableTextFieldWidget;
import net.createmod.catnip.gui.widget.BoxWidget;
import net.createmod.ponder.foundation.ui.PonderButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplaceBlocksScreen extends AbstractStepEditorScreen {

    private HintableTextFieldWidget blockField;
    private HintableTextFieldWidget posXField, posYField, posZField;
    private HintableTextFieldWidget pos2XField, pos2YField, pos2ZField;
    private boolean spawnParticles = true;
    private BoxWidget particlesToggle;
    private PonderButton pickBtn1, pickBtn2;
    @Nullable
    private PonderButton jeiBtn;

    public ReplaceBlocksScreen(DslScene scene, int sceneIndex, SceneEditorScreen parent) {
        super(Component.translatable("ponderer.ui.replace_blocks.add"), scene, sceneIndex, parent);
    }

    public ReplaceBlocksScreen(DslScene scene, int sceneIndex, SceneEditorScreen parent,
                               int editIndex, DslScene.DslStep step) {
        super(Component.translatable("ponderer.ui.replace_blocks.edit"), scene, sceneIndex, parent, editIndex, step);
    }

    @Override
    protected boolean usesBlockProps() { return true; }

    @Override
    protected int getFormRowCount() { return 4 + blockPropRowCount(); }

    @Override
    protected String getHeaderTitle() { return UIText.of("ponderer.ui.replace_blocks"); }

    @Override
    protected void buildForm() {
        int x = guiLeft + 70, y = guiTop + 26, sw = 38;
        int lx = guiLeft + 10;

        blockField = createTextField(x, y, 124, 18, UIText.of("ponderer.ui.replace_blocks.hint"));
        jeiBtn = createJeiButton(x + 129, y, blockField, IdFieldMode.BLOCK);
        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui.replace_blocks"), UIText.of("ponderer.ui.replace_blocks.tooltip"));
        y += 22;

        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui.block_properties"), UIText.of("ponderer.ui.block_properties.tooltip"));
        y = buildBlockPropsUI(x, y);

        posXField = createSmallNumberField(x, y, sw, "X");
        posYField = createSmallNumberField(x + sw + 5, y, sw, "Y");
        posZField = createSmallNumberField(x + 2 * (sw + 5), y, sw, "Z");
        pickBtn1 = createPickButton(x + 3 * (sw + 5), y, PickState.TargetField.POS1);
        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui.replace_blocks.pos_from"), UIText.of("ponderer.ui.replace_blocks.pos_from.tooltip"));
        y += 22;

        pos2XField = createSmallNumberField(x, y, sw, "X");
        pos2YField = createSmallNumberField(x + sw + 5, y, sw, "Y");
        pos2ZField = createSmallNumberField(x + 2 * (sw + 5), y, sw, "Z");
        pickBtn2 = createPickButton(x + 3 * (sw + 5), y, PickState.TargetField.POS2);
        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui.replace_blocks.pos_to"), UIText.of("ponderer.ui.replace_blocks.pos_to.tooltip"));
        y += 22;

        particlesToggle = createToggle(x, y);
        particlesToggle.withCallback(() -> spawnParticles = !spawnParticles);
        addRenderableWidget(particlesToggle);
        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui.replace_blocks.particles"), UIText.of("ponderer.ui.replace_blocks.particles.tooltip"));
    }

    @Override
    protected void populateFromStep(DslScene.DslStep step) {
        super.populateFromStep(step);
        if (step.block != null) blockField.setValue(step.block);
        if (step.blockPos != null && step.blockPos.size() >= 3) {
            posXField.setValue(String.valueOf(step.blockPos.get(0)));
            posYField.setValue(String.valueOf(step.blockPos.get(1)));
            posZField.setValue(String.valueOf(step.blockPos.get(2)));
        }
        if (step.blockPos2 != null && step.blockPos2.size() >= 3) {
            pos2XField.setValue(String.valueOf(step.blockPos2.get(0)));
            pos2YField.setValue(String.valueOf(step.blockPos2.get(1)));
            pos2ZField.setValue(String.valueOf(step.blockPos2.get(2)));
        }
        if (step.spawnParticles != null) {
            spawnParticles = step.spawnParticles;
        }
    }

    @Override
    protected void renderForm(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var font = Minecraft.getInstance().font;
        int lx = guiLeft + 10, y = guiTop + 29, lc = 0xCCCCCC;

        graphics.drawString(font, UIText.of("ponderer.ui.replace_blocks"), lx, y, lc);
        y += 22;
        graphics.drawString(font, UIText.of("ponderer.ui.block_properties"), lx, y, lc);
        y += blockPropRowCount() * 22;
        graphics.drawString(font, UIText.of("ponderer.ui.replace_blocks.pos_from"), lx, y, lc);
        y += 22;
        graphics.drawString(font, UIText.of("ponderer.ui.replace_blocks.pos_to"), lx, y, lc);
        y += 22;
        graphics.drawString(font, UIText.of("ponderer.ui.replace_blocks.particles"), lx, y + 3, lc);
    }

    @Override
    protected void renderFormForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderToggleState(graphics, particlesToggle, spawnParticles);
        renderPickButtonLabel(graphics, pickBtn1);
        renderPickButtonLabel(graphics, pickBtn2);
        renderJeiButtonLabel(graphics, jeiBtn);
        renderBlockPropsForeground(graphics);
    }

    @Override
    protected String getStepType() { return "replace_blocks"; }

    @Override
    protected Map<String, String> snapshotForm() {
        syncBlockPropFieldsToEntries();
        Map<String, String> m = new HashMap<>();
        m.put("block", blockField.getValue());
        snapshotBlockProps(m);
        m.put("posX", posXField.getValue());
        m.put("posY", posYField.getValue());
        m.put("posZ", posZField.getValue());
        m.put("pos2X", pos2XField.getValue());
        m.put("pos2Y", pos2YField.getValue());
        m.put("pos2Z", pos2ZField.getValue());
        m.put("particles", String.valueOf(spawnParticles));
        return m;
    }

    @Override
    protected void restoreFromSnapshot(Map<String, String> snapshot) {
        restoreKeyFrame(snapshot);
        if (snapshot.containsKey("block")) blockField.setValue(snapshot.get("block"));
        restoreBlockProps(snapshot);
        if (snapshot.containsKey("posX")) posXField.setValue(snapshot.get("posX"));
        if (snapshot.containsKey("posY")) posYField.setValue(snapshot.get("posY"));
        if (snapshot.containsKey("posZ")) posZField.setValue(snapshot.get("posZ"));
        if (snapshot.containsKey("pos2X")) pos2XField.setValue(snapshot.get("pos2X"));
        if (snapshot.containsKey("pos2Y")) pos2YField.setValue(snapshot.get("pos2Y"));
        if (snapshot.containsKey("pos2Z")) pos2ZField.setValue(snapshot.get("pos2Z"));
        if (snapshot.containsKey("particles")) spawnParticles = Boolean.parseBoolean(snapshot.get("particles"));
    }

    @Nullable
    @Override
    protected DslScene.DslStep buildStep() {
        errorMessage = null;
        String blockId = blockField.getValue().trim();
        if (blockId.isEmpty()) { errorMessage = UIText.of("ponderer.ui.replace_blocks.error.required"); return null; }
        ResourceLocation loc = ResourceLocation.tryParse(blockId);
        if (loc == null) { errorMessage = UIText.of("ponderer.ui.replace_blocks.error.invalid_id"); return null; }
        if (BuiltInRegistries.BLOCK.getOptional(loc).isEmpty()) {
            errorMessage = UIText.of("ponderer.ui.replace_blocks.error.unknown", blockId); return null;
        }

        Integer px = parseInt(posXField.getValue(), "X");
        Integer py = parseInt(posYField.getValue(), "Y");
        Integer pz = parseInt(posZField.getValue(), "Z");
        if (px == null || py == null || pz == null) return null;

        String pos2X = pos2XField.getValue().trim();
        String pos2Y = pos2YField.getValue().trim();
        String pos2Z = pos2ZField.getValue().trim();
        boolean hasPos2 = !pos2X.isEmpty() || !pos2Y.isEmpty() || !pos2Z.isEmpty();
        Integer px2 = null, py2 = null, pz2 = null;
        if (hasPos2) {
            if (pos2X.isEmpty() || pos2Y.isEmpty() || pos2Z.isEmpty()) {
                errorMessage = UIText.of("ponderer.ui.replace_blocks.error.partial_to");
                return null;
            }
            px2 = parseInt(pos2X, "X2");
            py2 = parseInt(pos2Y, "Y2");
            pz2 = parseInt(pos2Z, "Z2");
            if (px2 == null || py2 == null || pz2 == null) return null;
        }

        DslScene.DslStep s = new DslScene.DslStep();
        s.type = "replace_blocks";
        s.block = blockId;
        s.blockProperties = collectBlockProperties();
        s.blockPos = List.of(px, py, pz);
        if (hasPos2) s.blockPos2 = List.of(px2, py2, pz2);
        if (!spawnParticles) s.spawnParticles = false;
        return s;
    }
}

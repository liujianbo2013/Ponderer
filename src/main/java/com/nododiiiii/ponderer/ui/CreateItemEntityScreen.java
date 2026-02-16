package com.nododiiiii.ponderer.ui;

import com.nododiiiii.ponderer.ponder.DslScene;
import net.createmod.catnip.config.ui.HintableTextFieldWidget;
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

public class CreateItemEntityScreen extends AbstractStepEditorScreen {

    private HintableTextFieldWidget itemField;
    private HintableTextFieldWidget countField;
    private HintableTextFieldWidget posXField, posYField, posZField;
    private HintableTextFieldWidget motionXField, motionYField, motionZField;
    private PonderButton pickBtnPos;

    public CreateItemEntityScreen(DslScene scene, int sceneIndex, SceneEditorScreen parent) {
        super(Component.translatable("ponderer.ui.create_item_entity.add"), scene, sceneIndex, parent);
    }

    public CreateItemEntityScreen(DslScene scene, int sceneIndex, SceneEditorScreen parent,
                                  int editIndex, DslScene.DslStep step) {
        super(Component.translatable("ponderer.ui.create_item_entity.edit"), scene, sceneIndex, parent, editIndex, step);
    }

    @Override
    protected int getFormRowCount() { return 5; }

    @Override
    protected String getHeaderTitle() { return UIText.of("ponderer.ui.create_item_entity"); }

    @Override
    protected void buildForm() {
        int x = guiLeft + 70, y = guiTop + 26, sw = 38;
        int lx = guiLeft + 10;

        itemField = createTextField(x, y, 140, 18, UIText.of("ponderer.ui.create_item_entity.hint"));
        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui.create_item_entity.item"), UIText.of("ponderer.ui.create_item_entity.item.tooltip"));
        y += 22;

        countField = createSmallNumberField(x, y, 50, "1");
        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui.create_item_entity.count"), UIText.of("ponderer.ui.create_item_entity.count.tooltip"));
        y += 22;

        posXField = createSmallNumberField(x, y, sw, "X");
        posYField = createSmallNumberField(x + sw + 5, y, sw, "Y");
        posZField = createSmallNumberField(x + 2 * (sw + 5), y, sw, "Z");
        pickBtnPos = createPickButton(x + 3 * (sw + 5), y, PickState.TargetField.POS1, true);
        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui.create_item_entity.pos"), UIText.of("ponderer.ui.create_item_entity.pos.tooltip"));
        y += 22;

        motionXField = createSmallNumberField(x, y, sw, "0");
        motionYField = createSmallNumberField(x + sw + 5, y, sw, "0");
        motionZField = createSmallNumberField(x + 2 * (sw + 5), y, sw, "0");
        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui.create_item_entity.motion"), UIText.of("ponderer.ui.create_item_entity.motion.tooltip"));
        y += 22;
    }

    @Override
    protected void populateFromStep(DslScene.DslStep step) {
        super.populateFromStep(step);
        if (step.item != null) itemField.setValue(step.item);
        if (step.count != null) countField.setValue(String.valueOf(step.count));
        if (step.pos != null && step.pos.size() >= 3) {
            posXField.setValue(String.valueOf(step.pos.get(0)));
            posYField.setValue(String.valueOf(step.pos.get(1)));
            posZField.setValue(String.valueOf(step.pos.get(2)));
        }
        if (step.motion != null && step.motion.size() >= 3) {
            motionXField.setValue(String.valueOf(step.motion.get(0)));
            motionYField.setValue(String.valueOf(step.motion.get(1)));
            motionZField.setValue(String.valueOf(step.motion.get(2)));
        }
    }

    @Override
    protected void renderForm(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var font = Minecraft.getInstance().font;
        int lx = guiLeft + 10, y = guiTop + 29, lc = 0xCCCCCC;

        graphics.drawString(font, UIText.of("ponderer.ui.create_item_entity.item"), lx, y, lc);
        y += 22;
        graphics.drawString(font, UIText.of("ponderer.ui.create_item_entity.count"), lx, y, lc);
        y += 22;
        graphics.drawString(font, UIText.of("ponderer.ui.create_item_entity.pos"), lx, y, lc);
        y += 22;
        graphics.drawString(font, UIText.of("ponderer.ui.create_item_entity.motion"), lx, y, lc);
    }

    @Override
    protected void renderFormForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderPickButtonLabel(graphics, pickBtnPos);
    }

    @Override
    protected String getStepType() { return "create_item_entity"; }

    @Override
    protected Map<String, String> snapshotForm() {
        Map<String, String> m = new HashMap<>();
        m.put("item", itemField.getValue());
        m.put("count", countField.getValue());
        m.put("posX", posXField.getValue());
        m.put("posY", posYField.getValue());
        m.put("posZ", posZField.getValue());
        m.put("motionX", motionXField.getValue());
        m.put("motionY", motionYField.getValue());
        m.put("motionZ", motionZField.getValue());
        return m;
    }

    @Override
    protected void restoreFromSnapshot(Map<String, String> snapshot) {
        restoreKeyFrame(snapshot);
        if (snapshot.containsKey("item")) itemField.setValue(snapshot.get("item"));
        if (snapshot.containsKey("count")) countField.setValue(snapshot.get("count"));
        if (snapshot.containsKey("posX")) posXField.setValue(snapshot.get("posX"));
        if (snapshot.containsKey("posY")) posYField.setValue(snapshot.get("posY"));
        if (snapshot.containsKey("posZ")) posZField.setValue(snapshot.get("posZ"));
        if (snapshot.containsKey("motionX")) motionXField.setValue(snapshot.get("motionX"));
        if (snapshot.containsKey("motionY")) motionYField.setValue(snapshot.get("motionY"));
        if (snapshot.containsKey("motionZ")) motionZField.setValue(snapshot.get("motionZ"));
    }

    @Nullable
    @Override
    protected DslScene.DslStep buildStep() {
        errorMessage = null;
        String itemId = itemField.getValue().trim();
        if (itemId.isEmpty()) {
            errorMessage = UIText.of("ponderer.ui.create_item_entity.error.required");
            return null;
        }

        ResourceLocation itemLoc = ResourceLocation.tryParse(itemId);
        if (itemLoc == null) {
            errorMessage = UIText.of("ponderer.ui.create_item_entity.error.invalid_id");
            return null;
        }
        if (BuiltInRegistries.ITEM.getOptional(itemLoc).isEmpty()) {
            errorMessage = UIText.of("ponderer.ui.create_item_entity.error.unknown", itemId);
            return null;
        }

        Double px = parseDouble(posXField.getValue(), "X");
        Double py = parseDouble(posYField.getValue(), "Y");
        Double pz = parseDouble(posZField.getValue(), "Z");
        if (px == null || py == null || pz == null) return null;

        Double mx = motionXField.getValue().trim().isEmpty() ? 0.0 : parseDouble(motionXField.getValue(), UIText.of("ponderer.ui.create_item_entity.motion") + " X");
        Double my = motionYField.getValue().trim().isEmpty() ? 0.0 : parseDouble(motionYField.getValue(), UIText.of("ponderer.ui.create_item_entity.motion") + " Y");
        Double mz = motionZField.getValue().trim().isEmpty() ? 0.0 : parseDouble(motionZField.getValue(), UIText.of("ponderer.ui.create_item_entity.motion") + " Z");
        if (mx == null || my == null || mz == null) return null;

        DslScene.DslStep s = new DslScene.DslStep();
        s.type = "create_item_entity";
        s.item = itemId;
        s.count = Math.max(1, parseIntOr(countField.getValue(), 1));
        s.pos = List.of(px, py, pz);
        s.motion = List.of(mx, my, mz);
        return s;
    }
}

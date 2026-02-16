package com.nododiiiii.ponderer.ui;

import com.nododiiiii.ponderer.ponder.DslScene;
import net.createmod.catnip.config.ui.HintableTextFieldWidget;
import net.createmod.ponder.foundation.ui.PonderButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndicateEffectScreen extends AbstractStepEditorScreen {

    private final String stepType;
    private HintableTextFieldWidget posXField, posYField, posZField;
    private PonderButton pickBtn1;

    public IndicateEffectScreen(String stepType, DslScene scene, int sceneIndex, SceneEditorScreen parent) {
        super(Component.translatable("ponderer.ui." + stepType + ".add"), scene, sceneIndex, parent);
        this.stepType = stepType;
    }

    public IndicateEffectScreen(String stepType, DslScene scene, int sceneIndex, SceneEditorScreen parent,
                                int editIndex, DslScene.DslStep step) {
        super(Component.translatable("ponderer.ui." + stepType + ".edit"), scene, sceneIndex, parent, editIndex, step);
        this.stepType = stepType;
    }

    @Override
    protected int getFormRowCount() { return 1; }

    @Override
    protected String getHeaderTitle() { return UIText.of("ponderer.ui." + stepType); }

    @Override
    protected void buildForm() {
        int x = guiLeft + 70, y = guiTop + 26, sw = 38;
        int lx = guiLeft + 10;

        posXField = createSmallNumberField(x, y, sw, "X");
        posYField = createSmallNumberField(x + sw + 5, y, sw, "Y");
        posZField = createSmallNumberField(x + 2 * (sw + 5), y, sw, "Z");
        pickBtn1 = createPickButton(x + 3 * (sw + 5), y, PickState.TargetField.POS1);
        addLabelTooltip(lx, y + 3, UIText.of("ponderer.ui." + stepType + ".pos"), UIText.of("ponderer.ui." + stepType + ".pos.tooltip"));
    }

    @Override
    protected void renderFormForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderPickButtonLabel(graphics, pickBtn1);
    }

    @Override
    protected void populateFromStep(DslScene.DslStep step) {
        super.populateFromStep(step);
        if (step.blockPos != null && step.blockPos.size() >= 3) {
            posXField.setValue(String.valueOf(step.blockPos.get(0)));
            posYField.setValue(String.valueOf(step.blockPos.get(1)));
            posZField.setValue(String.valueOf(step.blockPos.get(2)));
        }
    }

    @Override
    protected void renderForm(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var font = Minecraft.getInstance().font;
        graphics.drawString(font, UIText.of("ponderer.ui." + stepType + ".pos"), guiLeft + 10, guiTop + 29, 0xCCCCCC);
    }

    @Override
    protected String getStepType() { return stepType; }

    @Override
    protected Map<String, String> snapshotForm() {
        Map<String, String> m = new HashMap<>();
        m.put("posX", posXField.getValue());
        m.put("posY", posYField.getValue());
        m.put("posZ", posZField.getValue());
        return m;
    }

    @Override
    protected void restoreFromSnapshot(Map<String, String> snapshot) {
        restoreKeyFrame(snapshot);
        if (snapshot.containsKey("posX")) posXField.setValue(snapshot.get("posX"));
        if (snapshot.containsKey("posY")) posYField.setValue(snapshot.get("posY"));
        if (snapshot.containsKey("posZ")) posZField.setValue(snapshot.get("posZ"));
    }

    @Nullable
    @Override
    protected DslScene.DslStep buildStep() {
        errorMessage = null;

        Integer px = parseInt(posXField.getValue(), "X");
        Integer py = parseInt(posYField.getValue(), "Y");
        Integer pz = parseInt(posZField.getValue(), "Z");
        if (px == null || py == null || pz == null) return null;

        DslScene.DslStep s = new DslScene.DslStep();
        s.type = stepType;
        s.blockPos = List.of(px, py, pz);
        return s;
    }
}

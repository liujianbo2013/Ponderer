package com.nododiiiii.ponderer.compat.jei;

import com.nododiiiii.ponderer.ui.AbstractStepEditorScreen;
import com.nododiiiii.ponderer.ui.IdFieldMode;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IJeiRuntime;
import net.createmod.catnip.config.ui.HintableTextFieldWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.Optional;

@JeiPlugin
public class PondererJeiPlugin implements IModPlugin {

    @Nullable
    private static AbstractStepEditorScreen activeEditor = null;
    @Nullable
    private static IdFieldMode activeMode = null;
    @Nullable
    private static IJeiRuntime runtime = null;

    private static boolean eventRegistered = false;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("ponderer", "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        if (!eventRegistered) {
            eventRegistered = true;
            NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, PondererJeiPlugin::onMouseClick);
        }
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        activeEditor = null;
        activeMode = null;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiScreenHandler(AbstractStepEditorScreen.class, screen -> {
            if (activeEditor != screen) return null;
            return new PondererGuiProperties(screen);
        });

        registration.addGhostIngredientHandler(
                AbstractStepEditorScreen.class,
                new StepEditorGhostHandler()
        );
    }

    // ---- State management (called from JeiCompat) ----

    static void setActiveEditor(AbstractStepEditorScreen screen, IdFieldMode mode) {
        activeEditor = screen;
        activeMode = mode;
    }

    static void clearActiveEditor() {
        activeEditor = null;
        activeMode = null;
    }

    @Nullable
    static IdFieldMode getActiveMode() {
        return activeMode;
    }

    @Nullable
    static AbstractStepEditorScreen getActiveEditor() {
        return activeEditor;
    }

    // ---- Click interception ----

    private static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractStepEditorScreen editor)) return;
        if (activeMode == null || runtime == null) return;
        if (activeEditor != editor) return;

        IIngredientListOverlay overlay = runtime.getIngredientListOverlay();
        IBookmarkOverlay bookmarks = runtime.getBookmarkOverlay();

        Optional<ITypedIngredient<?>> ingredient = overlay.getIngredientUnderMouse();
        if (ingredient.isEmpty()) {
            ingredient = bookmarks.getIngredientUnderMouse();
        }
        if (ingredient.isEmpty()) return;

        Optional<ItemStack> stackOpt = ingredient.get().getItemStack();
        if (stackOpt.isEmpty()) {
            event.setCanceled(true);
            return;
        }
        ItemStack stack = stackOpt.get();

        String id = StepEditorGhostHandler.resolveId(stack, activeMode);
        if (id != null) {
            HintableTextFieldWidget field = editor.getJeiTargetField();
            if (field != null) {
                field.setValue(id);
            }
            editor.deactivateJei();
            event.setCanceled(true);
        } else {
            editor.showJeiIncompatibleWarning(activeMode);
            event.setCanceled(true);
        }
    }

    // ---- IGuiProperties implementation ----

    private static class PondererGuiProperties implements IGuiProperties {
        private final AbstractStepEditorScreen screen;

        PondererGuiProperties(AbstractStepEditorScreen screen) {
            this.screen = screen;
        }

        @Override
        public Class<? extends Screen> screenClass() { return screen.getClass(); }

        @Override
        public int guiLeft() { return screen.getGuiLeft(); }

        @Override
        public int guiTop() { return screen.getGuiTop(); }

        @Override
        public int guiXSize() { return screen.getGuiWidth(); }

        @Override
        public int guiYSize() { return screen.getGuiHeight(); }

        @Override
        public int screenWidth() { return screen.width; }

        @Override
        public int screenHeight() { return screen.height; }
    }
}

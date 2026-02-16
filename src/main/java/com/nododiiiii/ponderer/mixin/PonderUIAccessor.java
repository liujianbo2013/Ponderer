package com.nododiiiii.ponderer.mixin;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Accessor mixin to read/write PonderUI's private fields needed for coordinate picking.
 */
@Mixin(PonderUI.class)
public interface PonderUIAccessor {

    @Accessor("hoveredBlockPos")
    @Nullable
    BlockPos ponderer$getHoveredBlockPos();

    @Accessor("identifyMode")
    boolean ponderer$getIdentifyMode();

    @Accessor("identifyMode")
    void ponderer$setIdentifyMode(boolean value);

    @Accessor("scenes")
    List<PonderScene> ponderer$getScenes();

    @Accessor("index")
    int ponderer$getIndex();

    @Accessor("index")
    void ponderer$setIndex(int value);

    @Accessor("lazyIndex")
    LerpedFloat ponderer$getLazyIndex();
}

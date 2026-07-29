package org.cobalt.mixin.gui;

import java.util.Comparator;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {

  @Accessor("SCORE_DISPLAY_ORDER")
  static Comparator<PlayerScoreEntry> cobalt$getScoreDisplayOrder() {
    throw new AssertionError();
  }

}

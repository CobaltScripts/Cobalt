package org.cobalt.mixin.gui;

import java.util.Comparator;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface HudAccessor {

  @Accessor("SCORE_DISPLAY_ORDER")
  static Comparator<PlayerScoreEntry> cobalt$getScoreDisplayOrder() {
    throw new AssertionError();
  }

}

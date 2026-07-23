package org.cobalt.mixin.screen;

import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.cobalt.Cobalt;
import org.cobalt.ui.theme.ThemeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashManager.class)
public class SplashManagerMixin {

  @Inject(
    method = "getSplash",
    at = @At("HEAD"),
    cancellable = true
  )
  private void cobalt$modifySplash(CallbackInfoReturnable<SplashRenderer> cir) {
    Component splash = cobalt$createSplash();
    SplashRenderer splashRenderer = new SplashRenderer(splash);

    cir.setReturnValue(splashRenderer);
  }

  @Unique
  private Component cobalt$createSplash() {
    int color = ThemeManager.getActiveTheme().getAccentPrimary().getRGB();

    return Component.literal(Cobalt.MOD_NAME + " on top!")
      .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
  }

}

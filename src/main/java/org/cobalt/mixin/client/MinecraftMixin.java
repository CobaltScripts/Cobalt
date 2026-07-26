package org.cobalt.mixin.client;

import java.util.List;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.cobalt.Cobalt;
import org.cobalt.addon.Addon;
import org.cobalt.addon.AddonManager;
import org.cobalt.addon.AddonMetadata;
import org.cobalt.event.EventBus;
import org.cobalt.event.impl.TickEvent;
import org.cobalt.event.impl.WorldEvent;
import org.cobalt.module.ModuleManager;
import org.cobalt.util.config.SettingContainer;
import org.cobalt.util.scheduling.Multithreading;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

  @Inject(method = "tick", at = @At("HEAD"))
  private void cobalt$onStartTick(CallbackInfo callbackInfo) {
    TickEvent.Start event = new TickEvent.Start();
    EventBus.post(event);
  }

  @Inject(method = "tick", at = @At("RETURN"))
  private void cobalt$onEndTick(CallbackInfo callbackInfo) {
    TickEvent.End event = new TickEvent.End();
    EventBus.post(event);
  }

  @Inject(method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V", at = @At("HEAD"))
  private void cobalt$onWorldChange(final ClientLevel level, final boolean stopSound, final CallbackInfo ci) {
    WorldEvent.Change event = new WorldEvent.Change();
    EventBus.post(event);
  }

  @ModifyArg(
    method = "updateTitle",
    at = @At(
      value = "INVOKE",
      target = "Lcom/mojang/blaze3d/platform/Window;setTitle(Ljava/lang/String;)V"
    ),
    index = 0
  )
  private String cobalt$modifyTitle(String oldTitle) {
    return Cobalt.MOD_NAME + " " + Cobalt.MINECRAFT_VERSION + " (v" + Cobalt.MOD_VERSION + ")";
  }

  @Inject(method = "close", at = @At("HEAD"))
  public void cobalt$onClose(CallbackInfo callbackInfo) {
    List<Pair<AddonMetadata, Addon>> addonsList = AddonManager.getAddons();

    addonsList.forEach((addon) -> {
      addon.getSecond().onUnload();
    });

    Multithreading.runAsync(() -> {
      ModuleManager.INSTANCE.getModules().forEach(SettingContainer::saveConfig);
    });
  }

}

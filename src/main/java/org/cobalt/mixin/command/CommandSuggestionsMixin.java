package org.cobalt.mixin.command;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import org.cobalt.command.CommandManager;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandSuggestions.class)
public abstract class CommandSuggestionsMixin {

  @Shadow
  @Final
  private Screen screen;

  @Shadow
  @Final
  private EditBox input;

  @Shadow
  @Nullable
  private ParseResults<ClientSuggestionProvider> currentParse;

  @Shadow
  private CommandSuggestions.@Nullable SuggestionsList suggestions;

  @Shadow
  private boolean keepSuggestions;

  @Shadow
  @Nullable
  private CompletableFuture<Suggestions> pendingSuggestions;

  @Shadow
  @Final
  private Minecraft minecraft;

  @Inject(
    method = "updateCommandInfo",
    at = @At(
      value = "INVOKE",
      target = "Lcom/mojang/brigadier/StringReader;canRead()Z",
      remap = false
    ),
    cancellable = true
  )
  public void cobalt$refresh(CallbackInfo callbackInfo, @Local(name = "reader") StringReader reader) {
    if (!(this.screen instanceof ChatScreen) || this.minecraft.player == null) {
      return;
    }

    if (!reader.canRead() || reader.peek() != CommandManager.PREFIX) {
      reader.setCursor(0);
      return;
    }

    reader.skip();

    int cursor = this.input.getCursorPosition();
    CommandDispatcher<ClientSuggestionProvider> dispatcher = CommandManager.getDispatcher$cobalt();

    if (this.currentParse == null) {
      ClientSuggestionProvider suggestionProvider = this.minecraft.player.connection.getSuggestionsProvider();
      this.currentParse = dispatcher.parse(reader, suggestionProvider);
    }

    if (cursor >= 1 && (this.suggestions == null || !this.keepSuggestions)) {
      this.pendingSuggestions = dispatcher.getCompletionSuggestions(this.currentParse, cursor);
      this.pendingSuggestions.thenAccept(result -> {
        if (this.pendingSuggestions.isDone()) updateUsageInfo(this.currentParse, result);
      });
    }

    callbackInfo.cancel();
  }

  @Shadow
  protected abstract void updateUsageInfo(ParseResults<ClientSuggestionProvider> currentParse, Suggestions suggestions);

}

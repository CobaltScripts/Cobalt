package org.cobalt.mixin.render;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.cobalt.internal.modules.NameProtect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TextRenderer.class)
public class NameProtect_TextRendererMixin {

  @Unique
  private static OrderedText replaceWordWithText(OrderedText orderedText, String target, MutableText replacement) {
    List<String> chars = new ArrayList<>();
    List<Style> styles = new ArrayList<>();

    orderedText.accept((index, style, codePoint) -> {
      chars.add(new String(Character.toChars(codePoint)));
      styles.add(style);
      return true;
    });

    StringBuilder rawBuilder = new StringBuilder(chars.size());
    for (String c : chars) rawBuilder.append(c);
    String raw = rawBuilder.toString();

    if (!raw.contains(target)) return orderedText;

    MutableText rebuilt = Text.empty();
    int searchIndex = 0;
    int rawLen = raw.length();

    while (searchIndex < rawLen) {
      int found = raw.indexOf(target, searchIndex);
      if (found == -1) {
        for (int i = searchIndex; i < rawLen; i++) {
          rebuilt.append(Text.literal(chars.get(i)).setStyle(styles.get(i)));
        }

        break;
      }

      for (int i = searchIndex; i < found; i++) {
        rebuilt.append(Text.literal(chars.get(i)).setStyle(styles.get(i)));
      }

      rebuilt.append(replacement);
      searchIndex = found + target.length();
    }

    return rebuilt.asOrderedText();
  }

  @ModifyVariable(
    method = "prepare(Lnet/minecraft/text/OrderedText;FFIZI)Lnet/minecraft/client/font/TextRenderer$GlyphDrawable;",
    at = @At("HEAD"),
    argsOnly = true
  )
  private OrderedText modifyMinecraftName(OrderedText text) {
    if (NameProtect.INSTANCE.isEnabled()) {
      MutableText replacement = NameProtect.getName();
      return replaceWordWithText(text, NameProtect.getMcIGN(), replacement);
    }

    return text;
  }

}

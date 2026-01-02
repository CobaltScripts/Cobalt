package org.cobalt.mixin.rsa;

import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Thank you Twiston for this superb class */
@Mixin(value = YggdrasilServicesKeyInfo.class, remap = false)
public class RSANuker_YggdrasilServicesKeyInfoMixin {

  @Redirect(
    method = "validateProperty(Lcom/mojang/authlib/properties/Property;)Z",
    at = @At(
      value = "INVOKE",
      target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
    ),
    remap = false
  )
  private void redirectLoggerError(Logger instance, String s, Object o, Object o1) {
    // Do nothing - Suppress the log
  }

}

package org.cobalt.mixin.mojang;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(YggdrasilServicesKeyInfo.class)
public class YggdrasilServicesKeyInfoMixin {

  @WrapWithCondition(
    method = "validateProperty(Lcom/mojang/authlib/properties/Property;)Z",
    at = @At(
      value = "INVOKE",
      target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
    )
  )
  private boolean cobalt$hideLoggerErrors(Logger instance, String str, Object obj, Object obj1) {
    return false;
  }

}

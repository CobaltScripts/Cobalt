package org.cobalt.mixin.client;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {

  @Invoker("onButton")
  void invokeOnButton(long handle, MouseButtonInfo rawButtonInfo, @MouseButtonInfo.Action int action);

}

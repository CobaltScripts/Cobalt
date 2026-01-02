package org.cobalt.mixin.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cobalt.api.event.impl.client.BlockChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
abstract class BlockBreak_ClientPlayerInteractionManagerMixin {

  @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"))
  private void onBlockChange(BlockPos pos, BlockState newState, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
    if (MinecraftClient.getInstance().world != (Object) this) {
      return;
    }

    BlockState oldBlock = ((World) (Object) this).getBlockState(pos);

    if (oldBlock.getBlock() != newState.getBlock()) {
      new BlockChangeEvent(pos.toImmutable(), oldBlock, newState).post();
    }
  }

}

package org.cobalt.mixin.render;

import net.minecraft.client.render.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Frustum.class)
public interface Frustum_FrustumInvoker {

	@Invoker
	int invokeIntersectAab(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

}

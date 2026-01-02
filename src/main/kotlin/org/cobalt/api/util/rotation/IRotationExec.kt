package org.cobalt.api.util.rotation

import net.minecraft.client.network.ClientPlayerEntity

interface IRotationExec {

  fun onRotate(player: ClientPlayerEntity)

}

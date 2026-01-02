package org.cobalt.api.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandRegistryAccess
import org.cobalt.api.command.annotation.DefaultHandler
import org.cobalt.api.command.annotation.SubCommand

object CommandManager {

  private val commands = mutableListOf<Command>()

  fun register(command: Command) {
    commands.add(command)
  }

  fun dispatchAll() {
    ClientCommandRegistrationCallback.EVENT.register(CommandManager::dispatchAll)
  }

  private fun dispatchAll(dispatcher: CommandDispatcher<FabricClientCommandSource>, access: CommandRegistryAccess) {
    commands.forEach { command ->
      val rootNames = listOf(command.name) + command.aliases

      rootNames.forEach { rootName ->
        var root = literal(rootName)

        command::class.declaredMemberFunctions.find { it.findAnnotation<DefaultHandler>() != null }
          ?.let { method ->
            method.isAccessible = true
            root = attachExecution(root, method, command)
          }

        command::class.declaredMemberFunctions.forEach { method ->
          method.findAnnotation<SubCommand>()?.let {
            method.isAccessible = true
            root = root.then(attachExecution(literal(method.name), method, command))
          }
        }

        dispatcher.register(root)
      }
    }
  }

  private fun attachExecution(
    builder: LiteralArgumentBuilder<FabricClientCommandSource>,
    method: KFunction<*>,
    command: Command,
  ): LiteralArgumentBuilder<FabricClientCommandSource> {
    val params = method.parameters.drop(1)
    if (params.isEmpty()) return builder.executes { method.call(command); 1 }
    return builder.then(buildArguments(params, 0, method, command))
  }

  private fun buildArguments(
    params: List<KParameter>,
    index: Int,
    method: KFunction<*>,
    command: Command,
  ): RequiredArgumentBuilder<FabricClientCommandSource, *> {
    val param = params[index]
    val argBuilder = when (param.type.classifier) {
      Int::class -> argument(param.name ?: "arg$index", IntegerArgumentType.integer())
      String::class -> argument(param.name ?: "arg$index", StringArgumentType.string())
      Double::class -> argument(param.name ?: "arg$index", DoubleArgumentType.doubleArg())
      Boolean::class -> argument(param.name ?: "arg$index", BoolArgumentType.bool())
      else -> throw IllegalArgumentException("Unsupported parameter type: ${param.type}")
    }

    return if (index == params.lastIndex) {
      argBuilder.executes { ctx ->
        val args = params.mapIndexed { i, p ->
          when (p.type.classifier) {
            Int::class -> IntegerArgumentType.getInteger(ctx, p.name ?: "arg$i")
            String::class -> StringArgumentType.getString(ctx, p.name ?: "arg$i")
            Double::class -> DoubleArgumentType.getDouble(ctx, p.name ?: "arg$i")
            Boolean::class -> BoolArgumentType.getBool(ctx, p.name ?: "arg$i")
            else -> throw IllegalArgumentException("Unsupported parameter type: ${p.type}")
          }
        }
        method.call(command, *args.toTypedArray())
        1
      }
    } else {
      argBuilder.then(buildArguments(params, index + 1, method, command))
    }
  }

}

package org.cobalt.command

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible
import net.minecraft.client.multiplayer.ClientSuggestionProvider
import org.cobalt.command.annotation.DefaultHandler
import org.cobalt.command.annotation.SubCommand

abstract class Command(val name: String, val aliases: List<String> = emptyList()) {

  internal fun build(): List<LiteralArgumentBuilder<ClientSuggestionProvider>> {
    val mainRoot = LiteralArgumentBuilder.literal<ClientSuggestionProvider>(name)

    for (function in this::class.declaredFunctions) {
      function.isAccessible = true

      when {
        function.findAnnotation<DefaultHandler>() != null -> {
          mainRoot.executes {
            function.call(this@Command)
            return@executes 1
          }
        }

        function.findAnnotation<SubCommand>() != null -> {
          mainRoot.then(buildSubCommand(function))
        }
      }
    }

    val aliasRoots = aliases.filter { it.isNotBlank() }.map { alias ->
      val aliasRoot = LiteralArgumentBuilder.literal<ClientSuggestionProvider>(alias)

      mainRoot.arguments.forEach { child -> aliasRoot.then(child) }
      mainRoot.command?.let { aliasRoot.executes(it) }

      return@map aliasRoot
    }

    return listOf(mainRoot) + aliasRoots
  }

  private fun buildSubCommand(function: KFunction<*>): LiteralArgumentBuilder<ClientSuggestionProvider> {
    val literal = LiteralArgumentBuilder.literal<ClientSuggestionProvider>(function.name)
    val valueParams = function.parameters.filter { it.kind == KParameter.Kind.VALUE }

    if (valueParams.isEmpty()) {
      literal.executes {
        function.call(this@Command)
        return@executes 1
      }

      return literal
    }

    val arguments = valueParams.mapIndexed { index, param ->
      val name = param.name ?: "argument$index"
      createArgument(name, param.type.classifier)
    }

    arguments.last().executes { ctx ->
      val instanceParam = function.parameters.firstOrNull {
        it.kind == KParameter.Kind.INSTANCE
      }

      val argsMap = buildMap<KParameter, Any?> {
        if (instanceParam != null) {
          put(instanceParam, this@Command)
        }

        valueParams.forEachIndexed { index, param ->
          val argumentName = param.name ?: "argument$index"
          val argumentValue = valueExtractors[param.type.classifier]?.invoke(ctx, argumentName)
            ?: error("Unsupported type: ${param.type}")

          put(param, argumentValue)
        }
      }

      function.callBy(argsMap)
      return@executes 1
    }

    return literal.then(arguments.reduceRight { arg, acc ->
      arg.then(acc)
    })
  }

  private fun createArgument(
    name: String,
    type: Any?,
  ): RequiredArgumentBuilder<ClientSuggestionProvider, *> {
    val argType = argumentTypeSuppliers[type]?.invoke()
      ?: throw IllegalArgumentException("Unsupported parameter type: $type")

    return RequiredArgumentBuilder.argument(name, argType)
  }

  private val argumentTypeSuppliers: Map<Any?, () -> ArgumentType<*>> = mapOf(
    Double::class to { DoubleArgumentType.doubleArg() },
    Int::class to { IntegerArgumentType.integer() },
    String::class to { StringArgumentType.word() },
    Boolean::class to { BoolArgumentType.bool() },
    Float::class to { FloatArgumentType.floatArg() },
  )

  private val valueExtractors: Map<Any?, (CommandContext<ClientSuggestionProvider>, String) -> Any?> = mapOf(
    Double::class to { c, n -> DoubleArgumentType.getDouble(c, n) },
    Int::class to { c, n -> IntegerArgumentType.getInteger(c, n) },
    String::class to { c, n -> StringArgumentType.getString(c, n) },
    Boolean::class to { c, n -> BoolArgumentType.getBool(c, n) },
    Float::class to { c, n -> FloatArgumentType.getFloat(c, n) },
  )

}

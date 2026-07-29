package org.cobalt.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.client.multiplayer.ClientSuggestionProvider
import org.cobalt.Cobalt.minecraft
import org.cobalt.command.impl.MainCommand
import org.cobalt.util.chat.ChatUtils

object CommandManager {

  const val PREFIX: Char = '.'

  @JvmStatic
  internal val dispatcher = CommandDispatcher<ClientSuggestionProvider>()

  @JvmStatic
  internal fun registerCommands() {
    val builtIn = arrayOf(
      MainCommand
    )

    builtIn.forEach { command ->
      registerCommand(command)
    }
  }

  @JvmStatic
  fun registerCommand(command: Command) {
    command.build().forEach { dispatcher.register(it) }
  }

  @JvmStatic
  internal fun handleCommandExecution(content: String): Boolean {
    if (!content.startsWith(PREFIX)) {
      return false
    }

    val player = minecraft.player ?: return false
    val commandLine = content.removePrefix(PREFIX.toString()).trim()

    if (commandLine.isEmpty()) {
      return false
    }

    try {
      dispatcher.execute(commandLine, player.connection.suggestionsProvider)
      minecraft.commandHistory().addCommand(content)
    } catch (exception: CommandSyntaxException) {
      ChatUtils.sendSystemMessage("<red>${exception.message}</red>")
    }

    return true
  }

}

package io.github.alxiw.kotlinarchiver

import io.github.alxiw.kotlinarchiver.controller.ArchiverController
import io.github.alxiw.kotlinarchiver.exceptions.ArchiveException
import io.github.alxiw.kotlinarchiver.parser.Actions
import io.github.alxiw.kotlinarchiver.parser.Command
import io.github.alxiw.kotlinarchiver.parser.CommandParser
import org.apache.commons.cli.ParseException

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        val parser = CommandParser(args)
        val command: Command
        try {
            command = parser.parse()
            if (command.name == "h") {
                printHelpMessage()
            } else {
                println(command.toString())
                try {
                    val controller = ArchiverController()
                    controller.execute(command)
                    println("Done!")
                } catch (e: ArchiveException) {
                    println("An error occurred during processing")
                }
            }
        } catch (e: ParseException) {
            println("Unavailable action. Run with '-help' for usage information.")
        }

    } else {
        printHelpMessage()
    }
}

private fun printHelpMessage() {
    println("Available actions in the command line")
    for (i in 0 until Actions.values().size) {
        println("${i + 1}. ${Actions.values()[i].action}\n   ${Actions.values()[i].format}")
    }
}

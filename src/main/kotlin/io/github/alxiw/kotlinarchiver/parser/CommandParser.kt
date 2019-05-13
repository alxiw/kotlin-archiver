package io.github.alxiw.kotlinarchiver.parser

import org.apache.commons.cli.*

class CommandParser(private val args: Array<String>) {

    private val options = Options()

    init {

        val pck = Option("p", "pack", false, "Pack")
        val add = Option("a", "add", false, "Add")
        val ext = Option("e", "extract", false, "Extract")
        val get = Option("g", "get", false, "Get Comment")

        val zip = Option("z", "zip", true, "Zip")
        val src = Option("s", "source", true, "Source")

        val com = Option("c", "comment", true, "Comment")
        val out = Option("o", "out", true, "Out")

        val hlp = Option("h", "help", false, "Help")

        src.args = Option.UNLIMITED_VALUES

        options.addOption(pck)
        options.addOption(add)
        options.addOption(ext)
        options.addOption(get)

        options.addOption(zip)
        options.addOption(src)

        options.addOption(com)
        options.addOption(out)

        options.addOption(hlp)
    }

    @Throws(ParseException::class)
    fun parse(): Command {
        val parser = DefaultParser()
        val commandLine = parser.parse(options, args)

        if (commandLine.options.isNotEmpty()) {
            val name = commandLine.options[0].opt
            if ((commandLine.options[0].opt == "p" || commandLine.options[0].opt == "a") && commandLine.hasOption("z")) {
                val file = commandLine.getOptionValue("z")
                val sources: Array<String>? = commandLine.getOptionValues("s")
                val comment: String? = if (commandLine.hasOption("c")) commandLine.getOptionValue("c") else null
                return Command(name = name, zip = file, sources = sources, comment = comment)
            } else if (commandLine.options[0].opt == "e" && commandLine.hasOption("z")) {
                val file = commandLine.getOptionValue("z")
                val out: String? = if (commandLine.hasOption("o")) commandLine.getOptionValue("o") else null
                return Command(name = name, zip = file, out = out)
            } else if (commandLine.options[0].opt == "g" && commandLine.hasOption("z")) {
                val file = commandLine.getOptionValue("z")
                return Command(name = name, zip = file)
            } else if (commandLine.options[0].opt == "h") {
                return Command(name = name)
            } else {
                throw ParseException("Unrecognized command")
            }
        } else {
            throw ParseException("Unrecognized command")
        }
    }
}

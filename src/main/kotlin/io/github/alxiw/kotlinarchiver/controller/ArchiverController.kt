package io.github.alxiw.kotlinarchiver.controller

import io.github.alxiw.kotlinarchiver.ArchiveException
import io.github.alxiw.kotlinarchiver.core.ArchiverCore
import io.github.alxiw.kotlinarchiver.core.AntZipArchiverCore
import io.github.alxiw.kotlinarchiver.parser.Command

class ArchiverController {

    private var arc: ArchiverCore? = null

    @Throws(ArchiveException::class)
    fun execute(command: Command) {
        if (command.zip?.endsWith(".zip") == true) {
            this.arc = AntZipArchiverCore()
            perform(command)
        } else {
            throw ArchiveException()
        }
    }

    @Throws(ArchiveException::class)
    private fun perform(command: Command) {
        when (command.name) {
            "p" -> command.zip?.let { zip ->
                command.sources?.let {
                    arc?.pack(path = zip, sources = it, comment = command.comment)
                }
            }
            "a" -> command.zip?.let { zip ->
                command.sources?.let {
                    arc?.add(path = zip, sources = it)
                }
                command.comment?.let {
                    arc?.setComment(path = zip, comment = it)
                }
            }
            "e" -> command.zip?.let { zip ->
                arc?.extract(path = zip, out = command.out)
            }
            "g" -> command.zip?.let { zip ->
                println("comment: ${arc?.getComment(zip) ?: "<empty>"}")
            }
            else -> throw ArchiveException()
        }
    }
}

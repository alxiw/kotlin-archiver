package io.github.alxiw.kotlinarchiver.core

import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipFile
import org.apache.tools.zip.ZipOutputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream

import java.io.FileOutputStream
import java.io.UnsupportedEncodingException
import kotlin.math.min

class AntZipArchiverCore : ArchiverCore {

    private var bufferSize = 4096
    private var charset = Charsets.UTF_8.name()

    override fun pack(path: String, sources: Array<String>, comment: String?): Int {
        var errorCount = 0

        ZipOutputStream(FileOutputStream(path)).use { zos ->
            zos.encoding = charset
            comment?.let { comment ->
                zos.setComment(comment)
            }
            sources.forEach { source ->
                errorCount += watchDir(zos, source, "")
            }
        }

        return errorCount
    }

    override fun add(path: String, sources: Array<String>): Int {
        var errorCount = 0

        val tempPath: String = path + "_temp"
        val tempZip = File(path)
        tempZip.renameTo(File(tempPath))

        ZipOutputStream(FileOutputStream(path)).use { zos ->
            zos.encoding = charset
            rewriteZip(tempPath, zos)
            sources.forEach { source ->
                errorCount += watchDir(zos, source, "")
            }
        }

        File(tempPath).delete()

        return errorCount
    }

    override fun setComment(path: String, comment: String): Int {
        val tempPath: String = path + "_temp"
        val tempZip = File(path)
        tempZip.renameTo(File(tempPath))

        ZipOutputStream(FileOutputStream(path)).use { zos ->
            zos.encoding = charset
            rewriteZip(tempPath, zos)
            zos.setComment(comment)
        }

        File(tempPath).delete()
        return 1
    }

    override fun extract(path: String, out: String?) {
        var outPath: String
        if (out == null) {
            outPath = path
            val i = outPath.lastIndexOf('.')
            if (i != -1) {
                outPath = outPath.substring(0, i)
            }
        } else {
            outPath = out
        }

        File(outPath).mkdirs()
        val zip = ZipFile(File(path), charset)

        val zipFileEntries = zip.entries
        while (zipFileEntries.hasMoreElements()) {
            val entry = zipFileEntries.nextElement() as ZipEntry
            val currentEntry = entry.name

            val outFile = File(outPath, currentEntry)
            outFile.parentFile.mkdirs()

            if (!entry.isDirectory) {
                extractFile(zip, entry, outFile)
            }
        }
    }

    override fun getComment(path: String): String? {
        var info: String? = null
        val file = File(path)
        val fileLength = file.length().toInt()
        val buffer = ByteArray(min(fileLength, 16384))

        FileInputStream(file).use { fis ->
            fis.skip((fileLength - buffer.size).toLong())
            val length = fis.read(buffer)
            if (length > 0) {
                info = getZipCommentFromBuffer(buffer, length)
            }
        }

        return info
    }

    private fun watchDir(zos: ZipOutputStream, path: String?, prefix: String): Int {
        var errorCount = 0
        if (path != null) {
            val filePath = File(path)
            val listFiles = filePath.listFiles()
            if (listFiles == null) {
                errorCount++
            } else {
                when {
                    filePath.isDirectory -> for (child in listFiles) {
                        errorCount += watchDir(
                            zos,
                            child.path,
                            if (prefix !== "") prefix + File.separatorChar + filePath.name else filePath.name
                        )
                    }
                    filePath.exists() -> {
                        archiveFile(
                            zos,
                            path,
                            if (prefix !== "") prefix + File.separatorChar + filePath.name else filePath.name
                        )
                    }
                    else -> errorCount++
                }
            }
        } else {
            errorCount++
        }
        return errorCount
    }

    private fun archiveFile(zos: ZipOutputStream, srcPath: String, outPath: String) {
        val buffer = ByteArray(bufferSize)
        var length: Int
        try {
            zos.putNextEntry(ZipEntry(outPath))
            FileInputStream(srcPath).use { fis ->
                while (true) {
                    length = fis.read(buffer)
                    if (length <= 0) break
                    zos.write(buffer, 0, length)
                }
            }
        } finally {
            zos.closeEntry()
        }
    }

    private fun extractFile(zip: ZipFile, entry: ZipEntry, outFile: File) {
        val buffer = ByteArray(bufferSize)
        var length: Int

        FileOutputStream(outFile).use {
            BufferedInputStream(zip.getInputStream(entry)).use { bis ->
                BufferedOutputStream(it, bufferSize).use { bos ->
                    try {
                        while (true) {
                            length = bis.read(buffer, 0, bufferSize)
                            if (length == -1) break
                            bos.write(buffer, 0, length)
                        }
                    } finally {
                        bos.flush()
                    }
                }
            }
        }
    }

    private fun rewriteZip(tempPath: String, zos: ZipOutputStream) {
        val buffer = ByteArray(bufferSize)
        var length: Int

        ZipFile(tempPath, charset).use { sZip ->
            val zipFileEntries = sZip.entries
            while (zipFileEntries.hasMoreElements()) {
                val entry = zipFileEntries.nextElement() as ZipEntry
                val currentEntry = entry.name
                try {
                    zos.putNextEntry(ZipEntry(currentEntry))
                    BufferedInputStream(sZip.getInputStream(entry)).use { bis ->
                        while (true) {
                            length = bis.read(buffer, 0, bufferSize)
                            if (length == -1) {
                                break
                            }
                            zos.write(buffer, 0, length)
                        }
                    }
                } finally {
                    zos.closeEntry()
                }
            }
            zos.setComment(getComment(tempPath))
        }
    }

    private fun getZipCommentFromBuffer(buffer: ByteArray, length: Int): String? {
        val magicDirEnd = byteArrayOf(0x50, 0x4b, 0x05, 0x06)
        val bufferLength = min(buffer.size, length)
        for (i in bufferLength - magicDirEnd.size - 22 downTo 0) {
            var isMagicStart = true
            for (k in magicDirEnd.indices) {
                if (buffer[i + k] != magicDirEnd[k]) {
                    isMagicStart = false
                    break
                }
            }
            if (isMagicStart) {
                val commentLength = buffer[i + 20] + buffer[i + 21] * 256
                val realLength = bufferLength - i - 22
                try {
                    return String(buffer, i + 22, min(commentLength, realLength), Charsets.UTF_8)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        }

        return null
    }
}

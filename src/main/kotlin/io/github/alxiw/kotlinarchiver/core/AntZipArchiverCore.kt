package io.github.alxiw.kotlinarchiver.core

import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipFile
import org.apache.tools.zip.ZipOutputStream

import java.io.*

class AntZipArchiverCore : ArchiverCore {

    private var bufferSize = 4096
    private var charset = Charsets.UTF_8.name()

    override fun pack(path: String, sources: Array<String>, comment: String?): Int {
        var errorCount = 0

        ZipOutputStream(FileOutputStream(path)).use {
            it.encoding = charset
            comment?.let { comment ->
                it.setComment(comment)
            }
            for (source in sources) {
                errorCount += watchDir(it, source, "")
            }
        }

        return errorCount
    }

    override fun add(path: String, sources: Array<String>): Int {
        var errorCount = 0

        val tempPath: String = path + "_temp"
        val tempZip = File(path)
        tempZip.renameTo(File(tempPath))

        ZipOutputStream(FileOutputStream(path)).use {
            it.encoding = charset
            reWriteZip(tempPath, it)
            for (source in sources) {
                errorCount += watchDir(it, source, "")
            }
        }

        File(tempPath).delete()
        return errorCount
    }

    override fun setComment(path: String, comment: String): Int {
        val tempPath: String = path + "_temp"
        val tempZip = File(path)
        tempZip.renameTo(File(tempPath))

        ZipOutputStream(FileOutputStream(path)).use {
            it.encoding = charset
            reWriteZip(tempPath, it)
            it.setComment(comment)
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
        val buffer = ByteArray(Math.min(fileLength, 16384))

        FileInputStream(file).use {
            it.skip((fileLength - buffer.size).toLong())

            val length = it.read(buffer)
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
            when {
                filePath.isDirectory -> for (child in filePath.listFiles()) {
                    errorCount += watchDir(zos, child.path, if (prefix !== "") prefix + File.separatorChar + filePath.name else filePath.name)
                }
                filePath.exists() -> archiveFile(zos, path, if (prefix !== "") prefix + File.separatorChar + filePath.name else filePath.name)
                else -> errorCount++
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
            FileInputStream(srcPath).use {
                while (true) {
                    length = it.read(buffer)
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
            val bos = BufferedOutputStream(it, bufferSize)
            val bis = BufferedInputStream(zip.getInputStream(entry))
            try {
                while (true) {
                    length = bis.read(buffer, 0, bufferSize)
                    if (length == -1) break
                    bos.write(buffer, 0, length)
                }
            } finally {
                bis.close()
                bos.flush()
                bos.close()
            }
        }

    }

    private fun reWriteZip(tempPath: String, zos: ZipOutputStream) {
        val buffer = ByteArray(bufferSize)
        var length: Int

        val sZip = ZipFile(tempPath, charset)
        val zipFileEntries = sZip.entries
        while (zipFileEntries.hasMoreElements()) {
            val entry = zipFileEntries.nextElement() as ZipEntry
            val currentEntry = entry.name
            try {
                zos.putNextEntry(ZipEntry(currentEntry))
                BufferedInputStream(sZip.getInputStream(entry)).use {
                    while (true) {
                        length = it.read(buffer, 0, bufferSize)
                        if (length == -1) break
                        zos.write(buffer, 0, length)
                    }
                }
            } finally {
                zos.closeEntry()
            }
        }
        sZip.close()
        zos.setComment(getComment(tempPath))
    }

    private fun getZipCommentFromBuffer(buffer: ByteArray, length: Int): String? {
        val magicDirEnd = byteArrayOf(0x50, 0x4b, 0x05, 0x06)
        val bufferLength = Math.min(buffer.size, length)
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
                    return String(buffer, i + 22, Math.min(commentLength, realLength), Charsets.UTF_8)
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }
}


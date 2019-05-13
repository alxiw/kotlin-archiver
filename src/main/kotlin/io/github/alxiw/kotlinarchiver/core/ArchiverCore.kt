package io.github.alxiw.kotlinarchiver.core

interface ArchiverCore {

    fun pack(path: String, sources: Array<String>, comment: String?): Int
    fun add(path: String, sources: Array<String>): Int
    fun setComment(path: String, comment: String): Int
    fun extract(path: String, out: String?)
    fun getComment(path: String): String?

}

package io.github.alxiw.kotlinarchiver.parser

enum class Actions(val action: String, val format: String) {

    PACK("Pack", "-p -z <zip_file> -s <source_1> <source_2> ... [-c <comment>]"),
    ADD("Add", "-a -z <zip_file> -s <source_1> <source_2> ..."),
    ADD_COMMENT("Add comment", "-a -z <zip_file> -c <comment>"),
    EXTRACT("Extract", "-e -z <zip_file> [-o <out_dir>]"),
    GET_COMMENT("Get comment", "-g -z <zip_file>"),
    HELP("Help", "-h")

}

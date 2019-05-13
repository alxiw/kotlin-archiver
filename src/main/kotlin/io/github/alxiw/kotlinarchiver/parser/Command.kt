package io.github.alxiw.kotlinarchiver.parser

data class Command(val name: String) {

    var zip: String? = null
    var sources: Array<String>? = null
    var comment: String? = null
    var out: String? = null

    // -p and -a constructor
    constructor(name: String, zip: String, sources: Array<String>?, comment: String?) : this(name) {
        this.zip = zip
        this.sources = sources
        this.comment = comment
    }

    // -e constructor
    constructor(name: String, zip: String, out: String?) : this(name) {
        this.zip = zip
        this.out = out
    }

    // -g constructor
    constructor(name: String, zip: String) : this(name) {
        this.zip = zip
    }

    override fun toString(): String {

        val response = StringBuilder()

        if (this.name == "p" || this.name == "a") {
            response.append("command: $name, zip: $zip")
            this.sources?.let {
                response.append(", sources: ${getStringFromSources(it)}")
            }
            this.comment?.let {
                response.append(", comment: $it")
            }
        } else if (this.name == "e") {
            response.append("command: $name, zip: $zip")
            this.out?.let {
                response.append(", out: $it")
            }
        } else if (this.name == "g") {
            response.append("command: $name, zip: $zip")
        } else {
            response.append("command: $name")
            this.zip?.let {
                response.append(", zip: $it")
            }
            this.comment?.let {
                response.append(", comment: $it")
            }
            this.sources?.let {
                response.append(", sources: ${getStringFromSources(it)}")
            }
            this.out?.let {
                response.append(", out: $it")
            }
        }

        return response.toString()
    }

    private fun getStringFromSources(sources: Array<String>?): String {
        sources?.let {
            return sources.joinToString(separator = " ")
        }
        return ""
    }
}

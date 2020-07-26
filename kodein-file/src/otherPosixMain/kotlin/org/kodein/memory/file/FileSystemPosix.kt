package org.kodein.memory.file

import kotlinx.cinterop.*
import org.kodein.memory.io.IOException
import platform.posix.*

public actual object FileSystem {
    internal actual val pathSeparator: String = "/"

    public actual val tempDirectory: Path by lazy {
        var path: String? = null

        for (v in listOf("TMPDIR", "TMP", "TEMP", "TEMPDIR")) {
            path = getenv(v)?.toKString()
            if (path != null) break
        }

        Path(path ?: "/tmp")
    }

    public actual var currentDirectory: Path
        get() {
            memScoped {
                val ptr = allocArray<ByteVar>(4096)
                return Path(getcwd(ptr, 4096.convert())?.toKString() ?: throw IOException.fromErrno("directory"))
            }
        }
        set(value) {
            chdir(value.path)
        }

    public actual val roots: List<Path> = listOf(Path("/"))
}

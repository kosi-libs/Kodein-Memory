package org.kodein.memory.io

import org.kodein.memory.Closeable

public class SliceBuilder(private val initialCapacity: Int, private val alloc: (Int) -> Allocation) : Closeable {

    private val allocs = ArrayList<Allocation>()

    private var current: Allocation = alloc(initialCapacity).also { allocs += it }

    private var startPosition: Int = 0

    private var hasSubSlice = false

    public var copies: Int = 0
        private set

    private fun checkSize(size: Int) {
        if (current.position + size <= current.limit) return

        val neededSize = current.position - startPosition + size
        val factor =
                if (neededSize < (initialCapacity / 2))
                    1
                else
                    ((neededSize / initialCapacity) + 2)

        val previousAllocation = current
        val previousBuffer = previousAllocation.duplicate()
        previousBuffer.limitHere()
        val isOneSlice = startPosition == 0 && !hasSubSlice
        if (isOneSlice) {
            allocs.removeAt(allocs.lastIndex)
        }
        previousBuffer.position = startPosition
        current = alloc(factor * initialCapacity).also { allocs += it }
        startPosition = 0
        hasSubSlice = false
        current.putBytes(previousBuffer)
        ++copies
        if (isOneSlice) {
            previousAllocation.close()
        }
    }

    public inner class BuilderWriteable internal constructor(): Writeable {
        override val available: Int get() = Int.MAX_VALUE

        override fun putByte(value: Byte) {
            checkSize(Byte.SIZE_BYTES)
            current.putByte(value)
        }

        override fun putChar(value: Char) {
            checkSize(Char.SIZE_BYTES)
            current.putChar(value)
        }

        override fun putShort(value: Short) {
            checkSize(Short.SIZE_BYTES)
            current.putShort(value)
        }

        override fun putInt(value: Int) {
            checkSize(Int.SIZE_BYTES)
            current.putInt(value)
        }

        override fun putLong(value: Long) {
            checkSize(Long.SIZE_BYTES)
            current.putLong(value)
        }

        override fun putFloat(value: Float) {
            checkSize(Int.SIZE_BYTES)
            current.putFloat(value)
        }

        override fun putDouble(value: Double) {
            checkSize(Long.SIZE_BYTES)
            current.putDouble(value)
        }

        override fun putBytes(src: ByteArray, srcOffset: Int, length: Int) {
            checkSize(length)
            current.putBytes(src, srcOffset, length)
        }

        override fun putBytes(src: Readable, length: Int) {
            checkSize(length)
            current.putBytes(src, length)
        }

        override fun flush() {
            current.flush()
        }

        public fun subSlice(block: () -> Unit) : ReadBuffer {
            val startOffset = current.position - startPosition
            block()
            hasSubSlice = true
            val newStart = startPosition + startOffset
            return current.slice(newStart, current.position - newStart)
        }
    }

    private val writeable = BuilderWriteable()

    public fun newSlice(block: BuilderWriteable.() -> Unit): KBuffer {
        startPosition = current.position
        writeable.block()
        return current.slice(startPosition, current.position - startPosition)
    }

    public val allocationCount: Int get() = allocs.size

    public val allocationSize: Int get() = allocs.sumBy { it.capacity }

    override fun close() {
        allocs.forEach { it.close() }
    }

    public companion object {
        public fun native(initialCapacity: Int): SliceBuilder = SliceBuilder(initialCapacity) { Allocation.native(it) }
        public fun array(initialCapacity: Int): SliceBuilder = SliceBuilder(initialCapacity) { KBuffer.array(it).asManagedAllocation() }
    }
}

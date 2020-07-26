package org.kodein.memory.io

public class ByteArrayKBuffer constructor(public val array: ByteArray) : AbstractKBuffer(array.size) {

    override val implementation: String get() = "ByteArrayKBuffer"

    override fun createDuplicate(): ByteArrayKBuffer = ByteArrayKBuffer(array)

    override fun unsafeSetBytes(index: Int, src: ByteArray, srcOffset: Int, length: Int) {
        src.copyInto(array, destinationOffset = index, startIndex = srcOffset, endIndex = srcOffset + length)
    }

    override fun unsafeTrySetBytesOptimized(index: Int, src: AbstractKBuffer, srcOffset:Int, length: Int): Boolean = false

    override fun unsafeSetByte(index: Int, value: Byte) { array[index] = value }

    override fun unsafeSetShort(index: Int, value: Short) {
        slowStoreShort(value) { i, b -> array[index + i] = b }
    }

    override fun unsafeSetInt(index: Int, value: Int) {
        slowStoreInt(value) { i, b -> array[index + i] = b }
    }

    override fun unsafeSetLong(index: Int, value: Long) {
        slowStoreLong(value) { i, b -> array[index + i] = b }
    }

    override fun unsafeGetBytes(index: Int, dst: ByteArray, dstOffset: Int, length: Int) {
        array.copyInto(dst, destinationOffset = dstOffset, startIndex = index, endIndex = index + length)
    }

    override fun unsafeGetByte(index: Int): Byte = array[index]

    override fun unsafeGetShort(index: Int): Short = slowLoadShort { array[index + it] }

    override fun unsafeGetInt(index: Int): Int = slowLoadInt { array[index + it] }

    override fun unsafeGetLong(index: Int): Long = slowLoadLong { array[index + it] }

    override fun tryEqualsOptimized(index: Int, other: AbstractKBuffer, otherIndex: Int, length: Int): Boolean? {
        if (index == 0 && otherIndex == 0 && length == array.size && other is ByteArrayKBuffer && length == other.array.size) {
            return array.contentEquals(other.array)
        }
        return null
    }

    override fun backingArray(): ByteArray? = array
}

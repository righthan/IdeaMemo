package com.ldlywt.note.utils

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * PageToken 数据类，对应 protobuf 中的 PageToken 结构
 * message PageToken {
 *   int32 limit = 1;
 *   int32 offset = 2;
 * }
 */
data class PageToken(
    val limit: Int,
    val offset: Int
)

/**
 * PageToken 加密解密工具类
 * 实现了类似 Go 代码中 getPageToken 和 unmarshalPageToken 的功能
 * 使用模拟 protobuf 的二进制序列化方式，而不是 JSON
 */
object PageTokenCrypto {

    // Protobuf 字段标识符
    private const val LIMIT_FIELD_TAG = 1
    private const val OFFSET_FIELD_TAG = 2

    // Protobuf wire types
    private const val WIRE_TYPE_VARINT = 0

    /**
     * 创建并编码 PageToken
     * 对应 Go 代码中的 getPageToken 函数
     *
     * @param limit 分页限制
     * @param offset 偏移量
     * @return 编码后的字符串
     * @throws Exception 编码失败时抛出异常
     */
    fun createPageToken(limit: Int, offset: Int): String {
        val pageToken = PageToken(limit = limit, offset = offset)
        return encodePageToken(pageToken)
    }

    /**
     * 编码 PageToken 对象
     * 对应 Go 代码中的 marshalPageToken 函数
     * 模拟 protobuf 的二进制序列化
     *
     * @param pageToken 要编码的 PageToken 对象
     * @return base64 编码的字符串
     * @throws Exception 序列化或编码失败时抛出异常
     */
    fun encodePageToken(pageToken: PageToken): String {
        return try {
            val outputStream = ByteArrayOutputStream()

            // 编码 limit 字段 (字段号=1, wire_type=0)
            writeField(outputStream, LIMIT_FIELD_TAG, pageToken.limit)

            // 编码 offset 字段 (字段号=2, wire_type=0)
            writeField(outputStream, OFFSET_FIELD_TAG, pageToken.offset)

            val protobufBytes = outputStream.toByteArray()

            // 使用 Base64 编码
            Base64.encodeToString(protobufBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw Exception("Failed to encode page token: ${e.message}", e)
        }
    }

    /**
     * 解码 PageToken 字符串
     * 对应 Go 代码中的 unmarshalPageToken 函数
     * 模拟 protobuf 的二进制反序列化
     *
     * @param encodedToken base64 编码的字符串
     * @return 解码后的 PageToken 对象
     * @throws Exception 解码或反序列化失败时抛出异常
     */
    fun decodePageToken(encodedToken: String): PageToken {
        return try {
            // 1. Base64 解码
            val protobufBytes = Base64.decode(encodedToken, Base64.NO_WRAP)

            // 2. 解析 protobuf 字节流
            val inputStream = ByteArrayInputStream(protobufBytes)

            var limit = 0
            var offset = 0

            while (inputStream.available() > 0) {
                val tag = readVarint(inputStream)
                val fieldNumber = tag shr 3
                val wireType = tag and 0x7

                when (fieldNumber) {
                    LIMIT_FIELD_TAG -> {
                        if (wireType == WIRE_TYPE_VARINT) {
                            limit = readVarint(inputStream)
                        } else {
                            throw Exception("Invalid wire type for limit field")
                        }
                    }
                    OFFSET_FIELD_TAG -> {
                        if (wireType == WIRE_TYPE_VARINT) {
                            offset = readVarint(inputStream)
                        } else {
                            throw Exception("Invalid wire type for offset field")
                        }
                    }
                    else -> {
                        // 跳过未知字段
                        skipField(inputStream, wireType)
                    }
                }
            }

            PageToken(limit = limit, offset = offset)
        } catch (e: Exception) {
            throw Exception("Failed to decode page token: ${e.message}", e)
        }
    }

    /**
     * 写入一个 protobuf 字段
     */
    private fun writeField(outputStream: ByteArrayOutputStream, fieldNumber: Int, value: Int) {
        // 写入字段标签 (field_number << 3 | wire_type)
        val tag = (fieldNumber shl 3) or WIRE_TYPE_VARINT
        writeVarint(outputStream, tag)

        // 写入字段值
        writeVarint(outputStream, value)
    }

    /**
     * 写入 varint 编码的整数
     * 模拟 protobuf 的 varint 编码
     */
    private fun writeVarint(outputStream: ByteArrayOutputStream, value: Int) {
        var v = value
        while (v and 0x7F.inv() != 0) {
            outputStream.write((v and 0x7F) or 0x80)
            v = v ushr 7
        }
        outputStream.write(v and 0x7F)
    }

    /**
     * 读取 varint 编码的整数
     * 模拟 protobuf 的 varint 解码
     */
    private fun readVarint(inputStream: ByteArrayInputStream): Int {
        var result = 0
        var shift = 0

        while (true) {
            val byte = inputStream.read()
            if (byte == -1) {
                throw Exception("Unexpected end of stream while reading varint")
            }

            result = result or ((byte and 0x7F) shl shift)

            if ((byte and 0x80) == 0) {
                break
            }

            shift += 7
            if (shift >= 32) {
                throw Exception("Varint too long")
            }
        }

        return result
    }

    /**
     * 跳过未知字段
     */
    private fun skipField(inputStream: ByteArrayInputStream, wireType: Int) {
        when (wireType) {
            WIRE_TYPE_VARINT -> {
                readVarint(inputStream)
            }
            // 如果需要支持其他 wire types，可以在这里添加
            else -> {
                throw Exception("Unsupported wire type: $wireType")
            }
        }
    }

    /**
     * 验证 PageToken 是否有效
     *
     * @param encodedToken 编码的字符串
     * @return 如果有效返回 true，否则返回 false
     */
    fun isValidPageToken(encodedToken: String): Boolean {
        return try {
            decodePageToken(encodedToken)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 从编码的字符串中提取 limit 和 offset
     *
     * @param encodedToken 编码的字符串
     * @return Pair<limit, offset> 如果解码成功，否则抛出异常
     */
    fun extractLimitAndOffset(encodedToken: String): Pair<Int, Int> {
        val pageToken = decodePageToken(encodedToken)
        return Pair(pageToken.limit, pageToken.offset)
    }
}

/**
 * 扩展函数，为字符串添加 PageToken 解码功能
 */
fun String.toPageToken(): PageToken = PageTokenCrypto.decodePageToken(this)

/**
 * 扩展函数，为 PageToken 添加编码功能
 */
fun PageToken.encode(): String = PageTokenCrypto.encodePageToken(this)
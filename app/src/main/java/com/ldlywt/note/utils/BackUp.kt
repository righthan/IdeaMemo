package com.ldlywt.note.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import com.ldlywt.note.App
import com.ldlywt.note.bean.NoteShowBean
import dalvik.system.ZipPathValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.zeroturnaround.zip.commons.IOUtils
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class ExportItem(val dir: String, val file: File)

object BackUp {

    private const val my_key = "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6"

    suspend fun exportEncrypted(context: Context, uri: Uri): String = suspendCoroutine { continuation ->
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            val cipher = Cipher.getInstance("AES")
            val secretKey = SecretKeySpec(my_key.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val zipStream = ZipOutputStream(BufferedOutputStream(CipherOutputStream(stream, cipher)))
            try {
                val files = listOf(
                    ExportItem("/", File(context.dataDir.path + "/databases")),
                    ExportItem("/", context.filesDir),
                    ExportItem("/external/", context.getExternalFilesDir(null)!!)
                )
                for (i in files.indices) {
                    val item = files[i]
                    appendFile(zipStream, item.dir, item.file)
                }
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val fileName = cursor.getStringValue(OpenableColumns.DISPLAY_NAME)
                        continuation.resume(fileName)
                    }
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            } finally {
                zipStream.close()
            }
        }
    }

    suspend fun restoreFromEncryptedZip(context: Context, uri: Uri, isEncrypt: Boolean) = withContext(Dispatchers.IO) {
        // https://stackoverflow.com/questions/77683434/the-getnextentry-method-of-zipinputstream-throws-a-zipexception-invalid-zip-ent
        if (Build.VERSION.SDK_INT >= 34) {
            ZipPathValidator.clearCallback()
        }
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val stream = if (isEncrypt) {
                val cipher = Cipher.getInstance("AES")
                val secretKey = SecretKeySpec(my_key.toByteArray(), "AES")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                CipherInputStream(inputStream, cipher)
            } else {
                inputStream
            }

            val destFile = File(context.cacheDir, "decrypted_restore")
            destFile.mkdirs()

            val zipStream = ZipInputStream(BufferedInputStream(stream))
            var entry = zipStream.nextEntry
            while (entry != null) {
                val entryFile = File(destFile, entry.name)
                entryFile.parentFile?.mkdirs()

                if (!entry.isDirectory) {
                    FileOutputStream(entryFile).use { fileOut ->
                        zipStream.copyTo(fileOut)
                    }
                }

                entry = zipStream.nextEntry
            }

            // Restore logic here using the extracted files
            if (File(destFile.path + "/databases").exists()) {
                // restore database
                File(destFile.path + "/databases").copyRecursively(File(context.dataDir.path + "/databases"), true)
            }
            if (File(destFile.path + "/files").exists()) {
                // restore local storage
                File(destFile.path + "/files").copyRecursively(context.filesDir, true)
            }
            if (File(destFile.path + "/external/files").exists()) {
                // restore external files
                File(destFile.path + "/external/files").copyRecursively(context.getExternalFilesDir(null)!!, true)
            }

            destFile.deleteRecursively()
        }
    }


    suspend fun export(context: Context, uri: Uri): String = suspendCoroutine { continuation ->
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            val out = ZipOutputStream(stream)
            try {
                val files = listOf(
                    ExportItem("/", File(context.dataDir.path + "/databases")),
                    ExportItem("/", context.filesDir),
                    ExportItem("/external/", context.getExternalFilesDir(null)!!)
                )
                for (i in files.indices) {
                    val item = files[i]
                    appendFile(out, item.dir, item.file)
                }
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val fileName = cursor.getStringValue(OpenableColumns.DISPLAY_NAME)
                        continuation.resume(fileName)
                    }
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            } finally {
                IOUtils.closeQuietly(out)
            }
        }
    }


    private fun appendFile(out: ZipOutputStream, dir: String, file: File) {
        if (file.isDirectory) {
            val files = file.listFiles() ?: return
            for (childFile in files) {
                appendFile(out, "$dir${file.name}/", childFile)
            }
        } else {
            val entry = ZipEntry("$dir${file.name}")
            entry.size = file.length()
            entry.time = file.lastModified()
            out.putNextEntry(entry)
            FileInputStream(file).use { input ->
                input.copyTo(out)
            }
            out.closeEntry()
        }
    }

    suspend fun restoreFromSd(uri: Uri) = withContext(Dispatchers.IO) {
        val context = App.instance
        App.instance.contentResolver.openInputStream(uri)?.use { stream ->
            val destFile = File(context.cacheDir, "restore")
            org.zeroturnaround.zip.ZipUtil.unpack(stream, destFile)
            if (File(destFile.path + "/databases").exists()) {
                // restore database
                File(destFile.path + "/databases").copyRecursively(File(context.dataDir.path + "/databases"), true)
            }
            if (File(destFile.path + "/files").exists()) {
                // restore local storage
                File(destFile.path + "/files").copyRecursively(context.filesDir, true)
            }
            if (File(destFile.path + "/external/files").exists()) {
                // restore external files
                File(destFile.path + "/external/files").copyRecursively(context.getExternalFilesDir(null)!!, true)
            }
            destFile.delete()
        }
    }

    suspend fun exportJson(list: List<NoteShowBean>, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        val json = Json.encodeToString(list.toSet())
        val result = runCatching {
            BufferedOutputStream(App.instance.contentResolver.openOutputStream(uri)).use { out: BufferedOutputStream ->
                out.write(json.toByteArray())
            }
        }
        result
    }

    suspend fun exportTXTFile(list: List<NoteShowBean>, uri: Uri) = withContext(Dispatchers.IO) {
        (App.instance.contentResolver.openOutputStream(uri) as? FileOutputStream)?.use { stream ->
            val ow = OutputStreamWriter(stream)
            val writer = BufferedWriter(ow)
            list.forEachIndexed { _, noteShowBean ->
                noteShowBean.note.noteTitle?.let {
                    writer.append("${it}\n")
                }
                writer.append("${noteShowBean.note.createTime.toTime()}\n")
                noteShowBean.note.locationInfo?.let {
                    writer.append(it.plus("  ".plus(noteShowBean.note.weatherInfo).plus("\n")))
                }
                writer.append(noteShowBean.note.content.plus(""))
                writer.append("\n\n")
            }
            writer.close()
        }
    }
}


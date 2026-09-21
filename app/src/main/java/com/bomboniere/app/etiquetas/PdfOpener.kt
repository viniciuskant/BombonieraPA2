package com.bomboniere.app.etiquetas

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object PdfOpener {

    fun abrir(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile,
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            }

            context.startActivity(Intent.createChooser(intent, "Abrir PDF com..."))
        } catch (e: Exception) {
            android.util.Log.e("PDF", "Erro ao abrir PDF: ${e.message}", e)
        }
    }
}
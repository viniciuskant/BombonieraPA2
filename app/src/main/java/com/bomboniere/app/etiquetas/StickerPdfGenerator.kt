package com.bomboniere.app.etiquetas

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import android.text.TextUtils
import android.text.TextPaint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
object StickerPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val PAGE_MARGIN = 10f

    private const val STICKER_WIDTH = 90f
    private const val STICKER_HEIGHT = STICKER_WIDTH / 1.618f
    private const val H_SPACING = 4f
    private const val V_SPACING = 4f

    private const val STICKERS_PER_ROW = 6
    private const val STICKERS_PER_COLUMN = 13

    // ---------- Estilo ----------
    private const val BORDER_WIDTH = 2f
    private const val NAME_FONT_SIZE = 8f
    private const val LOTE_FONT_SIZE = 7f
    private const val LINE_GAP = 1f

    data class Sticker(val productName: String, val lote: String)


    fun gerarFolha(
        context: Context,
        stickers: List<Sticker>,
        outputFile: File,
    ): File {
        require(stickers.isNotEmpty()) { "Lista de adesivos vazia" }

        val pdf = PdfDocument()

        val namePaint = buildTextPaint(bold = true, size = NAME_FONT_SIZE)
        val lotePaint = buildTextPaint(bold = false, size = LOTE_FONT_SIZE)
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = BORDER_WIDTH
            color = Color.BLACK
            isAntiAlias = true
        }

        var pageNumber = 1
        var index = 0

        while (index < stickers.size) {
            val pageInfo = PdfDocument.PageInfo
                .Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber)
                .create()
            val page = pdf.startPage(pageInfo)
            val canvas = page.canvas

            var row = 0
            while (row < STICKERS_PER_COLUMN && index < stickers.size) {
                var col = 0
                while (col < STICKERS_PER_ROW && index < stickers.size) {
                    val x = PAGE_MARGIN + col * (STICKER_WIDTH + H_SPACING)
                    val y = PAGE_MARGIN + row * (STICKER_HEIGHT + V_SPACING)
                    drawSticker(canvas, stickers[index], x, y, namePaint, lotePaint, borderPaint)
                    col++
                    index++
                }
                row++
            }

            pdf.finishPage(page)
            pageNumber++
        }

        FileOutputStream(outputFile).use { pdf.writeTo(it) }
        pdf.close()
        return outputFile
    }

    private fun drawSticker(
        canvas: Canvas,
        sticker: Sticker,
        x: Float,
        y: Float,
        namePaint: Paint,
        lotePaint: Paint,
        borderPaint: Paint,
    ) {
        // Borda
        canvas.drawRect(x, y, x + STICKER_WIDTH, y + STICKER_HEIGHT, borderPaint)

        val padding = 4f
        val textWidth = (STICKER_WIDTH - padding * 2).toInt()

        val titlePaint = TextPaint(namePaint)

        // Nome com até 2 linhas
        val nameLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(
                sticker.productName,
                0,
                sticker.productName.length,
                titlePaint,
                textWidth
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(2)
                .setEllipsize(android.text.TextUtils.TruncateAt.END)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                sticker.productName,
                titlePaint,
                textWidth,
                Layout.Alignment.ALIGN_CENTER,
                1f,
                0f,
                false
            )
        }

        val loteText = "Lote: ${sticker.lote}"
        val loteWidth = lotePaint.measureText(loteText)

        val gap = 2f
        val totalHeight = nameLayout.height + gap + (lotePaint.descent() - lotePaint.ascent())
        val startY = y + (STICKER_HEIGHT - totalHeight) / 2f

        // Desenha o nome
        canvas.save()
        canvas.translate(x + padding, startY)
        nameLayout.draw(canvas)
        canvas.restore()

        // Desenha o lote
        val loteBaseline = startY + nameLayout.height + gap - lotePaint.ascent()

        canvas.drawText(
            loteText,
            x + (STICKER_WIDTH - loteWidth) / 2f,
            loteBaseline,
            lotePaint
        )
    }

    private fun buildTextPaint(bold: Boolean, size: Float): Paint = Paint().apply {
        color = Color.BLACK
        textSize = size
        isAntiAlias = true
        typeface = Typeface.create(
            Typeface.SANS_SERIF,
            if (bold) Typeface.BOLD else Typeface.NORMAL,
        )
    }

    fun gerarLote(data: String): String {
        require(data.isNotEmpty()) { "A data não pode estar vazia." }
        val digest = MessageDigest
            .getInstance("SHA-256")
            .digest(data.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }
        val decimal = BigInteger(hex, 16).toString()
        return if (decimal.length >= 8) decimal.substring(decimal.length - 8) else decimal
    }
}
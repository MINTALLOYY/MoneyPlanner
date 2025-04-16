package com.vibhu.moneyplanner

import android.content.Context
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.textract.AmazonTextract
import com.amazonaws.services.textract.AmazonTextractClient
import com.amazonaws.services.textract.model.DetectDocumentTextRequest
import com.amazonaws.services.textract.model.Document
import java.io.File
import java.nio.ByteBuffer
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.amazonaws.services.textract.model.AnalyzeExpenseRequest
import com.amazonaws.services.textract.model.BlockType
import com.amazonaws.services.textract.model.ExpenseDocument
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class TextractManager(private val textractClient: AmazonTextract) {

    fun analyzeDocument(imageFile: File, callback: (String?, String?, String?, Exception?) -> Unit) {
        try {
            // 1. Convert File to ByteBuffer (for Textract)
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream) // Adjust quality as needed
            val imageBytes = byteArrayOutputStream.toByteArray()
            val byteBuffer = ByteBuffer.wrap(imageBytes)

            // 2. Create Textract request
            val document = Document().withBytes(byteBuffer)
            val detectDocumentTextRequest = DetectDocumentTextRequest().withDocument(document)

            // 3. Call Textract API (asynchronously)
            Thread { // Perform network operations on a background thread
                try {
                    val result = textractClient.detectDocumentText(detectDocumentTextRequest)
                    val extractedTexts = extractTextFromAnalysis(result)
                    callback(extractedTexts[0], extractedTexts[1], extractedTexts[2], null) // Success!
                } catch (e: Exception) {
                    callback(null, null, null, e) // Error!
                }
            }.start()

        } catch (e: Exception) {
            callback(null, null, null, e)
        }
    }
    // New function to analyze receipts and extract total
    fun analyzeReceipt(imageFile: File, callback: (String?, Exception?) -> Unit) {
        try {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            val byteBuffer = ByteBuffer.wrap(imageBytes)

            val document = Document().withBytes(byteBuffer)
            val analyzeExpenseRequest = AnalyzeExpenseRequest().withDocument(document)

            Thread {
                try {
                    val result = textractClient.analyzeExpense(analyzeExpenseRequest)
                    val extractedData = extractReceiptTotal(result.expenseDocuments)
                    callback(extractedData, null)
                } catch (e: Exception) {
                    callback(null, e)
                }
            }.start()

        } catch (e: Exception) {
            callback(null, e)
        }
    }

    private fun extractTextFromAnalysis(result: com.amazonaws.services.textract.model.DetectDocumentTextResult): MutableList<String?> {
        val blocks = result.blocks ?: emptyList()
        val lines = mutableListOf<String>()

        for (block in blocks) {
            if (!block.text.isNullOrEmpty()) {
                lines.add(block.text!!)
                Log.d("block", block.text!! + " || ")
            }
        }
        Log.d("lines", lines.toString())

        return mutableListOf(
            getNameFromReceipt(lines),
            getDateFromReceipt(lines),
            getTotalFromReceipt(lines)
        )
    }

    private fun getTotalFromReceipt(lines: MutableList<String>) : String? {
        val totalKeywords = listOf("Total", "Amount Due", "Balance", "Grand Total", "Amount")

        // Check if Total is in a different block than the amount due
        for (i in 0 until lines.size - 1) {
            val line = lines[i]
            for (keyword in totalKeywords) {
                if (line.trim().equals(keyword, ignoreCase = true)) {
                    // Check if the next line is a currency amount
                    val nextLine = lines[i + 1]
                    val amountPattern = Pattern.compile("^\\s*([\\$£€]?\\s*[\\d,.]+(?:\\.\\d{2})?)\\s*$")
                    val matcher = amountPattern.matcher(nextLine)
                    if (matcher.find()) {
                        return matcher.group(1)
                    }
                }
            }
        }

        // Checks if the total and the amount due is in the same amount
        for (line in lines) {
            for(keyword in totalKeywords) {
                val pattern =
                    Pattern.compile("(?i)${keyword}\\s*[:\\s]*\\s*([\\$£€]?\\s*[\\d,.]+(?:\\.\\d{2})?)")
                val matcher = pattern.matcher(line)
                if (matcher.find()) {
                    return matcher.group(1)
                }
            }
        }
        return null
    }

    private fun getNameFromReceipt(lines: MutableList<String>) : String? {
        return lines[0]
    }

    private fun getDateFromReceipt(lines: MutableList<String>) : String? {
        val patterns = listOf(
            // Match dates with possible prefixes/suffixes
            Pattern.compile(".*?(\\d{2}/\\d{2}/\\d{4}).*?"),
            Pattern.compile(".*?(\\d{4}/\\d{2}/\\d{2}).*?")
        )

        for (line in lines) {
            patterns.forEach { pattern ->
                val matcher = pattern.matcher(line)
                if (matcher.matches()) {
                    return matcher.group(1)?.trim()
                }
            }
        }
        return null
    }


    private fun extractDocumentFromAnalysis(result: com.amazonaws.services.textract.model.DetectDocumentTextResult): String {
        val blocks = result.blocks
        val extractedText = StringBuilder()

        for (block in blocks) {
            if (block.blockType == "LINE") {
                extractedText.append(block.text).append("\n") // Add newline for each line
            }
        }
        return extractedText.toString()
    }

    private fun extractReceiptTotal(expenseDocuments: List<ExpenseDocument>?): String?{
        if (!expenseDocuments.isNullOrEmpty()) {
            for (expenseDocument in expenseDocuments) {
                val summaryFields = expenseDocument.summaryFields ?: continue
                for (field in summaryFields) {
                    val fieldType = field.type?.text ?: continue

                    val totalKeywords = listOf("Total", "Amount Due", "Balance", "Grand Total")

                    for (keyword in totalKeywords) {
                        val pattern = Pattern.compile("(?i)$keyword\\s*([\\d.,]+)")
                        val matcher = pattern.matcher(fieldType)

                        if (matcher.find()) {
                            return field.valueDetection?.text
                        }
                    }
                }
            }
        }
        return null
    }

    private fun extractReceiptData(expenseDocuments: List<ExpenseDocument>): String {
        val extractedData = StringBuilder()
        for (expenseDocument in expenseDocuments) {
            // Find the total amount
            val totalField = expenseDocument.summaryFields.find { it.type.text == "TOTAL" }
            if (totalField != null && totalField.valueDetection != null) {
                extractedData.append("Total: ").append(totalField.valueDetection.text).append("\n")
            }

            // Find the items and prices
            for (lineItemGroup in expenseDocument.lineItemGroups) {
                for (lineItem in lineItemGroup.lineItems) {
                    val itemName = lineItem.lineItemExpenseFields.find { it.type.text == "ITEM" }?.valueDetection?.text
                    val price = lineItem.lineItemExpenseFields.find { it.type.text == "PRICE" }?.valueDetection?.text

                    if (itemName != null && price != null) {
                        extractedData.append("Item: ").append(itemName).append(", Price: ").append(price).append("\n")
                    }
                }
            }
        }
        return extractedData.toString()
    }
}
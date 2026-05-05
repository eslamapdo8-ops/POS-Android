package com.pos.clothingstore.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * مدير الطباعة عبر البلوتوث (ESC/POS)
 */
class BluetoothPrinterManager(private val context: Context) {

    companion object {
        private const val TAG = "BluetoothPrinter"
    }

    /**
     * الحصول على قائمة الطابعات البلوتوث المقترنة
     */
    @SuppressLint("MissingPermission")
    fun getPairedPrinters(): List<BluetoothDevice> {
        if (!hasBluetoothPermission()) return emptyList()

        val bluetoothAdapter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bm?.adapter
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        } ?: return emptyList()

        return bluetoothAdapter.bondedDevices?.filter { device ->
            device.uuids?.any { uuid ->
                uuid.uuid == ParcelUuid.fromString("00001101-0000-1000-8000-00805F9B34FB").uuid
            } == true
        } ?: emptyList()
    }

    /**
     * طباعة فاتورة بيع
     */
    @SuppressLint("MissingPermission")
    fun printReceipt(
        device: BluetoothDevice,
        storeName: String,
        saleId: Int,
        items: List<Triple<String, Int, Double>>, // name, qty, price
        total: Double,
        paymentMethod: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (!hasBluetoothPermission()) {
            onComplete(false)
            return
        }

        try {
            val connection = BluetoothConnection(device)
            val printer = EscPosPrinter(connection, 203, 48f, 32)

            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale("ar"))
            val dateStr = dateFormat.format(Date())

            val sb = StringBuilder()
            sb.append("[C]<b>$storeName</b>\n")
            sb.append("[C]================================\n")
            sb.append("[L]تاريخ: $dateStr\n")
            sb.append("[L]فاتورة رقم: #$saleId\n")
            sb.append("[C]================================\n")

            items.forEach { (name, qty, price) ->
                val lineTotal = qty * price
                sb.append("[L]$name x $qty\n")
                sb.append("[R]${String.format("%.2f", lineTotal)} ر.س\n")
            }

            sb.append("[C]================================\n")
            sb.append("[R]<b>الإجمالي: ${String.format("%.2f", total)} ر.س</b>\n")
            sb.append("[L]طريقة الدفع: ${if (paymentMethod == "cash") "نقدي" else "بطاقة"}\n")
            sb.append("[C]================================\n")
            sb.append("[C]<b>شكراً لزيارتكم</b>\n")

            printer.printFormattedTextAndCut(sb.toString(), 500)
            onComplete(true)
        } catch (e: Exception) {
            onComplete(false)
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }
}

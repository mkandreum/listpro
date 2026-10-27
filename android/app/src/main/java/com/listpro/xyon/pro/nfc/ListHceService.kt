package com.listpro.xyon.pro.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import com.listpro.xyon.pro.ListProApp

/**
 * El móvil que comparte se comporta como una tarjeta NFC: al recibir el SELECT del AID de ListPro
 * responde con su código de sincronización. El otro móvil lo lee y se une a la misma lista.
 */
class ListHceService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        if (!commandApdu.contentEquals(NfcProtocol.SELECT_APDU)) return NfcProtocol.UNKNOWN
        val code = (application as ListProApp).repository.state.value.sync.code ?: return NfcProtocol.NOT_FOUND
        return code.toByteArray(Charsets.US_ASCII) + NfcProtocol.OK
    }

    override fun onDeactivated(reason: Int) = Unit
}

object NfcProtocol {
    private val AID = byteArrayOf(0xF0.toByte(), 0x01, 0x02, 0x03, 0x04, 0x05, 0x06)
    val SELECT_APDU = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, AID.size.toByte()) + AID
    val OK = byteArrayOf(0x90.toByte(), 0x00)
    val NOT_FOUND = byteArrayOf(0x6A, 0x82.toByte())
    val UNKNOWN = byteArrayOf(0x00, 0x00)

    /** Extrae el código de la respuesta del otro móvil, o null si no la entiende. */
    fun parse(response: ByteArray): String? {
        if (response.size < 2 || !response.takeLast(2).toByteArray().contentEquals(OK)) return null
        return String(response, 0, response.size - 2, Charsets.US_ASCII).takeIf { Regex("^[A-Z2-9]{8}$").matches(it) }
    }
}

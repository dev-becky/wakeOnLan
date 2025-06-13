package com.example.testewakeonlan

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class MainActivity : AppCompatActivity() {

    private lateinit var buttonPower: ImageView
    private var isComputerOn = false // Variável para rastrear o estado do computador
    private val ipAddress = "" // Colocar o IP do seu computador
    private val macAddress = "" //colocar o Mac do seu computador


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
         * Used to run app on full screen
         */
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        buttonPower = findViewById(R.id.btnPower)
        buttonPower.setOnClickListener {
            sendWakeOnLanPacket(macAddress, ipAddress) {
                isComputerOn = true
            }
        }

    }

    private fun sendWakeOnLanPacket(
        macAddress: String,
        broadcastAddress: String,
        onSuccess: () -> Unit
    ) {
        Thread {
            try {
                val macBytes = macAddress.split(":").map { it.toInt(16).toByte() }.toByteArray() //converte o array em bytes, necessário para construir o pacote WOL
                val packetData = ByteArray(102)
                for (i in 0 until 6) {
                    packetData[i] = 0xFF.toByte()
                }
                for (i in 1..16) {
                    System.arraycopy(macBytes, 0, packetData, i * 6, macBytes.size)
                }

                // Use o endereço de broadcast
                val broadcast = InetAddress.getByName("192.168.15.255")
                val packet = DatagramPacket(packetData, packetData.size, broadcast, 9) // Porta 9 é padrão para WoL (Pode variar)
                val socket = DatagramSocket()
                socket.broadcast = true // Permite enviar para o broadcast
                socket.send(packet)
                socket.close()
                runOnUiThread {
                    Toast.makeText(this, "Computador ligado com sucesso!", Toast.LENGTH_SHORT)
                        .show()
                }
                onSuccess()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Erro ao ligar o computador: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }
}




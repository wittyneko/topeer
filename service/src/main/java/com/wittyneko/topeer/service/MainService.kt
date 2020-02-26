package com.wittyneko.topeer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface

fun main() {
    println("start")
    val client2 = DatagramSocket()
    fun sendAddress(socket: DatagramSocket, packet: DatagramPacket) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val address = packet.socketAddress
                val clientAddress = packet.let { "${it.address?.hostAddress}:${it.port}" }
                val client2Address = client2.let { "${it.localAddress.hostAddress}:${it.localPort}" }
                val msg = "${client2Address},${clientAddress}"
                val data = msg.toByteArray()
                val sendPacket = DatagramPacket(data, data.size, address)
                socket.send(sendPacket)
                println("send $msg")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
    MainService().apply { onReceive = ::sendAddress }.start()
}

class MainService {

    val socket by lazy { DatagramSocket(2233) }

    var onReceive: ((DatagramSocket, DatagramPacket) -> Unit)? = null

    fun start() {

        try {
            println()
            val localHost = InetAddress.getLocalHost()
            println("localHost: $localHost")
            println("localHost.hostAddress: ${localHost.hostAddress}")
            println("localHost.hostName: ${localHost.hostName}")
            println("localHost.canonicalHostName: ${localHost.canonicalHostName}")
            println()
            val loopback = InetAddress.getLoopbackAddress()
            println("loopback: $loopback")
            println("loopback.hostAddress: ${loopback.hostAddress}")
            println("loopback.hostName: ${loopback.hostName}")
            println("loopback.canonicalHostName: ${loopback.canonicalHostName}")
            println()

            val enumeration = NetworkInterface.getNetworkInterfaces()
            enumeration.iterator().forEach { networkInterface ->
                println("networkInterface: ${networkInterface.name}, ${networkInterface.displayName}")
                networkInterface.inetAddresses.iterator().forEach { localHost ->

                    println("========================")
                    println("inetAddresses: $localHost")
                    println("inetAddresses.hostAddress: ${localHost.hostAddress}")
                    //println("inetAddresses.hostName: ${localHost.hostName}")
                    //println("inetAddresses.canonicalHostName: ${localHost.canonicalHostName}")
                }
                println()
            }

            println("inetAddress: ${socket.inetAddress}")
            println("localAddress: ${socket.localAddress}")
            println("localSocketAddress: ${socket.localSocketAddress}")
            println("remoteSocketAddress: ${socket.remoteSocketAddress}")


            while (true) {
                val receiveData = ByteArray(1024)
                val receivePaket = DatagramPacket(receiveData, receiveData.size)

                socket.receive(receivePaket)
                println("receive ${receivePaket.address}:${receivePaket.port}")
                println(String(receiveData.sliceArray(0 until receivePaket.length)))
                onReceive?.invoke(socket, receivePaket)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            socket.close()
        }
    }

}
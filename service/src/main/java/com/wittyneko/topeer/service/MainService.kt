package com.wittyneko.topeer.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Closeable
import java.net.*

const val CONNECT: Byte = 0x01
const val CONNECT_REPLY: Byte = -0x01
const val DISCONNECT: Byte = 0x02
const val SYNC: Byte = 0x03
const val USER: Byte = 0x04
const val USERS: Byte = 0x05
const val CLIENTS: Byte = 0x06

const val CLIENT: Byte = 0x7F
const val CLIENT_REPLY: Byte = -0x7F

const val DATA: Byte = 0x7E
const val DATA_REPLY: Byte = -0x7E

const val TYPE_MERGE = 0
const val TYPE_STRING: Byte = 0x01
const val TYPE_STRING_MERGE: Byte = -0x01


fun main() {
    println("start")
    
    val clientsMap = mutableMapOf<String, MutableMap<String, String>>()
    val usersMap = mutableMapOf<String, String>() //key: id, value: time,ip
    fun receive(socket: DatagramSocket, receivePacket: DatagramPacket) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val receiveData = receivePacket.data
                val code = receiveData[0]
                val body = receiveData.sliceArray(1 until receivePacket.length)
                //println("receive ${receiveData[0]},${String(receiveData.sliceArray(1 until receivePacket.length))}")
                val socketAddress = receivePacket.socketAddress
                val clientAddress = receivePacket.let { "${it.address?.hostAddress}:${it.port}" }
                val serviceAddress = socket.let { "${it.localAddress.hostAddress}:${it.localPort}" }
                val clientMap = clientsMap.getOrDefault(clientAddress, mutableMapOf())
                when (code) {
                    CONNECT -> {
                        clientsMap[clientAddress] = clientMap
                        val sendMsg = "${clientAddress},${serviceAddress}"
                        val sendData = byteArrayOf(CONNECT_REPLY, *sendMsg.toByteArray())
                        val sendPacket = DatagramPacket(sendData, sendData.size, socketAddress)
                        socket.send(sendPacket)
                        println("CONNECT $CONNECT_REPLY,$sendMsg")
                    }
                    DISCONNECT -> {
                        clientsMap.remove(clientAddress)
                        clientsMap.values.forEach { it.remove(clientAddress) }
                        usersMap.remove(clientMap[clientAddress])
                    }
                    DATA -> {
                    }
                    DATA_REPLY -> {
                    }
                    CLIENT -> {
                    }
                    CLIENT_REPLY -> {
                    }
                    CLIENTS -> {
                        clientsMap.keys.forEach { key ->
                            val ip = clientMap.getOrDefault(key, "")
                            if (ip.isEmpty()) {
                                val sendMsg = "${clientAddress},${key}"
                                val sendData = byteArrayOf(CLIENT, *sendMsg.toByteArray())
                                val sendPacket =
                                    DatagramPacket(sendData, sendData.size, socketAddress)
                                socket.send(sendPacket)
                                println("CLIENTS $CLIENT,$sendMsg")
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    GlobalScope.launch {
        MainService(3344).start()
        clientsMap.getOrPut("23.95.67.173:3344") { mutableMapOf() }
    }
    MainService().apply { onReceive = ::receive }.start()
}

// connect link joint
// disconnect
// send
// receive delivery

class MainService(port: Int = 2233) : Closeable {

    var socket = DatagramSocket(port)
        private set

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
                    println("inetAddresses: ${localHost.address.size}")
                    println("inetAddresses.hostAddress: ${localHost.hostAddress}")
                    //println("inetAddresses.hostName: ${localHost.hostName}")
                    //println("inetAddresses.canonicalHostName: ${localHost.canonicalHostName}")
                }
                println()
            }

            println("port: ${socket.port}")
            println("localPort: ${socket.localPort}")
            println("inetAddress: ${socket.inetAddress}")
            println("localAddress: ${socket.localAddress}")
            println("localSocketAddress: ${socket.localSocketAddress}")
            println("remoteSocketAddress: ${socket.remoteSocketAddress}")


            while (!socket.isClosed) {
                if (socket.isClosed) break
                val receiveData = ByteArray(1024)
                val receivePacket = DatagramPacket(receiveData, receiveData.size)

                socket.receive(receivePacket)
                println("receive ${receivePacket.address}:${receivePacket.port}")
                println("receive ${receiveData[0]},${String(receiveData.sliceArray(1 until receivePacket.length))}")
                onReceive?.invoke(socket, receivePacket)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            println("close ${socket.localPort}")
            socket.close()
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun sendPacket(address: InetSocketAddress, code: Byte, body: ByteArray) {
        val data = byteArrayOf(code, *body)
        val sendPacket = DatagramPacket(data, data.size, address)
        socket.send(sendPacket)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun sendPacket(address: InetSocketAddress, code: Byte, body: String) {
        sendPacket(address, code, body.toByteArray())
    }

    override fun close() = socket.close()

    fun connect(address: InetSocketAddress, msg: String = "connect") {
        if (socket.isClosed) socket = DatagramSocket(2233)
        sendPacket(address, CONNECT, msg)
        println("connect end")
    }

    fun disconnect(address: InetSocketAddress, msg: String = "disconnect") {
        sendPacket(address, DISCONNECT, msg)
        socket.close()
        println("disconnect end")
    }

    fun clients(address: InetSocketAddress, msg: String = "clients") {
        sendPacket(address, CLIENTS, msg)
        println("clients end")
    }

}
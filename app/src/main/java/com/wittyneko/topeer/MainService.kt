package com.wittyneko.topeer

import java.io.Closeable
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

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

class MainService : Closeable {

    var socket = DatagramSocket(2233)
        private set

    var onReceive: ((DatagramSocket, DatagramPacket) -> Unit)? = null

    fun start() {

        try {

            println("port: ${socket.port}")
            println("localPort: ${socket.localPort}")
            println("inetAddress: ${socket.inetAddress}")
            println("localAddress: ${socket.localAddress}")
            println("localSocketAddress: ${socket.localSocketAddress}")
            println("remoteSocketAddress: ${socket.remoteSocketAddress}")

            while (!socket.isClosed) {
                if (socket.isClosed) break
                val receiveData = ByteArray(1026)
                val receivePacket = DatagramPacket(receiveData, receiveData.size)

                socket.receive(receivePacket)
                println("receive ${receivePacket.address}:${receivePacket.port}")
                println("receive ${receiveData[0]},${String(receiveData.sliceArray(1 until receivePacket.length))}")
                onReceive?.invoke(socket, receivePacket)
            }
            //socket.receive(receivePaket)
        } catch (e: Throwable) {
            if (!socket.isClosed) e.printStackTrace()
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

    fun clients(address: InetSocketAddress, msg: String = "clients"){
        sendPacket(address, CLIENTS, msg)
        println("clients end")
    }

    fun sendData(address: InetSocketAddress, msg: String){
        sendPacket(address, DATA, msg)
        println("send data $msg")
    }
}
package com.wittyneko.topeer

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GlobalScope.launch(Dispatchers.IO) {
            MainService().start()
        }
    }
}

class MainService {

    val socket by lazy { DatagramSocket(2233) }

    var onReceive: ((DatagramPacket) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun start() {

        try {

            println("inetAddress: ${socket.inetAddress}")
            println("localAddress: ${socket.localAddress}")
            println("localSocketAddress: ${socket.localSocketAddress}")
            println("remoteSocketAddress: ${socket.remoteSocketAddress}")
            println("reuseAddress: ${socket.reuseAddress}")

            //链接中转站
            //val address = InetSocketAddress("service.tutoo.xyz", 2233)
            val address = InetSocketAddress("192.168.10.233", 2233)
            val data = "client message".toByteArray()
            val sendPacket = DatagramPacket(data, data.size, address)
            socket.send(sendPacket)
            println("send end")
            val receiveData = ByteArray(1024)
            val receivePaket = DatagramPacket(receiveData, receiveData.size)
            //接收节点
            socket.receive(receivePaket)
            println("receive ${receivePaket.address}:${receivePaket.port}")
            println(String(receiveData.sliceArray(0 until receivePaket.length)))

            //socket.receive(receivePaket)
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            println("close ${socket.localPort}")
            socket.close()
        }
    }

}
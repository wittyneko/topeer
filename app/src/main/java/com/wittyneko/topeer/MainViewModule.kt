package com.wittyneko.template.ui

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wittyneko.topeer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

class MainViewModule(
    val application: Application,
    val service: MainService
) : ViewModel() {

    private val clientsMap = mutableMapOf<String, MutableMap<String, String>>()
    private val usersMap = mutableMapOf<String, String>() //key: id, value: time,ip

    val clientsMapData = MutableLiveData(clientsMap)
    val usersMapData = MutableLiveData(usersMap)

    var user = MutableLiveData(Pair("", ""))
    var userMsg = MutableLiveData<Pair<String, String>>()

    //链接中转站
    val serviceAddress = InetSocketAddress("23.95.67.173", 2233)
    //val serviceAddress = InetSocketAddress("192.168.10.233", 2233)
    //val serviceAddress = InetSocketAddress("192.168.31.138", 2233)
    lateinit var userAddress: InetSocketAddress

    init {
        service.onReceive = ::receive
        GlobalScope.launch(Dispatchers.IO) {
            service.connect(serviceAddress)
            service.start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch(Dispatchers.IO) { service.close() }
    }

    fun receive(socket: DatagramSocket, receivePacket: DatagramPacket) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val receiveData = receivePacket.data
                val code = receiveData[0]
                val body = receiveData.sliceArray(1 until receivePacket.length)
                //println("receive ${receivePacket.address}:${receivePacket.port}")
                //println("receive ${receiveData[0]},${String(receiveData.sliceArray(1 until receivePacket.length))}")
                val socketAddress = receivePacket.socketAddress
                val clientAddress = receivePacket.let { "${it.address?.hostAddress}:${it.port}" }
                val serviceAddress = socket.let { "${it.localAddress.hostAddress}:${it.localPort}" }
                val clientMap = clientsMap.getOrElse(clientAddress) { mutableMapOf() }
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
                        userMsg.postValue( clientAddress to String(body))
                    }
                    DATA_REPLY -> {
                    }
                    CLIENT -> {
                        val msg = String(body).split(',')
                        val address = msg[1]
                        clientsMap[address] = clientMap
                        clientsMapData.postValue(clientsMap)
                    }
                    CLIENT_REPLY -> {
                    }
                    CLIENTS -> {
                        clientsMap.keys.forEach { key ->
                            val ip = clientMap.getOrElse(key) { "" }
                            if (ip.isNotEmpty()) {
                                val sendMsg = "${clientAddress},${key}"
                                val sendData = byteArrayOf(CLIENT, *sendMsg.toByteArray())
                                val sendPacket =
                                    DatagramPacket(sendData, sendData.size, socketAddress)
                                socket.send(sendPacket)
                                println("CLIENT_SEND $CLIENT,$sendMsg")
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun clients() = viewModelScope.launch(Dispatchers.IO) {
        service.clients(serviceAddress)
    }

    fun sendData(msg: String){
        viewModelScope.launch(Dispatchers.IO) { service.sendData(userAddress, msg) }
    }

}
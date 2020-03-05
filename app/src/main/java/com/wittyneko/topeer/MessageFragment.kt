package com.wittyneko.topeer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.wittyneko.template.ui.MainViewModule
import kotlinx.android.synthetic.main.fragment_message.view.*
import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import java.net.InetSocketAddress


class MessageFragment : BaseFragment() {

    override val kodein = Kodein.lazy {
        extend(parentKodein)
    }

    val viewModule: MainViewModule by instance()
    val adapter by lazy { Adapter(requireContext(), arrayListOf()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)
        view.list.adapter = adapter
        viewModule.user.observe(this) {
            viewModule.userAddress = InetSocketAddress(it.first, it.second.toInt())
        }
        viewModule.userMsg.observe(this) {
            addMessage(it)
        }
        view.btn_send.setOnClickListener {
            val text = view.edt_input.text.toString()
            viewModule.sendData(text)
            addMessage("0.0.0.0" to text )
            view.edt_input.setText("")
        }
        return view
    }

    fun addMessage(msg: Pair<String, String>){

        adapter.list.add(msg)
        adapter.notifyDataSetChanged()
        view!!.list.smoothScrollToPosition(adapter.itemCount)
    }

    class Adapter(val context: Context, val list: MutableList<Pair<String, String>>) :
        RecyclerView.Adapter<Adapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list.get(position)
            holder.text1.text = item.first
            holder.text2.text = item.second
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val text1 by lazy { itemView.findViewById<TextView>(android.R.id.text1) }
            val text2 by lazy { itemView.findViewById<TextView>(android.R.id.text2) }

            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    val item = list.get(position)
                }
            }
        }
    }

}

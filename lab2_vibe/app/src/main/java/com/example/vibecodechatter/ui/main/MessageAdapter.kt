package com.example.vibecodechatter.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vibecodechatter.data.model.ChattMessage
import com.example.vibecodechatter.databinding.ItemMessageBinding

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private val items = mutableListOf<ChattMessage>()

    fun submitMessages(messages: List<ChattMessage>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMessageBinding.inflate(inflater, parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChattMessage) {
            binding.usernameTextView.text = message.username
            binding.timestampTextView.text = message.displayTimestamp
            binding.messageTextView.text = message.message
        }
    }
}


package cn.edu.sjtu.ega816.kotlinChatter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import cn.edu.sjtu.ega816.kotlinChatter.databinding.ListitemChattBinding
import coil.load
import java.io.File

class ChattListAdapter(
    private val context: android.content.Context,
    private val chatts: List<Chatt>
) : BaseAdapter() {

    override fun getCount(): Int = chatts.size

    override fun getItem(position: Int): Any = chatts[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val binding = ListitemChattBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            viewHolder = ViewHolder(binding)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val chatt = chatts[position]
        val listItemView = viewHolder.binding

        listItemView.usernameView.text = chatt.username ?: ""
        listItemView.messageTextView.text = chatt.message ?: ""
        listItemView.timestampView.text = chatt.timestamp ?: ""

        // show image
        chatt.imageUrl?.let {
            listItemView.chattImage.visibility = View.VISIBLE
            listItemView.chattImage.load(it) {
                crossfade(true)
                crossfade(1000)
            }
        } ?: run {
            listItemView.chattImage.visibility = View.GONE
            listItemView.chattImage.setImageBitmap(null)
        }

        // show video button
        chatt.videoUrl?.let {
            listItemView.videoButton.visibility = View.VISIBLE
            listItemView.videoButton.setOnClickListener { v: View ->
                if (v.id == R.id.videoButton) {
                    val intent = Intent(context, VideoPlayActivity::class.java)
                    intent.putExtra("VIDEO_URI", Uri.parse(it))
                    context.startActivity(intent)
                }
            }
        } ?: run {
            listItemView.videoButton.visibility = View.INVISIBLE
            listItemView.videoButton.setOnClickListener(null)
        }

        return view
    }

    private class ViewHolder(val binding: ListitemChattBinding)
}
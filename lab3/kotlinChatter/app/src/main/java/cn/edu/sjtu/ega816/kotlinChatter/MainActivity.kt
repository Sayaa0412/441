package cn.edu.sjtu.ega816.kotlinChatter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import cn.edu.sjtu.ega816.kotlinChatter.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var view: ActivityMainBinding
    private lateinit var chattListAdapter: ChattListAdapter

    private val propertyObserver = object: ObservableList.OnListChangedCallback<ObservableArrayList<Int>>() {
        override fun onChanged(sender: ObservableArrayList<Int>?) { }
        override fun onItemRangeChanged(sender: ObservableArrayList<Int>?, positionStart: Int, itemCount: Int) { }
        override fun onItemRangeInserted(
            sender: ObservableArrayList<Int>?,
            positionStart: Int,
            itemCount: Int
        ) {
            println("onItemRangeInserted: $positionStart, $itemCount")
            runOnUiThread {
                chattListAdapter.notifyDataSetChanged()
            }
        }
        override fun onItemRangeMoved(sender: ObservableArrayList<Int>?, fromPosition: Int, toPosition: Int,
                                      itemCount: Int) { }
        override fun onItemRangeRemoved(sender: ObservableArrayList<Int>?, positionStart: Int,
                                        itemCount: Int) { }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        view = ActivityMainBinding.inflate(layoutInflater)
        setContentView(view.root)

        chattListAdapter = ChattListAdapter(this, ChattStore.chatts)
        view.chattListView.adapter = chattListAdapter

        ChattStore.chatts.addOnListChangedCallback(propertyObserver)

        ChattStore.getChatts()

        view.refreshContainer.setOnRefreshListener {
            refreshTimeline()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChattStore.chatts.removeOnListChangedCallback(propertyObserver)
    }

    fun startPost(view: android.view.View) {
        val intent = Intent(this, PostActivity::class.java)
        startActivity(intent)
    }

    private fun refreshTimeline() {
        ChattStore.getChatts()
        // stop the refreshing animation upon completion:
        view.refreshContainer.isRefreshing = false
    }
}
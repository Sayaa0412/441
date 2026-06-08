package com.example.vibecodechatter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vibecodechatter.databinding.ActivityMainBinding
import com.example.vibecodechatter.ui.main.MainViewModel
import com.example.vibecodechatter.ui.main.MessageAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var messageAdapter: MessageAdapter

    private val viewModel: MainViewModel by lazy {
        val application = application as ChatterApplication
        ViewModelProvider(
            this,
            MainViewModel.Factory(application.messageRepository)
        )[MainViewModel::class.java]
    }

    private val postScreenLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.refreshMessages()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupMessageList()
        bindUi()
    }

    private fun setupMessageList() {
        messageAdapter = MessageAdapter()
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = messageAdapter
            setHasFixedSize(true)
        }
    }

    private fun bindUi() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshMessages()
        }

        binding.addMessageFab.setOnClickListener {
            postScreenLauncher.launch(Intent(this, PostActivity::class.java))
        }

        viewModel.messages.observe(this) { messages ->
            messageAdapter.submitMessages(messages)
            binding.emptyTextView.isVisible = messages.isEmpty()
        }

        viewModel.isRefreshing.observe(this) { isRefreshing ->
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
        }

        viewModel.errorMessage.observe(this) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                viewModel.onErrorShown()
            }
        }
    }
}


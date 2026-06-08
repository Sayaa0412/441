package com.example.vibecodechatter

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.vibecodechatter.databinding.ActivityPostBinding
import com.example.vibecodechatter.ui.post.PostEvent
import com.example.vibecodechatter.ui.post.PostViewModel

class PostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostBinding

    private val viewModel: PostViewModel by lazy {
        val application = application as ChatterApplication
        ViewModelProvider(
            this,
            PostViewModel.Factory(
                application.messageRepository,
                application.userPreferences
            )
        )[PostViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        bindUi()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24)
        binding.toolbar.setNavigationContentDescription(R.string.back)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun bindUi() {
        binding.sendButton.setOnClickListener {
            submitMessage()
        }

        viewModel.lastUsername.observe(this) { savedUsername ->
            if (binding.usernameEditText.text.isNullOrBlank()) {
                binding.usernameEditText.setText(savedUsername)
            }
        }

        viewModel.isSending.observe(this) { isSending ->
            binding.usernameEditText.isEnabled = !isSending
            binding.messageEditText.isEnabled = !isSending
            binding.sendButton.isEnabled = !isSending
        }

        viewModel.postEvent.observe(this) { event ->
            when (event) {
                PostEvent.Success -> {
                    setResult(Activity.RESULT_OK)
                    viewModel.onEventHandled()
                    finish()
                }

                is PostEvent.Error -> {
                    Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                    viewModel.onEventHandled()
                }

                null -> Unit
            }
        }
    }

    private fun submitMessage() {
        val username = binding.usernameEditText.text?.toString().orEmpty()
        val message = binding.messageEditText.text?.toString().orEmpty()

        binding.usernameInputLayout.error = null
        binding.messageInputLayout.error = null

        var hasError = false
        if (username.isBlank()) {
            binding.usernameInputLayout.error = getString(R.string.field_required)
            hasError = true
        }

        if (message.isBlank()) {
            binding.messageInputLayout.error = getString(R.string.field_required)
            hasError = true
        }

        if (!hasError) {
            viewModel.send(username, message)
        }
    }
}

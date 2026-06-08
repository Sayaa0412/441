package com.example.vibecodechatter.data.model

data class ChattMessage(
    val username: String,
    val message: String,
    val rawTimestamp: String,
    val displayTimestamp: String = rawTimestamp.toDisplayTimestamp()
)

private fun String.toDisplayTimestamp(): String {
    val normalized = trim()
    if (normalized.isEmpty()) {
        return ""
    }

    return normalized
        .replace('T', ' ')
        .substringBeforeLast(".", normalized)
}


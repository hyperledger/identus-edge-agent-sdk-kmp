package org.hyperledger.identus.walletsdk.db

data class MessageReadStatus(
    val messageId: String,
    val isRead: Boolean
)

package org.hyperledger.identus.walletsdk.sampleapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message(
    @PrimaryKey
    val messageId: String,
    val isRead: Boolean
)

package io.iohk.atala.prism.sampleapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessage(message: Message)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateMessage(message: Message)

    @Query("SELECT isRead FROM message WHERE messageId = :messageId")
    fun isMessageRead(messageId: String): Boolean

    @Query("SELECT messageId, isRead FROM message WHERE messageId IN (:messageIds)")
    fun areMessagesRead(messageIds: List<String>): List<MessageReadStatus>
}

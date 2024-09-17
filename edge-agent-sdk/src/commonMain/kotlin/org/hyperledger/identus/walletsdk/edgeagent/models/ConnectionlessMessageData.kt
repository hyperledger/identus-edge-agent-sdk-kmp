package org.hyperledger.identus.walletsdk.edgeagent.models

import org.hyperledger.identus.walletsdk.domain.models.AttachmentDescriptor

data class ConnectionlessMessageData(
    val messageId: String,
    val messageBody: String,
    val attachmentDescriptor: AttachmentDescriptor,
    val messageThid: String,
    val messageFrom: String
)

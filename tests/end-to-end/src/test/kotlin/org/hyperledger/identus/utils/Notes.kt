package org.hyperledger.identus.utils

import io.iohk.atala.automation.utils.Logger
import java.io.File

/**
 * Writes content to 'notes' file.
 *
 * This used to add to GitHub summary report.
 */
object Notes {
    private val logger = Logger.get<Notes>()

    /**
     * Overwrites any content in Notes file with header
     */
    fun prepareNotes() {
        val file = File("notes")
        file.writeText("### End-to-end notes:\n\n")
    }

    /**
     * Adds a new message to notes file if CI environment is set.
     * @param message new message line
     */
    fun appendMessage(message: String) {
        if(System.getenv().containsKey("CI")) {
            logger.info("Adding to notes: $message")
            val file = File("notes")
            file.appendText(message + "\n", Charsets.UTF_8)
        } else {
            logger.info("Log: $message")
        }
    }
}

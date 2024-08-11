package org.hyperledger.identus.walletsdk.pluto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.hyperledger.identus.apollo.base64.base64UrlEncoded
import org.hyperledger.identus.walletsdk.domain.buildingblocks.Pluto
import org.hyperledger.identus.walletsdk.domain.models.UnknownError
import org.hyperledger.identus.walletsdk.pluto.models.backup.BackupV0_0_1
import org.hyperledger.identus.walletsdk.pollux.models.AnonCredential

/**
 * PlutoBackupTask class is responsible for running the backup task for the Pluto instance.
 *
 * @property pluto The Pluto instance to perform backup on.
 */
open class PlutoBackupTask(private val pluto: Pluto) {

    /**
     * Executes the backup process and returns a Flow of BackupV0_0_1 objects.
     *
     * @return a Flow of BackupV0_0_1 objects
     */
    suspend fun run(): Flow<BackupV0_0_1> { // Assuming PlutoBackup is already defined in your domain model
        return flow {
            val backup = withContext(Dispatchers.IO) {
                val linkSecret = async { getLinkSecretBackup() }
                val dids = async { getDids() }
                val didPairs = async { getDidPairBackups() }
                val privateKeys = async { getKeyBackups() }
                val credentials = async { getCredentialBackups() }
                val messages = async { getMessageBackups() }
                val mediator = async { getMediatorBackups() }
                BackupV0_0_1(
                    credentials = credentials.await(),
                    dids = dids.await(),
                    didPairs = didPairs.await(),
                    keys = privateKeys.await(),
                    linkSecret = linkSecret.await(),
                    messages = messages.await(),
                    mediators = mediator.await()
                )
            }
            emit(backup)
        }
    }

    /**
     * Retrieves a list of DIDs from the pluto object by calling getAllDIDs. The first DID in the list will be taken and
     * mapped to a BackupV0_0_1.DID object which contains the concatenated string of schema, method, and methodId separated
     * by ":" and the alias. Returns the list of mapped DIDs or an empty list if no DIDs are found.
     *
     * @return The list of mapped DIDs or an empty list if no DIDs are found.
     */
    private suspend fun getDids(): List<BackupV0_0_1.DID> {
        pluto.getAllDIDs().firstOrNull()?.let { listDid ->
            return listDid.map { did ->
                BackupV0_0_1.DID(did = "${did.schema}:${did.method}:${did.methodId}", alias = did.alias)
            }
        }
        return emptyList()
    }

    /**
     * Retrieves a list of credential backups.
     *
     * @return a list of [BackupV0_0_1.Credential] objects representing the backups.
     */
    private suspend fun getCredentialBackups(): List<BackupV0_0_1.Credential> {
        return pluto.getAllCredentials().first().map { credentialRecovery ->
            val backUpRestorationId = when (credentialRecovery.restorationId) {
                RestorationID.JWT.value -> {
                    PlutoRestoreTask.BackUpRestorationId.JWT
                }

                RestorationID.ANONCRED.value -> {
                    PlutoRestoreTask.BackUpRestorationId.ANONCRED
                }

                RestorationID.W3C.value -> {
                    PlutoRestoreTask.BackUpRestorationId.W3C
                }

                else -> {
                    throw UnknownError.SomethingWentWrongError("Unknown restoration ID ${credentialRecovery.restorationId}")
                }
            }

            if (backUpRestorationId == PlutoRestoreTask.BackUpRestorationId.ANONCRED) {
                val anonCredential = AnonCredential.fromStorableData(credentialRecovery.credentialData)

                BackupV0_0_1.Credential(
                    recoveryId = backUpRestorationId.value,
                    data = Json.encodeToString(anonCredential.toAnonCredentialBackUp()).base64UrlEncoded
                )
            } else {
                BackupV0_0_1.Credential(
                    recoveryId = backUpRestorationId.value,
                    data = credentialRecovery.credentialData.base64UrlEncoded
                )
            }
        }
    }

    /**
     * Retrieves a list of backup DID pairs from the Pluto service.
     *
     * @return a list of BackupV0_0_1.DIDPair objects representing the backup DID pairs.
     */
    private suspend fun getDidPairBackups(): List<BackupV0_0_1.DIDPair> {
        val pairs = pluto.getAllDidPairs().first()
        return pairs.map { didPair ->
            BackupV0_0_1.DIDPair(
                holder = didPair.holder.toString(),
                recipient = didPair.receiver.toString(),
                alias = didPair.name ?: ""
            )
        }
    }

    /**
     * Retrieves a list of key backups.
     *
     * @return a list of key backups.
     */
    private suspend fun getKeyBackups(): List<BackupV0_0_1.Key> {
        return pluto.getAllKeysForBackUp().first().map { key ->
            BackupV0_0_1.Key(
                key = key.key.base64UrlEncoded,
                did = key.did,
                index = key.index,
                recoveryId = key.recoveryId
            )
        }
    }

    /**
     * Retrieves the backup link secret from the Pluto service.
     * If the backup link secret exists, it will be returned as a String.
     * If no backup link secret exists, null will be returned.
     *
     * @return The backup link secret, or null if it does not exist.
     */
    private suspend fun getLinkSecretBackup(): String? {
        return pluto.getLinkSecret().firstOrNull()
    }

    /**
     * Retrieves the backups of all messages in Pluto.
     *
     * @return A list of base64-encoded backups of all messages.
     */
    private suspend fun getMessageBackups(): List<String> {
        val messages = pluto.getAllMessages().first()
        return messages.map { message ->
            Json.encodeToString(message.toBackUpMessage()).base64UrlEncoded
        }
    }

    /**
     * Retrieves a list of mediator backups.
     *
     * @return a list of [BackupV0_0_1.Mediator] objects representing the mediator backups.
     */
    private suspend fun getMediatorBackups(): List<BackupV0_0_1.Mediator> {
        return pluto.getAllMediators().first().map { mediator ->
            BackupV0_0_1.Mediator(
                mediatorDid = mediator.mediatorDID.toString(),
                holderDid = mediator.hostDID.toString(),
                routingDid = mediator.routingDID.toString()
            )
        }
    }
}

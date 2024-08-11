package org.hyperledger.identus.walletsdk.pluto

import org.hyperledger.identus.walletsdk.pluto.models.backup.BackupV0_0_1
import kotlin.reflect.KClass

/**
 * Represents the backup version.
 *
 * The backup version is an enumeration that provides different backup versions
 * along with corresponding backup classes. It also contains methods for working
 * with backup versions.
 *
 */
enum class BackupVersion(val backupClass: KClass<*>) {
    V0_0_1(BackupV0_0_1::class);

    /**
     * Returns the appropriate backup data model based on the current version of the application.
     *
     * @return The backup data model class for the current application version.
     */
    fun getBackUpDataModel(): KClass<*> {
        return when (this) {
            V0_0_1 -> BackupV0_0_1::class
        }
    }

    /**
     * Returns the semantic version of a given input string.
     *
     * The input string should represent a version number in the format "VX_Y_Z",
     * where X, Y, and Z are integers and '_' is used as the separator.
     *
     * @return the semantic version as a string.
     */
    fun toSemanticVersion(): String {
        return this.name.replace("_", ".").replace("V", "")
    }

    companion object {
        /**
         * Converts a semantic version string to a BackupVersion enum value.
         *
         * @param version the semantic version string to convert. The version must be in the format X.Y.Z where X, Y, and Z are integers.
         * @return the BackupVersion enum value corresponding to the given semantic version string.
         * @throws Exception if the given version string is not a valid semantic version.
         */
        @JvmStatic
        fun fromSemanticVersion(version: String): BackupVersion {
            return try {
                BackupVersion.valueOf("V${version.replace(".", "_")}")
            } catch (ex: Exception) {
                throw Exception("Invalid semantic version: $version")
            }
        }
    }
}

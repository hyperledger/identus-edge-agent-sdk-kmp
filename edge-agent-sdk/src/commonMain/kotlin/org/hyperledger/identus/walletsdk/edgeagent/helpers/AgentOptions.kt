package org.hyperledger.identus.walletsdk.edgeagent.helpers

/**
 * Class that represent agent options that the SDK user can define to modify some behaviors of the SDK.
 * @param experiments Represents the experimental features available
 */
data class AgentOptions(val experiments: Experiments = Experiments())

/**
 * Class to define experimental features available within the SDK.
 * @param liveMode Flag to enable or disable the live mode to listen for messages from the mediator.
 */
data class Experiments(val liveMode: Boolean = false)

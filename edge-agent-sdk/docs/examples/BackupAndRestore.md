# Back up and restore
## Requirements
1. A holder with existing data to back up.
2. A fresh instance with an empty database of the app using Edge agent SDK to restore
3. Create the edge agent instance but do not start it to keep the database empty
4. After the restore is completed, start the agent using agent.start()

## Flow
1. SDK A creates a back up `JWE`
2. SDK B creates the edge agent instance
3. SDK B restores from the `JWE` generated at back up
4. SDK B starts the agent

> NOTE: It is required to instantiate the EdgeAgent but not started as the start method will establish mediation, adding data to
> the database and making the restoration process unavailable. It needs to be an empty db. But we need the agent to be instantiated
> to have the seed available for the private key creation to use on the jwe decryption process. 

## Code reference

* Generate the `JWE` using the Edge agent function provided

Example
```kotlin
val jwe = agent.backupWallet()
```

* Restore the generated `JWE` into a fresh instance of the app using Edge agent SDK
* Start the agent after restoration is completed

Example
```kotlin
handler = createHandler(mediatorDID)
agent = createAgent(handler)
(pluto as PlutoImpl).start(context)

val jwe = "JWE_STRING"
agent.recoverWallet(jwe)

agent.start()
```
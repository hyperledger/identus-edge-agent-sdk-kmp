# End-to-end tests

## Setting up the environment variables

Duplicate `local.properties.example` file from `test/resources` and rename the copy to `local.properties`

Setup properties:

| Property                 | Explanation                        |
|--------------------------|------------------------------------|
| MEDIATOR_OOB_URL         | Mediator OOB url invitation        |
| AGENT_URL                | Agent url                          |
| PUBLISHED_DID            | Existing published DID             |
| JWT_SCHEMA_GUID          | Existing JWT schema guid           |
| ANONCRED_DEFINITION_GUID | Existing Anoncred definition guid  |
| APIKEY                   | APIKEY header token authentication |

## Running the end-to-end tests

### Building the SDK

In the command line navigate to the SDK directory 

1. Set the `ATALA_GITHUB_ACTOR` environment variable with your GitHub email
2. Set `ATALA_GITHUB_TOKEN` environment variable with your GitHub token 
3. Comment out the `signing` part of the `build.gradle.kts`
4. Change the version in `gradle.properties` to something else
5. Run the following command

```bash
./gradlew publishToMavenLocal
```

### Update the SDK dependency in e2e test

Now, in the `build.gradle.kts` file inside the `e2e tests` directory you'll have to update
the version of the sdk with  the new one you published to your maven local

E.g.
```kotlin
testImplementation("org.hyperledger.identus:edge-agent-sdk:1.2.3-MY-CHANGE")
```

### Running the tests

Full regression

```bash
./gradlew test --tests "org.hyperledger.identus.walletsdk.TestSuite"
```

Tagged scenario

```bash
./gradlew test --tests "org.hyperledger.identus.walletsdk.TestSuite" -Dcucumber.filter.tags="@mytag and @anothertag"
```

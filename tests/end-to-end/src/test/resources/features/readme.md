# Wallet SDK KMM E2E

End-to-end tests

- [Repository](https://github.com/input-output-hk/atala-prism-wallet-sdk-kmm)
- [Documentation](https://input-output-hk.github.io/atala-prism-wallet-sdk-kmm/)

## Environment variables

To define the environment to be tested we can set the `env` environment variable.

The default values for each environment are defined in `resources/environment` folder.

### Possible values

| env   | Description                                                                                               |
|-------|-----------------------------------------------------------------------------------------------------------|
| local | Local tests ran with `local-prism`                                                                        |
| dev   | Development environment. Some of the variables are not set since the database can be constantly wiped     | 
| sit   | Integration environment. Most of the variables should be set since it should be a more stable environment |

### Overriding environment variables

| Attribute      | Description                                                                                   | Environment default |
|----------------|-----------------------------------------------------------------------------------------------|---------------------|
| mediatorOobUrl | OOB invitation for mediator                                                                   | local; dev; sit     |
| agentUrl       | Prism-agent url                                                                               | local; dev; sit     |
| publicatedDid  | Did published. If the variable is not provided the automation will create a new published DID | sit                 |

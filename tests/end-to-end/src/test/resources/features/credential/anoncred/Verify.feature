@anoncreds @verification
Feature: Verify Anoncreds presentation
  The Edge Agent should be able to receive a verifiable credential from Cloud Agent and then send a presentation to another edge agent who will verify it

  @disabled
  Scenario: SDKs Anoncreds Verification
    Given Cloud Agent is connected to Edge Agent
    And Edge Agent has '1' anonymous credentials issued by Cloud Agent
    When Verifier Edge Agent request Edge Agent to verify the anonymous credential
    When Edge Agent sends the verification proof
    Then Verifier Edge Agent should see the verification proof is verified

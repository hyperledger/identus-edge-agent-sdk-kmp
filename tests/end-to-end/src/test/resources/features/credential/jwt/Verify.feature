@jwt @verification
Feature: Verify JWT presentation
  The Edge Agent should be able to receive a verifiable credential from Cloud Agent and then send a presentation to another edge agent who will verify it

  Scenario: Verify valid jwt credential
    Given Cloud Agent is connected to Edge Agent
    And Edge Agent has '1' jwt credentials issued by Cloud Agent
    When Verifier Edge Agent request Edge Agent to verify the JWT credential
    And Edge Agent sends the verification proof
    Then Verifier Edge Agent should see the verification proof is verified

  Scenario: Verify revoked jwt credential
    Given Cloud Agent is connected to Edge Agent
    And Edge Agent has '1' jwt credentials issued by Cloud Agent
    When Cloud Agent revokes '1' credentials
    Then Verifier Edge Agent request Edge Agent to verify the JWT credential
    When Edge Agent sends the verification proof
    Then Verifier Edge Agent should see the verification proof was not verified due revocation

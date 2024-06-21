@proof @jwt
Feature: Respond to request JWT proof
  The Edge Agent should be able to respond to a JWT request-of-proof

  Scenario: Respond to request proof
    Given Cloud Agent is connected to Edge Agent
    And Edge Agent has '1' jwt credentials issued by Cloud Agent
    When Cloud Agent asks for present-proof
    And Edge Agent sends the present-proof
    Then Cloud Agent should see the present-proof is verified

@proof @anoncred
Feature: Respond to anoncred request proof
  The Edge Agent should be able to respond to an anoncred proof-of-request

  Scenario: Respond to anoncred request proof
    Given Cloud Agent is connected to Edge Agent
    And Edge Agent has '1' anonymous credentials issued by Cloud Agent
    When Cloud Agent asks for present-proof for anoncred
    And Edge Agent sends the present-proof
    Then Cloud Agent should see the present-proof is verified

  @disabled
  Scenario: Respond to a present request with a wrong credential
    Given Cloud Agent is connected to Edge Agent
    And Edge Agent has '1' anonymous credentials issued by Cloud Agent
    When Cloud Agent asks for present-proof for anoncred with unexpected attributes
    And Edge Agent sends the present-proof
    Then Cloud Agent should see the present-proof is not verified

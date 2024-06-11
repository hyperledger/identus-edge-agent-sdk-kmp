@credential @jwt @blap
Feature: Receive verifiable credential
  The Edge Agent should be able to receive a verifiable credential from Cloud Agent

  Scenario: Receive one verifiable credential
    Given Cloud Agent is connected to Edge Agent
    When Cloud Agent offers a credential
    Then Edge Agent should receive the credential
    When Edge Agent accepts the credential
    And Cloud Agent should see the credential was accepted
    Then Edge Agent wait to receive 1 issued credentials
    And Edge Agent process 1 issued credentials
    And Edge Agent should have 1 credentials
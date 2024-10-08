package org.hyperledger.identus.walletsdk.configuration

import io.iohk.atala.automation.utils.Wait
import org.hyperledger.identus.walletsdk.utils.Notes
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.response.Response
import net.serenitybdd.rest.SerenityRest
import org.apache.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.hyperledger.identus.client.models.CreateManagedDidRequest
import org.hyperledger.identus.client.models.CreateManagedDidRequestDocumentTemplate
import org.hyperledger.identus.client.models.CredentialDefinitionInput
import org.hyperledger.identus.client.models.CredentialDefinitionResponse
import org.hyperledger.identus.client.models.CredentialSchemaInput
import org.hyperledger.identus.client.models.ManagedDIDKeyTemplate
import org.hyperledger.identus.client.models.Purpose
import org.hyperledger.identus.walletsdk.models.AnoncredSchema
import org.hyperledger.identus.walletsdk.models.JwtSchema
import org.hyperledger.identus.walletsdk.models.JwtSchemaProperty
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.seconds

object Environment {
    lateinit var agentUrl: String
    lateinit var mediatorOobUrl: String
    lateinit var publishedDid: String
    lateinit var jwtSchemaGuid: String
    lateinit var anoncredDefinitionId: String

    /**
     * Set up the variables based on the properties config file
     */
    fun setup() {
        // prepare notes
        Notes.prepareNotes()

        // configure environment variables
        val properties = Properties()

        val localProperties = this::class.java.classLoader.getResourceAsStream("local.properties")
        if (localProperties != null) {
            properties.load(localProperties)
        }

        // override default configuration with any env variable
        properties.putAll(System.getenv())

        mediatorOobUrl = properties.getProperty("MEDIATOR_OOB_URL")
        agentUrl = properties.getProperty("AGENT_URL")

        // set base uri for rest assured
        RestAssured.baseURI = agentUrl

        // configure rest assured
        if (properties.containsKey("APIKEY")) {
            val requestSpecification = RequestSpecBuilder()
                .addHeader("APIKEY", properties.getProperty("APIKEY"))
                .build()
            SerenityRest.setDefaultRequestSpecification(requestSpecification)
        }

        // check if DID and schema exist
        preparePublishedDid(properties.getProperty("PUBLISHED_DID"))
        checkJwtSchema(properties.getProperty("JWT_SCHEMA_GUID"))
        checkAnonCredSchema(properties.getProperty("ANONCRED_DEFINITION_GUID"))

        Notes.appendMessage("Mediator: $mediatorOobUrl")
        Notes.appendMessage("Agent: $agentUrl")
        Notes.appendMessage("DID: $publishedDid")
        Notes.appendMessage("JWT Schema: $jwtSchemaGuid")
        Notes.appendMessage("Anoncred Definition: $anoncredDefinitionId")
        Notes.appendMessage("SDK Version: ${getSdkVersion()}")
    }

    private fun getSdkVersion(): String {
        val file = File("build.gradle.kts")
        val input = file.readText()
        val regex = Regex("edge-agent-sdk:(.*)(?=\")")
        return regex.find(input)!!.groups[1]!!.value
    }

    /**
     * Checks if the environment PUBLISHED_DID variable exists in cloud-agent, otherwise it creates a new one.
     */
    private fun preparePublishedDid(publishedDid: String?) {
        try {
            assertThat(publishedDid).isNotEmpty()
            RestAssured
                .given().get("did-registrar/dids/$publishedDid")
                .then().assertThat().statusCode(200)
            Environment.publishedDid = publishedDid!!
            return
        } catch (e: AssertionError) {
            Notes.appendMessage("DID [${publishedDid}] not found. Creating a new one.")
        }

        val publicKey = ManagedDIDKeyTemplate(
            id = "key-1",
            purpose = Purpose.ASSERTION_METHOD
        )

        val documentTemplate = CreateManagedDidRequestDocumentTemplate(
            publicKeys = listOf(publicKey),
            services = emptyList()
        )

        val creationData = CreateManagedDidRequest(
            documentTemplate = documentTemplate
        )

        val creationResponse = RestAssured
            .given().body(creationData).post("/did-registrar/dids")
            .thenReturn()
        val longFormDid = creationResponse.body.jsonPath().getString("longFormDid")

        val publicationResponse = RestAssured
            .given().post("/did-registrar/dids/$longFormDid/publications")
            .thenReturn()

        assertThat(publicationResponse.statusCode).isEqualTo(HttpStatus.SC_ACCEPTED)
        val shortFormDid = publicationResponse.body.jsonPath().getString("scheduledOperation.didRef")

        lateinit var response: Response
        Wait.until(60.seconds, 1.seconds) {
            response = RestAssured.given()
                .get("$agentUrl/did-registrar/dids/${shortFormDid}")
                .thenReturn()
            response.body.jsonPath().getString("status") == "PUBLISHED"
        }
        Environment.publishedDid = response.body.jsonPath().getString("did")
    }

    /**
     * Checks if the environment variable exists in cloud-agent, otherwise it creates a new one.
     */
    private fun checkJwtSchema(schemaGuid: String?) {
        try {
            assertThat(schemaGuid).isNotEmpty()
            RestAssured
                .given().get("schema-registry/schemas/$schemaGuid")
                .then().assertThat().statusCode(200)
            jwtSchemaGuid = schemaGuid!!
            return
        } catch (e: AssertionError) {
            Notes.appendMessage("JWT schema [${schemaGuid}] not found. Creating a new one.")
        }

        val schemaName = "automation-jwt-schema-" + UUID.randomUUID()

        val jwtSchema = JwtSchema()
        jwtSchema.id = "https://example.com/$schemaName"
        jwtSchema.schema = "https://json-schema.org/draft/2020-12/schema"
        jwtSchema.description = "Automation schema description"
        jwtSchema.type = "object"

        jwtSchema.properties["automation-required"] = JwtSchemaProperty("string")
        jwtSchema.properties["automation-optional"] = JwtSchemaProperty("string")

        val credentialSchemaInput = CredentialSchemaInput(
            author = publishedDid,
            description = "Some description to automation generated schema",
            name = schemaName,
            tags = listOf("automation"),
            type = "https://w3c-ccg.github.io/vc-json-schemas/schema/2.0/schema.json",
            version = "0.0.1",
            schema = jwtSchema
        )

        val schemaCreationResponse = RestAssured.given()
            .body(credentialSchemaInput)
            .post("/schema-registry/schemas")
            .thenReturn()

        jwtSchemaGuid = schemaCreationResponse.body.jsonPath().getString("guid")
    }

    private fun checkAnonCredSchema(definitionId: String?) {
        try {
            assertThat(definitionId).isNotEmpty()
            RestAssured
                .given().get("credential-definition-registry/definitions/${definitionId}")
                .then().assertThat().statusCode(200)

            anoncredDefinitionId = definitionId!!
            return
        } catch (e: AssertionError) {
            Notes.appendMessage("Anoncred Definition not found for [${definitionId}]. Creating a new one.")
        }

        val schemaName = "automation-anoncred-schema-" + UUID.randomUUID()

        val anoncredSchema = AnoncredSchema()
        anoncredSchema.name = "Automation Anoncred"
        anoncredSchema.version = "1.0"
        anoncredSchema.issuerId = publishedDid
        anoncredSchema.attrNames = mutableListOf("name", "age", "gender")

        val credentialSchemaInput = CredentialSchemaInput(
            author = publishedDid,
            description = "Some description to automation generated schema",
            name = schemaName,
            tags = listOf("automation"),
            type = "AnoncredSchemaV1",
            version = "2.0.0",
            schema = anoncredSchema
        )

        val schemaCreationResponse = RestAssured.given()
            .body(credentialSchemaInput)
            .post("/schema-registry/schemas")
            .thenReturn()

        assertThat(schemaCreationResponse.statusCode).isEqualTo(HttpStatus.SC_CREATED)

        val newSchemaGuid = schemaCreationResponse.body.jsonPath().getString("guid")

        val definitionName = "automation-anoncred-definition-" + UUID.randomUUID()
        val definition = CredentialDefinitionInput(
            name = definitionName,
            version = "1.0.0",
            tag = "automation-test",
            author = publishedDid,
            schemaId = "$agentUrl/schema-registry/schemas/${newSchemaGuid}/schema",
            signatureType = "CL",
            supportRevocation = false,
            description = "Test Automation Auto-Generated"
        )

        val credentialDefinition = RestAssured
            .given().body(definition).post("/credential-definition-registry/definitions")
            .then().assertThat().statusCode(201)
            .extract().`as`(CredentialDefinitionResponse::class.java)

        anoncredDefinitionId = credentialDefinition.guid.toString()
    }
}

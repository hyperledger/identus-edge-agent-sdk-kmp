package io.iohk.atala.prism.configuration

import io.iohk.atala.automation.utils.Logger
import io.iohk.atala.automation.utils.Wait
import io.iohk.atala.prism.models.*
import io.iohk.atala.prism.utils.Notes
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.response.Response
import net.serenitybdd.rest.SerenityRest
import org.apache.http.HttpStatus
import org.assertj.core.api.Assertions
import java.util.*
import kotlin.time.Duration.Companion.seconds

object Environment {
    private val logger = Logger.get<Environment>()

    val agentUrl: String
    val mediatorOobUrl: String
    lateinit var publishedDid: String
    lateinit var schemaId: String

    /**
     * Set up the variables based on the properties config file
     */
    init {
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
        agentUrl = properties.getProperty("PRISM_AGENT_URL")

        // configure rest assured
        if (properties.containsKey("APIKEY")) {
            val requestSpecification = RequestSpecBuilder()
                .addHeader("APIKEY", properties.getProperty("APIKEY"))
                .build()
            SerenityRest.setDefaultRequestSpecification(requestSpecification)
        }

        // check if DID and schema exist
        preparePublishedDid(properties.getProperty("PUBLISHED_DID"))
        checkSchema(properties.getProperty("SCHEMA_ID"))

        Notes.appendMessage("Mediator: $mediatorOobUrl")
        Notes.appendMessage("Agent: $agentUrl")
        Notes.appendMessage("DID: $publishedDid")
        Notes.appendMessage("Schema: $schemaId")
    }

    /**
     * Checks if the environment PUBLISHED_DID variable exists in prism-agent, otherwise it creates a new one.
     */
    private fun preparePublishedDid(publishedDid: String?) {
        try {
            val response = RestAssured
                .given()
                .get("${agentUrl}/did-registrar/dids/$publishedDid")
                .then()
                .assertThat()
                .statusCode(200)
            this.publishedDid = publishedDid!!
            return
        } catch (e: AssertionError) {
            logger.warn("DID not found. Creating a new one.")
        }

        val publicKey = ManagedDIDKeyTemplate(
            id = "key-1",
            purpose = Purpose.assertionMethod
        )

        val documentTemplate = CreateManagedDidRequestDocumentTemplate(
            publicKeys = listOf(publicKey),
            services = emptyList()
        )

        val creationData = CreateManagedDidRequest(
            documentTemplate = documentTemplate
        )

        val creationResponse = RestAssured
            .given().body(creationData)
            .post("${agentUrl}/did-registrar/dids")
            .thenReturn()
        val longFormDid = creationResponse.body.jsonPath().getString("longFormDid")

        val publicationResponse = RestAssured
            .given()
            .post("${agentUrl}/did-registrar/dids/$longFormDid/publications")
            .thenReturn()

        Assertions.assertThat(publicationResponse.statusCode).isEqualTo(HttpStatus.SC_ACCEPTED)
        val shortFormDid = publicationResponse.body.jsonPath().getString("scheduledOperation.didRef")

        lateinit var response: Response
        Wait.until(60.seconds, 1.seconds) {
            response = RestAssured.given()
                .get("$agentUrl/did-registrar/dids/${shortFormDid}")
                .thenReturn()
            response.body.jsonPath().getString("status") == "PUBLISHED"
        }
        this.publishedDid = response.body.jsonPath().getString("did")

        Notes.appendMessage("Created new DID: ${this.publishedDid}")
    }

    /**
     * Checks if the environment SCHEMA_ID variable exists in prism-agent, otherwise it creates a new one.
     */
    fun checkSchema(schemaId: String?) {
        try {
            RestAssured
                .given()
                .get("${agentUrl}/schema-registry/schemas/$schemaId")
                .then()
                .assertThat()
                .statusCode(200)
            this.schemaId = schemaId!!
            return
        } catch (e: AssertionError) {
            logger.warn("Schema not found. Creating a new one.")
        }

        val schemaName = "automation-schema-" + UUID.randomUUID()

        val schema = Schema()
        schema.id = "https://example.com/$schemaName"
        schema.schema = "https://json-schema.org/draft/2020-12/schema"
        schema.description = "Automation schema description"
        schema.type = "object"

        schema.properties["automation-required"] = SchemaProperty("string")
        schema.properties["automation-optional"] = SchemaProperty("string")

        val credentialSchemaInput = CredentialSchemaInput(
            author = this.publishedDid,
            description = "Some description to automation generated schema",
            name = schemaName,
            tags = listOf("automation"),
            type = "https://w3c-ccg.github.io/vc-json-schemas/schema/2.0/schema.json",
            version = "0.0.1",
            schema = schema
        )

        val schemaCreationResponse = RestAssured.given()
            .body(credentialSchemaInput)
            .post("${agentUrl}/schema-registry/schemas")
            .thenReturn()

        this.schemaId = schemaCreationResponse.body.jsonPath().getString("guid")
        Notes.appendMessage("Created new schema: ${this.schemaId}")
    }

}

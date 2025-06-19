/*
 * Copyright (c) 2025 Atos Spain S.A. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress(
    "NoWildcardImports",
    "TooManyFunctions",
    "UnusedPrivateProperty",
    "NestedBlockDepth",
    "MaximumLineLength"
)

package com.eviden.bds.rd.uself.agent.services.openid.conf

import com.eviden.bds.rd.uself.common.models.CREDENTIAL_TYPES
import com.eviden.bds.rd.uself.common.models.HOLDER_DID
import com.eviden.bds.rd.uself.common.models.ISSUER_TIR
import com.eviden.bds.rd.uself.common.models.KEY_ALGORITHM
import com.eviden.bds.rd.uself.common.models.openid.auth.*
import com.eviden.bds.rd.uself.common.models.openid.issuer.Display
import com.eviden.bds.rd.uself.common.models.openid.issuer.Logo
import com.eviden.bds.rd.uself.common.models.openid.issuer.OpenIdCredentialIssuer
import com.eviden.bds.rd.uself.common.services.credentialPattern.CredentialPatternFactory
import com.eviden.bds.rd.uself.common.services.crypto.CryptoService
import com.eviden.bds.rd.uself.common.services.did.DIDService
import com.eviden.bds.rd.uself.common.services.did.method.DIDMethodType
import com.eviden.bds.rd.uself.common.services.tm.ebsi.TMEBSI
import com.eviden.bds.rd.uself.common.services.tm.ebsi.tirRegistry.TIRRegistryService
import com.eviden.bds.rd.uself.common.services.tm.trerepository.TRERepository
import com.nimbusds.jose.jwk.JWK
import org.koin.core.component.KoinComponent
import org.koin.java.KoinJavaComponent.inject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class OpenIdConfImp : OpenIdConf, KoinComponent {

    private val didService: DIDService by inject(DIDService::class.java)
    private val cryptoService: CryptoService by inject(CryptoService::class.java)

    private val tirRegistryService: TIRRegistryService by inject(TIRRegistryService::class.java)
    private val treRepository: TRERepository by inject(TRERepository::class.java)
    private val tmEbsi: TMEBSI by inject(TMEBSI::class.java)

    @Value("\${uself.server}")
    private lateinit var server: String

    @Value("\${uself.did.method}")
    private lateinit var didMethod: String

    @Value("\${uself.tir.id}")
    private lateinit var tirID: String

    @Value("\${uself.tir.sigkey}")
    private lateinit var tirSigKey: String

    @Value("\${uself.tir.ethkey}")
    private lateinit var tirEthKey: String

    companion object {
        var did: String? = null
    }

    override fun issuerDID(): String {
        //  cryptoService.store(HOLDER_DID.did, JWK.parse(HOLDER_DID.key))
        // cryptoService.store("006a1a542c9d467393ce2ef8a3fbf8f9", JWK.parse(HOLDER_DID.key2))
        if (did.isNullOrEmpty()) {
            if (didService.getAll().isEmpty()) {
                did = when (didMethod) {
                    "DID_EBSI_LEGAL_ENTITY_PARAM" -> {
                        println("Init DID_EBSI_LEGAL_ENTITY_PARAM")
                        initKMS()
                        tirID
                    }
                    "DID_EBSI_LEGAL_ENTITY_PRELOADED" -> {
                        val sigKey = JWK.parse(ISSUER_TIR.sigKey)
                        cryptoService.store(sigKey.keyID, sigKey)
                        val ethKey = JWK.parse(ISSUER_TIR.ethKey)
                        cryptoService.store(ethKey.keyID, ethKey)
                        cryptoService.storeAsDID(sigKey.keyID, ISSUER_TIR.did)
                        didService.resolveDID(ISSUER_TIR.did)
                        val tre = tirRegistryService.getIssuer(ISSUER_TIR.did)
                        tre.proxies = tirRegistryService.getIssuerProxy(ISSUER_TIR.did)
                        treRepository.insert(ISSUER_TIR.did, tre)
                        ISSUER_TIR.did
                    }
                    "DID_EBSI_LEGAL_ENTITY" -> {
                        didService.generateDIDDoc(DIDMethodType.DID_EBSI_LEGAL_ENTITY).id.toString()
                    }
                    "DID_EBSI_NATURAL_PERSON" -> didService.generateDIDDoc(DIDMethodType.DID_EBSI_NATURAL_PERSON).id.toString()
                    "DID_JWK" -> didService.generateDIDDoc(DIDMethodType.DID_JWK).id.toString()
                    "DID_WEB" -> didService.generateDIDDoc(DIDMethodType.DID_WEB).id.toString()
                    "DID_KEY_P256" -> didService.generateDIDDoc(DIDMethodType.DID_KEY_P256).id.toString()
                    "DID_KEY_ED25519" -> didService.generateDIDDoc(DIDMethodType.DID_KEY_ED25519).id.toString()
                    else -> {
                        HOLDER_DID.did
                    }
                }
            } else {
                println("Load DID_EBSI_LEGAL_ENTITY_PARAM")
                did = tirID
                initKMS()
                return didService.getAll().first()
            }
        }
        return did!!
    }

    private fun initKMS() {
        println("Initializing System with TIR ID: $tirID")
        if (tirID == ISSUER_TIR.did) {
            tirEthKey = ISSUER_TIR.ethKey
            tirSigKey = ISSUER_TIR.sigKey
        }
        val sigKey = JWK.parse(tirSigKey)
        cryptoService.store(sigKey.keyID, sigKey)
        val ethKey = JWK.parse(tirEthKey)
        cryptoService.store(ethKey.keyID, ethKey)
        cryptoService.storeAsDID(sigKey.keyID, tirID)
        didService.resolveDID(tirID)

        val tre = tirRegistryService.getIssuer(tirID)
        // tre.proxies = tirRegistryService.getIssuerProxy(tirID)

        println("Registering TIR's proxy with ID: $tirID")
        tmEbsi.setProxy(tirID)
        tre.proxies = tirRegistryService.getIssuerProxy(tirID)
        println("Registered TIR's proxy with ID: $tirID")
        treRepository.insert(tirID, tre)
        println("Initialized System with TIR ID: $tirID")
    }

    override fun serverURL(): String {
        return server
    }

    override fun authMetadata(): OpenIdConfiguration {
        return OpenIdConfiguration(
            issuer = "${serverURL()}/auth",
            authorizationEndpoint = "${serverURL()}/auth/authorize",
            tokenEndpoint = "${serverURL()}/auth/token",
            jwksUri = "${serverURL()}/auth/jwks",
            redirectUris = CredentialPatternFactory.getRedirectURIs(), // ∫getRedirectURI(),
            scopesSupported = getScopes(),
            responseTypesSupported = arrayListOf("vp_token", "id_token"),
            responseModesSupported = arrayListOf("query"),
            grantTypesSupported = arrayListOf("authorization_code"),
            subjectTypesSupported = arrayListOf("public"),
            idTokenSigningAlgValuesSupported = arrayListOf(KEY_ALGORITHM.ES256),
            requestObjectSigningAlgValuesSupported = arrayListOf(KEY_ALGORITHM.ES256),
            requestParameterSupported = true,
            requestUriParameterSupported = true,
            tokenEndpointAuthMethodsSupported = arrayListOf("private_key_jwt"),
            requestAuthenticationMethodsSupported = RequestAuthenticationMethods(
                authorizationEndpoint = arrayListOf("request_object")
            ),
            vpFormatsSupported = SupportedVPFormats(
                jwtVp = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
                jwtVpJson = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
                jwtVcJson = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
                jwtVc = Format(alg = arrayListOf(KEY_ALGORITHM.ES256))
            ),
            subjectSyntaxTypesSupported = arrayListOf("did:key", "did:ebsi"),
            subjectSyntaxTypesDiscriminations = arrayListOf("did:key:jwk_jcs-pub", "did:ebsi:v1"),
            subjectTrustFrameworksSupported = arrayListOf("ebsi"),
            idTokenTypesSupported = arrayListOf("subject_signed_id_token", "attester_signed_id_token")
        )
    }

    override fun issuerMetadata(): OpenIdCredentialIssuer {
        // There is a problem with the memory and the object I shouldn't need to initialize this again, to take a look
        issuerDID()
        return OpenIdCredentialIssuer(
            authorizationServer = "${serverURL()}/auth", // to be an array
            credentialIssuer = "${serverURL()}/issuer",
            credentialEndpoint = "${serverURL()}/issuer/credential",
            deferredCredentialEndpoint = "${serverURL()}/issuer/credential_deferred",
            credentialsSupported = CredentialPatternFactory.getSupportedCredentials(), // getListSupportedVC(),
            // credentialConfigurationsSupported = getMapSupportedVC(),
            display = Display(
                name = "uSelf Agent Issuer",
                locale = "en-GB",
                logo = Logo(
                    url = """
                        https://eviden.com/wp-content/themes/evidian/assets/build/images/favicons/favicon-196x196.png
                    """.trim(),
                    altText = "uSelf Agent Issuer Logo"
                )
            )
        )
    }

    /**
     * Retrieves a list of credential IDs excluding specific EBSI test credentials.
     *
     * @return A list of credential IDs with EBSI test credentials removed.
     */
    private fun getScopes(): ArrayList<String> {
        val credIds = CredentialPatternFactory.getCredIds()
        credIds.add("openid")
        return credIds
    }

//    override fun getMapSupportedVC(): MutableMap<String, CredentialSupported> {
//        return supportedVC
//    }

//    override fun getListSupportedVC(): ArrayList<CredentialSupported> {
//        return supportedVC.values.toTypedArray().toCollection(ArrayList())
//    }

//    override fun getSupportedCredential(credentialType: String): CredentialSupported {
//        return supportedVC[credentialType]!!
//    }

//    override fun getRedirectURI(): ArrayList<String> {
//        return redirectURISet.toTypedArray().toCollection(ArrayList())
//    }

    private final val inputDescriptor = InputDescriptors(
        id = "<any id, random or static>",
        format = SupportedVPFormats(
            jwtVc = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
            jwtVp = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
            jwtVcJson = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
            jwtVpJson = Format(alg = arrayListOf(KEY_ALGORITHM.ES256))
        ),
        name = "holder-wallet-qualification",
        purpose = "The holder wallet qualification",
        constraints = Constrains(
            fields = arrayListOf(
                Fields(
                    path = arrayListOf("\$.vc.type"),
                    filter = Filter(
                        type = "array",
                        contains = Contains(const = CREDENTIAL_TYPES.VERIFIABLE_ATTESTATION)
                    )
                )
            )
        )
    )

//    override fun getPresentationDefinition(presentationDefinitionId: String): PresentationDefinition {
//        return supportedVP[presentationDefinitionId]!!
//    }

//    override fun getFormattedPresentationDefinition(presentationDefinitionId: String): String {
//        return Json.encodeToString(getPresentationDefinition(presentationDefinitionId))
//    }

//    override fun init() {
//        initCredentials2()
//        initOpenIdClients()
//    }

//    private fun initOpenIdClients() {
//        // OpenID Clients
//        OpenIDClientFactory.init()
//    }

//    private fun initCredentials() {
//        val suppCredentials = SupportedCredentialFactory.getAll()
//        suppCredentials.forEach { (key, value) ->
//            supportedVC[key] = value.getSupportedCredential()
//            // redirectURISet.add("${serverURL()}${value.redirectURI}")
//        }
//
//        // LIST OF Verifiable PRESENTATIONS for EBSI
//        val presentationDefinition = PresentationDefinition(
//            id = "holder-wallet-qualification-presentation",
//            format = SupportedVPFormats(
//                jwtVp = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
//                jwtVpJson = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
//                jwtVcJson = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
//                jwtVc = Format(alg = arrayListOf(KEY_ALGORITHM.ES256))
//            ),
//            inputDescriptors = arrayListOf(inputDescriptor, inputDescriptor, inputDescriptor)
//        )
//
//        // supportedVP[presentationDefinition.id] = presentationDefinition
//    }
//
//    private fun initCredentials2() {
//        val suppCredentials = CredentialPatternFactory.getAll()
//        suppCredentials.forEach { (key, value) ->
//            supportedVC[key] = value.getCredentialSupported()
//         //   redirectURISet.add("${serverURL()}${value.getRedirectURI()}")
//        }
//
//        // LIST OF Verifiable PRESENTATIONS for EBSI
//        val presentationDefinition = PresentationDefinition(
//            id = "holder-wallet-qualification-presentation",
//            format = SupportedVPFormats(
//                jwtVp = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
//                jwtVpJson = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
//                jwtVcJson = Format(alg = arrayListOf(KEY_ALGORITHM.ES256)),
//                jwtVc = Format(alg = arrayListOf(KEY_ALGORITHM.ES256))
//            ),
//            inputDescriptors = arrayListOf(inputDescriptor, inputDescriptor, inputDescriptor)
//        )
//
//        // supportedVP[presentationDefinition.id] = presentationDefinition
//    }

//    override fun getAuthorizationDetails(credentialType: String): AuthorizationDetails {
//        val credential = CredentialPatternFactory.get(credentialType)
//        return when (credential.format) {
//            FORMAT.JWT_VC -> AuthorizationDetails(
//                type = OPENID_CREDENTIAL,
//                format = FORMAT.JWT_VC,
//                types = credential.types!!
//            )
//
//            FORMAT.SD_JWT -> AuthorizationDetails(
//                type = OPENID_CREDENTIAL,
//                format = FORMAT.SD_JWT,
//                vct = credential.vct
//            )
//
//            else -> error("Format not supported")
//        }
//    }
}

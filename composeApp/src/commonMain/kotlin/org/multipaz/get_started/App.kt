package org.multipaz.get_started

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import multipazgettingstartedsample.composeapp.generated.resources.Res
import multipazgettingstartedsample.composeapp.generated.resources.driving_license_card_art
import org.jetbrains.compose.resources.getDrawableResourceBytes
import org.jetbrains.compose.resources.getSystemResourceEnvironment
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.multipaz.asn1.ASN1Integer
import org.multipaz.compose.prompt.PromptDialogs
import org.multipaz.crypto.Algorithm
import org.multipaz.crypto.Crypto
import org.multipaz.crypto.EcCurve
import org.multipaz.crypto.X500Name
import org.multipaz.crypto.X509CertChain
import org.multipaz.document.Document
import org.multipaz.document.DocumentStore
import org.multipaz.document.buildDocumentStore
import org.multipaz.documenttype.DocumentTypeRepository
import org.multipaz.documenttype.knowntypes.DrivingLicense
import org.multipaz.mdoc.util.MdocUtil
import org.multipaz.prompt.PromptModel
import org.multipaz.securearea.CreateKeySettings
import org.multipaz.securearea.SecureArea
import org.multipaz.securearea.SecureAreaRepository
import org.multipaz.storage.Storage
import org.multipaz.util.Logger
import kotlin.time.Duration.Companion.days

lateinit var storage: Storage
lateinit var secureArea: SecureArea
lateinit var secureAreaRepository: SecureAreaRepository

lateinit var documentTypeRepository: DocumentTypeRepository
lateinit var documentStore: DocumentStore

@Composable
@Preview
fun App(promptModel: PromptModel) {
    MaterialTheme {
        // This ensures all prompts inherit the app's main style
        PromptDialogs(promptModel)

        val coroutineScope = rememberCoroutineScope()

        coroutineScope.launch {
            storage = org.multipaz.util.Platform.getNonBackedUpStorage()
            secureArea = org.multipaz.util.Platform.getSecureArea(storage)
            secureAreaRepository = SecureAreaRepository.Builder()
                .add(secureArea)
                .build()

            documentTypeRepository = DocumentTypeRepository().apply {
                addDocumentType(DrivingLicense.getDocumentType())
            }
            documentStore = buildDocumentStore(
                storage = storage,
                secureAreaRepository = secureAreaRepository
            ) {}

            // Creating a Document
            val document = documentStore.createDocument(
                displayName = "Erika's Driving License",
                typeDisplayName = "Utopia Driving License",
                cardArt = ByteString(
                    getDrawableResourceBytes(
                        getSystemResourceEnvironment(),
                        Res.drawable.driving_license_card_art,
                    )
                ),
            )

            // 1. Prepare Timestamps
            val now = Clock.System.now()
            val signedAt = now
            val validFrom = now
            val validUntil = now + 365.days

            // 2. Generate IACA Certificate
            val iacaKey = Crypto.createEcPrivateKey(EcCurve.P256)
            val iacaCert = MdocUtil.generateIacaCertificate(
                iacaKey = iacaKey,
                subject = X500Name.fromName(name = "CN=Test IACA Key"),
                serial = ASN1Integer.fromRandom(numBits = 128),
                validFrom = validFrom,
                validUntil = validUntil,
                issuerAltNameUrl = "https://issuer.example.com",
                crlUrl = "https://issuer.example.com/crl"
            )

            // 3. Generate Document Signing (DS) Certificate
            val dsKey = Crypto.createEcPrivateKey(EcCurve.P256)
            val dsCert = MdocUtil.generateDsCertificate(
                iacaCert = iacaCert,
                iacaKey = iacaKey,
                dsKey = dsKey.publicKey,
                subject = X500Name.fromName(name = "CN=Test DS Key"),
                serial = ASN1Integer.fromRandom(numBits = 128),
                validFrom = validFrom,
                validUntil = validUntil
            )

            // 4. Create the mDoc Credential
            DrivingLicense.getDocumentType().createMdocCredentialWithSampleData(
                document = document,
                secureArea = secureArea,
                createKeySettings = CreateKeySettings(
                    algorithm = Algorithm.ESP256,
                    nonce = "Challenge".encodeToByteString(),
                    userAuthenticationRequired = true
                ),
                dsKey = dsKey,
                dsCertChain = X509CertChain(listOf(dsCert)),
                signedAt = signedAt,
                validFrom = validFrom,
                validUntil = validUntil,
            )

            Logger.i(
                "Multipaz Getting Started Sample",
                "App: Document created with ID: ${document.identifier}"
            )

            val documents = mutableStateListOf<Document>()
            for (documentId in documentStore.listDocuments()) {
                documentStore.lookupDocument(documentId).let { document ->
                    if (document != null && !documents.contains(document)) {
                        documents.add(document)
                        Logger.i(
                            "Multipaz Getting Started Sample",
                            "Document found: ${document.identifier}, type: ${document.metadata.typeDisplayName}, display name: ${document.metadata.displayName}"
                        )
                    }
                }
            }

            for (document in documents) {
                documentStore.deleteDocument(document.identifier)
                Logger.i(
                    "Multipaz Getting Started Sample",
                    "Document deleted: ${document.identifier}"
                )
            }
        }
    }
}
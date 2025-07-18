package org.multipaz.get_started

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.multipaz.compose.prompt.PromptDialogs
import org.multipaz.prompt.PromptModel
import org.multipaz.securearea.SecureArea
import org.multipaz.securearea.SecureAreaRepository
import org.multipaz.storage.Storage

lateinit var storage: Storage
lateinit var secureArea: SecureArea
lateinit var secureAreaRepository: SecureAreaRepository

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
        }
    }
}
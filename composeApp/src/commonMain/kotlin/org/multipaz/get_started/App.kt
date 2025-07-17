package org.multipaz.get_started

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.multipaz.compose.prompt.PromptDialogs
import org.multipaz.prompt.PromptModel

@Composable
@Preview
fun App(promptModel: PromptModel) {
	MaterialTheme {
		// This ensures all prompts inherit the app's main style
		PromptDialogs(promptModel)
		// ... rest of your UI
	}
}
package org.multipaz.get_started

import androidx.compose.ui.window.ComposeUIViewController
import org.multipaz.prompt.IosPromptModel

fun MainViewController() = ComposeUIViewController {
    App(IosPromptModel())
}
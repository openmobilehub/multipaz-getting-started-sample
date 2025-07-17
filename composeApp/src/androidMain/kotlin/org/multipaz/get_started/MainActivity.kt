package org.multipaz.get_started

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import org.multipaz.context.initializeApplication
import org.multipaz.prompt.AndroidPromptModel

// IMPORTANT: Multipaz's PromptDialogs require the activity to be a FragmentActivity
// to support the BiometricPrompt and other platform features.
class MainActivity : FragmentActivity() { // use FragmentActivity
    val promptModel = AndroidPromptModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initializeApplication(this.applicationContext)
        setContent {
            App(promptModel)
        }
    }
}
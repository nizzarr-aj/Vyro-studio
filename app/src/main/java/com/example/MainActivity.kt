package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.di.VyroApplication
import com.example.core.model.AppLanguage
import com.example.core.model.UserPreferences
import com.example.ui.navigation.VyroStudioApp
import com.example.ui.theme.VyroStudioTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as VyroApplication).container

        setContent {
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            val prefs by container.userPreferencesRepository.preferences
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            val baseContext = LocalContext.current
            val baseConfig = LocalConfiguration.current

            val localizedPair = remember(prefs.language, baseConfig) {
                createLocalizedContextAndConfig(baseContext, baseConfig, prefs.language)
            }
            val layoutDirection = if (prefs.language.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalContext provides localizedPair.first,
                LocalConfiguration provides localizedPair.second,
                LocalLayoutDirection provides layoutDirection
            ) {
                VyroStudioTheme(isArabic = prefs.language == AppLanguage.ARABIC) {
                    VyroStudioApp(
                        container = container,
                        widthSizeClass = widthSizeClass
                    )
                }
            }
        }
    }

    private fun createLocalizedContextAndConfig(
        baseContext: Context,
        baseConfig: Configuration,
        language: AppLanguage
    ): Pair<Context, Configuration> {
        val locale = Locale.forLanguageTag(language.code)
        Locale.setDefault(locale)
        val config = Configuration(baseConfig).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        return baseContext.createConfigurationContext(config) to config
    }
}

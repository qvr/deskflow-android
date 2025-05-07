package org.tfv.deskflow.ui.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.tfv.deskflow.data.appPrefsStore
import org.tfv.deskflow.data.models.AppPrefs
import org.tfv.deskflow.data.models.AppPrefs.ActionsConfig
import org.tfv.deskflow.data.models.AppPrefs.ScreenConfig
import org.tfv.deskflow.data.models.copy


fun newDefaultAppPrefs(): AppPrefs {
    return AppPrefs.getDefaultInstance().copy {
        screen = screen.copy {
            name = "Android Screen"
            server = server.copy {
                address = "localhost"
                port = 24800
                useTls = false
            }
        }
        actions = actions.copy {
            shortcuts.clear()
        }
    }
}

class AppPrefsViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = application.applicationContext.appPrefsStore

    val appPrefs = dataStore.data
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly,
            newDefaultAppPrefs())

    fun resetActionShortcuts() {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy {
                    actions = actions.copy {
                        this.shortcuts.clear()
                    }
                }
            }
        }
    }

    fun setActionShortcut(shortcut: ActionsConfig.Shortcut) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy {
                    actions = actions.copy {
                        shortcuts[shortcut.actionId] = shortcut
                    }
                }
            }
        }
    }

    fun update(appPrefs: AppPrefs) {
        viewModelScope.launch {
            dataStore.updateData {
                if (it == appPrefs || appPrefs == newDefaultAppPrefs())
                    return@updateData it

                it.toBuilder().mergeFrom(appPrefs)
                    .build()
            }
        }
    }

    fun updateScreenConfig(screenConfig: ScreenConfig) {
        updateScreenConfig(screenConfig.name, screenConfig.server)
    }

    fun updateScreenConfig(name: String, server: ScreenConfig.ServerConfig) {
        viewModelScope.launch {
            dataStore.updateData {
                it.copy {
                    screen = screen.copy {
                        this.name = name
                        this.server = server
                    }
                }
            }
        }
    }
}
package org.tfv.deskflow.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import org.tfv.deskflow.data.models.AppPrefs

val Context.appPrefsStore: DataStore<AppPrefs> by dataStore(
    fileName = "app_prefs.pb",
    serializer = AppPrefsSerializer,

)

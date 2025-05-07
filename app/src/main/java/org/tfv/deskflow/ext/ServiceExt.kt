package org.tfv.deskflow.ext

import android.app.Service
import android.os.LocaleList
import java.util.Locale

val Service.currentLocales: LocaleList
  get() = resources.configuration.locales

val Service.currentDefaultLocale: Locale
  get() = currentLocales.let { it[0] ?: Locale.getDefault() }

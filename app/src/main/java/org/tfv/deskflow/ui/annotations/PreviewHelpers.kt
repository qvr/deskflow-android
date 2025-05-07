package org.tfv.deskflow.ui.annotations

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview


@Preview(name = "Phone Light", device = Devices.PHONE, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Phone Dark",device = Devices.PHONE, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Tablet Light",device = Devices.TABLET, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Tablet Dark",device = Devices.TABLET, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class PreviewAll

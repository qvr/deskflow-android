/*
 * MIT License
 *
 * Copyright (c) 2025 Jonathan Glanz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.tfv.deskflow.components.test

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ServiceTestRule
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.tfv.deskflow.components.GlobalKeyboardManager
import org.tfv.deskflow.services.GlobalInputService
import java.util.concurrent.TimeoutException



/**
 * JUnit4 test that uses a [ServiceTestRule] to interact with a bound service.
 *
 *
 * [ServiceTestRule] is a JUnit rule that provides a
 * simplified mechanism to start and shutdown your service before
 * and after the duration of your test. It also guarantees that the service is successfully
 * connected when starting (or binding to) a service. The service can be started
 * (or bound) using one of the helper methods. It will automatically be stopped (or unbound) after
 * the test completes and any methods annotated with
 * [`After`](http://junit.sourceforge.net/javadoc/org/junit/After.html) are
 * finished.
 *
 *
 * Note: This rule doesn't support [android.app.IntentService] because it's automatically
 * destroyed when [android.app.IntentService.onHandleIntent] finishes
 * all outstanding commands. So there is no guarantee to establish a successful connection
 * in a timely manner.
 */


@MediumTest
@RunWith(AndroidJUnit4::class)
class GlobalKeyboardManagerTest {




  @Rule
  val serviceRule: ServiceTestRule = ServiceTestRule()

  fun getGlobalInputServiceBinder(): IBinder {
        val serviceIntent =
      Intent(ApplicationProvider.getApplicationContext(), GlobalInputService::class.java)


    // Bind the service and grab a reference to the binder.
    val binder: IBinder = serviceRule.bindService(serviceIntent)

    return binder
    // Get the reference to the service, or you can call public methods on the binder directly.
//    val service = (binder as AccessibilityService.IAccessibilityServiceClientWrapper).getService()

  }
//  @Test
//  @Throws(TimeoutException::class)
//  fun testWithBoundService() {
//
//    // Create the service Intent.
//    val serviceIntent: Intent =
//      Intent(ApplicationProvider.getApplicationContext<Context?>(), LocalService::class.java)
//
//    // Data can be passed to the service via the Intent.
//    serviceIntent.putExtra(LocalService.SEED_KEY, 42L)
//
//    // Bind the service and grab a reference to the binder.
//    val binder: IBinder = mServiceRule.bindService(serviceIntent)
//
//    // Get the reference to the service, or you can call public methods on the binder directly.
//    val service: LocalService = (binder as LocalService.LocalBinder).getService()
//
//    // Verify that the service is working correctly.
//    Assert.assertThat<T?>(service.getRandomInt(), CoreMatchers.`is`<Int?>(CoreMatchers.any<Int?>(Int::class.java)))
//  }

  @Test
  @Throws(TimeoutException::class)
  fun testLoadSystemActionDefaults() {
    val binder = getGlobalInputServiceBinder()
    val context = ApplicationProvider.getApplicationContext<Context?>()
    val manager = GlobalKeyboardManager(binder as AccessibilityService, context!!)
    // Verify that the service is working correctly.
    //Assert.assertThat<T?>(service.getRandomInt(), CoreMatchers.`is`<Int?>(CoreMatchers.any<Int?>(Int::class.java)))
  }
}
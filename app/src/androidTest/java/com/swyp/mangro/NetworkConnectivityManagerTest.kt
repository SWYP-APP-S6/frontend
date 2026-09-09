package com.swyp.mangro

import android.os.SystemClock
import android.util.Log
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.swyp.mangro.core.utils.NetworkConnectivityStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkConnectivityManagerTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val application = instrumentation.targetContext.applicationContext as MangroApplication
    private val manager = application.networkConnectivityManager

    @Test
    fun initialStatusMatchesExpectedNetwork() {
        val expected = InstrumentationRegistry.getArguments().getString("expectedInitialStatus")
        assumeTrue("expectedInitialStatus 인자가 필요한 시작 상태 검사", expected != null)
        val status = NetworkConnectivityStatus.valueOf(requireNotNull(expected))

        // 재시도 전에 확인해 Application 생성 직후의 초기 상태 누락을 검사한다.
        assertStatus("initial", status)
    }

    @Test
    fun observesNetworkChangesWhileForegroundAndBackground() {
        assumeTrue("네트워크 전환은 에뮬레이터에서만 실행", shell("getprop ro.kernel.qemu") == "1")
        val originalWifi = shell("settings get global wifi_on") == "1"
        val originalData = shell("settings get global mobile_data") == "1"

        try {
            setNetwork(wifi = true, data = false)
            awaitStatus("wifi", NetworkConnectivityStatus.WIFI)

            ActivityScenario.launch(MainActivity::class.java).use {
                setNetwork(wifi = false, data = true)
                awaitStatus("mobile", NetworkConnectivityStatus.MOBILE_DATA)

                setNetwork(wifi = false, data = false)
                awaitStatus("foreground-offline", NetworkConnectivityStatus.NONE)

                setNetwork(wifi = true, data = false)
                awaitStatus("foreground-reconnected", NetworkConnectivityStatus.WIFI)

                shell("input keyevent KEYCODE_HOME")
                awaitCondition("Activity stopped") { !hasResumedActivity() }

                setNetwork(wifi = false, data = false)
                awaitStatus("background-offline", NetworkConnectivityStatus.NONE)

                shell("am start -W -n ${application.packageName}/com.swyp.mangro.MainActivity -f 0x10200000")
                awaitCondition("Activity resumed") { hasResumedActivity() }
                assertStatus("returned-offline", NetworkConnectivityStatus.NONE)
                assertSame(manager, application.networkConnectivityManager)

                shell("input keyevent KEYCODE_HOME")
                awaitCondition("Activity stopped again") { !hasResumedActivity() }

                setNetwork(wifi = true, data = false)
                awaitStatus("background-reconnected", NetworkConnectivityStatus.WIFI)

                shell("am start -W -n ${application.packageName}/com.swyp.mangro.MainActivity -f 0x10200000")
                awaitCondition("Activity resumed again") { hasResumedActivity() }
                assertStatus("returned-online", NetworkConnectivityStatus.WIFI)
            }
        } finally {
            setNetwork(wifi = originalWifi, data = originalData)
        }
    }

    private fun awaitStatus(label: String, expected: NetworkConnectivityStatus) {
        awaitCondition(label) { manager.connectivityStatus.value == expected }
        assertStatus(label, expected)
    }

    private fun hasResumedActivity(): Boolean {
        var resumed = false
        instrumentation.runOnMainSync {
            resumed = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED)
                .any { it is MainActivity }
        }
        return resumed
    }

    private fun assertStatus(label: String, expected: NetworkConnectivityStatus) {
        instrumentation.runOnMainSync {
            assertEquals(label, expected, manager.connectivityStatus.value)
            assertEquals(label, expected != NetworkConnectivityStatus.NONE, manager.isConnected())
            Log.i("ConnectivityQA", "$label: status=${manager.connectivityStatus.value}, connected=${manager.isConnected()}")
        }
    }

    private fun awaitCondition(label: String, condition: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + 15_000
        while (!condition()) {
            check(SystemClock.elapsedRealtime() < deadline) {
                "$label timed out: status=${manager.connectivityStatus.value}"
            }
            SystemClock.sleep(100)
        }
    }

    private fun setNetwork(wifi: Boolean, data: Boolean) {
        shell("svc data ${if (data) "enable" else "disable"}")
        shell("svc wifi ${if (wifi) "enable" else "disable"}")
    }

    private fun shell(command: String): String = android.os.ParcelFileDescriptor.AutoCloseInputStream(
        instrumentation.uiAutomation.executeShellCommand(command),
    ).bufferedReader().use { it.readText().trim() }
}

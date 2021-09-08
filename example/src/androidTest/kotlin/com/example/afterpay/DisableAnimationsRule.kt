package com.afterpay.android

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.IOException

private const val SHELL_COMMAND = "settings put global %s %d"
private const val TRANSITION_ANIMATION_SCALE = "transition_animation_scale"
private const val WINDOW_ANIMATION_SCALE = "window_animation_scale"
private const val ANIMATOR_DURATION = "animator_duration_scale"

class DisableAnimationsRule : TestRule {

    override fun apply(base: Statement, description: Description) = object : Statement() {

        @Throws(Throwable::class)
        override fun evaluate() {
            changeAnimationStatus(enable = false)
            try {
                base.evaluate()
            } finally {
                changeAnimationStatus(enable = true)
            }
        }
    }

    @Throws(IllegalStateException::class, IOException::class)
    private fun changeAnimationStatus(enable: Boolean = true) {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).run {
            val enableInt = if (enable) 1 else 0
            listOf(TRANSITION_ANIMATION_SCALE, WINDOW_ANIMATION_SCALE, ANIMATOR_DURATION)
                .onEach { executeShellCommand(SHELL_COMMAND.format(it, enableInt)) }
        }
    }
}

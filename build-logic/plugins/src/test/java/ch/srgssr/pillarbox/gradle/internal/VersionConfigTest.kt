/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle.internal

import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class VersionConfigTest(
    private val envVersionName: String?,
    private val versionName: String,
    private val versionCode: Int,
) {
    private lateinit var versionConfig: VersionConfig

    @BeforeTest
    fun setUp() {
        versionConfig = VersionConfig(envVersionName)
    }

    @Test
    fun `version name`() {
        assertEquals(versionName, versionConfig.versionName())
    }

    @Test
    fun `version name with default`() {
        assertEquals("dev", VersionConfig().versionName(default = "dev"))
        assertEquals("1.2.3", VersionConfig(envVersionName = "1.2.3").versionName(default = "dev"))
    }

    @Test
    fun `version code`() {
        assertEquals(versionCode, versionConfig.versionCode())
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{index}: envVersionName={0}, versionName={1}, versionCode={2}")
        fun parameters(): Iterable<Any> {
            return listOf<Array<Any?>>(
                // Invalid envVersionName
                arrayOf(null, "Local", 9999),
                arrayOf("", "", 9999),
                arrayOf("Foo", "Foo", 9999),

                // Invalid version format
                arrayOf("1.2", "1.2", 9999),
                arrayOf("1a2b3", "1a2b3", 9999),
                arrayOf("1.2.3.4", "1.2.3.4", 9999),
                arrayOf("111.2.3", "111.2.3", 9999),
                arrayOf("1.222.3", "1.222.3", 9999),
                arrayOf("1.2.333", "1.2.333", 9999),

                // Valid version format
                arrayOf("0.0.0", "0.0.0", 0),
                arrayOf("1.2.3", "1.2.3", 10203),
                arrayOf("12.34.56", "12.34.56", 123456),
                arrayOf("01.02.03", "01.02.03", 10203),
                arrayOf("99.99.99", "99.99.99", 999999),
            )
        }
    }
}

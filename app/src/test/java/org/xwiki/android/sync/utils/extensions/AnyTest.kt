package org.xwiki.android.sync.utils.extensions

import org.junit.Assert
import org.junit.Test

/**
 * Tests for extensions which can be applied to any object
 *
 * @version $Id$
 */
class AnyTest {
    @Test
    fun testTag() {
        Assert.assertEquals(TAG, AnyTest::class.java.simpleName)
    }
}

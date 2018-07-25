package org.xwiki.android.sync.utils.extensions

import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import retrofit2.HttpException

/**
 * Tests for extensions which can be applied to throwable objects
 *
 * @version $Id$
 */
class ThrowableTest {
    @Test
    fun testUnauthorized() {
        listOf(
            Mockito.mock(HttpException::class.java).apply {
                Mockito.`when`(code()).thenReturn(403)
            } to false,
            Mockito.mock(HttpException::class.java).apply {
                Mockito.`when`(code()).thenReturn(402)
            } to false,
            Mockito.mock(HttpException::class.java).apply {
                Mockito.`when`(code()).thenReturn(401)
            } to true,
            Mockito.mock(HttpException::class.java).apply {
                Mockito.`when`(code()).thenReturn(404)
            } to false,
            Throwable() to false
        ).forEach {
            (error, awaited) ->
            Assert.assertEquals(error.unauthorized, awaited)
        }
    }
}

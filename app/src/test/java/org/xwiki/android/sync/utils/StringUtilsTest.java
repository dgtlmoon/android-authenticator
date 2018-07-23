package org.xwiki.android.sync.utils;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * StringUtilsTest.
 *
 * @version $Id$
 */
public class StringUtilsTest {
    @Test
    public void isEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty(" \n\t\r"));
        assertFalse(StringUtils.isEmpty("121"));
        assertFalse(StringUtils.isEmpty("\n12\n"));
    }

    @Test
    public void nonEmptyOrNull() {
        assertNull(StringUtils.nonEmptyOrNull(null));
        assertNull(StringUtils.nonEmptyOrNull(""));
        assertNull(StringUtils.nonEmptyOrNull(" \n\t\r"));
        assertEquals(StringUtils.nonEmptyOrNull("121"), "121");
        assertEquals(StringUtils.nonEmptyOrNull("\n12\n"), "\n12\n");
    }

    @Test
    public void isEmail() {
        assertTrue(StringUtils.isEmail("fitz.lee@outlook.com"));
        assertTrue(StringUtils.isEmail("fitz.lee.lee@o.com"));
        assertFalse(StringUtils.isEmail("fitz.lee@outlook"));
        assertFalse(StringUtils.isEmail("@outlook.com"));
        assertFalse(StringUtils.isEmail("fitz.lee@outlook:"));
        assertFalse(StringUtils.isEmail("fitz@@"));
        assertFalse(StringUtils.isEmail(null));
        assertFalse(StringUtils.isEmail(""));
    }

    @Test
    public void isPhone() {
        assertFalse(StringUtils.isPhone(null));
        assertFalse(StringUtils.isPhone(""));
        assertFalse(StringUtils.isPhone("+"));
        assertFalse(StringUtils.isPhone("abcd"));
        assertFalse(StringUtils.isPhone("--------"));

        assertTrue(StringUtils.isPhone("123"));
        assertTrue(StringUtils.isPhone("+123"));
        assertTrue(StringUtils.isPhone("123-123"));
        assertTrue(StringUtils.isPhone("+123-123"));
        assertTrue(StringUtils.isPhone("123-123-(123)"));
        assertTrue(StringUtils.isPhone("+123-123-(123)"));
    }

    @Test
    public void iso8601ToDate() {
        assertNotNull(StringUtils.iso8601ToDate("2016-05-20T13:11:48+0200"));
        assertNull(StringUtils.iso8601ToDate("2011-09-24T19:45:31"));
        assertNull(StringUtils.iso8601ToDate("2011-092419:45:31"));
        assertNull(StringUtils.iso8601ToDate("201"));
        assertNull(StringUtils.iso8601ToDate(""));
        assertNull(StringUtils.iso8601ToDate(null));
    }

    @Test
    public void dateToIso8601String() {
        assertNotNull(StringUtils.dateToIso8601String(new Date()));
        //System.out.println(StringUtils.dateToIso8601String(new Date()));
        assertNull(StringUtils.iso8601ToDate(null));
    }
}
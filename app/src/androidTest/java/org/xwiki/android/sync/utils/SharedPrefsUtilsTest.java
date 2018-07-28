package org.xwiki.android.sync.utils;

import android.content.Context;
import android.support.test.filters.Suppress;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xwiki.android.sync.AppContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * SharedPrefsUtilsTest.
 */

@RunWith(AndroidJUnit4.class)
public class SharedPrefsUtilsTest {

    private Context mContext;

    private final static String commonArrayListTestKey = "arrayList";
    private final static String commonValueTestKey = "value";
    private final static Random random = new Random();
    private final static Integer tests = 10;

    @Before
    public void setUp() {
        mContext = AppContext.getInstance().getApplicationContext();
    }

    @Before
    @After
    public void clean() {
        SharedPrefsUtils.removeKeyValue(mContext, commonArrayListTestKey);
        SharedPrefsUtils.removeKeyValue(mContext, commonValueTestKey);
    }

    @Test
    public void arrayListCorrectlyPutGet() {
        List<String> testList = new ArrayList<>();

        for (int i = 0; i < tests; i++) {
            testList.add(random.nextInt() + "");
        }

        SharedPrefsUtils.putArrayList(
            mContext,
            commonArrayListTestKey,
            testList
        );

        List<String> saved = SharedPrefsUtils.getArrayList(
            mContext,
            commonArrayListTestKey
        );

        assertTrue(
            testList.containsAll(saved) && saved.containsAll(testList)
        );
    }

    @Test(expected = NullPointerException.class)
    public void arrayListPutNullThrowNullPointerException() {
        SharedPrefsUtils.putArrayList(
            mContext,
            commonArrayListTestKey,
            null
        );
    }

    @Test
    public void arrayListGetReturnNull() {
        assertNull(
            SharedPrefsUtils.getArrayList(
                mContext,
                commonArrayListTestKey
            )
        );
    }

    @Test
    public void valueCorrectPutString() {
        String value = random.toString();

        SharedPrefsUtils.putValue(
            mContext,
            commonValueTestKey,
            value
        );

        assertEquals(
            value,
            SharedPrefsUtils.getValue(
                mContext,
                commonValueTestKey,
                null
            )
        );
    }

    @Test
    public void valueCorrectPutInt() {
        int value = random.nextInt();

        SharedPrefsUtils.putValue(
            mContext,
            commonValueTestKey,
            value
        );

        assertEquals(
            value,
            SharedPrefsUtils.getValue(
                mContext,
                commonValueTestKey,
                random.nextInt()
            )
        );
    }

    @Test
    public void valueCorrectPutBoolean() {
        Boolean value = random.nextBoolean();

        SharedPrefsUtils.putValue(
            mContext,
            commonValueTestKey,
            value
        );

        assertEquals(
            value,
            SharedPrefsUtils.getValue(
                mContext,
                commonValueTestKey,
                !value
            )
        );
    }

    @Test
    public void valueReturnDefaultStringIfValueIsAbsent() {
        assertNull(
            SharedPrefsUtils.getValue(
                mContext,
                commonValueTestKey,
                null
            )
        );
    }

    @Test
    public void valueReturnDefaultIntIfValueIsAbsent() {
        int defaultVal = random.nextInt();
        assertEquals(
            defaultVal,
            SharedPrefsUtils.getValue(
                mContext,
                commonValueTestKey,
                defaultVal
            )
        );
    }

    @Test
    public void valueReturnDefaultBooleanIfValueIsAbsent() {
        assertEquals(
            true,
            SharedPrefsUtils.getValue(
                mContext,
                commonValueTestKey,
                true
            )
        );
        assertEquals(
            false,
            SharedPrefsUtils.getValue(
                mContext,
                commonValueTestKey,
                false
            )
        );
    }

    @Test
    public void correctlyRemoveValue() {
        valueCorrectPutBoolean();
        SharedPrefsUtils.removeKeyValue(mContext, commonValueTestKey);
        valueReturnDefaultBooleanIfValueIsAbsent();

        valueCorrectPutInt();
        SharedPrefsUtils.removeKeyValue(mContext, commonValueTestKey);
        valueReturnDefaultIntIfValueIsAbsent();

        valueCorrectPutString();
        SharedPrefsUtils.removeKeyValue(mContext, commonValueTestKey);
        valueReturnDefaultStringIfValueIsAbsent();

        arrayListCorrectlyPutGet();
        SharedPrefsUtils.removeKeyValue(mContext, commonArrayListTestKey);
        arrayListGetReturnNull();
    }
}

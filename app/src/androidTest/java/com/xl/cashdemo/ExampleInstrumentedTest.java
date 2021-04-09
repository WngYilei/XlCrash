package com.xl.cashdemo;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        assertEquals("com.xl.cashdemo", appContext.getPackageName());

        String a  = null;
        if (a!=null && !a.equals("")){
           System.out.println("测试测试");
        }
    }
}
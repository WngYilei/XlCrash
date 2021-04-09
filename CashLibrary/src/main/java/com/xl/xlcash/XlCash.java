/*
 * *   
 *   Created by wyl on 3/13/21 1:09 PM  
 *  project_name:CashDemo     
 *  创建时间：3/13/21 1:09 PM   
 *  修改人：wyl   
 *  修改时间：3/13/21 1:09 PM    
 *  修改备注：
 *  版权所有违法必究   
 *  @version    
 * /
 */
package com.xl.xlcash;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.xl.xlcash.compat.ActivityKillerV15_V20;
import com.xl.xlcash.compat.ActivityKillerV21_V23;
import com.xl.xlcash.compat.ActivityKillerV24_V25;
import com.xl.xlcash.compat.ActivityKillerV26;
import com.xl.xlcash.compat.ActivityKillerV28;
import com.xl.xlcash.compat.IActivityKiller;
import com.xl.xlcash.utils.CrashLog;
import com.xl.xlcash.utils.EmailUtils;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.weishu.reflection.Reflection;


public final class XlCash {

    private static IActivityKiller sActivityKiller;
    private static ExceptionHandler sExceptionHandler;
    private static boolean sInstalled = false;//标记位，避免重复安装卸载
    private static boolean sIsSafeMode;

    private XlCash() {
    }

    private static void install(Context ctx, ExceptionHandler exceptionHandler) {
        if (sInstalled) {
            return;
        }
        try {
            //解除 android P 反射限制
            Reflection.unseal(ctx);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        sInstalled = true;
        sExceptionHandler = exceptionHandler;

        initActivityKiller();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (sExceptionHandler != null) {
                    sExceptionHandler.uncaughtExceptionHappened(t, e);
                }
                if (t == Looper.getMainLooper().getThread()) {
                    isChoreographerException(e);
                    safeMode();
                }
            }
        });

    }

    /**
     * 替换ActivityThread.mH.mCallback，实现拦截Activity生命周期，直接忽略生命周期的异常的话会导致黑屏，目前
     * 会调用ActivityManager的finishActivity结束掉生命周期抛出异常的Activity
     */
    private static void initActivityKiller() {
        //各版本android的ActivityManager获取方式，finishActivity的参数，token(binder对象)的获取不一样
        if (Build.VERSION.SDK_INT >= 28) {
            sActivityKiller = new ActivityKillerV28();
        } else if (Build.VERSION.SDK_INT >= 26) {
            sActivityKiller = new ActivityKillerV26();
        } else if (Build.VERSION.SDK_INT == 25 || Build.VERSION.SDK_INT == 24) {
            sActivityKiller = new ActivityKillerV24_V25();
        } else if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT <= 23) {
            sActivityKiller = new ActivityKillerV21_V23();
        } else if (Build.VERSION.SDK_INT >= 15 && Build.VERSION.SDK_INT <= 20) {
            sActivityKiller = new ActivityKillerV15_V20();
        } else if (Build.VERSION.SDK_INT < 15) {
            sActivityKiller = new ActivityKillerV15_V20();
        }

        try {
            hookmH();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void hookmH() throws Exception {

        final int LAUNCH_ACTIVITY = 100;
        final int PAUSE_ACTIVITY = 101;
        final int PAUSE_ACTIVITY_FINISHING = 102;
        final int STOP_ACTIVITY_HIDE = 104;
        final int RESUME_ACTIVITY = 107;
        final int DESTROY_ACTIVITY = 109;
        final int NEW_INTENT = 112;
        final int RELAUNCH_ACTIVITY = 126;
        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Object activityThread = activityThreadClass.getDeclaredMethod("currentActivityThread").invoke(null);

        Field mhField = activityThreadClass.getDeclaredField("mH");
        mhField.setAccessible(true);
        final Handler mhHandler = (Handler) mhField.get(activityThread);
        Field callbackField = Handler.class.getDeclaredField("mCallback");
        callbackField.setAccessible(true);
        callbackField.set(mhHandler, new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (Build.VERSION.SDK_INT >= 28) {//android P 生命周期全部走这
                    final int EXECUTE_TRANSACTION = 159;
                    if (msg.what == EXECUTE_TRANSACTION) {
                        try {
                            mhHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            sActivityKiller.finishLaunchActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    }
                    return false;
                }
                switch (msg.what) {
                    case LAUNCH_ACTIVITY:// startActivity--> activity.attach  activity.onCreate  r.activity!=null  activity.onStart  activity.onResume
                        try {
                            mhHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            sActivityKiller.finishLaunchActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    case RESUME_ACTIVITY://回到activity onRestart onStart onResume
                        try {
                            mhHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            sActivityKiller.finishResumeActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    case PAUSE_ACTIVITY_FINISHING://按返回键 onPause
                    case PAUSE_ACTIVITY://开启新页面时，旧页面执行 activity.onPause
                        try {
                            mhHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            sActivityKiller.finishPauseActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    case STOP_ACTIVITY_HIDE://开启新页面时，旧页面执行 activity.onStop
                        try {
                            mhHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            sActivityKiller.finishStopActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    case DESTROY_ACTIVITY:// 关闭activity onStop  onDestroy
                        try {
                            mhHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            notifyException(throwable);
                        }
                        return true;
                }
                return false;
            }
        });
    }


    private static void notifyException(Throwable throwable) {
        if (sExceptionHandler == null) {
            return;
        }
        if (isSafeMode()) {
            sExceptionHandler.bandageExceptionHappened(throwable);
        } else {
            sExceptionHandler.uncaughtExceptionHappened(Looper.getMainLooper().getThread(), throwable);
            safeMode();
        }
    }

    public static boolean isSafeMode() {
        return sIsSafeMode;
    }

    private static void safeMode() {
        sIsSafeMode = true;
        if (sExceptionHandler != null) {
            sExceptionHandler.enterSafeMode();
        }
        while (true) {
            try {
                Looper.loop();
            } catch (Throwable e) {
                isChoreographerException(e);
                if (sExceptionHandler != null) {
                    sExceptionHandler.bandageExceptionHappened(e);
                }
            }
        }
    }

    /**
     * view measure layout draw时抛出异常会导致Choreographer挂掉
     * <p>
     * 建议直接杀死app。以后的版本会只关闭黑屏的Activity
     *
     * @param e
     */
    private static void isChoreographerException(Throwable e) {
        if (e == null || sExceptionHandler == null) {
            return;
        }
        StackTraceElement[] elements = e.getStackTrace();
        if (elements == null) {
            return;
        }

        for (int i = elements.length - 1; i > -1; i--) {
            if (elements.length - i > 20) {
                return;
            }
            StackTraceElement element = elements[i];
            if ("android.view.Choreographer".equals(element.getClassName())
                    && "Choreographer.java".equals(element.getFileName())
                    && "doFrame".equals(element.getMethodName())) {
                sExceptionHandler.mayBeBlackScreen(e);
                return;
            }

        }
    }

    /**
     * 初始化
     *
     * @param context
     * @param email   配置发送邮箱地址
     */

    public static void install(final Context context, String email) {
        if (email != null && !TextUtils.isEmpty(email)) {
            EmailUtils.getInstance().setEmail(email);
        }
        final Thread.UncaughtExceptionHandler sysExcepHandler = Thread.getDefaultUncaughtExceptionHandler();
        final Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        XlCash.install(context, new ExceptionHandler() {
            @Override
            protected void onUncaughtExceptionHappened(Thread thread, final Throwable throwable) {
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", throwable);
                CrashLog.saveCrashLog(context, throwable);

                String msg = CrashLog.getExceptionMsg(context, throwable);
                EmailUtils.getInstance().sendEmail(context.getPackageName(), msg + "");

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        toast.setText("捕获到导致崩溃的异常");
                        Log.e("TAG", "run======: " + throwable);
                        toast.show();
                    }
                });
            }

            @Override
            protected void onBandageExceptionHappened(final Throwable throwable) {
                Log.e("TAG", "onBandageExceptionHappened: " + throwable.getMessage());

                String msg = CrashLog.getExceptionMsg(context, throwable);
                EmailUtils.getInstance().sendEmail(context.getPackageName(), msg + "");


                throwable.printStackTrace();//打印警告级别log，该throwable可能是最开始的bug导致的，无需关心
                toast.setText("XlCroach Worked");
                toast.show();
            }

            @Override
            protected void onEnterSafeMode() {
                toast.setText("已经进入安全模式");
                toast.show();
            }

            @Override
            protected void onMayBeBlackScreen(Throwable e) {
                Thread thread = Looper.getMainLooper().getThread();
                Log.e("AndroidRuntime", "--->onUncaughtExceptionHappened:" + thread + "<---", e);
                //黑屏时建议直接杀死app
                sysExcepHandler.uncaughtException(thread, new RuntimeException("black screen"));
            }

        });

    }

}

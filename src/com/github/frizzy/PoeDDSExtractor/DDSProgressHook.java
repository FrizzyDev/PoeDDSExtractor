package com.github.frizzy.PoeDDSExtractor;

import com.sun.source.util.TaskListener;

import java.util.ArrayList;
import java.util.List;

public class DDSProgressHook {

    private DDSProgressHook ( ) {

    }

    private static String stepName = "";

    private static int currentValue = 0;

    private static int endValue = 0;

    private static List < ProcessListener > listeners = new ArrayList <>(  );

    /**
     *
     */
    public static void setStep ( String name ) {
        DDSProgressHook.stepName = name;
    }

    public static void setValues ( int currentValue, int endValue ) {
        DDSProgressHook.currentValue = currentValue;
        DDSProgressHook.endValue = endValue;
    }

    public static void updateCurrentValue ( int currentValue ) {
        DDSProgressHook.currentValue = currentValue;

        for ( ProcessListener pl : listeners ) {
          pl.update( currentValue, DDSProgressHook.endValue );
        }
    }

    public static void addUpdateListener ( ProcessListener listener ) {
        listeners.add( listener );
    }
}

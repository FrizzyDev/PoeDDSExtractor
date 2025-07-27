package com.github.frizzy.PoeDDSExtractor;

import com.sun.source.util.TaskListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the progress of each step in the extraction, conversion, and texture extraction process.
 * This is only friendly to one step executed at a time.
 *
 * @author Frizzy
 * @version 0.0.1
 * @since 0.0.1
 */
public class DDSProgressHook {

    private DDSProgressHook ( ) {

    }

    /**
     * The name of the current step of whatever process is occurring.
     */
    private static String stepName = "";

    /**
     * The current progress of the process.
     */
    private static int currentValue = 0;

    /**
     * The end value, or last step, of the process.
     */
    private static int endValue = 0;

    /**
     * Listeners that need to be notified when the progress is updated.
     */
    private static List < ProcessListener > listeners = new ArrayList <>(  );

    /**
     * Sets the step name.
     */
    public static void setStep ( String name ) {
        DDSProgressHook.stepName = name;
    }

    /**
     * Sets the current value and end value to the provided values.
     */
    public static void setValues ( int currentValue, int endValue ) {
        DDSProgressHook.currentValue = currentValue;
        DDSProgressHook.endValue = endValue;
    }

    /**
     * Updates the current value.
     */
    public static void updateCurrentValue ( int currentValue ) {
        DDSProgressHook.currentValue = currentValue;

        for ( ProcessListener pl : listeners ) {
          pl.update( currentValue, DDSProgressHook.endValue );
        }
    }

    /**
     * Adds the listener to the progress hook.
     */
    public static void addUpdateListener ( ProcessListener listener ) {
        listeners.add( listener );
    }
}

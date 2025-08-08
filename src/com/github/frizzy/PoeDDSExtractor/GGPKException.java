package com.github.frizzy.PoeDDSExtractor;

/**
 * Thrown to communicate that a critical exception occurred in the GGPK2
 * class and that any subsequent calls to GGPK2 will fail.
 */
public class GGPKException extends Exception {

    public GGPKException ( String message ) {
        super( message );
    }
}

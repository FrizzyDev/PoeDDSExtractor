package com.github.frizzy.PoeDDSExtractor;

import java.io.File;
import java.util.List;

/**
 *
 *
 * @author Frizzy
 * @version 0.0.2
 * @since 0.0.2
 */
public class BankFile {

    /**
     *
     */
    private String path;

    /**
     *
     */
    private File fReference;

    /**
     *
     */
    private List < File > wavFiles;


    /**
     *
     * @param path
     * @param fRef
     */
    public BankFile ( String path , File fRef ) {
        this.path = path;
        this.fReference = fRef;
    }

    /**
     *
     * @return
     */
    public String getPath ( ) {
        return path;
    }

    /**
     *
     * @return
     */
    public File getFile ( ) {
        return fReference;
    }

    /**
     *
     * @return
     */
    public List < File > getWavFiles ( ) {
        return wavFiles;
    }

    public void setWavFiles ( List < File > wavFiles ) {
        this.wavFiles = wavFiles;
    }
}

package com.github.frizzy.PoeDDSExtractor.Bank;

import java.nio.file.Path;
import java.util.List;

/**
 * Class BankFile represents a .bank file extracted or moved from the Content.ggpk or
 * _.index.bin files.
 * <br>
 *
 *
 * @author Frizzy
 * @version 0.0.2
 * @since 0.0.2
 */
public class BankFile {

    /**
     * The internal Path of the .bank file in the .ggpk file.
     * <br>
     * If the content file is the _.index.bin file, the path will represent
     * what it would be in the .ggpk file.
     */
    private final String path;

    /**
     * The reference to the .bank file on disk, extracted/moved to the
     * specified directory.
     */
    private final Path diskPath;

    /**
     * A list of .wav files extracted from the .bank file.
     */
    private List < Path > wavFiles;


    /**
     * Constructs the BankFile instance with the specified internal path
     * and on disk file reference.
     */
    public BankFile ( String path , Path fRef ) {
        this.path = path;
        this.diskPath = fRef;
    }

    /**
     * Returns the internal path of the .bank file in the .ggpk file.
     */
    public String getPath ( ) {
        return path;
    }

    /**
     * Returns the path to the extracted/moved bank file on disk.
     */
    public Path getDiskPath( ) {
        return diskPath;
    }

    /**
     * Returns the list of .wav files extracted from the .bank file.
     */
    public List < Path > getWavFiles ( ) {
        return wavFiles;
    }

    /**
     * Sets the .wav files list.
     */
    public void setWavFiles ( List < Path > wavFiles ) {
        this.wavFiles = wavFiles;
    }
}

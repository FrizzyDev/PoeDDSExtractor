package com.github.frizzy.PoeDDSExtractor;

import java.io.File;
import java.util.List;

/**
 * Wrapper class to make it easier to manage passing around files for the entire
 * process.
 *
 * @author Frizzy
 * @version 0.0.2
 * @since 0.0.2
 */
public class DDSFile {


    /**
     * The path of the .dds file in the Content.ggpk
     * file.
     */
    private final String ddsPath;

    /**
     * Textures that have not been extracted and are stored
     * in the .dds file.
     */
    private List < Texture > unextractedTextures;

    /**
     * Textures that have been extracted.
     */
    private List < File > extractedTextures;

    /**
     * The file reference to the .dds file.
     */
    private final File fReference;

    /**
     * The file reference to the .png file, converted
     * from the .dds file.
     */
    private File pngFile;

    /**
     *
     * @param path The path of the DDSFile in the Content.ggpk file.
     * @param textures A list of textures the DDSFile contains.
     * @param fRef The on disk file reference.
     */
    public DDSFile (  String path, List < Texture> textures, File fRef ) {
        this.ddsPath = path;
        this.unextractedTextures = textures;
        this.fReference = fRef;
    }

    /**
     * Sets the .png file reference of this DDSFile.
     * The .png file is set when the .dds file is converted via DDSConverter.
     */
    public void setPngFile( File pngFile ) {
        this.pngFile = pngFile;
    }

    /**
     * Sets the list of extracted textures.
     * Set when the DDSFile is passed to the DDSExtractor.
     */
    public void setExtractedTextures ( List < File > textures ) {
        this.extractedTextures = textures;
    }

    /**
     *
     * @param textures
     */
    public void setUnextractedTextures ( List < Texture > textures ) {
        this.unextractedTextures = textures;
    }

    /**
     * Returns the internal Content.ggpk path of the .dds file.
     */
    public String getDdsPath ( ) {
        return ddsPath;
    }

    /**
     * Returns a list of textures stored within the .dds file.
     * This list only represents the content of a .dds file, not if they
     * have actually been extracted.
     */
    public List < Texture > getUnextractedTextures( ) {
        return unextractedTextures;
    }

    /**
     * Returns a list of File references to the extracted textures.
     * This will return null until the textures have been extracted by
     * DDSExtractor.
     */
    public List < File > getExtractedTextures ( ) {
        return extractedTextures;
    }

    /**
     * Returns the File reference of the .dds file on disk.
     */
    public File getFile ( ) {
        return fReference;
    }

    /**
     * Returns the File reference of the .png file on disk. This will
     * return null until the .dds file has been converted.
     */
    public File getPngFile ( ) {
        return pngFile;
    }
}


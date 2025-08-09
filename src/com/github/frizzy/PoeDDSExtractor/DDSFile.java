package com.github.frizzy.PoeDDSExtractor;

import java.nio.file.Path;
import java.util.List;

/**
 * Wrapper class to make it easier to manage passing around files for the entire
 * process.
 *
 * @author Frizzy
 * @version 0.0.2
 * @since 0.0.2
 */
public class DDSFile implements Comparable < DDSFile > {

    /**
     *
     */
    private final String ddsName;

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
    private List < Path > extractedTextures;

    /**
     * The file reference to the .dds file.
     */
    private final Path diskPath;

    /**
     * The file reference to the .png file, converted
     * from the .dds file.
     */
    private Path pngPath;

    /**
     *
     * @param path The path of the DDSFile in the Content.ggpk file.
     * @param textures A list of textures the DDSFile contains.
     * @param diskPath The on disk file reference.
     */
    public DDSFile (  String path, List < Texture> textures, Path diskPath ) {
        this.ddsPath = path;
        this.unextractedTextures = textures;
        this.diskPath = diskPath;
        this.ddsName = diskPath.getFileName().toString();
    }

    /**
     * Sets the .png file reference of this DDSFile.
     * The .png file is set when the .dds file is converted via DDSConverter.
     */
    public void setPNGPath( Path pngPath ) {
        this.pngPath = pngPath;
    }

    /**
     * Sets the list of extracted textures.
     * Set when the DDSFile is passed to the DDSExtractor.
     */
    public void setExtractedTextures ( List < Path > textures ) {
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
    public List < Path > getExtractedTextures ( ) {
        return extractedTextures;
    }

    /**
     * Returns the name of the .dds file.
     */
    public String getName ( ) {
        return ddsName;
    }

    /**
     * Returns the File reference of the .dds file on disk.
     */
    public Path getDiskPath( ) {
        return diskPath;
    }

    /**
     * Returns the File reference of the .png file on disk. This will
     * return null until the .dds file has been converted.
     */
    public Path getPNGPath( ) {
        return pngPath;
    }

    @Override
    public String toString( ) {
        return "DDSFile{" +
                "ddsPath='" + ddsPath + '\'' +
                ", unextractedTextures=" + unextractedTextures +
                ", extractedTextures=" + extractedTextures +
                ", diskPath=" + diskPath +
                ", pngPath=" + pngPath +
                '}';
    }

    @Override
    public int compareTo( DDSFile o ) {
        return o.getName().compareTo( getName() );
    }
}


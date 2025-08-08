package com.github.frizzy.PoeDDSExtractor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Revision of DDSExtractor, performing the same process in a more streamline way, utilizing
 * the wrapper DDSFile.
 *
 * @author Frizzy
 * @version 0.0.2
 * @since 0.0.2
 */
public class DDSExtractor2 {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger( DDSExtractor2.class.getName( ) );

    /**
     * The uiimages.txt or uidivinationimages.txt file.
     */
    private final File txtFile;

    /**
     * Boolean flag determining if previously extracted textures should be overwritten.
     * <br>
     * Setting this to true can drastically improve performance when restarting the process.
     */
    private boolean overwrite;

    /**
     * @param txtFile The uiimages.txt or uidivinationimages.txt file the texture name, path, and coordinate information is retrieved from.
     */
    public DDSExtractor2( File txtFile , boolean overwrite ) {
        this.txtFile = txtFile;
        this.overwrite = overwrite;
    }

    /**
     * Adds a handler to the logger.
     */
    public void addLoggerHandler ( Handler handler ) {
        LOGGER.addHandler( handler );
    }

    /**
     * Extracts textures from each dds file in the list, extracting the individual textures into the same
     * path the .dds files are located in.
     */
    public List < DDSFile > extractSubTextures ( List < DDSFile > wantedTextures ) {
        return extractSubTextures( wantedTextures, null );
    }

    /**
     * Extracts textures for the DDSFiles in the list. The same list is returned once textures have been extracted
     * from every .png file in each DDSFile.
     * <br>
     * Textures are extracted into the specified outputPath.
     *
     * @param wantedTextures A list of DDSFiles that have been extracted and need their textures extracted.
     *
     * @param outputPath A path that can be optionally specified to have all extracted textures saved to.
     */
    @SuppressWarnings( "all" ) //temp
    public List < DDSFile > extractSubTextures( List < DDSFile > wantedTextures, Path outputPath ) {
        if ( wantedTextures != null && !wantedTextures.isEmpty( ) ) {
            for ( DDSFile dFile : wantedTextures ) {
                List < File > extractedTextures = new ArrayList <>(  );

                File pngFile = dFile.getPngFile();
                List < Texture > textures = dFile.getUnextractedTextures();

                final String sourcePath = dFile.getDdsPath();

                if ( !sourcePath.equals( "Unavailable" ) ) {

                    for ( Texture t : textures ) {
                        int[] coords = t.coordinates();

                        Optional < File > opt = extract( coords[ 0 ] , coords[ 2 ] , coords[ 1 ] , coords[ 3 ] , pngFile , t.name(), outputPath );
                        opt.ifPresentOrElse( extracted -> {
                                    if ( extracted.exists( ) ) {
                                       extractedTextures.add( extracted );
                                    }
                                } ,
                                ( ) -> {
                                    LOGGER.log( Level.WARNING , "No file was returned. The sub texture was not extracted." );
                                } );
                    }
                }

                dFile.setExtractedTextures( extractedTextures );
            }
        }

        return wantedTextures;
    }

    /**
     * Completes the extraction process.
     *
     * @param x1          The first x coordinate of the sub texture.
     * @param x2          The second x coordinate of the sub texture. x1 is subtracted to create the width of the image.
     * @param y1          The first y coordinate of the sub texture.
     * @param y2          The second y coordinate of the sub texture. y1 is subtracted to create the height of the image.
     * @param pngFile     The png file containing the sub textures.
     * @param textureName The name of the sub texture being extracted.
     * @param output A path that all extracted textures will be saved to. Can be null.
     * @return Returns an Optional of File to help protect the process from null values.
     */
    private Optional < File > extract( int x1 , int x2 , int y1 , int y2 , File pngFile , String textureName, Path output ) {
        if ( pngFile == null )
            return Optional.empty( );

        /*
         * In case a .dds file is passed, we get the .png file if it exists. ImageIO cannot read .dds files.
         * In the event someone else looks at this, the TwelveMonkeys ImageIO plugin library cannot read the .dds
         * files as it does not support the correct format.
         */
        if ( pngFile.getName( ).contains( ".dds" ) ) {
            pngFile = new File( pngFile.getAbsolutePath( ).replace( ".dds" , ".png" ) );

            if ( !pngFile.exists( ) ) {
                LOGGER.log( Level.WARNING , "File: " + pngFile.getAbsolutePath( ) + " does not exist." );
                return Optional.empty( );
            }
        }

        try {
            LOGGER.log( Level.INFO , "Reading file: " + pngFile );
            BufferedImage parent = ImageIO.read( pngFile );

            if ( parent != null ) {
                LOGGER.log( Level.INFO , "Extracting texture: " + textureName );
                BufferedImage extracted = parent.getSubimage( x1 , y1 , x2 , y2 );

                String subbedName = textureName.substring( textureName.lastIndexOf( "/" ) );
                File extractedFile;

                if ( output != null ) {
                    extractedFile = new File( output + File.separator + subbedName + ".png" );
                } else {
                    extractedFile = new File( pngFile.getParentFile( ).getAbsolutePath( ) + File.separator + subbedName + ".png" );
                }

                if ( extractedFile.exists( ) && overwrite || !extractedFile.exists( ) ) {
                    completeWrite( extracted , extractedFile , textureName );
                } else {
                    LOGGER.log( Level.INFO , "Overwrite is false and file already exists. Image was not saved." );
                }

                return Optional.of( extractedFile );
            } else {
                LOGGER.log( Level.WARNING , "Parent image was null, no textures can be extracted." );
            }
        } catch ( IOException | RasterFormatException e ) {
            LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
        }

        return Optional.empty( );
    }

    /**
     * Completes the image write process when a texture has been extracted from the parent .png file.
     */
    private void completeWrite( BufferedImage extractedImg , File extractedFile , String textureName ) {
        try {
            ImageIO.write( extractedImg , "png" , extractedFile );
            LOGGER.log( Level.INFO , "Extracted texture: " + textureName + " was saved to: " + extractedFile.getAbsolutePath( ) + "." );
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
        }
    }
}

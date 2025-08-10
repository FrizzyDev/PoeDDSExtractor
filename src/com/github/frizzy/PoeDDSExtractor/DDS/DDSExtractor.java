package com.github.frizzy.PoeDDSExtractor.DDS;

import com.github.frizzy.PoeDDSExtractor.GGPKUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extracts all the textures from a dds file into their own png images into the same folder
 * the extracted dds file is located in.
 *
 * @author Frizzy
 * @version 0.0.1
 * @since 0.0.1
 * @deprecated
 */
public class DDSExtractor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger( DDSExtractor.class.getName( ) );

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
    public DDSExtractor( File txtFile , boolean overwrite ) {
        this.txtFile = txtFile;
        this.overwrite = overwrite;
    }

    public Map < File , List < File > > extractSubTextures ( Map < File, List < String > > wantedTextures ) {
        return extractSubTextures( wantedTextures, null );
    }

    public Map < File, List < File > > extractAllSubTextures ( List < File > extractedFiles ) {
        return extractAllSubTextures( extractedFiles, null );
    }

    /**
     * Extracts sub textures from the provided list of png files. The png files provided
     * should be files that were converted from .dds by DDSExtractor and the sub textures should have a path
     * in uiimages1.txt.
     * <br>
     * Returns a map with the key being the parent .png file the sub textures were extracted from and the value
     * list being all the sub textures. The extracted sub textures are stored in the same directory as the png file.
     *
     * @param wantedTextures A map containing files textures will be extracted from and the individual textures wanted from each file. <Br>
     *                       The key should be the png file the extraction process will work with.
     * @param outputPath A path that can be optionally specified to have all extracted textures saved to.
     */
    @SuppressWarnings( "all" ) //temp
    public Map < File, List < File > > extractSubTextures( Map < File, List < String > > wantedTextures, Path outputPath ) {
        Map < File, List < File > > allTextures = new HashMap <>( );

        Iterator < File > it = wantedTextures.keySet( ).iterator( );

        if ( wantedTextures != null && !wantedTextures.isEmpty( ) ) {

            while ( it.hasNext( ) ) {
                File pngFile = it.next( );
                List < String > textures = wantedTextures.get( pngFile );

                final String sourcePath = GGPKUtils.getSourcePath( pngFile );

                if ( !sourcePath.equals( "Unavailable" ) ) {
                    List < String > coordinateLines = getCoordinateLines( sourcePath );

                    /*
                     * THERES TOO MANY LOOPS, WHAT DO
                     * probably fine tho
                     */
                    for ( String line : coordinateLines ) {

                        for ( String textureName : textures ) {
                            if ( line.toLowerCase( ).contains( textureName.toLowerCase( ) ) ) {

                                int[] coords = GGPKUtils.getCoordinatesFrom( line );

                                Optional < File > opt = extract( coords[ 0 ] , coords[ 2 ] , coords[ 1 ] , coords[ 3 ] , pngFile , textureName, outputPath );
                                opt.ifPresentOrElse( extracted -> {
                                            if ( extracted.exists( ) ) {
                                                if ( !allTextures.containsKey( pngFile ) ) {
                                                    allTextures.put( pngFile , new ArrayList <>( ) );
                                                }

                                                allTextures.get( pngFile ).add( extracted );
                                            }
                                        } ,
                                        ( ) -> {
                                            LOGGER.log( Level.WARNING , "No file was returned. The sub texture was not extracted." );
                                        } );
                            }
                        }
                    }
                }
            }
        }

        return allTextures;
    }

    /**
     * Extracts all sub textures in each provided file. The extracted textures are stored in the
     * png file directory.
     * <br>
     * Returns a map with the key being the parent .png file the sub textures were extracted from and the value
     * list being all the sub textures from that png file.
     *
     * @param extractedFiles The list of .png files that will have textures extracted from.
     */
    public Map < File, List < File > > extractAllSubTextures( List < File > extractedFiles, Path output ) {
        Map < File, List < File > > allTextures = new HashMap <>( );

        if ( extractedFiles != null && !extractedFiles.isEmpty( ) ) {

            for ( File pngFile : extractedFiles ) {
                final String sourcePath = GGPKUtils.getSourcePath( pngFile );

                if ( !sourcePath.equals( "Unavailable" ) ) {
                    List < String > coordinateLines = getCoordinateLines( sourcePath );

                    for ( String line : coordinateLines ) {

                        String textureName = line.substring( 1 , line.indexOf( "\" " ) );

                        int[] coords = GGPKUtils.getCoordinatesFrom( line );

                        Optional < File > opt = extract( coords[ 0 ] , coords[ 2 ] , coords[ 1 ] , coords[ 3 ] , pngFile , textureName, output );
                        opt.ifPresentOrElse( extracted -> {
                                    if ( extracted.exists( ) ) {
                                        if ( !allTextures.containsKey( pngFile ) ) {
                                            allTextures.put( pngFile , new ArrayList <>( ) );
                                        } else {
                                            List < File > list = allTextures.get( pngFile );
                                            list.add( extracted );
                                        }
                                    }
                                } ,
                                ( ) -> {
                                    LOGGER.log( Level.WARNING , "No file was returned. The sub texture was not extracted." );
                                } );
                    }
                }
            }
        }

        return allTextures;
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

    /**
     * Retrieves the lines that contains the texture path/name, internal ggpk file path, and
     * x1, x2, y1, y2 coordinate information from the uiimages.txt file.
     * <br>
     * Any line containing the sourcePath means the converted .dds file contains that sub texture.
     */
    private List < String > getCoordinateLines( final String sourcePath ) {
        List < String > coordinateLines = new ArrayList <>( );

        try ( BufferedReader bReader = new BufferedReader( new InputStreamReader( new FileInputStream( txtFile ) , StandardCharsets.UTF_16LE ) ) ) {
            String line;
            while ( ( line = bReader.readLine( ) ) != null ) {
                if ( line.toLowerCase( ).contains( sourcePath.toLowerCase( ) ) ) {

                    if ( !coordinateLines.contains( line ) ) {
                        coordinateLines.add( line );
                    }
                }
            }
        } catch ( IOException | NullPointerException e ) {
            LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
        }

        return coordinateLines;
    }

//    /**
//     * Retrieves the content.gppk internal path of the dds file.
//     * The internal path would look something like this: <br>
//     * "Art/Textures/Interface/2D/2DArt/UIImages/Common/4K/3.dds"
//     */
//    private String getSourcePath( File ddsFile ) throws IOException {
//        File parent = ddsFile.getParentFile( );
//
//        if ( parent.isDirectory( ) ) {
//            return Files.readString( Path.of( parent.getAbsolutePath( ) + File.separator + "path.txt" ) );
//        }
//
//        return "Unavailable";
//    }
}

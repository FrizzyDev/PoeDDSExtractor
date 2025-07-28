package com.github.frizzy.PoeDDSExtractor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class to help with a couple of things in PoeDDSExtractor.
 *
 * @author Frizzy
 * @version 0.0.1
 * @since 0.0.1
 */
public class GPPKUtils {

    private static final Logger LOGGER = Logger.getLogger( GPPKUtils.class.getName() ) ;

    private GPPKUtils ( ) {

    }

    /**
     * Adds all the File keys to a list.
     */
    public static List < File > getFilesFromMap ( Map < File, List < String > > map ) {
        return new ArrayList <>( map.keySet( ) );
    }

    /**
     * Reads all lines from the provided uiimages.txt file and splits them into an array.
     * <br>
     * The first index is the texture name, the second index is the .dds file path in the gppk file,
     * the third index is the x1, x2, y1, y2 coordinates.
     */
    public static List < String[] > getAllLinesSplit ( File uiimagesTxt ) {
        List < String [ ] > lines = new ArrayList <>(  );

        try ( BufferedReader bReader = new BufferedReader( new InputStreamReader( new FileInputStream( uiimagesTxt ) , StandardCharsets.UTF_16LE ) ) ) {
            String line;
            while ( ( line = bReader.readLine( ) ) != null ) {
                String[] split = line.split( "\" " );
                split [ 0 ] = split [ 0 ].replace( "\"" , "" );
                split [ 1 ] = split [ 1 ].replace( "\"" , "" );

                lines.add( split );
            }
        } catch ( IOException | NullPointerException e ) {
            LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
        }

        return lines;
    }

    /**
     * Gets all the textures stored in the .dds file of the provided gppk path.
     */
    public static List < String > getAllTexturesFor ( File uiimagesTxt, String gppkDDSFilePath ) {
        List < String > textures = new ArrayList <>(  );

        try ( BufferedReader bReader = new BufferedReader( new InputStreamReader( new FileInputStream( uiimagesTxt ) , StandardCharsets.UTF_16LE ) ) ) {
            String line;
            while ( ( line = bReader.readLine( ) ) != null ) {
                if ( line.toLowerCase( ).contains( gppkDDSFilePath.toLowerCase(  ) ) ) {
                    String textureName = line.substring( 1 , line.indexOf( "\" " )  );

                    if ( !textures.contains( textureName ) ) {
                        textures.add( textureName );
                    }
                }
            }
        } catch ( IOException | NullPointerException e ) {
            LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
        }

        return textures;
    }

    /**
     * Gets a textures x,y coordinates from the provided uiimages.txt line.
     * <br>
     * The returned array layout is index 0 is the x1 coordinate, index 1 is y1, index 2 is x2, index 3 is y2.
     */
    public static int[] getCoordinatesFrom ( String uiimagesTxtLine ) {
        String coordinates = uiimagesTxtLine.substring( uiimagesTxtLine.lastIndexOf( "\"" ) + 2 );
        String[] split = coordinates.split( " " ); // i0 is x1, i1 is y1, i2 is x2, i3 is y2
        int[] coords = new int[4];

        coords [ 0 ] = Integer.parseInt( split[ 0 ] ); // x1
        coords [ 1 ] = Integer.parseInt( split[ 1 ] ); // y

        /*
         * If subtracting x1 from x2 or y1 from y2 is less than or equal to 0, then the x2 or y2 is likely
         * the same as x1 or y1.
         */
        if ( Integer.parseInt( split [ 2 ] ) - coords [ 0 ]  <= 0 ) {
            coords [ 2 ] = coords [ 0 ];
        } else {
            coords [ 2 ] = Integer.parseInt( split[ 2 ] ) - coords [ 0 ]; // x2
        }

        if ( Integer.parseInt( split [ 3 ] ) - coords [ 1 ]  <= 0 ) {
            coords [ 3 ] = coords [ 1 ];
        } else {
            coords [ 3 ] = Integer.parseInt( split[ 3 ] ) - coords [ 1 ]; // y2
        }

        return coords;
    }

    /**
     * In the event all interface .dds files are extracted into an output directory but no conversion or texture
     * extraction was done, this can be called to gather all the .dds files.
     * <br>
     * This method assumes the source path is the same as the output path when extracting all .dds files was done.
     */
    public static List < File > gatherDDSFrom ( Path source ) {
        List < File > ddsFiles = new ArrayList <>( );

        File output = source.toFile();

        if ( output.exists() ) {
            try {
                for ( File folder :  output.listFiles() ) {
                    if ( folder.isDirectory()  ) {

                        directoryLoop: {
                            for ( File file : folder.listFiles() ) {
                                if ( file.getName().endsWith( ".dds" ) ) {
                                    ddsFiles.add( file );
                                    break directoryLoop;
                                }
                            }
                        }
                    }
                }
            } catch ( NullPointerException e ) {
                LOGGER.log( Level.SEVERE, e.getMessage(), e );
            }
        }

        return ddsFiles;
    }

    /**
     * In the event all interface .dds files are extracted into an output directory but no conversion or texture
     * extraction was done, this can be called to gather all the .dds files.
     * <br>
     * This method assumes the source path is the same as the output path when extracting all .dds files was done.
     */
    public static List < File > gatherPNGFrom ( Path source ) {
        List < File > pngFiles = new ArrayList <>( );

        File output = source.toFile();

        if ( output.exists() ) {
            try {
                for ( File folder :  output.listFiles() ) {
                    if ( folder.isDirectory()  ) {

                        directoryLoop: {
                            for ( File file : folder.listFiles() ) {
                                if ( file.getName().endsWith( ".png" ) ) {
                                    pngFiles.add( file );
                                    break directoryLoop;
                                }
                            }
                        }
                    }
                }
            } catch ( NullPointerException e ) {
                LOGGER.log( Level.SEVERE, e.getMessage(), e );
            }
        }

        return pngFiles;
    }

    /**
     * Retrieves the content.gppk internal path of the dds file.
     * <br>
     * If the path.txt file is not found or an error occurred,
     * "Unavailable" is returned.
     * <br>
     * The internal path would look something like this:
     * <br>
     * "Art/Textures/Interface/2D/2DArt/UIImages/Common/4K/3.dds"
     */
    public static String getSourcePath( File ddsFile ) {
        File parent = ddsFile.getParentFile( );

        if ( parent.isDirectory( ) ) {
            try {
                return Files.readString( Path.of( parent.getAbsolutePath( ) + File.separator + "path.txt" ) );
            } catch ( IOException e ) {
                LOGGER.log( Level.SEVERE, e.getMessage(), e );
            }
        }

        return "Unavailable";
    }

}

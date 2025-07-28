package com.github.frizzy.PoeDDSExtractor.Testing;

import com.github.frizzy.PoeDDSExtractor.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main( String[] args ) throws IOException {
        getSpecificFiles();
    }

    public static void gatherPNGTest ( ) throws FileNotFoundException {
        String gppkLocation = "C:\\Users\\frizz\\OneDrive\\Desktop\\win-x64";
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Output" );
        GPPK gppk = new GPPK( gppkLocation, contentLocation, false, true );
        Optional < File >  opt = gppk.extractUIImagesTXT( outputLocation );

        opt.ifPresent( uiimagestxt -> {
            List < File > ddsFiles = GPPKUtils.gatherPNGFrom( outputLocation );
            Map < File , List < String > > allTextures = new HashMap <>(  );

            for ( File pngFile : ddsFiles ) {
                File txt = new File( pngFile.getParentFile().getAbsolutePath() + File.separator + "path.txt" );

                if ( txt.exists() ) {
                    try {
                        String path = Files.readString( txt.toPath() );
                        List < String > textures = GPPKUtils.getAllTexturesFor( uiimagestxt, path );

                        allTextures.put( pngFile, textures );

                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }

            DDSExtractor extractor = new DDSExtractor( uiimagestxt, false );
            Map < File , List < File > > allExtracted = extractor.extractSubTextures( allTextures );
        } );
    }

    public static void extractAllTest ( ) throws FileNotFoundException {
        String gppkLocation = "C:\\Users\\frizz\\OneDrive\\Desktop\\win-x64";
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Output" );
        Path convertBatLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\convert.bat" );
        Path texConvLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\texconv.exe" );

        GPPK gppk = new GPPK( gppkLocation, contentLocation, false, true );
        /*
         * Anytime you read the uiimages.txt file, make sure the encoding is set to StandardCharsets.UTF_16LE
         */
        Optional < File >  opt = gppk.extractUIImagesTXT( outputLocation );
        Map < File , List < String > > allTextures = new HashMap <>(  );

        opt.ifPresent( uiimagestxt -> {
            List < File > ddsFiles = GPPKUtils.gatherDDSFrom( outputLocation );
            DDSConverter converter = new DDSConverter( convertBatLocation, texConvLocation, false );
            List < File> convertedDDSFiles = converter.convert( ddsFiles );

            for ( File pngFile : convertedDDSFiles ) {
                File txt = new File( pngFile.getParentFile().getAbsolutePath() + File.separator + "path.txt" );

                if ( txt.exists() ) {
                    try {
                        String path = Files.readString( txt.toPath() );
                        List < String > textures = GPPKUtils.getAllTexturesFor( uiimagestxt, path );

                        allTextures.put( pngFile, textures );
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }

            DDSExtractor extractor = new DDSExtractor(  uiimagestxt , false );
            extractor.extractSubTextures( allTextures );
        } );
    }

    public static void gatherDDSTest ( ) throws FileNotFoundException {
        String gppkLocation = "C:\\Users\\frizz\\OneDrive\\Desktop\\win-x64";
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Output" );
        GPPK gppk = new GPPK( gppkLocation, contentLocation, false, true );
        Optional < File >  opt = gppk.extractUIImagesTXT(  outputLocation );

        opt.ifPresent( uiimagestxt -> {
            List < File > ddsFiles = GPPKUtils.gatherDDSFrom( outputLocation );
            Map < File , List < String > > allTextures = new HashMap <>(  );

            int ddsCounter = 0;
            int textureCounter = 0;

            for ( File ddsFile : ddsFiles ) {
                File txt = new File( ddsFile.getParentFile().getAbsolutePath() + File.separator + "path.txt" );

                if ( txt.exists() ) {
                    try {
                        String path = Files.readString( txt.toPath() );
                        List < String > textures = GPPKUtils.getAllTexturesFor( uiimagestxt, path );

                        allTextures.put( ddsFile, textures );

                        ddsCounter++;
                        textureCounter = textureCounter + textures.size();
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println( ddsCounter + " .dds files extracted with a total of: " + textureCounter + " textures waiting to be extracted." );
        } );
    }

    /**
     * Extracts all .dds files, converts all files, then extracts all textures from each png file.
     * This can take a few hours.
     * @throws FileNotFoundException
     */
    public static void allTest ( ) throws FileNotFoundException {
        long startTime = System.nanoTime( );

        /*
         * Replace the wanted files with the paths of whatever you want to extract, and the source
         * directories below where the required files are.
         */
        String gppkLocation = "C:\\Users\\frizz\\OneDrive\\Desktop\\win-x64";
        Path convertBatLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\convert.bat" );
        Path texConvLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\texconv.exe" );
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Test" );

        ToolsUnpacker up = new ToolsUnpacker();
        up.exportBat( convertBatLocation );
        System.exit( 0 );

        GPPK gppk = new GPPK( gppkLocation, contentLocation, false, true );
        Optional < File > opt = gppk.extractUIImagesTXT( outputLocation );
        opt.ifPresent( uiimagestxt -> {
            gppk.setOverwrite( false );
            Map < File, List < String > > allTextures = gppk.extractEverything( outputLocation, uiimagestxt );
            DDSConverter converter = new DDSConverter( convertBatLocation, texConvLocation, false );
            converter.convert( GPPKUtils.getFilesFromMap( allTextures ) );
            DDSExtractor extractor = new DDSExtractor( uiimagestxt, false  );
            extractor.extractSubTextures( allTextures );
        } );

        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        System.out.println( "Extract everything took: " + getHumanReadableTime( elapsedTime ) );
    }

    public static void getSpecificFiles( ) {
        long startTime = System.nanoTime( );

        /*
         * Replace the wanted files with the paths of whatever you want to extract, and the source
         * directories below where the required files are.
         */
        List < String > wantedFiles = buildWantedFiles( );
        String gppkLocation = "C:\\Users\\frizz\\OneDrive\\Desktop\\win-x64";
        Path convertBatLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\convert.bat" );
        Path texConvLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\texconv.exe" );
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Test " );

        try {
            GPPK gppk = new GPPK( gppkLocation, contentLocation, false, true );
            DDSConverter converter = new DDSConverter( convertBatLocation , texConvLocation, false);

            List < File > extractedFiles = gppk.extract( outputLocation , wantedFiles );
            List < File > converted = converter.convert( extractedFiles );

            Optional < File > uiimagesTxt = gppk.extractUIImagesTXT( outputLocation );

            uiimagesTxt.ifPresentOrElse( txtFile -> {
                DDSExtractor extractor = new DDSExtractor( txtFile , false );
                Map < File, List < String > > wantedTextures = getWantedTexturesFor( converted );

                Map < File, List < File > > allTextures = extractor.extractSubTextures( wantedTextures );

                //do what you want with textures here
            }, ( ) -> {
                //Log or something
            } );


        } catch ( FileNotFoundException e ) {
            e.printStackTrace( );
        }

        long endTime = System.nanoTime( );
        long elapsedTime = endTime - startTime;

        System.out.println( "Total execution time for " + wantedFiles.size() + " .dds files and all sub textures extracted: " );
        System.out.println( getHumanReadableTime( elapsedTime ) );
    }

    public static Map < File, List < String > > getWantedTexturesFor ( List < File > pngFiles ) {
        WantedTextureBuilder builder = new WantedTextureBuilder( pngFiles  );
        try {
            return builder.buildWantedTextures();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    public static List < String > buildWantedFiles( ) {
        List < String > wantedFiles = new ArrayList <>( );

        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/common/4k/1.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/common/4k/2.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/common/4k/3.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/4k/6.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/4k/11.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/1.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/2.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/3.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/4.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/5.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/7.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/13.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/10.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/15.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/16.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/17.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/marketplace/1.dds" );
        wantedFiles.add( "art/textures/interface/2d/2dart/uiimages/ingame/marketplace/2.dds" );


        return wantedFiles;
    }

    private static String getHumanReadableTime(long nanos) {
        TimeUnit unitToPrint = null;
        String result = "";
        long rest = nanos;
        for(TimeUnit t: TimeUnit.values()) {
            if (unitToPrint == null) {
                unitToPrint = t;
                continue;
            }
            // convert 1 of "t" to "unitToPrint", to get the conversion factor
            long factor = unitToPrint.convert(1, t);
            long value = rest % factor;
            rest /= factor;

            result = value + " " + unitToPrint + " " + result;

            unitToPrint = t;
            if (rest == 0) {
                break;
            }
        }
        if (rest != 0) {
            result = rest + " " + unitToPrint + " " + result;
        }

        return result.trim();
    }
}

//art_textures_interface_2d_2dart_uiimages_common_4k_1.dds
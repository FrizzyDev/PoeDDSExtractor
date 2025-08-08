package com.github.frizzy.PoeDDSExtractor.Testing;

import com.github.frizzy.PoeDDSExtractor.*;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Test {

    public static void main( String[] args ) throws IOException, GGPKException {
        String ggpkLocation = "C:\\Users\\frizz\\OneDrive\\Desktop\\LibGGPK3";
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Test" );
        GGPK2 ggpk = new GGPK2( ggpkLocation, contentLocation, false, true );

        ggpk.extractUIImagesTXT( outputLocation );
    }

    public static void testBank ( ) throws FileNotFoundException, GGPKException {
        String ggpkLocation = "C:\\Users\\frizz\\OneDrive\\Desktop\\LibGGPK3";
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing" );

        GGPK2 ggpk = new GGPK2( ggpkLocation, contentLocation, false, true );
        List < BankFile > bankFiles = ggpk.extractBank( outputLocation, buildWantedBanks() );

        BankExtractor extractor = new BankExtractor("C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing"  );
        bankFiles = extractor.extractWavFiles( bankFiles );

        for ( BankFile bf : bankFiles ) {
            System.out.println( "Path: " + bf.getPath() );
            System.out.println( "File: " + bf.getFile().getAbsolutePath() );

            for ( File wf : bf.getWavFiles() ) {
                System.out.println( "Wav File: " + wf.getAbsolutePath() );
            }
        }
    }

    private static List < String > buildWantedBanks ( ) {
        List < String > list = new ArrayList <>(  );

        list.add( "FMOD/Desktop/UI_General.bank" );
//        list.add( "FMOD/Desktop/UI_Hideout.bank" );
//        list.add( "FMOD/Desktop/UI_Party.bank" );
//        list.add( "FMOD/Desktop/UI_Trade.bank" );

        return list;
    }

    public void testGGPK2 ( ) throws IOException, GGPKException {
        String ggpkLocation = "C:\\Users\\frizz\\OneDrive\\Desktop\\LibGGPK3";
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Test" );
        Path convertBatLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\convert.bat" );
        Path texConvLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\texconv.exe" );

        GGPK2 ggpk = new GGPK2( ggpkLocation, contentLocation, false, true );

        List < DDSFile > ddsFiles = ggpk.extractDDS( outputLocation, buildWantedFiles() );

        WantedTextureBuilder builder = new WantedTextureBuilder( ggpk.getuiImagesTxtFile() );
        ddsFiles = builder.buildWantedTextures( ddsFiles );

        DDSConverter2 converter = new DDSConverter2( convertBatLocation, texConvLocation, true );

        ddsFiles = converter.convert( ddsFiles );

        DDSExtractor2 extractor = new DDSExtractor2( ggpk.getuiImagesTxtFile(), true  );
        ddsFiles = extractor.extractSubTextures( ddsFiles );

        for ( DDSFile dFile : ddsFiles ) {

            System.out.println( "DDSFile path: " + dFile.getDdsPath() );
            System.out.println( "DDSFile disk path: " + dFile.getFile().getAbsolutePath() );
            System.out.println( "PNG file disk path: " + dFile.getPngFile().getAbsolutePath() );
            System.out.println( "DDS File textures: " );

            for ( Texture t : dFile.getUnextractedTextures() ) {
                System.out.println( "Texture name: " + t.name() );
                System.out.println( "Texture path: " + t.path() );
                System.out.println( "Texture Coordinates: " + Arrays.toString( t.coordinates() ) );
                System.out.println( "----------------------------------------------------------" );
            }

            System.out.println( "DDS Extracted textures: " );

            for ( File f : dFile.getExtractedTextures() ) {
                System.out.println( "Texture path: " + f.getAbsolutePath() );
            }
        }
    }

    public static void testAudio ( ) throws IOException {
        final String bmsExePath = "C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing\\quickbms.exe";
        final String bmsScriptPath = "C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing\\Script.bms";
        final String fmodExtractPath = "C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing\\fsb_aud_extr.exe";
        final String fsbPath = "C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing\\00000000.fsb";
        //final String command = "for %i in (\"C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing\"*.fsb) do fsb_aud_extr.exe \"%i\"";

        CommandLine cmdLine = new CommandLine( bmsExePath );

        cmdLine.addArgument( bmsScriptPath );
        cmdLine.addArgument( "C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing\\UI_General.bank" );
        cmdLine.addArgument( "C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing" );

        DefaultExecutor executor = DefaultExecutor.builder().get();
        int code = executor.execute( cmdLine );

        CommandLine cmdLine2 = new CommandLine( fmodExtractPath );

        cmdLine2.addArgument( fsbPath );
        cmdLine2.addArgument( "C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing\\Output" );


//        CommandLine cmdLine2 = new CommandLine( "cmd.exe" );
//        cmdLine2.addArgument( "/C \"\"C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing\\batch.bat\"\"", false );
//
//        DefaultExecutor executor1 = DefaultExecutor.builder().get();
//
//        int code2 = executor1.execute( cmdLine2 );

    }

    public static void gatherPNGTest ( ) throws FileNotFoundException {
        String gppkLocation = "C:\\Users\\frizz\\OneDrive\\Desktop\\win-x64";
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Output" );
        GGPK gppk = new GGPK( gppkLocation, contentLocation, false, true );
        Optional < File >  opt = gppk.extractUIImagesTXT( outputLocation );

        opt.ifPresent( uiimagestxt -> {
            List < File > ddsFiles = GGPKUtils.gatherPNGFrom( outputLocation );
            Map < File , List < String > > allTextures = new HashMap <>(  );

            for ( File pngFile : ddsFiles ) {
                File txt = new File( pngFile.getParentFile().getAbsolutePath() + File.separator + "path.txt" );

                if ( txt.exists() ) {
                    try {
                        String path = Files.readString( txt.toPath() );
                        List < String > textures = GGPKUtils.getAllTexturesFor1( uiimagestxt, path );

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

        GGPK gppk = new GGPK( gppkLocation, contentLocation, false, true );
        /*
         * Anytime you read the uiimages.txt file, make sure the encoding is set to StandardCharsets.UTF_16LE
         */
        Optional < File >  opt = gppk.extractUIImagesTXT( outputLocation );
        Map < File , List < String > > allTextures = new HashMap <>(  );

        opt.ifPresent( uiimagestxt -> {
            List < File > ddsFiles = GGPKUtils.gatherDDSFrom( outputLocation );
            DDSConverter converter = new DDSConverter( convertBatLocation, texConvLocation, false );
            List < File> convertedDDSFiles = converter.convert( ddsFiles );

            for ( File pngFile : convertedDDSFiles ) {
                File txt = new File( pngFile.getParentFile().getAbsolutePath() + File.separator + "path.txt" );

                if ( txt.exists() ) {
                    try {
                        String path = Files.readString( txt.toPath() );
                        List < String > textures = GGPKUtils.getAllTexturesFor1( uiimagestxt, path );

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
        GGPK gppk = new GGPK( gppkLocation, contentLocation, false, true );
        Optional < File >  opt = gppk.extractUIImagesTXT(  outputLocation );

        opt.ifPresent( uiimagestxt -> {
            List < File > ddsFiles = GGPKUtils.gatherDDSFrom( outputLocation );
            Map < File , List < String > > allTextures = new HashMap <>(  );

            int ddsCounter = 0;
            int textureCounter = 0;

            for ( File ddsFile : ddsFiles ) {
                File txt = new File( ddsFile.getParentFile().getAbsolutePath() + File.separator + "path.txt" );

                if ( txt.exists() ) {
                    try {
                        String path = Files.readString( txt.toPath() );
                        List < String > textures = GGPKUtils.getAllTexturesFor1( uiimagestxt, path );

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

        GGPK gppk = new GGPK( gppkLocation, contentLocation, false, true );
        Optional < File > opt = gppk.extractUIImagesTXT( outputLocation );
        opt.ifPresent( uiimagestxt -> {
            gppk.setOverwrite( false );
            Map < File, List < String > > allTextures = gppk.extractEverything( outputLocation, uiimagestxt );
            DDSConverter converter = new DDSConverter( convertBatLocation, texConvLocation, false );
            converter.convert( GGPKUtils.getFilesFromMap( allTextures ) );
            DDSExtractor extractor = new DDSExtractor( uiimagestxt, false  );
            extractor.extractSubTextures( allTextures );
        } );

        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        System.out.println( "Extract everything took: " + getHumanReadableTime( elapsedTime ) );
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
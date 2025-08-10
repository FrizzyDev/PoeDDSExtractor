package com.github.frizzy.PoeDDSExtractor.Testing;

import com.github.frizzy.PoeDDSExtractor.*;
import com.github.frizzy.PoeDDSExtractor.Bank.BankExtractor;
import com.github.frizzy.PoeDDSExtractor.Bank.BankFile;
import com.github.frizzy.PoeDDSExtractor.DDS.DDSConverter2;
import com.github.frizzy.PoeDDSExtractor.DDS.DDSExtractor2;
import com.github.frizzy.PoeDDSExtractor.DDS.DDSFile;
import com.github.frizzy.PoeDDSExtractor.DDS.Texture;
import com.github.frizzy.PoeDDSExtractor.Exception.GGPKException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings( "all" )
public class Test {

    public static void main( String[] args ) throws IOException, GGPKException, InterruptedException {
        Path ggpkLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\LibGGPK3\\" );
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Test" );

        GGPK2 ggpk = new GGPK2( ggpkLocation, contentLocation, false, true );
    }

    public static void testBank ( ) throws FileNotFoundException, GGPKException {
        Path ggpkLocation = Path.of( "C:\\Users\\frizz\\OneDrive\\Desktop\\LibGGPK3" );
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing" );

        GGPK2 ggpk = new GGPK2( ggpkLocation, contentLocation, false, true );
        List < BankFile > bankFiles = ggpk.extractBank( outputLocation, buildWantedBanks() );

        BankExtractor extractor = new BankExtractor("C:\\Users\\frizz\\Documents\\GGGFiles\\Bank testing"  );
        bankFiles = extractor.extractWavFiles( bankFiles );

        for ( BankFile bf : bankFiles ) {
            System.out.println( "Path: " + bf.getPath() );
            System.out.println( "File: " + bf.getDiskPath().toAbsolutePath() );

            for ( Path wf : bf.getWavFiles() ) {
                System.out.println( "Wav File: " + wf.toAbsolutePath() );
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

    public static void testGGPK2( ) throws IOException, GGPKException {
        Path ggpkLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\LibGGPK3\\" );
        Path contentLocation = Path.of( "C:\\Program Files (x86)\\Grinding Gear Games\\Path of Exile\\Content.ggpk" );
        Path outputLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\Test" );
        Path convertBatLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\convert.bat" );
        Path texConvLocation = Path.of( "C:\\Users\\frizz\\Documents\\GGGFiles\\texconv.exe" );

        GGPK2 ggpk = new GGPK2( ggpkLocation, contentLocation, false, false );

        List < DDSFile > ddsFiles = ggpk.extractDDS( outputLocation, buildWantedFiles() );

        WantedTextureBuilder builder = new WantedTextureBuilder( ggpk.getuiImagesTxtFile() );
        ddsFiles = builder.buildWantedTextures( ddsFiles );

        DDSConverter2 converter = new DDSConverter2( convertBatLocation, texConvLocation, true );

        ddsFiles = converter.convert( ddsFiles );

        DDSExtractor2 extractor = new DDSExtractor2( ggpk.getuiImagesTxtFile(), true  );
        ddsFiles = extractor.extractSubTextures( ddsFiles );

        for ( DDSFile dFile : ddsFiles ) {

            System.out.println( "DDSFile path: " + dFile.getDdsPath() );
            System.out.println( "DDSFile disk path: " + dFile.getDiskPath().toAbsolutePath() );
            System.out.println( "PNG file disk path: " + dFile.getPNGPath().toAbsolutePath() );
            System.out.println( "DDS File textures: " );

            for ( Texture t : dFile.getUnextractedTextures() ) {
                System.out.println( "Texture name: " + t.name() );
                System.out.println( "Texture path: " + t.path() );
                System.out.println( "Texture Coordinates: " + Arrays.toString( t.coordinates() ) );
                System.out.println( "----------------------------------------------------------" );
            }

            System.out.println( "DDS Extracted textures: " );

            for ( Path p : dFile.getExtractedTextures() ) {
                System.out.println( "Texture path: " + p.toAbsolutePath() );
            }
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
package com.github.frizzy.PoeDDSExtractor;

import com.github.frizzy.PoeDDSExtractor.Bank.BankFile;
import com.github.frizzy.PoeDDSExtractor.Command.CommandArg;
import com.github.frizzy.PoeDDSExtractor.Command.CommandPair;
import com.github.frizzy.PoeDDSExtractor.DDS.DDSFile;
import com.github.frizzy.PoeDDSExtractor.DDS.Texture;
import com.github.frizzy.PoeDDSExtractor.Exception.GGPKException;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * GPPK builds the command line process to extract the .dds files from the PoE bundle
 * archives. Once the .dds files needed are extracted, class Extractor can be used to
 * extract the wanted textures.
 * <br>
 * As of right now, only stand alone PoE clients work with this process, until the command line
 * tool is updated to process steam distros.
 *
 * @author Frizzy
 * @version 0.0.2
 * @since 0.0.2
 */
public class GGPK2 {

    private static final Logger LOGGER = Logger.getLogger( GGPK2.class.getPackageName( ) );

    /**
     * The path of the uiimages.txt file within the content.gppk file.
     * This file contains texture names and what .dds file it is stored in,
     * and contains the x1, x2, y1, y2 coordinates to successfully extract
     * the textures.
     */
    static final String UIIMAGES_TXT_LOC = "art/uiimages1.txt";

    static final String UIDIVINATION_TXT_LOC = "art/uidivinationimages.txt";

    /**
     * Previously, two different tools were needed for certain extraction proceses.
     * <br>
     * ExtractGGPK.exe has been updated to handle .dds files and bank files, and
     * process Content.ggpk or _.index.bin files.
     * <br>
     * I would note that ExtractGGPK.exe is not an exe/dll retrieved from the LibGGPK3
     * library, but something I wrote to streamline the process a bit better.
     */
    @SuppressWarnings( "unused" )
    @Deprecated
    static final String BUNDLED_GGPK_TOOL = "ExtractBundledGGPK3.exe";

    static final String GGPK_TOOL = "ExtractGGPK.exe";

    /**
     * The File object of ExtractGGPK3.exe.
     * This is used to extract bank files or files not stored
     * within the internal bundles, such as a .bank file.
     */
    private final Path extractGGPKexe;

    /**
     * Path to the uiimages.txt file on disk.
     */
    private final Path uiImagesDiskPath;

    /**
     * Path to the uidivinationimages.txt file on disk.
     */
    private final Path uiDivinationImagesDiskPath;

    /**
     * The path to the Content.gppk file.
     */
    private final Path contentPath;

    /**
     * Boolean flag determining if previously extracted .dds files should be overwritten.
     * <br>
     * Setting this to true has a huge performance improvement. Unless your extracted .dds files
     * are corrupted or modified, and you want to replace them, I would always set this to true.
     */
    private boolean overwrite;

    /**
     * @param ggpkPath    Path to the directory containing the LibGPPK tools.
     * @param contentPath Path to the content.gppk file.
     * @param overwrite   Determines if previously extracted files should be overwritten.
     */
    public GGPK2( final Path ggpkPath , Path contentPath, boolean overwrite ) throws FileNotFoundException, GGPKException {
        if ( !Files.exists( contentPath ) )
            throw new FileNotFoundException( "Content.gppk was not found" );

        this.contentPath = contentPath;
        this.overwrite = overwrite;

        Optional < Path > egOpt = getGPPKExe( ggpkPath );

        extractGGPKexe = egOpt.orElseThrow( ( ) -> new FileNotFoundException( "ExtractGGPK3.exe was not found." ) );

        uiImagesDiskPath = extractUIImagesTXT( ggpkPath )
                .orElseThrow( ( ) ->
                        new GGPKException( "uiimages.txt was not returned. GGPK2 extraction processes will not function." ) );

        uiDivinationImagesDiskPath = extractUIDivinationImagesTXT( ggpkPath )
                .orElseThrow( ( ) ->
                        new GGPKException( "uidivinationimages.txt was not returned. GGPK2 extraction processes will not function." ) );
    }

    /**
     * Adds a handler to the logger.
     */
    public void addLoggerHandler( Handler handler ) {
        LOGGER.addHandler( handler );
    }

    /**
     * Returns the uiimagestxt file.
     */
    public Path getuiImagesTxtFile( ) {
        return uiImagesDiskPath;
    }

    /**
     * Returns the uidivinationtxt file.
     */
    public Path getUiDivinationTxtFile( ) {
        return uiDivinationImagesDiskPath;
    }

    /**
     * If previously extracted files should be overwritten.
     */
    public void setOverwrite( boolean overwrite ) {
        this.overwrite = overwrite;
    }

    /**
     * Extracts the wanted .bank files and returns them in a list.
     */
    public List < BankFile > extractBank( final Path outputPath , List < String > wantedBanks ) {
        List < BankFile > extracted = new ArrayList <>( );
        File contentGGPK = contentPath.toFile( );
        File outputDirectory = outputPath.toFile( );

        if ( ( !contentGGPK.exists( ) || !outputDirectory.exists( ) ) ) {
            LOGGER.log( Level.WARNING , "One of the files provided does not exist." );
            return extracted;
        }

        if ( ( !contentGGPK.isFile( ) || !outputDirectory.isDirectory( ) ) ) {
            LOGGER.log( Level.WARNING , "One of the files provided is not of the correct type." );
            return extracted;
        }

        if ( wantedBanks.isEmpty( ) ) {
            LOGGER.log( Level.WARNING , "Wanted files list is empty. Extraction operation will not continue." );
            return extracted;
        }

        for ( String wb : wantedBanks ) {
            try {

                if ( contentPath.toString( ).endsWith( "ggpk" ) ) {
                    Optional < ? > opt = extractContentFile( outputPath , wb );

                    opt.ifPresentOrElse( a -> {
                        if ( a instanceof BankFile bf ) {
                            extracted.add( bf );
                        }
                    } , ( ) -> {

                    } );
                } else if ( contentPath.toString( ).endsWith( ".bin" ) ) {
                    File parent = contentGGPK.getParentFile( );

                    if ( parent.isDirectory( ) && parent.getName( ).equalsIgnoreCase( "Path of Exile" ) ) {
                        File bankRef = new File( parent.getAbsolutePath( ) + File.separator + wb.replaceAll( "/" , "\"" ) );

                        if ( bankRef.exists( ) ) {
                            final Path newPath = Path.of( outputPath + File.separator + bankRef.getName( ) );

                            Files.move( bankRef.toPath( ) , newPath , overwrite ? StandardCopyOption.REPLACE_EXISTING : StandardCopyOption.ATOMIC_MOVE );
                            BankFile bankFile = new BankFile( wb , newPath );
                            extracted.add( bankFile );
                        }
                    }
                }

            } catch ( IOException e ) {
                LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
            }
        }

        return extracted;
    }

    /**
     * Extracts all the wanted files from the Content.gppk file and saves them to the specified directory.
     * <br>
     * The function will iterate through the provided list and locate the .dds files one by one. The returned list
     * contains each extracted file to be easily passed to the DDSConverter and then finally the DDSExtractor to complete
     * the entire process.
     * <br>
     * If overwrite has been set to false, any previously extracted .dds files will be returned.
     * <br>
     * As a side note, LibGGPK3 is not thread-safe. Do not execute this function on more than one thread.
     *
     * @param outputPath  The output directory where extracted .dds files and related files will be extracted to.
     * @param wantedFiles The internal content.gppk file paths for the wanted .dds files.
     * @return A list of the extracted .dds files.
     */
    public List < DDSFile > extractDDS( final Path outputPath , List < String > wantedFiles ) throws GGPKException {
        if ( uiImagesDiskPath == null || uiDivinationImagesDiskPath == null )
            throw new GGPKException( "ui txt file is null. Extraction processes cannot continue." );

        /*
         * The directories containing the extracted .dds files.
         * Extracted .dds files will be stored in their own directory
         * named after the path to that .dds file within the content.gppk archive.
         * This is done this way because once the dds extraction finishes, the individual textures
         * will be extracted into the same folder.
         */
        List < DDSFile > extracted = new ArrayList <>( );
        File contentGPPK = contentPath.toFile( );
        File outputDirectory = outputPath.toFile( );

        if ( ( !contentGPPK.exists( ) || !outputDirectory.exists( ) ) ) {
            LOGGER.log( Level.WARNING , "One of the files provided does not exist." );
            return extracted;
        }

        if ( ( !contentGPPK.isFile( ) || !outputDirectory.isDirectory( ) ) ) {
            LOGGER.log( Level.WARNING , "One of the files provided is not of the correct type." );
            return extracted;
        }

        if ( wantedFiles.isEmpty( ) ) {
            LOGGER.log( Level.WARNING , "Wanted files list is empty. Extraction operation will not continue." );
            return extracted;
        }

        for ( String wf : wantedFiles ) {
            try {
                Optional < ? > ef = extractContentFile( outputPath , wf );

                ef.ifPresentOrElse( a -> {
                    if ( a instanceof DDSFile df ) {
                        extracted.add( df );
                    }
                } , ( ) -> {
                    LOGGER.log( Level.WARNING , "No file was returned. File for: " + wf + " was not extracted." );
                } );
            } catch ( IOException e ) {
                LOGGER.log( Level.SEVERE , e.getMessage( ) );
            }
        }

        return extracted;
    }

    /**
     * Extracts all interface .dds files from the Content.ggpk file, returning everything extracted in a list. The DDSFile
     * values contain the internal path of the .dds file, a list of textures the file contains, and a java.io.File
     * reference to find the .dds file on disk.
     */
    public List < DDSFile > extractEverythingDDS( final Path outputPath ) throws GGPKException {
        if ( uiImagesDiskPath == null || uiDivinationImagesDiskPath == null )
            throw new GGPKException( "ui txt file is null. Extraction processes cannot continue." );

        List < DDSFile > allFiles = new ArrayList <>( );

        List < String[] > gppkFiles = GGPKUtils.getAllLinesSplit( uiImagesDiskPath );
        /*
         * Validation list tracks what .dds files have already been extracted, as the uiimages.txt will
         * have the .dds file paths referenced multiple times.
         */
        List < String > validation = new ArrayList <>( );

        for ( String[] array : gppkFiles ) {
            String internalPath = array[ 1 ];

            if ( !validation.contains( internalPath ) ) {
                try {
                    Optional < ? > opt = extractContentFile( outputPath , internalPath );

                    opt.ifPresentOrElse( extracted -> {
                        if ( extracted instanceof DDSFile df ) {
                            allFiles.add( df );
                        }

                    } , ( ) -> {
                        LOGGER.log( Level.WARNING , "Optional returned empty for: " + internalPath +
                                "\nSome uiimages.txt paths do not produce a .dds file. Validate this is accurate." +
                                "\nThe create directory process could have failed as well." );
                    } );

                    validation.add( internalPath );
                } catch ( IOException e ) {
                    LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
                }
            }
        }

        return allFiles;
    }

    /**
     * Convenience method to extract the uiimages.txt file.
     * <br>
     * This can throw a NoSuchFileException if the uiimages.txt file was not extracted, as the entirety of the
     * extraction/conversion process can not continue without this file.
     *
     * @param outputPath The path the uiimages.txt file will be extracted to.
     */
    public Optional < Path > extractUIImagesTXT( Path outputPath ) {
        try {
            Optional < Path > opt = extractTextFile( outputPath , UIIMAGES_TXT_LOC );
            opt.orElseThrow( ( ) -> new ExecuteException( "uiimages.txt file was not returned. This likely means the extract tool" +
                    " encountered an error. Check if you do not have the Content.ggpk file already opened in another tool." , -1 ) );

            return opt;
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
        }

        return Optional.empty( );
    }

    /**
     * Convenience method to extract the uidivinationimages.txt file.
     * <br>
     * This can throw a NoSuchFileException if the uidivinationimages.txt file was not extracted, as the entirety
     * of the extraction/Conversion process can not continue without this file.
     *
     * @param outputPath The path the uidivinationimages.txt file will be extracted to.
     */
    public Optional < Path > extractUIDivinationImagesTXT( Path outputPath ) {

        try {
            Optional < Path > opt = extractTextFile( outputPath , UIDIVINATION_TXT_LOC );
            opt.orElseThrow( ( ) -> new ExecuteException( "uidivinationimages.txt file was not returned. This likely means the extract tool" +
                    " encountered an error. Check if you do not have the Content.ggpk file already opened in another tool." , -1 ) );

            return opt;
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
        }

        return Optional.empty( );
    }

    /**
     * Finishes the extraction process for extracting the uiimages.txt or uidivinationimages.txt file.
     *
     * @param outputPath The directory the .txt file will be extracted to.
     * @param wantedFile Which .txt file is requested, either the uiimages.txt or uidivinationimages.txt file.
     * @return
     * @throws IOException
     */
    private Optional < Path > extractTextFile( Path outputPath , String wantedFile ) throws IOException {
        if ( GGPKUtils.testLock( contentPath ) ) {
            throw new IOException( "Content.ggpk file is locked. Check to see if another tool is currently using the file." );
        }

        String temp = wantedFile.replaceAll( "/" , "_" ).replace( ".txt" , "" );
        Path outputDir = Path.of ( outputPath.toString( ) + File.separator + temp );

        Path potentialCur = Path.of( outputDir + File.separator + Path.of( wantedFile.substring( wantedFile.lastIndexOf( "/" ) ) ) );
        if ( Files.exists( potentialCur ) && !overwrite ) {
            return Optional.of( potentialCur );
        } else if ( Files.exists( potentialCur ) && overwrite ) {
            boolean deleted = Files.deleteIfExists( potentialCur );
            LOGGER.log( Level.INFO, "Deletion success of: " + potentialCur + " - " + deleted );
        }

        final String subbed = wantedFile.substring( wantedFile.lastIndexOf( "/" ) + 1 );

        if ( ( overwrite && Files.exists( outputDir ) ) || !Files.exists( outputDir ) ) {
            if ( !Files.exists( outputDir ) ) {
                Path created = Files.createDirectory( outputDir );

                if ( !Files.exists( created ) && !overwrite ) {
                    LOGGER.log( Level.WARNING , "Output directory for wanted file could not be created. Operation will not continue." );
                    return Optional.empty( );
                }
            }

            CommandPair < DefaultExecuteResultHandler, ByteArrayOutputStream > cPair = GGPKUtils.runCommandLine(
                    extractGGPKexe, new CommandArg <>( contentPath.toString( ) , true ),
                    new CommandArg <>( wantedFile, true ), new CommandArg <> ( outputDir.toString() , true ) ) ;

            int exitValue = cPair.rh.getExitValue();
            LOGGER.log( Level.INFO , cPair.bs.toString() );

            if ( exitValue == 0 ) {
                try ( Stream < Path > paths = Files.list( outputDir ) ) {
                    List < Path > pathList = paths.toList();

                    for ( Path p : pathList ) {
                        if ( p.getFileName().toString().equalsIgnoreCase( subbed ) ) {
                            LOGGER.log( Level.INFO , "FILE SUCCESSFULLY EXTRACTED: " + p.toAbsolutePath() );
                            return Optional.of( p );
                        }
                    }

                } catch ( IOException e ) {
                    LOGGER.log( Level.SEVERE, e.getMessage(), e );
                }
            } else {
                ExecuteException ee = cPair.rh.getException();
                LOGGER.log( Level.SEVERE , ee.getMessage( ) , ee );
            }
        }

        return Optional.empty( );
    }

    /**
     * Extracts the wanted file from the Content.gppk archive.
     * <br>
     * Extracted files are stored in a folder named after the extracted file within the output path.
     *
     * @param outputPath The directory that will hold the extracted files.
     * @param wantedFile The internal .dds file wanted.
     */
    private Optional < ? > extractContentFile( Path outputPath , String wantedFile ) throws IOException {
        if ( GGPKUtils.testLock( contentPath ) ) {
            throw new IOException( "Content.ggpk file is locked. Check to see if another tool is currently using the file." );
        }

        /*
         * We name the output directory to the ggpk archive path of the file.
         * This is probably temporary.
         * If someone has a better naming convention for the folders of the extracted files, I am all ears.
         * I wanted something relatively identifiable, but I suppose it is not strictly necessary.
         */
        String temp = "";
        Path outputDir = null;

        String extension = FilenameUtils.getExtension( wantedFile );
        if ( extension.equalsIgnoreCase( "dds" ) || extension.equalsIgnoreCase( "txt" ) ) {
            String sub = wantedFile.replaceAll( "/" , "_" );

            temp = extension.equalsIgnoreCase( "dds" ) ?
                    sub.replace( ".dds" , "" ) : sub.replace( ".txt" , "" );

            outputDir = Path.of( outputPath.toString( ) + File.separator + temp );
        } else if ( extension.equalsIgnoreCase( "bank" ) ) {
            outputDir = outputPath;
        }

        if ( outputDir == null ) {
            LOGGER.log( Level.WARNING , "Output Directory is null." );
            return Optional.empty( );
        }

        final String subbed = wantedFile.substring( wantedFile.lastIndexOf( "/" ) + 1 );
        final Path path = Path.of( outputDir + File.separator + subbed + "_path.txt" );

        if ( ( overwrite && Files.exists( outputDir ) ) || !Files.exists( outputDir ) || extension.equalsIgnoreCase( "bank" ) ) {
            if ( !wantedFile.contains( ".bank" ) && !Files.exists( outputDir ) ) {
                Path created = Files.createDirectory( outputDir );

                if ( !Files.exists( created ) && !overwrite ) {
                    LOGGER.log( Level.WARNING , "Output directory for wanted file could not be created. Operation will not continue." );
                    return Optional.empty( );
                }
            }

            CommandPair < DefaultExecuteResultHandler, ByteArrayOutputStream > cPair = GGPKUtils.runCommandLine(
                    extractGGPKexe , new CommandArg <>( contentPath.toString( ), true ) ,
                    new CommandArg <>( wantedFile, true ), new CommandArg <>( outputDir.toString( ), true ) );

            int exitCode = cPair.rh.getExitValue( );

            LOGGER.log( Level.INFO , cPair.bs.toString( ) );

            if ( exitCode == 0 ) {
                try ( Stream < Path > paths = Files.list( outputDir ) ) {
                    List < Path > pathList = paths.toList( );

                    for ( Path p : pathList ) {
                        if ( p.getFileName( ).toString( ).equalsIgnoreCase( subbed ) ) {
                            LOGGER.log( Level.INFO , "FILE SUCCESSFULLY EXTRACTED: " + p.toAbsolutePath( ) );

                            /*
                             * Write the wanted file internal content.gppk path to be used for future reference.
                             */
                            LOGGER.log( Level.INFO , "Writing path.txt in folder of: " + p.toAbsolutePath( ) );
                            try {
                                Files.writeString( path , wantedFile );
                            } catch ( IOException e ) {
                                LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
                            }

                            if ( extension.equalsIgnoreCase( "dds" ) ) {
                                List < Texture > textures;

                                if ( wantedFile.toLowerCase( ).contains( "divinationcards" ) ) {
                                    textures = GGPKUtils.getAllTexturesFor2( uiDivinationImagesDiskPath , wantedFile );
                                } else {
                                    textures = GGPKUtils.getAllTexturesFor2( uiImagesDiskPath , wantedFile );
                                }

                                DDSFile dFile = new DDSFile( wantedFile , textures , p );

                                return Optional.of( dFile );
                            } else if ( extension.equalsIgnoreCase( "bank" ) ) {
                                BankFile bFile = new BankFile( wantedFile , p );

                                return Optional.of( bFile );
                            }
                        }
                    }
                } catch ( IOException e ) {
                    LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
                }
            } else {
                LOGGER.log( Level.WARNING , "Exit code returned was non-zero. The file was likely not extracted and the"
                        + " path.txt file was not created." );
            }
        } else if ( ( Files.exists( outputDir ) && !overwrite ) ) {
            try ( Stream < Path > paths = Files.list( outputDir ) ) {
                List < Path > pathList = paths.toList();

                Path toAdd = null;
                Path txt = null;
                for ( Path p : pathList ) {
                    final String ext = FilenameUtils.getExtension( p.getFileName().toString() );
                    if ( ext.equalsIgnoreCase( "dds" ) || ext.equalsIgnoreCase( "bank" ) ) {
                        toAdd = p;
                    }

                    /*
                     * In the event there is multiple txt files within the same directory of the extracted dds or bank
                     * file, we read the txt file and validate the path is the same as the passed wantedFile.
                     */
                    if ( ext.equalsIgnoreCase( "txt" ) ) {
                        final String txtPath = Files.readString( p );

                        if ( txtPath.equalsIgnoreCase( wantedFile ) )
                            txt = p;
                    }
                }

                if ( txt == null ) {
                    /*
                     * Previous iterations of this method did not produce a path.txt file in some cases.
                     * I believe it is corrected, but as a just in case, I am keeping this here for now.
                     */
                    Files.writeString( path , wantedFile );
                }

                /*
                 * Similar to the txt file, previous iterations of this method did not return the extracted .dds
                 * file reference within the Optional, causing the resulting list to be missing a lot of files.
                 * This should be corrected, but I am leaving it here as a just in case.
                 * This also will make sure any previously extracted .dds files are returned in the event boolean
                 * overwrite is false.
                 */
                if ( toAdd != null && txt != null ) {
                    final String p = Files.readString( txt );
                    List < Texture > textures = GGPKUtils.getAllTexturesFor2( uiImagesDiskPath , p );

                    DDSFile dFile = new DDSFile( p , textures , toAdd );

                    return Optional.of( dFile );
                }

            }
        }

        return Optional.empty( );
    }

    /**
     * Attempts to retrieve the ExtractGGPK.exe and returns an Optional instance.
     */
    private Optional < Path > getGPPKExe( Path ggpkPath ) {
        if ( Files.isDirectory( ggpkPath) ) {
            try ( Stream < Path > paths = Files.list( ggpkPath ) ) {
                List < Path > pathList = paths.toList();

                for ( Path p : pathList ) {
                    if ( p.getFileName().toString().equalsIgnoreCase( GGPK_TOOL ) ) {
                        return Optional.of( p );
                    }
                }

            } catch ( IOException e ) {
                LOGGER.log( Level.SEVERE, e.getMessage(), e );
            }
        }

        return Optional.empty( );
    }
}

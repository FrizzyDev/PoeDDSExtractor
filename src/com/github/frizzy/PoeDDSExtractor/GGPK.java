package com.github.frizzy.PoeDDSExtractor;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * GPPK builds the command line process to extract the .dds files from the PoE bundle
 * archives. Once the .dds files needed are extracted, class Extractor can be used to
 * extract the wanted textures.
 * <br>
 * As of right now, only stand alone PoE clients work with this process, until the command line
 * tool is updated to process steam distros.
 *
 * @author Frizzy
 * @version 0.0.1
 * @since 0.0.1
 * @deprecated Replaced by GGPK2, do not use, not functional
 */
public class GGPK {

    private static final Logger LOGGER = Logger.getLogger( GGPK.class.getPackageName() );

    /**
     * The path of the uiimages.txt file within the content.gppk file.
     * This file contains texture names and what .dds file it is stored in,
     * and contains the x1, x2, y1, y2 coordinates to successfully extract
     * the textures.
     */
    static final String UIIMAGES_TXT_LOC = "art/uiimages1.txt";
    static final String UIDIVINATION_TXT_LOC = "art/uidivinationimages.txt";
    static final String BUNDLED_GGPK_TOOL = "ExtractBundledGGPK3.exe";
    static final String GGPK_TOOL = "ExtractGGPK.exe";

    /**
     * The File object of ExtractBundledGGPK3.exe.
     * This is used to extract files stored in the internal bundles,
     * such as a .dds file.
     */
    private final File extractBundledGGPKexe;

    /**
     * The File object of ExtractGGPK3.exe.
     * This is used to extract bank files or files not stored
     * within the internal bundles, such as a .bank file.
     */
    private File extractGGPKexe;

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
     * Unused currently.
     */
    private boolean duplicate;

    /**
     * Unused currently.
     */
    private int duplicateValue = 2;

    /**
     *
     * @param ggpkPath Path to the directory containing the LibGPPK tools.
     * @param contentPath Path to the content.gppk file.
     * @param duplicate Duplicates the content.gppk files to perform the extraction process faster by
     *                  being able to execute ExtractBundledGGPK3.exe multiple times at once.
     * @param overwrite Determines if previously extracted files should be overwritten.
     */
    public GGPK( final String ggpkPath, Path contentPath, boolean duplicate, boolean overwrite ) throws FileNotFoundException {
        Optional < File > bgtOpt = getGPPKExe( ggpkPath, BUNDLED_GGPK_TOOL );
        Optional < File > egOpt = getGPPKExe( ggpkPath, GGPK_TOOL  );

        extractBundledGGPKexe = bgtOpt.orElseThrow(
                ( ) -> new FileNotFoundException ( "ExtractBundledGGPK3.exe was not found." ) );
        extractGGPKexe = egOpt.orElseThrow( ( ) -> new FileNotFoundException( "ExtractGGPK3.exe was not found." ) );

        if ( !Files.exists( contentPath ) )
            throw new FileNotFoundException( "Content.gppk was not found" );

        this.contentPath = contentPath;
        this.overwrite = overwrite;
    }

    public void setOverwrite ( boolean overwrite ) {
        this.overwrite = overwrite;
    }

    /**
     * Sets the amount of times the content.gppk file should be duplicated.
     */
    public void setDuplicateValue ( int newValue ) {
        this.duplicateValue = newValue;
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
     * @param outputPath The output directory where extracted .dds files and related files will be extracted to.
     * @param wantedFiles The internal content.gppk file paths for the wanted .dds files.
     * @return A list of the extracted .dds files.
     */
    public List < File > extract ( final Path outputPath, List< String > wantedFiles ) {
        /*
         * The directories containing the extracted .dds files.
         * Extracted .dds files will be stored in their own directory
         * named after the path to that .dds file within the content.gppk archive.
         * This is done this way because once the dds extraction finishes, the individual textures
         * will be extracted into the same folder.
         */
        List < File > extracted = new ArrayList <>(  );
        File contentGPPK = contentPath.toFile();
        File outputDirectory = outputPath.toFile();

        if ( ( !contentGPPK.exists() || !outputDirectory.exists() )  ) {
            LOGGER.log( Level.WARNING, "One of the files provided does not exist." );
            return extracted;
        }

        if ( ( !contentGPPK.isFile() || !outputDirectory.isDirectory() ) ) {
            LOGGER.log( Level.WARNING, "One of the files provided is not of the correct type." );
            return extracted;
        }

        if ( wantedFiles.isEmpty() ) {
            LOGGER.log( Level.WARNING, "Wanted files list is empty. Extraction operation will not continue." );
            return extracted;
        }

        int processed = 0;
        for ( String wf : wantedFiles ) {
            try {
                Optional < File > ef = extractFile( outputPath, wf );
                ef.ifPresentOrElse( a -> extracted.add( ef.get( ) ) , ( ) -> {
                    LOGGER.log( Level.WARNING, "No file was returned. File for: " + wf + " was not extracted." );
                } );
            } catch ( IOException e ) {
                LOGGER.log( Level.SEVERE, e.getMessage() );
            }

            processed++;

        }

        return extracted;
    }

    /**
     * Extracts all interface .dds files specified in the uiimages.txt file and returns them within a map that contains
     * all the textures each .dds file contains.
     * <br>
     * If overwrite is set to false, any previously extracted .dds files will be returned.
     * <br>
     * This is a very time-consuming method, with testing on my personal PC, it took 1.5 hours. The biggest issue is
     * LibGPPK3 only has the ability to extract one file at a time, and reloading and searching the GPPK file for one
     * .dds file at a time is the main reason this takes a while. I would not use this and instead build your list of
     * wanted files and extract those only.
     * <br>
     * I will do some profiling at some point to see if the process can be sped up within this code.
     */
    public Map < File, List < String > > extractEverything ( final Path outputPath, File uiimagesTxt ) {
        Map < File, List < String > > allFiles = new HashMap <>(  );

        List < String [ ] > gppkFiles = GGPKUtils.getAllLinesSplit( null );
        /*
         * Validation list tracks what .dds files have already been extracted, as the uiimages.txt will
         * have the .dds file paths referenced multiple times.
         */
        List < String > validation = new ArrayList <> (  );
        Map < File , String > ddsFiles = new HashMap <>(  );

        for ( String [ ] array : gppkFiles ) {
            String internalPath = array [ 1 ];

            if ( !validation.contains( internalPath ) ) {
                try {
                    Optional < File > opt = extractFile( outputPath, internalPath );

                    opt.ifPresentOrElse( extracted -> {
                        ddsFiles.put( extracted , internalPath );
                        LOGGER.log( Level.INFO, "File: " + extracted.getAbsolutePath() + " was added to map." );
                    } , ( ) -> {
                        LOGGER.log( Level.WARNING, "Optional returned empty for: " + internalPath +
                                "\nSome uiimages.txt paths do not produce a .dds file. Validate this is accurate." +
                                "\nThe create directory process could have failed as well.");
                    } );

                    validation.add( internalPath );
                } catch ( IOException e ) {
                    LOGGER.log( Level.SEVERE, e.getMessage(), e );
                }
            }
        }

        /*
         * Now get all the textures stored in each .dds file.
         */
        LOGGER.log( Level.INFO, "Gathering all textures for .dds files." );

        for ( File ddsFile : ddsFiles.keySet( ) ) {
            String internalPath = ddsFiles.get( ddsFile );

            List < String > allTextures = GGPKUtils.getAllTexturesFor1( uiimagesTxt , internalPath );
            allFiles.put( ddsFile , allTextures );
            LOGGER.log( Level.INFO, "Got all textures for: " + internalPath );

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
    public Optional < File > extractUIImagesTXT ( Path outputPath ) {

        try {
            Optional < File > opt = extractFile ( outputPath, UIIMAGES_TXT_LOC );
            opt.orElseThrow( ( ) -> new NoSuchFileException( "No file was returned. uiimages.txt was not extracted." ) );

            return opt;
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
        }

        return Optional.empty();
    }

    /**
     * Convenience method to extract the uidivinationimages.txt file.
     * <br>
     * This can throw a NoSuchFileException if the uidivinationimages.txt file was not extracted, as the entirety
     * of the extraction/Conversion process can not continue without this file.
     * @param outputPath The path the uidivinationimages.txt file will be extracted to.
     */
    public Optional < File > extractUIDivinationImagesTXT ( Path outputPath ) {

        try {
            Optional < File >  opt = extractFile ( outputPath, UIDIVINATION_TXT_LOC );
            opt.orElseThrow( ( ) -> new NoSuchFileException( "No file was returned. uidivinationimages.txt was not extracted." ) );

            return opt;
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
        }

        return Optional.empty();
    }

    /**
     * Extracts the wanted file from the Content.gppk archive.
     * <br>
     * Extracted files are stored in a folder named after the extracted file within the output path.
     *
     * @param outputPath The directory that will hold the extracted files.
     * @param wantedFile The internal .dds file wanted.
     */
    private Optional < File > extractFile ( Path outputPath, String wantedFile ) throws IOException {
        LOGGER.log( Level.INFO, "STARTING FILE EXTRACTION FOR: " + wantedFile );

        /*
         * We name the output directory to the gppk archive path of the file.
         * This is probably temporary.
         * If someone has a better naming convention for the folders of the extracted files, I am all ears.
         * I wanted something relatively identifiable, but I suppose it is not strictly necessary.
         */
        String temp = "";
        File outputDir = null;
        File toolToUse = null;

        String extension = FilenameUtils.getExtension( wantedFile );
        if ( extension.equalsIgnoreCase( "dds" ) || extension.equalsIgnoreCase( "txt" ) ) {
            String sub = wantedFile.replaceAll( "/", "_" );

            temp = extension.equalsIgnoreCase( "dds" ) ?
                    sub.replace( ".dds", "" ) : sub.replace( ".txt", "" );

            outputDir = new File( outputPath.toString() + File.separator + temp );
            toolToUse = extractBundledGGPKexe;
        } else if ( extension.equalsIgnoreCase( "bank" ) ) {
            outputDir = outputPath.toFile();
            toolToUse = extractGGPKexe;
        }

        if ( outputDir == null ) {
            LOGGER.log( Level.WARNING, "Output Directory is null." );
            return Optional.empty();
        }

        final String subbed = wantedFile.substring( wantedFile.lastIndexOf( "/" ) + 1 );
        final Path path = Path.of( outputDir + File.separator + subbed + "_path.txt" );

        if ( ( overwrite && outputDir.exists() ) || !outputDir.exists() || extension.equalsIgnoreCase( "bank" ) ) {
            if ( !wantedFile.contains( ".bank" )) {
                boolean created = outputDir.mkdir();

                if ( !created && !overwrite ) {
                    LOGGER.log( Level.WARNING, "Output directory for wanted file could not be created. Operation will not continue." );
                    return Optional.empty();
                }
            }

            CommandLine commandLine = new CommandLine( toolToUse );
            commandLine.addArgument( contentPath.toString() );
            commandLine.addArgument( wantedFile );
            commandLine.addArgument( outputDir.toString() );

            /*
             * stdOut will allow us to write the cmd.exe output to our logger.
             */
            ByteArrayOutputStream stdOut = new ByteArrayOutputStream(  );
            PumpStreamHandler psh = new PumpStreamHandler( stdOut );
            DefaultExecutor executor = DefaultExecutor.builder().get();

            executor.setStreamHandler( psh );

            int exitCode = executor.execute( commandLine );
            LOGGER.log( Level.INFO, stdOut.toString() );

            if ( exitCode == 0 ) {
                File[] files = outputDir.listFiles();

                if ( files != null ) {
                    for ( File f : files ) {
                        if ( f.getName().equalsIgnoreCase( subbed ) ) {
                            LOGGER.log( Level.INFO, "FILE SUCCESSFULLY EXTRACTED: " + f.getAbsolutePath() );

                            /*
                             * Write the wanted file internal content.gppk path to be used for future reference.
                             */
                            LOGGER.log( Level.INFO, "Writing path.txt at: " + outputDir + File.separator + "path.txt" );

                            Files.writeString( path, wantedFile );

                            return Optional.of( f );
                        }
                    }
                }
            } else {
                LOGGER.log( Level.WARNING, "Exit code returned was non-zero. The file was likely not extracted and the"
                        + " path.txt file was not created.");
            }
        } else if ( ( outputDir.exists() && !overwrite ) ) {
            File [ ] files = outputDir.listFiles();

            if ( files != null ) {
                File toAdd = null;
                File txt = null;
                for ( File f : files ) {
                    final String ext = FilenameUtils.getExtension( f.getName() );
                    if ( ext.equalsIgnoreCase( "dds" )  || ext.equalsIgnoreCase( "bank" ) ) {
                        toAdd = f;
                    }

                    if ( ext.equalsIgnoreCase( "txt" ) ) {
                        txt = f;
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
                if ( toAdd != null ) {
                    return Optional.of( toAdd );
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Attempts to retrieve the ExtractBundledGGPK3.exe and returns an Optional instance.
     */
    private Optional <File> getGPPKExe ( String gppkPath, String TOOL ) {
        File file = new File( gppkPath );

        if ( file.isDirectory() ) {
            File[] files = file.listFiles();

            if ( files != null ) {
                for ( File f : files ) {
                    if ( f.getName().contains( TOOL ) ) {
                        return Optional.of( f );
                    }
                }
            }
        }

        return Optional.empty();
    }
}

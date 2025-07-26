package com.github.frizzy.PoeDDSExtractor;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts .dds files to png using the Microsoft texconv.exe command line tool.
 *
 * @author Frizzy
 * @version 0.0.1
 * @since 0.0.1
 */
public class DDSConverter {

    private static final Logger LOGGER = Logger.getLogger( DDSConverter.class.getName() );

    /**
     * Names of the files used for the .dds conversion.
     */
    static final String CONVERT_BAT = "convert.bat";
    static final String TEXCONV_EXE = "texconv.exe";

    /**
     * File reference to the original location of the convert.bat file.
     * <br>
     * convert.bat is not removed from this location, just copied to the required
     * directories for .dds conversion.
     */
    private File convertBat;

    /**
     * File reference to the original location of the texconv.exe file.
     * <br>
     * texconv is not removed from this location, just copied to the required
     * directories for .dds conversion.
     */
    private File texConv;

    private boolean overwrite;

    /**
     *
     */
    public DDSConverter ( Path convertBatPath, Path texConvPath, boolean overwrite ) {
        this.convertBat = convertBatPath.toFile();
        this.texConv = texConvPath.toFile();
        this.overwrite = overwrite;
    }

    public void setOverwrite ( boolean overwrite ) {
        this.overwrite = overwrite;
    }

    /**
     * Converts the supplied .dds files to .png files. The .bat file used to run the commands
     * in texconv.exe should preserve color accuracy.
     */
    public List < File > convert ( List < File > ddsFiles ) {
        //The directory that convert.bat and texconv.exe will be copied to.
        File converterOutLoc = null;
        List < File > convertedFiles = new ArrayList <>(  );

        for ( File ddsFile : ddsFiles ) {
            if ( ddsFile.getName().endsWith( ".dds" ) ) {
                File pngFile = new File( ddsFile.getAbsolutePath().replace( ".dds", ".png" ) );

                if ( ( pngFile.exists() && overwrite ) || !pngFile.exists() ) {
                    converterOutLoc = ddsFile.getParentFile();
                    copyConverterTo( converterOutLoc );

                    Optional < File > opt = executeConvert( ddsFile, converterOutLoc );

                    opt.ifPresentOrElse( convertedFiles::add , ( ) -> {
                        LOGGER.log( Level.WARNING, "No file was returned. dds file: " + ddsFile.getName() + " was not converted." );
                        LOGGER.log( Level.WARNING, "This likely means executeConvert ( ) failed." );
                    } );
                } else if ( pngFile.exists() && overwrite ) {
                    convertedFiles.add( pngFile );
                }
            }

            //Conversion is done, delete the .bat and .exe files.
            if ( converterOutLoc != null ) {
                LOGGER.log( Level.INFO, "Deleting convert.bat and texconv.exe." );
                File batFile = new File( converterOutLoc.getAbsolutePath() + File.separator + CONVERT_BAT );
                File exeFile = new File( converterOutLoc.getAbsolutePath() + File.separator + TEXCONV_EXE );

                boolean batDeleted = batFile.delete();
                boolean exeDeleted = exeFile.delete();

                if ( !batDeleted ) {
                    LOGGER.log( Level.WARNING, "convert.bat was not deleted." );
                }

                if ( !exeDeleted ) {
                    LOGGER.log( Level.WARNING, "texconv.exe was not deleted." );
                }
            }
        }

        return convertedFiles;
    }

    /**
     * Executes the convert.bat file to convert .dds files within its location.
     * <br>
     * Returns the created .png file.
     */
    private Optional < File > executeConvert ( File ddsFile, File converterLoc ) {
        File batFile = new File( converterLoc.getAbsolutePath() + File.separator + CONVERT_BAT );

        CommandLine cmdLine = new CommandLine( "cmd.exe" );
        cmdLine.addArgument( "/C " + "\"\"" + batFile.getAbsolutePath() + "\"\"", false );

        ByteArrayOutputStream stdOut = new ByteArrayOutputStream(  );
        PumpStreamHandler psh = new PumpStreamHandler( stdOut );
        DefaultExecutor executor = DefaultExecutor.builder( ).get( );

        executor.setStreamHandler( psh );

        try {
            int exitCode = executor.execute( cmdLine );
            LOGGER.log( Level.INFO, stdOut.toString() );

            if ( exitCode == 0 ) {
                String name = ddsFile.getName().replace( ".dds", ".png" );
                return Optional.of( new File ( converterLoc.getAbsolutePath() + File.separator + name ) );
            }
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
        }

        return Optional.empty();
    }

    /**
     * Copies texconv.exe and convert.bat to the directory the dds file is located in.
     */
    private void copyConverterTo( final File ddsFolder ) {
            copy( convertBat, Path.of( ddsFolder.getAbsolutePath() + File.separator + CONVERT_BAT ) );
            copy( texConv, Path.of( ddsFolder.getAbsolutePath() + File.separator + TEXCONV_EXE ) );
    }

    /**
     * Completes copy process.
     */
    private void copy( File batOrTexconv, Path out ) {

        try {
            Files.copy( batOrTexconv.toPath(), out, StandardCopyOption.REPLACE_EXISTING );
            LOGGER.log( Level.INFO, "File: " + batOrTexconv.getAbsolutePath() + " was copied to: " + out );
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
        }
    }
}

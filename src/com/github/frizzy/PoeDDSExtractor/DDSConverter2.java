package com.github.frizzy.PoeDDSExtractor;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Converts .dds files to png using the Microsoft texconv.exe command line tool.
 *
 * @author Frizzy
 * @version 0.0.2
 * @since 0.0.2
 */
public class DDSConverter2 {

    private static final Logger LOGGER = Logger.getLogger( DDSConverter2.class.getName() );

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

    /**
     * Boolean flag determining if previously converted .dds files should be overwritten.
     * <br>
     * This can improve performance when restarting the conversion process.
     */
    private boolean overwrite;

    /**
     * Creates the DDSConverter instance with the path to the convert.bat file, texconv.exe file, and if
     * previously converted .dds files should be overwritten or not.
     */
    public DDSConverter2( Path convertBatPath, Path texConvPath, boolean overwrite ) {
        this.convertBat = convertBatPath.toFile();
        this.texConv = texConvPath.toFile();
        this.overwrite = overwrite;
    }

    /**
     * Adds the handler to the logger.
     */
    public void addLoggerHandler ( Handler handler ) {
        LOGGER.addHandler( handler );
    }

    /**
     * Sets the overwrite flag.
     * <br>
     * May be important to know changing the overwrite flag will only have an effect before convert() is called.
     */
    public void setOverwrite ( boolean overwrite ) {
        this.overwrite = overwrite;
    }

    /**
     * Converts the supplied .dds files to .png files, and then returns them with the
     * png file reference added. The .bat file used to run the commands
     * in texconv.exe should preserve color accuracy.
     */
    public List < DDSFile > convert ( List < DDSFile > ddsFiles ) {
        //The directory that convert.bat and texconv.exe will be copied to.
        Path converterOutLoc = null;

        for ( DDSFile ddsFile : ddsFiles ) {
            Path ref = ddsFile.getDiskPath();

            if ( ref.getFileName().toString().endsWith( ".dds" ) ) {
                Path pngFile = Path.of ( ref.toAbsolutePath().toString().replace( ".dds", ".png" ) );

                if ( ( Files.exists( pngFile )  && overwrite ) || !Files.exists( pngFile ) ) {
                    converterOutLoc = ref.getParent ( );
                    copyConverterTo( converterOutLoc );

                    Optional < Path > opt = executeConvert( ref, converterOutLoc );

                    if ( opt.isPresent() ) {
                        ddsFile.setPNGPath( pngFile );
                    } else {
                        LOGGER.log( Level.WARNING, "No png file was returned." );
                    }
                }
            }

            //Conversion is done, delete the .bat and .exe files.
            if ( converterOutLoc != null ) {
                LOGGER.log( Level.INFO, "Deleting convert.bat and texconv.exe." );
                Path batFile = Path.of ( converterOutLoc.toAbsolutePath() + File.separator + CONVERT_BAT );
                Path exeFile = Path.of ( converterOutLoc.toAbsolutePath() + File.separator + TEXCONV_EXE );

                try {
                    boolean batDeleted = Files.deleteIfExists( batFile );
                    boolean exeDeleted = Files.deleteIfExists( exeFile );

                    if ( !batDeleted ) {
                        LOGGER.log( Level.WARNING, "convert.bat was not deleted." );
                    }

                    if ( !exeDeleted ) {
                        LOGGER.log( Level.WARNING, "texconv.exe was not deleted." );
                    }
                } catch ( IOException e ) {
                    LOGGER.log( Level.SEVERE, e.getMessage(), e );
                }
            }
        }

        return ddsFiles;
    }

    /**
     * Executes the convert.bat file to convert .dds files within its location.
     * <br>
     * Returns the created .png file.
     */
    private Optional < Path > executeConvert ( Path ddsFile, Path converterLoc ) {
        Path batFile = Path.of ( converterLoc.toAbsolutePath() + File.separator + CONVERT_BAT );

        CommandLine cmdLine = new CommandLine( "cmd.exe" );
        cmdLine.addArgument( "/C " + "\"\"" + batFile.toAbsolutePath() + "\"\"", false );

        ByteArrayOutputStream stdOut = new ByteArrayOutputStream(  );
        PumpStreamHandler psh = new PumpStreamHandler( stdOut );
        DefaultExecutor executor = DefaultExecutor.builder( ).get( );

        executor.setStreamHandler( psh );

        try {
            int exitCode = executor.execute( cmdLine );
            LOGGER.log( Level.INFO, stdOut.toString() );

            if ( exitCode == 0 ) {
                String name = ddsFile.getFileName().toString().replace( ".dds", ".png" );
                return Optional.of( Path.of ( converterLoc.toAbsolutePath() + File.separator + name ) );
            }
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
        }

        return Optional.empty();
    }

    /**
     * Copies texconv.exe and convert.bat to the directory the dds file is located in.
     */
    private void copyConverterTo( final Path ddsFolder ) {
            copy( convertBat, Path.of( ddsFolder.toAbsolutePath() + File.separator + CONVERT_BAT ) );
            copy( texConv, Path.of( ddsFolder.toAbsolutePath() + File.separator + TEXCONV_EXE ) );
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

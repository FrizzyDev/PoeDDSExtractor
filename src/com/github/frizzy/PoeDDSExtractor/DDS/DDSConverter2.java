package com.github.frizzy.PoeDDSExtractor.DDS;

import com.github.frizzy.PoeDDSExtractor.Command.CommandArg;
import com.github.frizzy.PoeDDSExtractor.Command.CommandPair;
import com.github.frizzy.PoeDDSExtractor.GGPKUtils;
import org.apache.commons.exec.DefaultExecuteResultHandler;

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
     * File reference to the original location of the texconv.exe file.
     * <br>
     * texconv is not removed from this location, just copied to the required
     * directories for .dds conversion.
     */
    private Path texConvPath;

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
    public DDSConverter2( Path texConvPath, boolean overwrite ) {
        this.texConvPath = texConvPath;
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

        for ( DDSFile ddsFile : ddsFiles ) {
            Path ref = ddsFile.getDiskPath();

            if ( ref.getFileName().toString().endsWith( ".dds" ) ) {
                Path pngFile = Path.of ( ref.toAbsolutePath().toString().replace( ".dds", ".png" ) );

                if ( ( Files.exists( pngFile ) && overwrite ) || !Files.exists( pngFile ) ) {

                    try {
                        Optional < Path > opt = executeConvert( ref );

                        if ( opt.isPresent() ) {
                            ddsFile.setPNGPath( opt.get() );
                        } else {
                            LOGGER.log( Level.WARNING, "No png file was returned." );
                        }
                    } catch ( IOException e ) {
                        LOGGER.log( Level.SEVERE, e.getMessage(), e );
                    }
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
    private Optional < Path > executeConvert ( Path ddsFile ) throws IOException {
        Path outLocation = ddsFile.getParent();
        String command = "\"\"" + ddsFile + "\"\" -srgb -ft png -f R8G8B8A8_UNORM_SRGB " + "-y -o \"\"" + outLocation + "\"\"";

        CommandPair < DefaultExecuteResultHandler, ByteArrayOutputStream > cPair = GGPKUtils.runCommandLine(
                texConvPath , new CommandArg <>( command, false ) );

        int exitCode = cPair.rh.getExitValue();
        LOGGER.log( Level.INFO, cPair.bs.toString() );

        if ( exitCode == 0 ) {
            String name = ddsFile.getFileName().toString().replace( ".dds", ".png" );
            return Optional.of( Path.of ( ddsFile.getParent() + File.separator + name ) );
        }

        return Optional.empty();
    }
}

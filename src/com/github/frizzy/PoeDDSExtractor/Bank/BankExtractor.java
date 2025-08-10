package com.github.frizzy.PoeDDSExtractor.Bank;

import com.github.frizzy.PoeDDSExtractor.*;
import com.github.frizzy.PoeDDSExtractor.Command.CommandArg;
import com.github.frizzy.PoeDDSExtractor.Command.CommandPair;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Extracts .wav files from a ggpk .bank file, utilizing tools in the bank_tools.zip
 * file.
 *
 * @author Frizzy
 * @version 0.0.2
 * @since 0.0.2
 */
public class BankExtractor {

    private static final Logger LOGGER = Logger.getLogger( BankExtractor.class.getName() );

    /**
     * Path to the directory containing the required bank
     * processing tools.
     */
    private final String toolsPath;

    /**
     * Creates the BankExtractor instance.
     * <br>
     * Can throw a FileNotFoundException if quickbms.exe or script.bms is not found.
     */
    public BankExtractor ( String toolsPath ) throws FileNotFoundException {
        if ( !Files.exists( Path.of( toolsPath + File.separator + "quickbms.exe" ) ))
            throw new FileNotFoundException( "quickbms.exe could not be found." );
        if ( !Files.exists( Path.of( toolsPath + File.separator + "Script.bms" ) ) )
            throw new FileNotFoundException( "script.bms could not be found." );

        this.toolsPath = toolsPath;
    }

    /**
     * Adds a handler to the logger.
     */
    public void addLoggerHandler ( Handler handler ) {
        LOGGER.addHandler( handler );
    }

    /**
     * Extracts all .wav files from the banks in the provided list.
     * @return A map with the .bank file as the key and a list of wav files as the values.
     */
    public List < BankFile > extractWavFiles( List < BankFile > banks  ) {
        for ( BankFile bank : banks ) {
            try {
                Optional < Path > opt = completeExtraction( bank.getDiskPath() );

                opt.ifPresent( wavFilesDir -> {
                    try ( Stream < Path > paths = Files.list( wavFilesDir ) ) {
                        List < Path > pathList = paths.toList();

                        bank.setWavFiles( pathList );
                    } catch ( IOException e ) {
                        LOGGER.log( Level.SEVERE, e.getMessage(), e );
                    }
                } );
            } catch ( IOException e ) {
                LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
            }
        }

        return banks;
    }

    /**
     * Completes the extraction process of the .wav files, returning the directory they are stored
     * in or an empty optional if the process failed.
     *
     * @param bankFile The bank file .wav files will be extracted from.
     */
    private Optional < Path > completeExtraction ( Path bankFile ) throws IOException {
        final String bmsExePath = toolsPath + File.separator + "quickbms.exe";
        final String bmsScriptPath = toolsPath + File.separator + "Script.bms";
        final String baseName = FilenameUtils.getBaseName( bankFile.getFileName().toString() );

        //Directory .wav files will be extracted to.
        Path bankOutputDir = Path.of ( bankFile.getParent().toAbsolutePath()  + File.separator + baseName );

        if ( !Files.exists( bankOutputDir ) ) {
            Path created = Files.createDirectory( bankOutputDir );

            if ( !Files.exists( created ) ) {
                LOGGER.log( Level.WARNING, "Bank output directory could not be created. Extraction will not complete." );
                return Optional.empty( );
            }
        }

        CommandPair < DefaultExecuteResultHandler, ByteArrayOutputStream > cPair = GGPKUtils.runCommandLine(
                Path.of( bmsExePath ),
                new CommandArg <>( bmsScriptPath, true ),
                new CommandArg <>( bankFile.toAbsolutePath().toString(), true ),
                new CommandArg <>( bankOutputDir.toAbsolutePath().toString() , true ) );

        int bmsExitCode = cPair.rh.getExitValue();

        if ( bmsExitCode == 0 ) {
            LOGGER.log( Level.FINE, cPair.bs.toString() );
            ToolsUnpacker unpacker = new ToolsUnpacker();

            boolean success = unpacker.exportBankToolsTo(
                    Path.of( bankOutputDir + File.separator + "bank_tools.zip" ), bankOutputDir );

            if ( success ) {
                final String command = "/C \"\"" + bankOutputDir.toAbsolutePath() + File.separator + "batch.bat\"\"";
                CommandPair < DefaultExecuteResultHandler, ByteArrayOutputStream > cPair2 = GGPKUtils.runCommandLine(
                        Path.of( "cmd.exe" ),
                        new CommandArg <>( command, false  ) );

                int fsbExitCode = cPair2.rh.getExitValue();

                if ( fsbExitCode == 0 ) {
                    LOGGER.log( Level.FINE, cPair2.bs.toString() );
                    try ( Stream < Path > paths = Files.list( bankOutputDir ) ) {
                        List < Path > pathList = paths.toList();

                        for ( Path p : pathList ) {
                            if ( !FilenameUtils.getExtension( p.getFileName().toString() ).equalsIgnoreCase( "wav" ) ) {
                                boolean deleted = Files.deleteIfExists( p );

                                if ( deleted ) {
                                    LOGGER.log( Level.FINE, p.getFileName().toString() + " deleted" );
                                }
                            } else if (  Files.isDirectory( p ) && p.getFileName().toString().equalsIgnoreCase( "fmodex" ) ) {
                                //Delete file in fmodex folder
                                try ( Stream < Path > fmpaths = Files.list( p ) ) {
                                    List < Path > fmPathList = fmpaths.toList();

                                    for ( Path p2 : fmPathList ) {
                                        Files.delete( p2 );
                                    }
                                }

                                //Delete fmodex
                                Files.delete( p );
                            }
                        }

                        return Optional.of( bankOutputDir );
                    } catch ( IOException e ) {
                        LOGGER.log( Level.SEVERE, e.getMessage(), e );
                    }
                } else {
                    LOGGER.log( Level.WARNING,
                            "Executor did not return a successful exit code. The wav files may not have been extracted." );
                }
            } else {
                LOGGER.log( Level.WARNING, "Bank tools were not successfully exported. The extraction process" +
                        " cannot continue.");
            }
        } else {
            LOGGER.log( Level.WARNING, "Executor did not return a successful exit code." +
                    "The fsb file may not have been extracted. Wav files will not be extracted at this point.");
        }

        return Optional.empty();
    }
}

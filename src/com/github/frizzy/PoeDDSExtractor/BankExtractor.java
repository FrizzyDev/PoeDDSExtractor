package com.github.frizzy.PoeDDSExtractor;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extracts .wav files from a ggpk .bank file, utilizing tools in the bank_ tools.zip
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
                Optional < File > opt = completeExtraction( bank.getFile() );
                List < File > wavFiles = new ArrayList <>( );

                opt.ifPresent( wavFilesDir -> {
                    File[ ] files = wavFilesDir.listFiles();

                    if ( files != null ) {
                        Collections.addAll( wavFiles , files );
                        bank.setWavFiles( wavFiles );
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
    private Optional < File > completeExtraction ( File bankFile ) throws IOException {
        final String bmsExePath = toolsPath + File.separator + "quickbms.exe";
        final String bmsScriptPath = toolsPath + File.separator + "Script.bms";
        final String baseName = FilenameUtils.getBaseName( bankFile.getName() );

        //Directory .wav files will be extracted to.
        File bankOutputDir = new File( bankFile.getParentFile().getAbsolutePath()  + File.separator + baseName );

        if ( !bankOutputDir.exists() ) {
            boolean created = bankOutputDir.mkdir();

            if ( !created ) {
                LOGGER.log( Level.WARNING, "Bank output directory could not be created. Extraction will not complete." );
                return Optional.empty( );
            }
        }

        CommandLine bmsCmdLine = new CommandLine( bmsExePath );

        bmsCmdLine.addArgument( bmsScriptPath ); //Path to the script that performs extraction
        bmsCmdLine.addArgument( bankFile.getAbsolutePath() ); //Path to the bank file
        bmsCmdLine.addArgument( bankOutputDir.getAbsolutePath() ); // Path to the output dir

        ByteArrayOutputStream bmsOut = new ByteArrayOutputStream(  );
        PumpStreamHandler bmsPsh = new PumpStreamHandler( bmsOut );
        DefaultExecutor bmsExecutor = DefaultExecutor.builder().get();

        bmsExecutor.setStreamHandler( bmsPsh );

        int bmsExitCode = bmsExecutor.execute( bmsCmdLine );

        if ( bmsExitCode == 0 ) {
            LOGGER.log( Level.FINE, bmsOut.toString() );
            ToolsUnpacker unpacker = new ToolsUnpacker();

            boolean success = unpacker.exportBankToolsTo(
                    Path.of( bankOutputDir + File.separator + "bank_tools.zip" ), bankOutputDir.toPath() );

            if ( success ) {
                final String command = "/C \"\"" + bankOutputDir.getAbsolutePath() + File.separator + "batch.bat\"\"";

                CommandLine fsbCmdLine = new CommandLine( "cmd.exe" );
                fsbCmdLine.addArgument( command, false );

                ByteArrayOutputStream fsbOut = new ByteArrayOutputStream(  );
                PumpStreamHandler fsbPsh = new PumpStreamHandler( fsbOut );
                DefaultExecutor fsbExecutor = DefaultExecutor.builder().get();

                fsbExecutor.setStreamHandler( fsbPsh );

                int fsbExitCode = fsbExecutor.execute( fsbCmdLine );

                if ( fsbExitCode == 0 ) {
                    LOGGER.log( Level.FINE, fsbOut.toString() );
                    File[] files = bankOutputDir.listFiles();

                    if ( files != null ) {
                        for ( File f : files ) {
                            if ( !FilenameUtils.getExtension( f.getName() ).equalsIgnoreCase( "wav" ) ) {
                                boolean deleted = f.delete();

                                if ( !deleted ) {
                                    LOGGER.log( Level.FINE, f.getName() + " deleted" );
                                }
                            } else if (  f.isDirectory() && f.getName().equalsIgnoreCase( "fmodex" ) ) {
                                File[] dirFiles = f.listFiles();

                                if ( dirFiles != null ) {
                                    for ( File df : dirFiles ) {
                                        df.delete();
                                    }
                                }

                                f.delete();
                             }
                        }
                    }

                    return Optional.of( bankOutputDir );
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

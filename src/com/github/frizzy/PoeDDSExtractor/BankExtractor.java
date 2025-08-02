package com.github.frizzy.PoeDDSExtractor;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Frizzy
 * @version 0.0.1
 * @since 0.0.1
 */
public class BankExtractor {

    private static final Logger LOGGER = Logger.getLogger( BankExtractor.class.getName() );

    /**
     * Path to the directory containing the required bank
     * processing tools.
     */
    private final String toolsPath;

    /**
     *
     */
    public BankExtractor ( String toolsPath ) {
        this.toolsPath = toolsPath;
    }

    /**
     *
     * @param bankSourceFilePath
     * @return
     */
    public List < File > extractWavFiles( Path bankSourceFilePath ) {
        File bankFile = bankSourceFilePath.toFile();

        if ( bankFile.exists() ) {
            try {
                Optional < File > opt = completeExtraction( bankFile );
                List < File > files = new ArrayList <>(  );

                opt.ifPresent( wavFilesDir -> {
                    Collections.addAll( files, wavFilesDir.listFiles() );
                } );

                return files;
            } catch ( IOException e ) {
                LOGGER.log( Level.SEVERE, e.getMessage(), e );
            }
        }

        return null;
    }

    /**
     * Extracts all wav files from the .bank files stored in the specified path. The returned
     * map is the bank files as the keys, and a list of wav files extracted from each bank file.
     * @return
     */
    public Map < File , List < File > > extractAllWavFiles ( List < File > banks  ) {
        Map < File , List < File > > all = new HashMap <>(  );

        for ( File bank : banks ) {
            try {
                Optional < File > opt = completeExtraction( bank );
                List < File > wavFiles = new ArrayList <>( );

                opt.ifPresent( wavFilesDir -> {
                    Collections.addAll( wavFiles , wavFilesDir.listFiles( ) );
                    all.put( bank , wavFiles );
                } );
            } catch ( IOException e ) {
                LOGGER.log( Level.SEVERE , e.getMessage( ) , e );
            }
        }


        return all;
    }

    /**
     * Completes the extraction process of the .wav files, returning the directory they are stored
     * in or an empty optional if the process failed.
     *
     * @param bankFile The bank file .wav files will be extracted from.
     *
     */
    private Optional < File > completeExtraction ( File bankFile ) throws IOException {
        final String bmsExePath = toolsPath + File.separator + "quickbms.exe";
        final String bmsScriptPath = toolsPath + File.separator + "Script.bms";
        final String baseName = FilenameUtils.getBaseName( bankFile.getName() );

        //Directory .wav files will be extracted to.
        File bankOutputDir = new File( bankFile.getParentFile().getAbsolutePath()  + File.separator + baseName );

        if ( !bankOutputDir.exists() ) {
            boolean created = bankOutputDir.mkdir();
            if ( !created )
                return Optional.empty();
        }

        CommandLine bmsCmdLine = new CommandLine( bmsExePath );

        bmsCmdLine.addArgument( bmsScriptPath ); //Path to the script that performs extraction
        bmsCmdLine.addArgument( bankFile.getAbsolutePath() ); //Path to the bank file
        bmsCmdLine.addArgument( bankOutputDir.getAbsolutePath() ); // Path to the output dir

        DefaultExecutor bmsExecutor = DefaultExecutor.builder().get();

        int bmsExitCode = bmsExecutor.execute( bmsCmdLine );

        if ( bmsExitCode == 0 ) {
            boolean success = extractBankToolsTo( Path.of( bankOutputDir + File.separator + "bank_tools.zip" ), bankOutputDir.toPath() );

            if ( success ) {
                final String command = "/C \"\"" + bankOutputDir.getAbsolutePath() + File.separator + "batch.bat\"\"";

                CommandLine fsbCmdLine = new CommandLine( "cmd.exe" );
                fsbCmdLine.addArgument( command, false );

                DefaultExecutor fsbExecutor = DefaultExecutor.builder().get();

                int fsbExitCode = fsbExecutor.execute( fsbCmdLine );

                if ( fsbExitCode == 0 ) {
                    for ( File f : bankOutputDir.listFiles() ) {
                        if ( !FilenameUtils.getExtension( f.getName() ).equalsIgnoreCase( "wav" )
                        || f.isDirectory() ) {
                            boolean deleted = f.delete();

                            if ( !deleted ) {
                                LOGGER.log( Level.FINE, f.getName() + " deleted" );
                            }
                        }
                    }

                    return Optional.of( bankOutputDir );
                }
            }
        }

        return Optional.empty();
    }

    /**
     *
     * @param zipPath
     * @param outDirPath
     */
    private boolean extractBankToolsTo ( Path zipPath, Path outDirPath )  {
        final String resourcePath = "/com/github/frizzy/PoeDDSExtractor/Resources/bank_tools.zip";
        InputStream stream = BankExtractor.class.getResourceAsStream( resourcePath );

        if ( stream != null ) {
            try {
                Files.copy( stream, zipPath);

                ToolsUnpacker unpacker = new ToolsUnpacker();
                unpacker.unzip( zipPath, outDirPath );
            } catch ( IOException e ) {
                LOGGER.log( Level.SEVERE, e.getMessage(), e );
                return false;
            }
        }

        return true;
    }
}

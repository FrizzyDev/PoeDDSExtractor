package com.github.frizzy.PoeDDSExtractor;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloader and zip extractor I shamelessly cobbled together with code from stack overflow.
 * <br>
 * Pass the URL for LibGPPK3 release and TexConv to download and the files will be downloaded
 * to wherever the output is specified. In the case of LibGPPK3, feed unzip () the downloaded zip
 * file for the LibGPPK3 release.
 *
 * @author Frizzy
 * @version 0.0.1
 */
public class ToolsUnpacker {

    private static final Logger LOGGER = Logger.getLogger( ToolsUnpacker.class.getName() );

    public ToolsUnpacker( ) {

    }

//    /**
//     * Exports the ExtractGPPK.exe file to the specified directory/file.
//     * <br>
//     * ExtractGPPK.exe is a command line tool I made to extract .bank files and
//     * is not included with LibGPPK3. This tool requires the other files included with
//     * LibGGPK3 and should be extracted to the same directory.
//     * @param out
//     */
//    public void exportExtractExe ( Path out ) {
//        final String resource = "/com/github/frizzy/PoeDDSExtractor/Resources/ExtractGPPK.exe";
//        try ( InputStream is = ToolsUnpacker.class.getResourceAsStream( resource ) ) {
//            Files.copy( is, out );
//        } catch ( IOException e ) {
//            LOGGER.log( Level.SEVERE, e.getMessage(), e );
//        }
//    }

    /**
     * Downloads LibGPPK windows release and returns the path on disk.
     * <br>
     * <a href="https://stackoverflow.com/questions/13441720/download-binary-file-from-github-using-java">Stack Overflow</a>
     */
    public Path download(String downloadURL, String output) throws IOException
    {
        URL website = new URL(downloadURL);
        String fileName = getFileName(downloadURL);
        Path out = Paths.get( output + File.separator + fileName );

        try ( InputStream inputStream = website.openStream())
        {
            Files.copy(inputStream, out, StandardCopyOption.REPLACE_EXISTING);
        }

        return out;
    }

    /**
     * Exports the convert.bat file to the specified directory/file.
     * <br>
     * Returns true if successful.
     */
    public void exportBat ( Path out ) {
        try ( InputStream is = getStreamFor( "/com/github/frizzy/PoeDDSExtractor/Resources/convert.bat" ) ) {
            Files.copy( is, out );
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
        }
    }

    /**
     * Exports texconv.exe to the path provided.
     * <br>
     * Returns true if successful.
     */
    public boolean exportTexConvTo ( Path texPath ) {
        try ( InputStream stream = getStreamFor( "/com/github/frizzy/PoeDDSExtractor/Resources/texconv.exe" ) ) {
            Files.copy( stream, texPath );

            try ( InputStream cStream = getStreamFor( "/com/github/frizzy/PoeDDSExtractor/Resources/texconv_copyright.txt" ) ) {
                final String s = texPath.toString();
                final String p = s.substring( 0, s.lastIndexOf( "\\" ) + 1 );
                Files.copy( cStream, Path.of( p + File.separator + "texconv_copyright.txt" ) );
            }
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
            return false;
        }

        return true;
    }

    /**
     * Exports the LibGGPK3 tools needed.
     * <br>
     * Returns true if successful.
     */
    public boolean exportGGPKToolsTo ( Path zipPath, Path outPath ) {
        try ( InputStream stream = getStreamFor( "/com/github/frizzy/PoeDDSExtractor/Resources/LibGGPK3.zip" ) ) {
            Files.copy( stream, zipPath );
            unzip( zipPath, outPath );

            try ( InputStream cStream = getStreamFor( "/com/github/frizzy/PoeDDSExtractor/Resources/libggpk3_copyright.txt" ) ) {
                Files.copy( cStream, Path.of( outPath + File.separator + "libggpk3_copyright.txt" ) );
            }
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
            return false;
        }

        return true;
    }

    /**
     *
     * @param zipPath The path to the bank_tools.zip on disk.
     * @param outDirPath The directory the contents of bank
     */
    public boolean exportBankToolsTo( Path zipPath, Path outDirPath )  {
        try ( InputStream stream = getStreamFor( "/com/github/frizzy/PoeDDSExtractor/Resources/bank_tools.zip" ) ) {
            Files.copy( stream, zipPath);
            unzip( zipPath, outDirPath );
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
            return false;
        }

        return true;
    }

    /**
     * Extracts LibGPPK3 download.
     * <br>
     * <a href="https://stackoverflow.com/questions/10633595/java-zip-how-to-unzip-folder">Stack Overflow</a>
     */
    private void unzip(Path zip, Path targetDir)  {

        try ( InputStream is = new FileInputStream( zip.toFile() ) ) {
            targetDir = targetDir.toAbsolutePath();

            try ( ZipInputStream zipIn = new ZipInputStream(is)) {

                for ( ZipEntry ze; (ze = zipIn.getNextEntry()) != null; ) {

                    Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
                    if (!resolvedPath.startsWith(targetDir)) {
                        throw new RuntimeException("Entry with an illegal path: "
                                + ze.getName());
                    }
                    if (ze.isDirectory()) {
                        Files.createDirectories(resolvedPath);
                    } else {
                        Files.createDirectories(resolvedPath.getParent());
                        Files.copy(zipIn, resolvedPath);
                    }
                }
            }
        } catch ( IOException | RuntimeException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
        }
    }

    private InputStream getStreamFor ( final String resourcePath ) {
        return ToolsUnpacker.class.getResourceAsStream( resourcePath );
    }

    /**
     * I don't personally see why this is necessary, but I am going to leave it for now.
     * Eventually I want to remove the dependency on commons-io.
     * <a href="https://stackoverflow.com/questions/13441720/download-binary-file-from-github-using-java">Stack Overflow</a>
     */
    private String getFileName(String downloadURL) throws UnsupportedEncodingException {
        System.out.println( "Download URL " + downloadURL );
        String baseName = FilenameUtils.getBaseName(downloadURL);
        String extension = FilenameUtils.getExtension(downloadURL);
        String fileName = baseName + "." + extension;

        int questionMarkIndex = fileName.indexOf("?");
        if (questionMarkIndex != -1)
        {
            fileName = fileName.substring(0, questionMarkIndex);
        }

        fileName = fileName.replaceAll("-", "");
        return URLDecoder.decode(fileName, "UTF-8");
    }
}

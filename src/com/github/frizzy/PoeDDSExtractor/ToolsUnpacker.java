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

    /**
     * Exports the convert.bat file to the specified directory/file.
     * <br>
     * I was original here! I didn't copy it from Stack overflow
     */
    public void exportBat ( Path out ) {
        final String resource = "/com/github/frizzy/PoeDDSExtractor/Resources/convert.bat";
        try ( InputStream is = ToolsUnpacker.class.getResourceAsStream( resource ) ) {
            Files.copy( is, out );
        } catch ( IOException e ) {
            LOGGER.log( Level.SEVERE, e.getMessage(), e );
        }
    }

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
     * Extracts LibGPPK3 download.
     * <br>
     * <a href="https://stackoverflow.com/questions/10633595/java-zip-how-to-unzip-folder">Stack Overflow</a>
     */
    public void unzip(Path zip, Path targetDir)  {

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

package info.persistent.dex.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by Vasiliy on 22.11.2015
 */
public class DexUtils {

    private static final String CLASSES_DEX = "classes.dex";

    /**
     * Checks if input files array contain directories and
     * adds it's contents to the file list if so.
     * Otherwise just adds a file to the list.
     *
     * @return a List of file names to process
     */

    public static List<String> collectFileNames(String[] inputFileNames) {
        List<String> fileNames = new ArrayList<String>();
        for (String inputFileName : inputFileNames) {
            File file = new File(inputFileName);
            if (file.isDirectory()) {
                String dirPath = file.getAbsolutePath();
                for (String fileInDir: file.list()){
                    fileNames.add(dirPath + File.separator + fileInDir);
                }
            } else {
                fileNames.add(inputFileName);
            }
        }
        return fileNames;
    }

    /**
     * Checks if input files array contain directories and
     * adds it's contents to the file list if so.
     * Otherwise just adds a file to the list.
     *
     * @return a List of file names to process
     */

    public static List<String> collectFiles(File[] inputFiles) {
        List<String> fileNames = new ArrayList<String>();
        for (File file : inputFiles) {
            if (file.isDirectory()) {
                String dirPath = file.getAbsolutePath();
                for (String fileInDir: file.list()){
                    fileNames.add(dirPath + File.separator + fileInDir);
                }
            } else {
                fileNames.add(file.getAbsolutePath());
            }
        }
        return fileNames;
    }

    /**
     * Tries to open an input file as a Zip archive (jar/apk) with a
     * "classes.dex" inside.
     *
     * @param fileName the name of the file to open
     * @return a RandomAccessFile for classes.dex, or null if the input file
     *         is not a zip archive
     * @throws IOException if the file isn't found, or it's a zip and
     *         classes.dex isn't found inside
     */
    public static RandomAccessFile openInputFileAsZip(String fileName) throws IOException {
        ZipFile zipFile;

        /*
         * Try it as a zip file.
         */
        try {
            zipFile = new ZipFile(fileName);
        } catch (FileNotFoundException fnfe) {
            /* not found, no point in retrying as non-zip */
            System.err.println("Unable to open '" + fileName + "': " +
                    fnfe.getMessage());
            throw fnfe;
        } catch (ZipException ze) {
            /* not a zip */
            return null;
        }

        /*
         * We know it's a zip; see if there's anything useful inside.  A
         * failure here results in some type of IOException (of which
         * ZipException is a subclass).
         */
        ZipEntry entry = zipFile.getEntry(CLASSES_DEX);
        if (entry == null) {
            String message = "Unable to find '" + CLASSES_DEX +
                    "' in '" + fileName + "'";
            System.err.println(message);
            zipFile.close();
            throw new ZipException(message);
        }

        InputStream zis = zipFile.getInputStream(entry);

        /*
         * Create a temp file to hold the DEX data, open it, and delete it
         * to ensure it doesn't hang around if we fail.
         */
        File tempFile = File.createTempFile("dexdeps", ".dex");
        //System.out.println("+++ using temp " + tempFile);
        RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
        boolean deleted = tempFile.delete();
        System.out.println("Temp file deleted: " + deleted);

        /*
         * Copy all data from input stream to output file.
         */
        final int bufferSize = 32768;
        byte copyBuf[] = new byte[bufferSize];
        int actual;

        while (true) {
            actual = zis.read(copyBuf);
            if (actual == -1)
                break;

            raf.write(copyBuf, 0, actual);
        }

        zis.close();
        raf.seek(0);

        return raf;
    }

    /**
     * Opens an input file, which could be a .dex or a .jar/.apk with a
     * classes.dex inside.  If the latter, we extract the contents to a
     * temporary file.
     *
     * @param fileName the name of the file to open
     */
    public static RandomAccessFile openInputFile(String fileName) throws IOException {
        RandomAccessFile raf;

        raf = DexUtils.openInputFileAsZip(fileName);
        if (raf == null) {
            File inputFile = new File(fileName);
            raf = new RandomAccessFile(inputFile, "r");
        }

        return raf;
    }
}

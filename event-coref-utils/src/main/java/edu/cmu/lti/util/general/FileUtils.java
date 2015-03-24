package edu.cmu.lti.util.general;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A utility class for handling files.
 *
 * @author Jun Araki
 */
public class FileUtils {

    private static final String ENCODING_DEFAULT = System.getProperty("file.encoding");

    private static final String ENCODING_UTF8 = "UTF8";

    private static final String FILE_EXTENSION_SEPARATOR = ".";

    private static final String DIRECTORY_SEPARATOR = "/";

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);


    /**
     * Returns the name of the file or directory specified with the pathName.
     *
     * @param pathName
     * @return the name of the file or directory specified with the pathName
     */
    public static String getName(String pathName) {
        if (StringUtils.isNullOrEmptyString(pathName)) {
            return null;
        }

        return (new File(pathName)).getName();
    }

    /**
     * Returns the short name (without any extension) of the file or directory specified with the path
     * name. For instance, if input is '/tmp/sample.txt', then output is 'sample'.
     *
     * @param pathName
     * @return the short name (without any extensions) of the file or directory specified with the
     * path name
     */
    public static String getShortName(String pathName) {
        String fileOrDirName = getName(pathName);
        if (fileOrDirName == null) {
            return null;
        }

        return removeFileExtension(fileOrDirName);
    }

    /**
     * Returns the full file name based on the specified file short name and file extension.
     *
     * @param fileShortName
     * @param fileExtension
     * @return the full file name based on the specified file short name and file extension
     */
    public static String getFileName(String fileShortName, String fileExtension) {
        StringBuilder buf = new StringBuilder();
        buf.append(fileShortName);
        buf.append(fileExtension);

        return buf.toString();
    }

    /**
     * Returns the path based on the specified directory and file name.
     *
     * @param directoryName
     * @param fileName
     * @return the path based on the specified directory and file name.
     */
    public static String getFilePath(String directoryName, String fileName) {
        StringBuilder buf = new StringBuilder();
        buf.append(directoryName);
        buf.append(DIRECTORY_SEPARATOR);
        buf.append(fileName);

        return buf.toString();
    }

    /**
     * Returns the specified path name without a file extension. For instance, if input is
     * '/tmp.dir/sample.txt', then output is '/tmp.dir/sample'.
     *
     * @param pathName
     * @return the specified pathName without a file extension
     */
    public static String removeFileExtension(String pathName) {
        if (StringUtils.isNullOrEmptyString(pathName)) {
            return null;
        }

        File fileOrDir = new File(pathName);
        String parent = fileOrDir.getParent();
        String child = fileOrDir.getName();
        String escapedFileExtensionSeparator = "\\" + FILE_EXTENSION_SEPARATOR;
        child = child.split(escapedFileExtensionSeparator)[0];

        return (new File(parent, child)).getPath();
    }

    /**
     * Appends the specified string with an underscore to the specified file name. This method appends
     * the string just in front of the extension if the file name has it. For example,
     * appendStringToFileName("sample.txt", "_123") returns "sample_123.txt", and
     * appendStringToFileName("sample", "_123") returns "sample_123".
     *
     * @param fileName
     * @param str
     * @return the file name with the the specified string appended
     */
    public static String appendStringToFileName(String fileName, String str) {
        String extensionDelimeter = ".";
        StringBuilder buf = new StringBuilder();

        String extension = "";
        if (fileName.contains(extensionDelimeter)) {
            int index = fileName.indexOf(extensionDelimeter);
            extension = fileName.substring(index);
            fileName = fileName.substring(0, index);
        }

        buf.append(fileName);
        buf.append(str);
        buf.append(extension);

        return buf.toString();
    }

    /**
     * Tests whether the specified file or directory exists.
     *
     * @param fileOrDirPath
     * @return true if the specified file or directory exists; false otherwise
     */
    public static boolean exists(String fileOrDirPath) {
        if (StringUtils.isNullOrEmptyString(fileOrDirPath)) {
            return false;
        }

        File fileOrDirectory = new File(fileOrDirPath);
        if (fileOrDirectory.exists()) {
            return true;
        }

        return false;
    }

    /**
     * Tests whether the specified path denotes a directory.
     *
     * @param dirPath
     * @return true if the specified path denotes a directory; false otherwise
     */
    public static boolean isDirectory(String dirPath) {
        if (StringUtils.isNullOrEmptyString(dirPath)) {
            return false;
        }

        File dir = new File(dirPath);
        if (dir.isDirectory()) {
            return true;
        }

        return false;
    }

    /**
     * Creates the specified directory.
     *
     * @param dirPath
     * @return true if the specified directory is successfully creates; false otherwise
     */
    public static boolean mkdir(String dirPath) {
        File dir = new File(dirPath);
        if (dir.mkdirs()) {
            return true;
        }

        return false;
    }

    /**
     * Returns the content of the specified file.
     *
     * @param filePath
     * @return the content of the specified file
     */
    public static String readFile(String filePath) {
        return readFileWithEncoding(filePath, ENCODING_DEFAULT);
    }

    /**
     * Returns the content of the specified file.
     *
     * @param file
     * @return the content of the specified file
     */
    public static String readFile(File file) {
        return readFile(file.getAbsolutePath());
    }

    /**
     * Returns the content of the file which is located in the specified relative path of the
     * specified class.
     *
     * @param clazz
     * @param relativeFilePath
     * @return the content of the file which is located in the specified relative path of the
     * specified class
     */
    public static <T> String readFile(Class<T> clazz, String relativeFilePath) {
        StringBuilder buf = new StringBuilder();

        InputStream is = null;
        BufferedReader br = null;
        try {
            is = clazz.getClassLoader().getResourceAsStream(relativeFilePath);
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                buf.append(line + System.lineSeparator());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return buf.toString();
    }

    /**
     * Returns the content of the specified file with the specified encoding.
     *
     * @param filePath
     * @param encoding
     * @return the content of the specified file with the specified encoding
     */
    public static String readFileWithEncoding(String filePath, String encoding) {
        StringBuilder buf = new StringBuilder();
        BufferedReader fin = null;

        try {
            fin = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding));

            String line = null;
            while ((line = fin.readLine()) != null) {
                buf.append(line + System.lineSeparator());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return buf.toString();
    }

    /**
     * Returns the content of the specified file with the specified encoding.
     *
     * @param file
     * @param encoding
     * @return the content of the specified file with the specified encoding
     */
    public static String readFileWithEncoding(File file, String encoding) {
        return readFileWithEncoding(file.getAbsolutePath(), encoding);
    }

    /**
     * Returns the content of the specified file with utf8.
     *
     * @param filePath
     * @return the content of the specified file with utf8
     */
    public static String readFileWithUTF8(String filePath) {
        return readFileWithEncoding(filePath, ENCODING_UTF8);
    }

    /**
     * Returns the content of the specified file with utf8.
     *
     * @param file
     * @return the content of the specified file with utf8
     */
    public static String readFileWithUTF8(File file) {
        return readFileWithUTF8(file.getAbsolutePath());
    }

    /**
     * Returns a list of lines in the specified file.
     *
     * @param filePath
     * @return a list of lines in the specified file
     */
    public static List<String> readLines(String filePath) {
        return readLinesWithEncoding(filePath, ENCODING_DEFAULT);
    }

    /**
     * Returns a list of lines in the specified file.
     *
     * @param file
     * @return a list of lines in the specified file
     */
    public static List<String> readLines(File file) {
        return readLines(file.getAbsolutePath());
    }

    /**
     * Returns a list of lines in the file which is located in the specified relative path of the
     * specified class.
     *
     * @param clazz
     * @param relativeFilePath
     * @return a list of lines in the file which is located in the specified relative path of the
     * specified class
     */
    public static <T> List<String> readLines(Class<T> clazz, String relativeFilePath) {
        List<String> lines = new ArrayList<String>();

        InputStream is = null;
        BufferedReader br = null;
        try {
            is = clazz.getClassLoader().getResourceAsStream(relativeFilePath);
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lines;
    }

    /**
     * Returns a list of lines in the specified file with the specified encoding.
     *
     * @param filePath
     * @param encoding
     * @return a list of lines in the specified file with the specified encoding
     */
    public static List<String> readLinesWithEncoding(String filePath, String encoding) {
        List<String> lines = new ArrayList<String>();

        BufferedReader fin = null;
        try {
            fin = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), encoding));
            String line = null;
            while ((line = fin.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return lines;
    }

    /**
     * Returns a list of lines in the specified file with the specified encoding.
     *
     * @param file
     * @param encoding
     * @return a list of lines in the specified file with the specified encoding
     */
    public static List<String> readLinesWithEncoding(File file, String encoding) {
        return readLinesWithEncoding(file.getAbsolutePath(), encoding);
    }

    /**
     * Returns a list of lines in the specified file with utf8.
     *
     * @param filePath
     * @return a list of lines in the specified file with utf8
     */
    public static List<String> readLinesWithUTF8(String filePath) {
        return readLinesWithEncoding(filePath, ENCODING_UTF8);
    }

    /**
     * Returns a list of lines in the specified file with utf8.
     *
     * @param file
     * @return a list of lines in the specified file with utf8
     */
    public static List<String> readLinesWithUTF8(File file) {
        return readLinesWithUTF8(file.getAbsolutePath());
    }

    /**
     * Creates the specified file, and writes the specified content in it.
     *
     * @param filePath
     * @param content
     */
    public static void writeFile(String filePath, String content) {
        writeFileWithEncoding(filePath, content, ENCODING_DEFAULT);
    }

    /**
     * Creates the specified file, and writes the specified content in it.
     *
     * @param file
     * @param content
     */
    public static void writeFile(File file, String content) {
        writeFile(file.getAbsolutePath(), content);
    }

    /**
     * Creates the specified file, and writes the specified content in it with the specified encoding.
     *
     * @param filePath
     * @param content
     * @param encoding
     */
    public static void writeFileWithEncoding(String filePath, String content, String encoding) {
        BufferedWriter fout = null;

        try {
            fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), encoding));
            fout.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fout != null) {
                    fout.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates the specified file, and writes the specified content in it with the specified encoding.
     *
     * @param file
     * @param content
     * @param encoding
     */
    public static void writeFileWithEncoding(File file, String content, String encoding) {
        writeFileWithEncoding(file.getAbsolutePath(), content, encoding);
    }

    /**
     * Creates the specified file, and writes the specified content in it with utf8.
     *
     * @param filePath
     * @param content
     */
    public static void writeFileWithUTF8(String filePath, String content) {
        writeFileWithEncoding(filePath, content, ENCODING_DEFAULT);
    }

    /**
     * Creates the specified file, and writes the specified content in it with utf8.
     *
     * @param file
     * @param content
     */
    public static void writeFileWithUTF8(File file, String content) {
        writeFileWithUTF8(file.getAbsolutePath(), content);
    }

    /**
     * Appends the specified content to the specified file.
     *
     * @param filePath
     * @param content
     */
    public static void appendFile(String filePath, String content) {
        FileOutputStream fos = null;
        DataOutputStream dos = null;

        try {
            fos = new FileOutputStream(filePath, true);
            dos = new DataOutputStream(fos);
            dos.writeBytes(content);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes the specified file or directory.
     *
     * @param fileOrDirPath
     */
    public static void removeFileOrDirectory(String fileOrDirPath) {
        if (!exists(fileOrDirPath)) {
            return;
        }

        File fileOrDirectory = new File(fileOrDirPath);
        fileOrDirectory.delete();
    }

    /**
     * Returns a list of files under the specified directory.
     *
     * @param dirPath
     * @return a list of files under the specified directory
     */
    public static List<File> findFiles(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles();
        return Arrays.asList(files);
    }

    /**
     * Returns a list of files with the specified name under the specified directory.
     *
     * @param dirPath
     * @param fileName
     * @return a list of files with the specified name under the specified directory
     */
    public static List<File> findFilesWithName(String dirPath, final String fileName) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String fileNameStr) {
                return fileNameStr.equals(fileName);
            }
        });
        return Arrays.asList(files);
    }

    /**
     * Returns a list of files with the specified extension under the specified directory.
     *
     * @param dirPath
     * @param fileExtension
     * @return a list of files with the specified extension under the specified directory
     */
    public static List<File> findFilesWithExtension(String dirPath, final String fileExtension) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            return null;
        }

        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String fileName) {
                return fileName.endsWith(fileExtension);
            }
        });
        return Arrays.asList(files);
    }

    public static String getRandomFileName(String prefix) {
        return new String(prefix + new Double(Math.random() * 100000000).toString());
    }

    public static BufferedReader openFileRead(String path) throws IOException {
        return new BufferedReader(new FileReader(path));
    }

    public static BufferedWriter openFileWrite(String path) throws IOException {
        return new BufferedWriter(new FileWriter(path));
    }

    public static String getParentDirName(String path) {
        File parent = (new File(path)).getParentFile();
        if (parent == null) {
            return null;
        }

        return parent.getName();
    }

    /**
     * A simple tester method.
     */
    public static void main(String[] args) {
        String filePath = "C:\\Temp\\tmp.txt";
        logger.info(readFile(filePath));

        List<String> lines = readLines(filePath);
        for (int i = 0; i < lines.size(); i++) {
            logger.info("line " + (i + 1) + ": " + lines.get(i));
        }

        writeFile(filePath, "b\nc\nd\n");
    }

}

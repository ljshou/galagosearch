// BSD License (http://www.galagosearch.org/license)
package org.galagosearch.tupleflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;
import org.galagosearch.tupleflow.execution.Step;

/**
 * Lots of static methods here that have broad use.
 *
 * @author trevor
 */
public class Utility {
    /**
     * Builds a simple Sorter step that can be added to a TupleFlow stage.
     *
     * @param sortOrder An order object representing how and what to sort.
     * @return a Step object that can be added to a TupleFlow Stage.
     */
    public static Step getSorter(Order sortOrder) {
        Parameters p = new Parameters();
        p.add("class", sortOrder.getOrderedClass().getName());
        p.add("order", Utility.join(sortOrder.getOrderSpec()));
        return new Step(Sorter.class, p);
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String wrap(String t) {
        int start = 0;
        StringBuilder result = new StringBuilder();

        while (t.length() > start + 50) {
            int end = t.indexOf(" ", start + 50);

            if (end < 0) {
                break;
            }
            result.append(t, start, end);
            result.append('\n');
            start = end + 1;
        }

        result.append(t.substring(start));
        return result.toString();
    }

    public static String escape(String raw) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (c == '"') {
                builder.append("&quot;");
            } else if (c == '&') {
                builder.append("&amp;");
            } else if (c == '<') {
                builder.append("&gt;");
            } else if (c == '>') {
                builder.append("&lt;");
            } else if (c <= 127) {
                builder.append(c);
            } else {
                int unsigned = ((int) c) & 0xFFFF;

                builder.append("&#");
                builder.append(unsigned);
                builder.append(";");
            }
        }

        return builder.toString();
    }

    public static String strip(String source, String suffix) {
        if (source.endsWith(suffix)) {
            return source.substring(0, source.length() - suffix.length());
        }

        return null;
    }

    public static String makeString(byte[] word) {
        try {
            return new String(word, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is not supported by your Java Virtual Machine.");
        }
    }

    public static byte[] makeBytes(String word) {
        try {
            return word.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is not supported by your Java Virtual Machine.");
        }
    }

    /**
     * For an array master, returns
     * an array containing the last master.length-index elements.
     */
    public static String[] subarray(String[] master, int index) {
        if (master.length <= index) {
            return new String[0];
        } else {
            String[] sub = new String[master.length - index];
            System.arraycopy(master, index, sub, 0, sub.length);
            return sub;
        }
    }

    /**
     * Returns a string containing all the elements of args, space delimited.
     */
    public static String join(String[] args, String delimiter) {
        String output = "";
        StringBuilder builder = new StringBuilder();

        for (String arg : args) {
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(arg);
        }

        return builder.toString();
    }

    public static String join(String[] args) {
        return join(args, " ");
    }

    public static String caps(String input) {
        if (input.length() == 0) {
            return input;
        }
        char first = Character.toUpperCase(input.charAt(0));
        return "" + first + input.substring(1);
    }

    public static String plural(String input) {
        return input + "s";
    }

    public static int compare(int one, int two) {
        return one - two;
    }

    public static int compare(long one, long two) {
        long result = one - two;

        if (result > 0) {
            return 1;
        }
        if (result < 0) {
            return -1;
        }
        return 0;
    }

    public static int compare(double one, double two) {
        double result = one - two;

        if (result > 0) {
            return 1;
        }
        if (result < 0) {
            return -1;
        }
        return 0;
    }

    public static int compare(float one, float two) {
        float result = one - two;

        if (result > 0) {
            return 1;
        }
        if (result < 0) {
            return -1;
        }
        return 0;
    }

    public static int compare(String one, String two) {
        return one.compareTo(two);
    }

    public static int compare(byte[] one, byte[] two) {
        int sharedLength = Math.min(one.length, two.length);

        for (int i = 0; i < sharedLength; i++) {
            int a = ((int) one[i]) & 0xFF;
            int b = ((int) two[i]) & 0xFF;
            int result = a - b;

            if (result < 0) {
                return -1;
            }
            if (result > 0) {
                return 1;
            }
        }

        return one.length - two.length;
    }

    public static int hash(byte b) {
        return ((int) b) & 0xFF;
    }

    public static int hash(int i) {
        return i;
    }

    public static int hash(long l) {
        return (int) l;
    }

    public static int hash(double d) {
        return (int) (d * 100000);
    }

    public static int hash(float f) {
        return (int) (f * 100000);
    }

    public static int hash(String s) {
        return s.hashCode();
    }

    public static int hash(byte[] b) {
        int h = 0;
        for (int i = 0; i < b.length; i++) {
            h += 7 * h + b[i];
        }
        return h;
    }

    public static void deleteDirectory(File directory) throws IOException {
        for (File sub : directory.listFiles()) {
            if (sub.isDirectory()) {
                deleteDirectory(sub);
            } else {
                sub.delete();
            }
        }

        directory.delete();
    }

    public static File createTemporary() throws IOException {
        return createTemporary(1024 * 1024 * 1024);
    }

    public static long getUnixFreeSpace(String pathname) throws IOException {
        try {
            // BUGBUG: will not work on windows
            String[] command = {"df", "-Pk", pathname};
            Process process = Runtime.getRuntime().exec(command);
            InputStream procOutput = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(procOutput));

            // skip the first line
            reader.readLine();
            String line = reader.readLine();
            String[] fields = line.split("\\s+");
            reader.close();

            process.getErrorStream().close();
            process.getInputStream().close();
            process.getOutputStream().close();
            process.waitFor();

            long freeSpace = Long.parseLong(fields[3]) * 1024;
            return freeSpace;
        } catch (InterruptedException ex) {
            return 0;
        }
    }

    public static long getFreeSpace(String pathname) throws IOException {
        try {
            // this will only work in Java 1.6 or later
            Method m = File.class.getMethod("getUsableSpace");
            Long result = (Long) m.invoke(new File(pathname));
            return (long) result;
        } catch (IllegalAccessException e) {
            return getUnixFreeSpace(pathname);
        } catch (IllegalArgumentException e) {
            return getUnixFreeSpace(pathname);
        } catch (ExceptionInInitializerError e) {
            return getUnixFreeSpace(pathname);
        } catch (NoSuchMethodException e) {
            return getUnixFreeSpace(pathname);
        } catch (InvocationTargetException e) {
            throw (IOException) new IOException("Trouble calling File.getUsableSpace").initCause(e);
        }
    }

    public static File createTemporary(long requiredSpace) throws IOException {
        // try to find a prefs file for this
        String homeDirectory = System.getProperty("user.home");
        File prefsFile = new File(homeDirectory + "/" + ".galagotmp");
        ArrayList<String> roots = new ArrayList<String>();
        File temporary = null;

        if (prefsFile.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(prefsFile));
            String line;

            while ((line = reader.readLine()) != null) {
                roots.add(line.trim());
            }

            reader.close();
        }

        for (String root : roots) {
            long freeSpace = getFreeSpace(root);

            if (freeSpace < requiredSpace) {
                continue;
            }
            String logString = String.format("Found %6.3fMB >= %6.3fMB left on %s",
                                             freeSpace / 1048576.0, requiredSpace / 1048576.0, root);
            Logger.getLogger(Utility.class.toString()).info(logString);
            temporary = File.createTempFile("tupleflow", "", new File(root));
            break;
        }

        if (temporary == null) {
            temporary = File.createTempFile("tupleflow", "");
        }

        return temporary;
    }

    /**
     * Copies the data from file into the stream.  Note that this method
     * does not close the stream (in case you want to put more in it).
     * 
     * @param file
     * @param stream
     * @throws java.io.IOException
     */
    public static void copyFileToStream(File file, OutputStream stream) throws IOException {
        FileInputStream input = new FileInputStream(file);
        long longLength = file.length();
        final int fiveMegabytes = 5 * 1024 * 1024;

        while (longLength > 0) {
            int chunk = (int) Math.min(longLength, fiveMegabytes);
            byte[] data = new byte[chunk];
            input.read(data, 0, chunk);
            stream.write(data, 0, chunk);
            longLength -= chunk;
        }

        input.close();
    }

    /**
     * Copies the data from the InputStream to a file, then closes both when
     * finished.
     * 
     * @param stream
     * @param file
     * @throws java.io.IOException
     */
    public static void copyStreamToFile(InputStream stream, File file) throws IOException {
        FileOutputStream output = new FileOutputStream(file);
        final int oneMegabyte = 1 * 1024 * 1024;
        byte[] data = new byte[oneMegabyte];

        while (true) {
            int bytesRead = stream.read(data);

            if (bytesRead < 0) {
                break;
            }
            output.write(data, 0, bytesRead);
        }

        stream.close();
        output.close();
    }

    public static void calculateMessageDigest(File file, MessageDigest instance) throws IOException {
        FileInputStream input = new FileInputStream(file);
        final int oneMegabyte = 1024 * 1024;
        byte[] data = new byte[oneMegabyte];

        while (true) {
            int bytesRead = input.read(data);

            if (bytesRead < 0) {
                break;
            }
            instance.update(data, 0, bytesRead);
        }

        input.close();
    }

    public static HashSet<String> readFileToStringSet(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        HashSet<String> set = new HashSet<String>();
        String line;

        while ((line = reader.readLine()) != null) {
            set.add(line.trim());
        }

        reader.close();
        return set;
    }
}

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.persistent.dex;

import com.android.dexdeps.DexData;
import com.android.dexdeps.DexDataException;
import info.persistent.dex.models.DexNode;
import info.persistent.dex.utils.DexUtils;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {

    private boolean includeClasses;
    private String packageFilter;
    private int maxDepth = Integer.MAX_VALUE;
    private DexMethodCounts.Filter filter = DexMethodCounts.Filter.ALL;

    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.run(args);
    }

    /**
     * Start things up.
     */
    void run(String[] args) {
        try {
            String[] inputFileNames = parseArgs(args);
            int overallCount = 0;
            for (String fileName : DexUtils.collectFileNames(inputFileNames)) {
                System.out.println("Processing " + fileName);
                RandomAccessFile raf = DexUtils.openInputFile(fileName);
                DexData dexData = new DexData(raf);
                dexData.load();
                DexNode dexNode = DexMethodCounts.generate(
                    dexData, includeClasses, packageFilter, maxDepth, filter);
                overallCount += dexNode.output("");

                raf.close();
            }
            System.out.println("Overall method count: " + overallCount);
        } catch (UsageException ue) {
            usage();
            System.exit(2);
        } catch (IOException ioe) {
            if (ioe.getMessage() != null) {
                System.err.println("Failed: " + ioe);
            }
            System.exit(1);
        } catch (DexDataException dde) {
            /* a message was already reported, just bail quietly */
            System.exit(1);
        }
    }




    String[] parseArgs(String[] args) {
        int idx;

        for (idx = 0; idx < args.length; idx++) {
            String arg = args[idx];

            if (arg.equals("--") || !arg.startsWith("--")) {
                break;
            } else if (arg.equals("--include-classes")) {
                includeClasses = true;
            } else if (arg.startsWith("--package-filter=")) {
                packageFilter = arg.substring(arg.indexOf('=') + 1);
            } else if (arg.startsWith("--max-depth=")) {
                maxDepth =
                    Integer.parseInt(arg.substring(arg.indexOf('=') + 1));
            } else if (arg.startsWith("--filter=")) {
                filter = Enum.valueOf(
                    DexMethodCounts.Filter.class,
                    arg.substring(arg.indexOf('=') + 1).toUpperCase());
            } else {
                System.err.println("Unknown option '" + arg + "'");
                throw new UsageException();
            }
        }

        // We expect at least one more argument (file name).
        int fileCount = args.length - idx;
        if (fileCount == 0) {
            throw new UsageException();
        }
        String[] inputFileNames = new String[fileCount];
        System.arraycopy(args, idx, inputFileNames, 0, fileCount);
        return inputFileNames;
    }

    void usage() {
        System.err.print(
            "DEX per-package/class method counts v1.0\n" +
            "Usage: dex-method-counts [options] <file.{dex,apk,jar,directory}> ...\n" +
            "Options:\n" +
            "  --include-classes\n" +
            "  --package-filter=com.foo.bar\n" +
            "  --max-depth=N\n"
        );
    }


    private static class UsageException extends RuntimeException {}
}

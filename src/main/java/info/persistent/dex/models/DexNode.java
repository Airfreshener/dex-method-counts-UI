package info.persistent.dex.models;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by Vasiliy on 21.11.2015
 */
public class DexNode {
    private static final PrintStream out = System.out;
    private int count = 0;
    NavigableMap<String, DexNode> children = new TreeMap<String, DexNode>();

    public int output(String indent) {
        int overallCount = 0;
        if (indent.length() == 0) {
            out.println("<root>: " + count);
            overallCount += count;
        }
        indent += "    ";
        for (String name : children.navigableKeySet()) {
            DexNode child = children.get(name);
            out.println(indent + name + ": " + child.count);
            overallCount += child.output(indent);
        }
        return overallCount;
    }

    public int outputToFile(FileWriter file, String indent) throws IOException {
        int overallCount = 0;
        if (indent.length() == 0) {
            file.write("<root>: " + count + "\n");
            overallCount += count;
        }
        indent += "    ";
        for (String name : children.navigableKeySet()) {
            DexNode child = children.get(name);
            file.write(indent + name + ": " + child.count + "\n");
            overallCount += child.outputToFile(file, indent);
        }
        return overallCount;
    }

    public NavigableMap<String, DexNode> childs(){
        return children;
    }

    public int count(){
        return count;
    }
    public void count_increment(){
        count++;
    }
}

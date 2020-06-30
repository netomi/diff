package tech.neidhart.diff.editscript;

import org.netomi.jdiffutils.format.UnifiedDiffFormatter;
import org.netomi.jdiffutils.util.FileUtils;

import java.util.List;

public class Test {

    public static void main(String[] args) throws Exception {

         String origFileName = "src/test/resources/original.txt";
         String newFileName = "src/test/resources/modified.txt";

//         String origFileName = "src/test/resources/testA.txt"; // hamlet.txt";
//         String newFileName = "src/test/resources/testB.txt"; // hamlet2.txt";

//        String origFileName = "src/test/resources/hamlet.txt"; // hamlet.txt";
//        String newFileName = "src/test/resources/original.txt"; //hamlet2.txt";

//         String origFileName = "src/test/resources/simple.txt"; // hamlet.txt";
//         String newFileName = "src/test/resources/simple2.txt"; // hamlet2.txt";

        List<String> original = FileUtils.loadFile(origFileName);
        List<String> modified = FileUtils.loadFile(newFileName);

        EditScript<String> script = new OptimizedDiffAlgorithm<String>().getEditScript(original, modified, null);



        //new UnifiedDiffFormatter().format( patch, System.out );

        System.out.println(script);
    }

}

package org.netomi.jdiffutils;

import org.netomi.jdiffutils.format.ClassicDiffFormatter;
import org.netomi.jdiffutils.format.SideBySideFormatter;
import org.netomi.jdiffutils.format.UnifiedDiffFormatter;
import org.netomi.jdiffutils.transform.CollapseWhitespaceTransformer;
import org.netomi.jdiffutils.transform.RemoveTrailingWhitespaceTransformer;
import org.netomi.jdiffutils.transform.Transformer;

public class Test {

    public static void main(String[] args) throws Exception {

         String origFileName = "src/test/resources/original.txt";
         String newFileName = "src/test/resources/modified.txt";

//         String origFileName = "src/test/resources/hamlet.txt"; // hamlet.txt";
//         String newFileName = "src/test/resources/hamlet2.txt"; // hamlet2.txt";

//        String origFileName = "src/test/resources/LNDSYN.txt"; // hamlet.txt";
//        String newFileName = "src/test/resources/hamlet.txt"; //hamlet2.txt";

//         String origFileName = "src/test/resources/simple.txt"; // hamlet.txt";
//         String newFileName = "src/test/resources/simple2.txt"; // hamlet2.txt";

        long start = System.currentTimeMillis();
        //Transformer<String> transformer = new TransformerChain<String>(new LowerCaseTransformer(), new CollapseWhitespaceTransformer());
        Transformer<String> transformer = new RemoveTrailingWhitespaceTransformer(); // LowerCaseTransformer();
        Patch patch = DiffUtils.diff(origFileName, newFileName);
        long end = System.currentTimeMillis();
        System.out.println("diff took " + (end - start) + " ms");

        //new ClassicDiffFormatter().format(patch, System.out);
        //new SideBySideFormatter(80).format(patch, System.out);
        new UnifiedDiffFormatter().format( patch, System.out );

//        Patch p = DiffUtils.loadPatch("src/test/resources/hamlet.diff");
//
//        new EditScriptFormatter().format(p, System.out);
        
        // System.out.println( "xxxxxxxxxxxxxxxx" );
        // SequencesComparator<String> myers2 = new SequencesComparator<String>( originalFile, newFile );
        // start = System.currentTimeMillis();
        // updatedScript = myers2.getScript();
        // end = System.currentTimeMillis();
        // System.out.println( "took " + ( end - start ) + " ms" + " lines: " + originalFile.size() );
        // updatedScript.visit( new ReplacementsFinder<String>( new MyReplacementsHandler() ) );

    }

}

package com.ufrgs;

import java.util.List;

public class Main {

    static String inputDir;
    static String outputDir;

    public static void main(String[] args) {

        if (args.length == 4) { // As specified by Max

            // Generate file with rectangles
            inputDir = args[0];
            int width = Integer.valueOf(args[1]);
            int height = Integer.valueOf(args[2]);
            outputDir = args[3];

            Entity root = Parser.buildHierarchy(inputDir);
            Rectangle baseRectangle = new Rectangle(width, height);
            new TreemapManager(root, baseRectangle);

        } else {
            argsError();
        }
    }

    private static void argsError() {
        System.out.println("Usage: \njava -cp ./bin com.ufrgs.Main input_dir width height output_dir");
        System.out.println("Width and Height are given in pixels (integers).");
    }
}

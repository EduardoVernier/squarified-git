package com.ufrgs;

import java.util.List;

public class Main {

//    static String inputDir = "/home/eduardo/PycharmProjects/dynamic-treemap-resources/dataset/hiv";
    static String inputDir = "./dataset/test";

    static String outputDir = "./output/test";

    public static void main(String[] args) {


        Entity root = Parser.buildHierarchy(inputDir);

        Rectangle baseRectangle = new Rectangle(1000, 1000);
        new TreemapManager(root, baseRectangle);
    }
}

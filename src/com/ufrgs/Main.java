package com.ufrgs;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        String dirPath = "./dataset/test";

        Entity root = Parser.buildHierarchy(dirPath);

        Rectangle baseRectangle = new Rectangle(1000, 1000);
        new TreemapManager(root, baseRectangle);
    }
}

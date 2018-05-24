package com.ufrgs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.Double.max;
import static java.lang.Math.pow;

public class TreemapManager {

    private Rectangle baseRectangle;
    private Entity root;
    private Treemap rootTreemap;
    private double normalizer = 0;
    private int revision = 0;
    private int nRevisions;


    TreemapManager(Entity root, Rectangle baseRectangle) {
        // Clean/create output directories
        File[] files = new File(Main.outputDir).listFiles();
        if (files != null && files.length > 0) {
            Arrays.stream(files).forEach(File::delete);
        }
        new File(Main.outputDir).mkdirs(); // In case path doesn't exist


        this.root = root;
        this.baseRectangle = baseRectangle;
        this.nRevisions = root.getNumberOfRevisions();

        // = new Treemap(root.getChildren(), this.baseRectangle.copy());
        Rectangle rectangle = this.baseRectangle.copy();
        this.rootTreemap = squarifiedToLT("", root.getChildren(), rectangle);
//
////        this.rootTreemap.origin.baseRectangle = baseRectangle;
//        this.rootTreemap.computeTreemap(this.revision);

        writeRectanglesToFile(this.rootTreemap, this.revision);

        for (int i = 1; i < this.nRevisions; ++i) {
            nextRevision();
        }
    }

    public void nextRevision() {
        revision++;

        // Rearrange cell with new weights
        // this.rootTreemap.origin.rectangle = this.baseRectangle.copy();
        this.rootTreemap.computeTreemap(this.revision);

        writeRectanglesToFile(this.rootTreemap, this.revision);
    }

    private Treemap squarifiedToLT(String treemapId, List<Entity> originalEntityList, Rectangle rectangle) {

        List<Entity> entityList = new ArrayList<>(); // First copy
        for (Entity entity : originalEntityList) {
            entityList.add(entity);
        }

        // Filter elements
        entityList.removeIf(entity -> entity.getWeight(0) == 0.0);
        // Sort elements
        entityList.sort(Comparator.comparing(o -> ((Entity) o).getWeight(0)).reversed());
        normalize(entityList, rectangle.width * rectangle.height);

        // Make a copy of the entities we are adding
        List<Entity> entityListCopy = new ArrayList<>();
        for (Entity entity : entityList) {
            entityListCopy.add(entity);
        }

        // Initialize treemap
        Treemap treemap = new Treemap(treemapId, originalEntityList, rectangle.copy());
        Block outsideBlock = new Block();
        treemap.origin = outsideBlock;

        List<Entity> currentRow = new ArrayList<>();

        while (true) {

            boolean verticalCut = rectangle.width > rectangle.height;

            if (!entityList.isEmpty() && improvesRatio(currentRow, getNormalizedWeight(entityList.get(0)), rectangle.getShortEdge())) {
                currentRow.add(entityList.get(0));
                entityList.remove(0);

            } else {
                // Convert current row of entities into blocks
                double area = 0;
                List<Block> blockList = new ArrayList<>();
                for (Entity entity : currentRow) {
                    blockList.add(new Block(entity));
                    area += entity.getWeight(0);
                }
                area *= normalizer;

                // Connect blocks horizontally or vertically depending on the squarified layout
                if (verticalCut) {
                    for (int i = 0; i < blockList.size() - 1; ++i) {
                        blockList.get(i).addBottomBlock(blockList.get(i + 1));
                    }
                } else {
                    for (int i = 0; i < blockList.size() - 1; ++i) {
                        blockList.get(i).addRightBlock(blockList.get(i + 1));
                    }
                }
                // Connect "chain" to outside block
                outsideBlock.addCentralBlock(blockList.get(0));

                // Recompute remaining baseRectangle
                if (verticalCut) {
                    rectangle.x = rectangle.x + area / rectangle.height;
                    rectangle.width = rectangle.width - area / rectangle.height;
                } else {
                    rectangle.y = rectangle.y + area / rectangle.width;
                    rectangle.height = rectangle.height - area / rectangle.width;
                }

                // Reset outside block for new iteration
                if (verticalCut) {
                    Block newOutside = new Block();
                    outsideBlock.addRightBlock(newOutside);
                    outsideBlock = newOutside;
                } else {
                    Block newOutside = new Block();
                    outsideBlock.addBottomBlock(newOutside);
                    outsideBlock = newOutside;
                }
                currentRow.clear();
            }

            if(entityList.isEmpty() && currentRow.isEmpty()) {
                break;
            }
        }

        // Compute rectangles
        treemap.computeTreemap(this.revision);

        // Make recursive calls
        for (Entity entity : entityListCopy) {
            if (entity.getChildren().size() > 0 && entity.getWeight(0) > 0.0) {
                Rectangle allowedArea = treemap.findBlock(treemap.origin, entity.getId()).rectangle.copy();
                // Treemap newTreemap = new Treemap(entity.getId(), entity.getChildren(), allowedArea);
                Treemap newTreemap = squarifiedToLT(entity.getId(), entity.getChildren(), allowedArea);
                treemap.addTreemap(newTreemap);
            }
        }
        return treemap;
    }

    // Test if adding a new entity to row improves ratios (get closer to 1)
    boolean improvesRatio(List<Entity> currentRow, double nextEntity, double length) {

        if (currentRow.size() == 0) {
            return true;
        }

        double minCurrent = Double.MAX_VALUE, maxCurrent = Double.MIN_VALUE;
        for (Entity entity : currentRow) {
            if (getNormalizedWeight(entity) > maxCurrent) {
                maxCurrent = getNormalizedWeight(entity);
            }

            if (getNormalizedWeight(entity) < minCurrent) {
                minCurrent = getNormalizedWeight(entity);
            }
        }

        double minNew = (nextEntity < minCurrent) ? nextEntity : minCurrent;
        double maxNew = (nextEntity > maxCurrent) ? nextEntity : maxCurrent;

        double sumCurrent = 0;
        for (Entity entity : currentRow) {
            sumCurrent += getNormalizedWeight(entity);
        }
        double sumNew = sumCurrent + nextEntity;

        double currentRatio = max(pow(length, 2) * maxCurrent / pow(sumCurrent, 2),
                pow(sumCurrent, 2) / (pow(length, 2) * minCurrent));
        double newRatio = max(pow(length, 2) * maxNew / pow(sumNew, 2),
                pow(sumNew, 2) / (pow(length, 2) * minNew));

        return currentRatio >= newRatio;
    }

    private void normalize(List<Entity> entityList, double area) {
        double sum = 0;
        for (Entity entity : entityList) {
            sum += entity.getWeight(revision);
        }
        this.normalizer = area / sum;
    }

    private double getNormalizedWeight(Entity entity) {
        return entity.getWeight(this.revision) * this.normalizer;
    }

    private void writeRectanglesToFile(Treemap treemap, int revision) {

        List<String> lines = new ArrayList<>();
        Stack stack = new Stack<Treemap>();
        stack.push(treemap);
        while (!stack.empty()) {
            Treemap tm = (Treemap) stack.pop();

            // Get parent ids
            List<String> parentIds = new ArrayList<>();
            for (Treemap childTreemap : tm.treemapList) {
                parentIds.add(childTreemap.id);
            }

            addLine(lines, tm.origin, parentIds);
            for (Treemap childTreemap : tm.treemapList) {
                stack.push(childTreemap);
            }
        }

        lines.sort(String.CASE_INSENSITIVE_ORDER);
//        for (String line : lines) {
//            System.out.println(line);
//        }

        Path file = Paths.get(String.format("%s/t%d.rect", Main.outputDir, revision));

        try {
            Files.write(file, lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addLine(List<String> lines, Block block, List<String> parentIds) {

        if (block.id != null &&
                !parentIds.contains(block.id) &&
                block.weightList.get(this.revision) > 0.0) {

            lines.add(String.format(Locale.ROOT, "%s,%.10f,%.10f,%.10f,%.10f",
                      block.id, block.rectangle.x, block.rectangle.y, block.rectangle.width, block.rectangle.height));
        }

        if(block.central != null) {
            addLine(lines, block.central, parentIds);
        }
        if(block.right != null) {
            addLine(lines, block.right, parentIds);
        }
        if(block.bottom != null) {
            addLine(lines, block.bottom, parentIds);
        }
    }

}

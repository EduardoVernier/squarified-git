package com.ufrgs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static java.lang.Double.max;
import static java.lang.Math.pow;

public class TreemapManager {

    private Rectangle baseRectangle;
    private Entity root;
    private Treemap treemap;
    private double normalizer = 0;
    private int revision = 0;


    TreemapManager(Entity root, Rectangle baseRectangle) {
        this.root = root;
        this.baseRectangle = baseRectangle;
        this.treemap = new Treemap();

        Rectangle rectangle = new Rectangle(baseRectangle.x, baseRectangle.y, baseRectangle.width, baseRectangle.height);
        squarifiedToLT(root.getChildren(), rectangle);

        this.treemap.origin.rectangle = this.baseRectangle;
        computeTreemapCoordinates(this.treemap.origin);

        writeRectanglesToFile(this.treemap, this.revision);
    }

    private void squarifiedToLT(List<Entity> entityList, Rectangle rectangle) {

        // Sort elements
        entityList.sort(Comparator.comparing(o -> ((Entity) o).getWeight(0)).reversed());
        normalize(entityList, rectangle.width * rectangle.height);

        Block outsideBlock = new Block();
        this.treemap.origin = outsideBlock;

        List<Entity> currentRow = new ArrayList<>();

        while (!entityList.isEmpty()) {

            boolean verticalCut = rectangle.width >= rectangle.height;

            if (improvesRatio(currentRow, getNormalizedWeight(entityList.get(0)), rectangle.getShortEdge())) {
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

                // Recompute remaining rectangle
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
        }
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


    //  ---------
    //  | C |   |
    //  |---| R |
    //  | B |   |
    //  ---------
    private void computeTreemapCoordinates(Block block) {

        if (block.right != null && block.bottom != null) {

            double baseWidth = block.rectangle.width;
            double baseHeight = block.rectangle.height;
            // C coordinates
            block.rectangle.width = ((block.getCentralWeight(revision) + block.bottom.getFullWeight(revision)) / (block.getCentralWeight(revision) + block.bottom.getFullWeight(revision) + block.right.getFullWeight(revision))) * baseWidth;
            block.rectangle.height = (block.getCentralWeight(revision) / (block.getCentralWeight(revision) + block.bottom.getFullWeight(revision))) * baseHeight;

            if (Double.isNaN(block.rectangle.height) || Double.isInfinite(block.rectangle.height)) {
                block.rectangle.height = 0;
            }

            if (Double.isNaN(block.rectangle.width) || Double.isInfinite(block.rectangle.width)) {
                block.rectangle.width = 0;
            }

            // B coordinates
            block.bottom.rectangle.x = block.rectangle.x;
            block.bottom.rectangle.width = block.rectangle.width;
            block.bottom.rectangle.y = block.rectangle.y + block.rectangle.height;
            block.bottom.rectangle.height = baseHeight - block.rectangle.height;

            // R coordinates
            block.right.rectangle.x = block.rectangle.x + block.rectangle.width;
            block.right.rectangle.width = baseWidth - block.rectangle.width;
            block.right.rectangle.y = block.rectangle.y;
            block.right.rectangle.height = baseHeight;

            computeTreemapCoordinates(block.right);
            computeTreemapCoordinates(block.bottom);

        } else if (block.right != null) {

            double baseWidth = block.rectangle.width;

            // C coordinates - Only the width changes
            block.rectangle.width = (block.getCentralWeight(revision) / (block.getCentralWeight(revision) + block.right.getFullWeight(revision))) * baseWidth;
            if (Double.isNaN(block.rectangle.width) || Double.isInfinite(block.rectangle.width)) {
                block.rectangle.width = 0;
            }
            // R coordinates
            block.right.rectangle.x = block.rectangle.x + block.rectangle.width;
            block.right.rectangle.width = baseWidth - block.rectangle.width;
            block.right.rectangle.y = block.rectangle.y;
            block.right.rectangle.height = block.rectangle.height;

            computeTreemapCoordinates(block.right);

        } else if (block.bottom != null) {

            double baseHeight = block.rectangle.height;

            // C coordinates - Only the height changes
            block.rectangle.height = (block.getCentralWeight(revision) / (block.getCentralWeight(revision) + block.bottom.getFullWeight(revision))) * baseHeight;
            if (Double.isNaN(block.rectangle.height) || Double.isInfinite(block.rectangle.height)) {
                block.rectangle.height = 0;
            }

            // B coordinates
            block.bottom.rectangle.x = block.rectangle.x;
            block.bottom.rectangle.width = block.rectangle.width;
            block.bottom.rectangle.y = block.rectangle.y + block.rectangle.height;
            block.bottom.rectangle.height = baseHeight - block.rectangle.height;

            computeTreemapCoordinates(block.bottom);
        }

        if (block.central != null) {
            block.central.rectangle = new Rectangle(block.rectangle.x, block.rectangle.y, block.rectangle.width, block.rectangle.height);
            computeTreemapCoordinates(block.central);
        }
    }



    private void writeRectanglesToFile(Treemap treemap, int revision) {

        List<String> lines = new ArrayList<>();
        addLine(lines, treemap.origin);

        for (String line : lines) {
            System.out.println(line);
        }
    }

    private void addLine(List<String> lines, Block block) {

        if (block.id != null) {
            lines.add(String.format(Locale.ROOT, "%s,%.10f,%.10f,%.10f,%.10f",
                      block.id, block.rectangle.x, block.rectangle.y, block.rectangle.width, block.rectangle.height));
        }

        if(block.central != null) {
            addLine(lines, block.central);
        }
        if(block.right != null) {
            addLine(lines, block.right);
        }
        if(block.bottom != null) {
            addLine(lines, block.bottom);
        }
    }

}

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

        Rectangle rectangle = this.baseRectangle.copy();
        squarifiedToLT(root.getChildren(), rectangle);

        rectangle = this.baseRectangle.copy();
        this.treemap.origin.rectangle = rectangle;
        this.treemap.computeTreemapCoordinates(this.revision);

        writeRectanglesToFile(this.treemap, this.revision);

        nextRevision();
    }

    public void nextRevision() {
        revision++;
        this.treemap.origin.rectangle = this.baseRectangle.copy();
        this.treemap.computeTreemapCoordinates(this.revision);
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

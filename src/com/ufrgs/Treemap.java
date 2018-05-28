package com.ufrgs;

import java.util.ArrayList;
import java.util.List;

public class Treemap {

    String id;
    Block origin;
    List<Treemap> treemapList;
    Rectangle baseRectangle;
    private List<Entity> entityList;

    public Treemap(String id, List<Entity> entityList, Rectangle rectangle) {
        this.id = id;
        this.baseRectangle = rectangle.copy();
        this.entityList = new ArrayList<>();
        for (Entity entity : entityList) {
            this.entityList.add(entity);
        }
        this.treemapList = new ArrayList<>();
    }

    public void computeTreemap(int revision) {

        if (this.origin != null) {
            this.origin.rectangle = baseRectangle.copy();
            computeCoordinates(this.origin, revision);
        }

        for (Entity entity : entityList) {
            // Find out which entities must be added
            if (entity.getAdditionRevision() == revision && revision != 0 ) {
                // Add them, recomputing treemap after each insertion
                if (findBlock(this.origin, entity.getId()) == null) {
                    addItem(entity);
                }

                // Trigger children treemap computation (reset origin coords)
                this.origin.rectangle = baseRectangle.copy();
                computeCoordinates(this.origin, revision);

                // Make recursive calls to create treemaps
                if (entity.getChildren().size() > 0) {
                    boolean exists = false;
                    for (Treemap tm : this.treemapList) {
                        if (tm.id.equals(entity.getId())) {
                            exists = true;
                        }
                    }
                    if (exists) {
                        break;
                    }

                    Rectangle allowedArea = this.findBlock(this.origin, entity.getId()).rectangle.copy();
                    Treemap newTreemap = new Treemap(entity.getId(), entity.getChildren(), allowedArea);
                    this.addTreemap(newTreemap);
                    newTreemap.computeTreemap(revision);
                }
            }
        }

        // Trigger treemap computation for children treemaps
        for (Treemap treemap : treemapList) {
            treemap.baseRectangle = this.findBlock(this.origin, treemap.id).rectangle.copy();
            treemap.computeTreemap(revision);
        }
    }

    //  ---------
    //  | C |   |
    //  |---| R |
    //  | B |   |
    //  ---------
    private void computeCoordinates(Block block, int revision) {

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

            computeCoordinates(block.right, revision);
            computeCoordinates(block.bottom, revision);

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

            computeCoordinates(block.right, revision);

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

            computeCoordinates(block.bottom, revision);
        }

        if (block.central != null) {
            block.central.rectangle = new Rectangle(block.rectangle.x, block.rectangle.y, block.rectangle.width, block.rectangle.height);
            computeCoordinates(block.central, revision);
        }
    }

    public void addItem(Entity entity) {
        if (origin == null) {
            origin = new Block(entity);
        } else {
            Block receiver = findWorstAspectRatioBlock(origin);

            if (receiver.rectangle.width > receiver.rectangle.height + 0.0001) {
                if (receiver.right == null) {
                    receiver.right = new Block(entity);
                    // System.out.println("Right insert " + receiver.right.id + " into " + receiver.id);
                } else {
                    if (receiver.central == null) {
                        receiver.central = new Block(receiver.id, receiver.weightList);
                        receiver.central.right = new Block(entity);
                        // Reset upper level
                        receiver.id = null;
                        receiver.weightList = new ArrayList<>();
                    } else {
                        if (receiver.central.right == null) {
                            receiver.central.right = new Block(entity);
                        } else {
                            // System.out.print("WEIRD CENTRAL RIGHT INSERT. ");
                            Block temp = receiver.central.right;
                            receiver.central.right = new Block(entity);
                            receiver.central.right.right = temp;
                        }
                    }
                    // System.out.println("Special Right insert " + receiver.central.right.id + " into " + receiver.central);
                }
            } else {
                if (receiver.bottom == null) {
                    receiver.bottom = new Block(entity);
                    // System.out.println("Bottom insert " + receiver.bottom.id + " into " + receiver.id);
                } else {
                    if (receiver.central == null) {
                        receiver.central = new Block(receiver.id, receiver.weightList);
                        receiver.central.bottom = new Block(entity);
                        // Reset upper level
                        receiver.id = null;
                        receiver.weightList = new ArrayList<>();
                    } else {
                        if (receiver.central.bottom == null) {
                            receiver.central.bottom = new Block(entity);
                        } else {
                            // System.out.print("WEIRD CENTRAL BOTTOM INSERT. ");
                            Block temp = receiver.central.bottom;
                            receiver.central.bottom = new Block(entity);
                            receiver.central.bottom.bottom = temp;
                        }
                    }
                    // System.out.println("Special Bottom insert " + receiver.central.bottom.id + " into " + receiver.central);
                }
            }
        }
    }

    private Block findWorstAspectRatioBlock(Block block) {
        // Find worst aspect ratio block inside the argument block
        Block bestCandidate = block;
        double worstAR = block.rectangle.getAspectRatio();

        if (block.central != null) {
            Block temp = findWorstAspectRatioBlock(block.central);
            if (temp != null && temp.rectangle.getAspectRatio() < worstAR) {
                bestCandidate = temp;
                worstAR = temp.rectangle.getAspectRatio();
            }
        }

        if (block.right != null) {
            Block temp = findWorstAspectRatioBlock(block.right);
            if (temp != null && temp.rectangle.getAspectRatio() < worstAR) {
                bestCandidate = temp;
                worstAR = temp.rectangle.getAspectRatio();
            }
        }

        if (block.bottom != null) {
            Block temp = findWorstAspectRatioBlock(block.bottom);
            if (temp != null && temp.rectangle.getAspectRatio() < worstAR) {
                bestCandidate = temp;
            }
        }
        return bestCandidate;
    }

    public void addTreemap(Treemap newTreemap) {
        this.treemapList.add(newTreemap);
    }

    public Block findBlock(Block block, String itemId) {

        if (block == null) {
            return null;
        }

        if (block.id != null && block.id.equals(itemId)) {
            return block;
        } else {
            Block found = null;
            if (block.central != null) {
                Block temp = findBlock(block.central, itemId);
                if (temp != null) {
                    found = temp;
                }
            }
            if (block.right != null) {
                Block temp = findBlock(block.right, itemId);
                if (temp != null) {
                    found = temp;
                }
            }
            if (block.bottom != null) {
                Block temp = findBlock(block.bottom, itemId);
                if (temp != null) {
                    found = temp;
                }
            }
            return found;
        }
    }
}



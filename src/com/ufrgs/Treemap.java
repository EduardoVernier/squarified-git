package com.ufrgs;

import java.util.List;

public class Treemap {

    Block origin;
    List<Treemap> treemapList;


    public void computeTreemapCoordinates(int revision) {
        computeCoordinates(this.origin, revision);

        // TODO: compute children
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
}



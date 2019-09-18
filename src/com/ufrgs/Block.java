package com.ufrgs;

import java.util.ArrayList;
import java.util.List;

public class Block {

    String id;
    List<Double> weightList = new ArrayList<>();
    Block central, bottom, right;
    Rectangle rectangle = new Rectangle(0, 0);

    public Block() {

    }

    public Block(String id, List<Double> weightList) {
        this.id = id;
        for (double weight : weightList) {
            this.weightList.add(weight);
        }
    }

    public Block(Entity entity) {
        this.id = entity.getId();
        this.weightList = entity.getWeightList();
    }

    public void addCentralBlock(Block block) {
        this.central = block;
    }

    public void addRightBlock(Block block) {
        this.right = block;
    }

    public void addBottomBlock(Block block) {
        this.bottom = block;
    }


    public double getFullWeight(int revision) {
        double fullWeight = 0;
        if (!weightList.isEmpty()) {
            fullWeight = weightList.get(revision);
        }

        if (this.central != null) {
            fullWeight += this.central.getFullWeight(revision);
        }

        if (this.right != null) {
            fullWeight += this.right.getFullWeight(revision);
        }

        if (this.bottom != null) {
            fullWeight += this.bottom.getFullWeight(revision);
        }
        return fullWeight;
    }

    public double getCentralWeight(int revision) {
        if (this.central == null) {
            if (weightList.isEmpty()) {
                return 0;
            } else {
                return this.weightList.get(revision);
            }
        } else {
            return this.central.getFullWeight(revision);
        }
    }

    @Override
    public String toString() {
        return id + "{" +
                "c=" + central +
                ", r=" + right +
                ", b=" + bottom +
                '}';
    }
}

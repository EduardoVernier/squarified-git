package com.ufrgs;

import java.util.ArrayList;
import java.util.List;

public class Entity {

    private String id;
    private String shortId;
    private String printId = "";
    private List<Double> weightList;
    public List<Rectangle> rectangleList;
    private List<Entity> children;

    public Entity(String id, int numberOfRevisions) {

        this.id = id;
        String split[] = getId().split("/");
        this.shortId = split[split.length - 1];

        // Initialize lists
        children = new ArrayList<>();
        weightList = new ArrayList<>(numberOfRevisions);
        for (int i = 0; i < numberOfRevisions; ++i) {
            weightList.add(0.0);
        }
    }

    public String getId() {
        return id;
    }

    public int getNumberOfRevisions() {
        return weightList.size();
    }

    public double getWeight(int revision) {
        return weightList.get(revision);
    }

    public void setWeight(double weight, int revision) {
        weightList.set(revision, weight);
    }

    public void addChild(Entity entity) {
        children.add(entity);
    }

    public List<Double> getWeightList() {
        return weightList;
    }

    public List<Entity> getChildren() {
        return children;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public int getAdditionRevision() {
        for (int i = 0; i <  weightList.size(); ++i) {
            if (weightList.get(i) > 0.0) {
                return i;
            }
        }
        return weightList.size();
    }

    @Override
    public String toString() {
        return  "id='" + id + '\'' +
                ", weightList=" + weightList;
    }
}
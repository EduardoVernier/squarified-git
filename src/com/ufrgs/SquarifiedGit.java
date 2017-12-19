package com.ufrgs;

import java.util.Comparator;
import java.util.List;

public class SquarifiedGit {

    public SquarifiedGit(List<List<Entity>> revisionList, double width, double height) {



    }


    public void squarify(List<Entity> entityList, ) {
        // Sort using entities weight -- layout tends to turn out better
        entityList.sort(Comparator.comparing(o -> ((Entity) o).getWeight(0)).reversed());

    }
}

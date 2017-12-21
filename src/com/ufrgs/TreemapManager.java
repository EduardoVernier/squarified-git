package com.ufrgs;

import java.util.Comparator;
import java.util.List;

public class TreemapManager {

    private Rectangle baseRectangle;
    private Entity root;
    private Treemap origin;

    TreemapManager(Entity root, Rectangle baseRectangle) {
        this.root = root;
        this.baseRectangle = baseRectangle;
        this.origin = squarifiedToLT(root.getChildren(), baseRectangle);

    }

    private Treemap squarifiedToLT(List<Entity> entityList, Rectangle rectangle) {

        // Sort elements
        entityList.sort(Comparator.comparing(o -> ((Entity) o).getWeight(0)).reversed());

        //

        return new Treemap();

    }
}

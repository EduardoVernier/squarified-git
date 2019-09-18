package com.ufrgs;

public class Rectangle {

    public double x, y, width, height;

    public Rectangle(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(double width, double height) {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
    }

    public double getShortEdge() {
        return (width < height) ? width : height;
    }

    public Rectangle copy() {
        return new Rectangle(this.x, this.y, this.width, this.height);
    }

    public double getAspectRatio() {
        if (width == 0 || height == 0) {
            return 1;
        }
        return Math.min(width/height, height/width);
    }
}

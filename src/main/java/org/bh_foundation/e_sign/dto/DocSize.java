package org.bh_foundation.e_sign.dto;


public class DocSize {

    private float width;
    private float height;

    public DocSize(
            float width,
            float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

}

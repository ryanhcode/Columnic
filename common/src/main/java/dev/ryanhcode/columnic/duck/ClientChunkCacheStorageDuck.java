package dev.ryanhcode.columnic.duck;

public interface ClientChunkCacheStorageDuck {

    boolean inRange(int x, int y, int z);
    int getIndex(int x, int y, int z);

    int getViewCenterY();
    void setViewCenterY(int y);

}

package com.forlulz.lol.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ResourceHero {

    private final static Random rnd = new Random();

    private String name;
    private List<String> soundAssetPaths;
    private String imageAssetPath;

    public ResourceHero() {
        soundAssetPaths = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageAssetPath() {
        return imageAssetPath;
    }

    public void setImageAssetPath(String imageAssetPath) {
        this.imageAssetPath = imageAssetPath;
    }

    public void addSoundAssetPath(String soundAssetPath) {
        soundAssetPaths.add(soundAssetPath);
    }

    public String getRandomSoundAssetPath() {
        return soundAssetPaths.get(rnd.nextInt(soundAssetPaths.size()));
    }
}

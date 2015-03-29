package com.forlulz.lol.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.android.vending.expansion.zipfile.ZipResourceFile.ZipEntryRO;
import com.google.android.vending.expansion.downloader.Helpers;

public class GameResourceLoader {

    private static final String TAG = GameResourceLoader.class.getSimpleName();

    private ZipResourceFile resource;
    private List<ResourceHero> heroes;

    public GameResourceLoader(Context context) throws IOException {
        ExpansionAPK expansionAPK = ExpansionAPK.main;
        boolean isMain = true;
        String fileName = Helpers.getExpansionAPKFileName(context, isMain, expansionAPK.getFileVersion());
        fileName = Helpers.generateSaveFileName(context, fileName);

        resource = new ZipResourceFile(fileName); // TODO: defense - file can be
                                                  // null!
        Map<String, ResourceHero> heroByNameMap = new HashMap<String, ResourceHero>();
        for (ZipEntryRO entry : resource.getAllEntries()) {
            String zipFileName = entry.mFileName;
            if (zipFileName.endsWith("/")) { // is directory
                continue;
            }

            // else it is a compressed file related to hero
            File file = new File(zipFileName);
            String heroName = file.getParentFile().getName();
            ResourceHero hero = heroByNameMap.get(heroName);
            if (hero == null) {
                hero = new ResourceHero();
                hero.setName(heroName);
                heroByNameMap.put(heroName, hero);
            }

            if (file.getName().endsWith(".mp3")) { // it's a voice
                hero.addSoundAssetPath(zipFileName);
            } else if (file.getName().endsWith(".jpg")) { // it's a picture
                hero.setImageAssetPath(zipFileName);
            } else {
                Log.w(TAG, "Unknown file type");
            }
        }

        heroes = new ArrayList<ResourceHero>(heroByNameMap.values());
    }

    public List<ResourceHero> getHeroes() {
        return heroes;
    }

    public AssetFileDescriptor resolveAssetPath(String assetPath) {
        return resource.getAssetFileDescriptor(assetPath);
    }
}

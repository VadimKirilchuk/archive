package com.forlulz.lol.util;

import android.content.Context;

import com.google.android.vending.expansion.downloader.Helpers;

/**
 * This is a little helper class that demonstrates simple testing of an
 * Expansion APK file delivered by Market. You may not wish to hard-code
 * things such as file lengths into your executable... and you may wish to
 * turn this code off during application development.
 */
public class ExpansionAPK {
    
    public static ExpansionAPK main = new ExpansionAPK(true, 1, 79467216L);
    public static ExpansionAPK patch;
    
    /**
     * Here is where you place the data that the validator will use to determine
     * if the file was delivered correctly. This is encoded in the source code
     * so the application can easily determine whether the file has been
     * properly delivered without having to talk to the server. If the
     * application is using LVL for licensing, it may make sense to eliminate
     * these checks and to just rely on the server.
     */
    public static final ExpansionAPK[] expansions = { main };
    
    /**
     * Helper method to get expansion name as [main|patch].[version].[package].obb 
     * 
     * @param context context
     * @param apk expansion to get name for
     * @return full expansion name as [main|patch].[version].[package].obb
     */
    public static String getExpansionName(Context context, ExpansionAPK apk) {
        return Helpers.getExpansionAPKFileName(context, apk.isMain, apk.getFileVersion());
    }
    
    private final boolean isMain;
    private final int fileVersion;
    private final long fileSize;


    public ExpansionAPK(boolean isMain, int fileVersion, long fileSize) {
        this.isMain = isMain;
        this.fileVersion = fileVersion;
        this.fileSize = fileSize;
    }

    public boolean isMain() {
        return isMain;
    }

    public int getFileVersion() {
        return fileVersion;
    }

    public long getFileSize() {
        return fileSize;
    }
    
}
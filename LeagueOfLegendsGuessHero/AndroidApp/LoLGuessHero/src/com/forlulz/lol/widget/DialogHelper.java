package com.forlulz.lol.widget;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.forlulz.lol.R;
import com.forlulz.lol.util.GameResourceLoader;
import com.forlulz.lol.util.ResourceHero;

public class DialogHelper {

    public static AlertDialog createHeroGuessFailedDialog(Activity context, final Runnable successAction,
            ResourceHero guess, ResourceHero actual, GameResourceLoader loader) throws IOException {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.hero_guess_failed, null);
        ImageView guessHeroImage = (ImageView) view.findViewById(R.id.guess_hero_image);
        TextView guessHeroName = (TextView) view.findViewById(R.id.guess_hero_name);
        ImageView actualHeroImage = (ImageView) view.findViewById(R.id.actual_hero_image);
        TextView actualHeroName = (TextView) view.findViewById(R.id.actual_hero_name);

        guessHeroImage.setImageDrawable(createDrawable(loader.resolveAssetPath(guess.getImageAssetPath())));
        guessHeroImage.setLayoutParams(new LinearLayout.LayoutParams(140, 140));
        guessHeroImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        guessHeroName.setText(guess.getName());

        actualHeroImage.setImageDrawable(createDrawable(loader.resolveAssetPath(actual.getImageAssetPath())));
        actualHeroImage.setLayoutParams(new LinearLayout.LayoutParams(140, 140));
        actualHeroImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        actualHeroName.setText(actual.getName());
        
        // 2. Chain together various setter methods to set the dialog
        // characteristics
        builder.setPositiveButton(R.string.ok, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                successAction.run();
            }

        }).setTitle(R.string.hero_guess_failed).setView(view);

        // 3. Get the AlertDialog from create()
        return builder.create();
    }

    private static Drawable createDrawable(AssetFileDescriptor descriptor) throws IOException {
        Drawable result = Drawable.createFromStream(descriptor.createInputStream(), null);
        descriptor.close();
        return result;
    }
}
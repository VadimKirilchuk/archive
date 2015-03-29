package com.forlulz.lol;

import com.forlulz.lol.game.single.SinglePlayerActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class GameMenuActivity extends Activity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_menu);

        findViewById(R.id.button_single_player).setOnClickListener(this);
        findViewById(R.id.button_multi_player).setOnClickListener(this);
        findViewById(R.id.button_options).setOnClickListener(this);
        findViewById(R.id.button_score).setOnClickListener(this);
        findViewById(R.id.button_about).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.button_single_player: {
                intent = new Intent(this, SinglePlayerActivity.class);
                break;
            }
            case R.id.button_multi_player: {
                Log.i("options", "multi");
                break;
            }
            case R.id.button_options: {
                Log.i("options", "options");
                break;
            }
            case R.id.button_score: {
                Log.i("options", "score");
                break;
            }
            case R.id.button_about: {
                Log.i("options", "about");
                break;
            }
            default:
                break;
        }
        
        if (intent != null) {
            startActivity(intent);
        } else {
            Log.w("options", "null intent in onclick handler");
        }
    }

}

package com.forlulz.lol.game.single;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.forlulz.lol.R;
import com.forlulz.lol.game.GameHandler;
import com.forlulz.lol.game.GameListener;
import com.forlulz.lol.game.GameStatsHolder;
import com.forlulz.lol.util.GameResourceLoader;
import com.forlulz.lol.util.ImageAdapter;
import com.forlulz.lol.util.ResourceHero;
import com.forlulz.lol.widget.DialogHelper;

public class SinglePlayerActivity extends Activity implements GameListener {

    private static final String LOG_TAG = SinglePlayerActivity.class.getSimpleName();

    private ProgressBar healthBar;
    private ProgressBar manaBar;
    private TextView countdownText;
    
    private MediaPlayer mediaPlayer;
    private GameHandler gameHandler;
    private GameResourceLoader loader;

    @Override
    public void onTick(long millisUntilFinished) {
        String seconds = String.valueOf(millisUntilFinished / 1000);
        countdownText.setText(seconds);
    }
    
    @Override
    public void onTimeOut(GameStatsHolder holder) {
        updateStats(holder);
        Toast.makeText(this, "Timeout", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onSuccess(GameStatsHolder holder) {
        updateStats(holder);
        gameHandler.startRound();
    }
    
    @Override
    public void onFail(final GameStatsHolder holder, ResourceHero choice, ResourceHero actual) {
        AlertDialog dialog;
        try {
            dialog = DialogHelper.createHeroGuessFailedDialog(this, new Runnable() {
                @Override
                public void run() {
                    updateStats(holder);
                    gameHandler.startRound();               
                }
            }, choice, actual, loader);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDeath(GameStatsHolder holder) {
        updateStats(holder);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = new MediaPlayer();
        try {
            loader = new GameResourceLoader(this);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Problem with loading game resources");
        }

        setContentView(R.layout.game_layout);

        healthBar = (ProgressBar) findViewById(R.id.healthbar);
        manaBar = (ProgressBar) findViewById(R.id.manabar);
        
        countdownText = (TextView) findViewById(R.id.countdown_text);
        
        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this, loader));
        
        gameHandler = new GameHandler(this, loader, mediaPlayer);
        gameHandler.reset();
        gameHandler.startRound(); //TODO: show dialog - READY TO GO? 
        
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ResourceHero selectedHero = loader.getHeroes().get(position);
                gameHandler.checkAnswer(selectedHero);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameHandler.stopTimer();
        mediaPlayer.release();
    }
    
    private void updateStats(GameStatsHolder holder) {
        healthBar.setProgress(holder.getHealth());
        manaBar.setProgress(holder.getMana());
    }
}

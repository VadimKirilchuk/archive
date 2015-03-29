package com.forlulz.lol.game;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.CountDownTimer;

import com.forlulz.lol.util.GameResourceLoader;
import com.forlulz.lol.util.ResourceHero;

public class GameHandler implements GameStatsHolder {

    private static final String LOG_TAG = GameHandler.class.getSimpleName();
    private static final Random RND = new Random();

    private GameResourceLoader loader;
    private MediaPlayer mediaPlayer;
    private GameListener listener;

    private ResourceHero currentHero;

    private boolean ended;
    private CountDownTimer countdown;
    private int health;
    private int mana;
    private long score;
    private int timeLimit;
    //TODO: it will be cool to implement regeneration and level ups =)

    public GameHandler(GameListener listener, GameResourceLoader loader, MediaPlayer mediaPlayer) {
        // TODO: assert not nulls!
        this.listener = listener;
        this.loader = loader;
        this.mediaPlayer = mediaPlayer;
    }
    
    public void reset() {
        this.health = 100;
        this.mana = 100;
        this.score = 0;
        this.timeLimit = 30000;
    }
    
    public void startRound() {
        countdown = new GameCountDownTimer(timeLimit, 1000);
        countdown.start();

        AssetFileDescriptor res = getRandomSound();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(res.getFileDescriptor(), res.getStartOffset(), res.getLength());
            res.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error during playing hero sound", e);
        }
    }

    public void checkAnswer(ResourceHero selectedHero) {
        if (ended) { //don't react if game over..
            return;
        }
        
        if (currentHero != null) {
            stopTimer();
            if (currentHero.getName().equals(selectedHero.getName())) {
                winRound();
            } else {
                failRound(selectedHero);
            }
        }
    }
    
    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getMana() {
        return mana;
    }

    @Override
    public long getScore() {
        return score;
    }
    
    private void winRound() {
        health += 10;
        mana += 5;
        if (health > 100) {
            health = 100;
        }
        if (mana > 100) {
            mana = 100;
        }
        listener.onSuccess(this);
    }

    private void failRound(ResourceHero selectedHero) {
        if (countdown != null) {
            countdown.cancel();
        }
        health -= 10;
        mana -= 5;
        if (health <= 0) {
            ended = true;
        }

        if (health < 0) {
            health = 0;
        }

        if (mana < 0) {
            mana = 0;
        }

        if (ended) {
            listener.onDeath(this);
        } else {
            listener.onFail(this, selectedHero, currentHero);
        }
    }
    
    private void onTimeOut() {
        health -= 5;
        mana -= 10;
        if (listener != null) {
            listener.onTimeOut(this);
        }
    }

    public void stopTimer() {
        if (countdown != null) {
            countdown.cancel();
        }
    }

    private void onTick(long millisUntilFinished) {
        if (listener != null) {
            listener.onTick(millisUntilFinished);
        }
    }

    private AssetFileDescriptor getRandomSound() {
        List<ResourceHero> heroes = loader.getHeroes();
        currentHero = heroes.get(RND.nextInt(heroes.size()));
        String assetPath = currentHero.getRandomSoundAssetPath();

        return loader.resolveAssetPath(assetPath);
    }

    private class GameCountDownTimer extends CountDownTimer {

        public GameCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            GameHandler.this.onTick(millisUntilFinished);
        }

        @Override
        public void onFinish() {
            GameHandler.this.onTimeOut();
        }

    }
}

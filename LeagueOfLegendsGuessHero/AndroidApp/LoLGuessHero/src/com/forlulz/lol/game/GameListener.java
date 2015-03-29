package com.forlulz.lol.game;

import com.forlulz.lol.util.ResourceHero;

public interface GameListener {

    void onTick(long millisUntilFinished);
    
    void onTimeOut(GameStatsHolder statsHolder);
    
    void onFail(GameStatsHolder statsHolder, ResourceHero choice, ResourceHero actual);
    
    void onSuccess(GameStatsHolder statsHolder);
    
    void onDeath(GameStatsHolder statsHolder);
}

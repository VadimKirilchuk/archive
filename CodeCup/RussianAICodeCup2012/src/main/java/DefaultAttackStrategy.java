import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Bonus;
import model.Move;
import model.Obstacle;
import model.ShellType;
import model.Tank;
import model.Unit;
import model.World;

class DefaultAttackStrategy extends AbstractStrategy {

    /*
     * We need to choose some enemy based on: 
     * 1) Visibility + chance to hit 
     * 2) Distance 
     * 3) Current health 
     * 4) Time to turn turret - also we
     * can help turret with tank turn
     */

    private static final double FIRE_ANGLE = MIN_ANGLE * 1; //dont increase the value...
    private Tank targetEnemy;
    private boolean noObstacle;
    private double tiksForTurretTurn;

    @Override
    void move(StrategyContext context) {
        findPerfectEnemy(context);
        tryAttackEnemy(context);
    }

    protected void findPerfectEnemy(StrategyContext context) {
        Tank self = context.self;
        World world = context.world;

        List<Tank> liveEnemies = context.liveEnemies;
        List<Tank> deadTanks = context.deadTanks;
        Bonus[] bonuses = world.getBonuses();
        Obstacle[] obstacles = world.getObstacles();
        List<Unit> allObstacles = new ArrayList<Unit>(deadTanks.size() + bonuses.length + obstacles.length);
        Collections.addAll(allObstacles, obstacles);
        Collections.addAll(allObstacles, bonuses);
        allObstacles.addAll(deadTanks);

        double minScore = Double.MAX_VALUE;
        for (Tank enemy : liveEnemies) {
            // check if enemy is visible
            boolean visible = noDirectObstacle(self.getX(), self.getY(), enemy.getX(), enemy.getY(), allObstacles);
            
            //TODO: take hp and armor in account!!!
            double distanceToEnemy = getDistance(self, enemy);
            double tiksToTurnTurret = tiksToTurnTurret(self, enemy);

            //10 tiks equal to 30 distance
            double score = 3 * tiksToTurnTurret + distanceToEnemy;
            score += visible ? 0 : 5000;
            if (score < minScore) {
                minScore = score;
                targetEnemy = enemy;
                noObstacle = visible;
                tiksForTurretTurn = tiksToTurnTurret;
            }
        }
    }

    private void tryAttackEnemy(StrategyContext context) {
        Tank self = context.self;
        Move move = context.move;
        World world = context.world;

        if (targetEnemy != null) {
            boolean usePremiumShell = false;
            if (self.getDistanceTo(targetEnemy) < 600 && self.getPremiumShellCount() > 0) {
                usePremiumShell = true;
            }
            int tiksToHit = tiksToHit(self, usePremiumShell ? ShellType.PREMIUM : ShellType.REGULAR,
                    targetEnemy.getX(), targetEnemy.getY());
            
            // lets predict next tank position
            double enemyX = predictUnitX(targetEnemy, tiksToHit);
            double enemyY = predictUnitY(targetEnemy, tiksToHit);
            
            if (MyStrategy.debug) {
                Shape shape = new Ellipse2D.Double(enemyX, enemyY, 10, 10);
                MyStrategy.debugView.drawWithColor(shape, Color.YELLOW);
            }
            
            double angleToEnemy = self.getTurretAngleTo(enemyX, enemyY);
            
            //check if turret speed is enough before reload
            int reloadTime = self.getRemainingReloadingTime();
            boolean turnHelp = false;
            if (tiksForTurretTurn >= reloadTime && noObstacle) {
                turnHelp = true;
            }
            
            if (angleToEnemy > FIRE_ANGLE) {
                turnTurretRight(move);
                if (turnHelp) {
                    turnRight(move);
                }
            } else if (angleToEnemy < -FIRE_ANGLE) {
                turnTurretLeft(move);
                if (turnHelp) {
                    turnLeft(move);
                }
            } else {
                if (noFireObstacle(self, targetEnemy, world)) {
                    if (usePremiumShell) {
                        fireRegular(move);
                    } else {
                        firePremium(move);
                    }
                } 
            }
        }
    }

}

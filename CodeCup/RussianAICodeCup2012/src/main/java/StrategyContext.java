import java.awt.Color;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import model.Bonus;
import model.Move;
import model.Obstacle;
import model.Tank;
import model.World;

public class StrategyContext {

    Tank self;
    World world;
    Move move;
    
    double health;
    double hull;

    List<Tank> liveEnemies = new ArrayList<Tank>(6);
    // need for obstacles check
    List<Tank> deadTanks = new ArrayList<Tank>(5);

    public void update(Tank self, World world, Move move) {
        this.self = self;
        this.world = world;
        this.move = move;

        health = self.getCrewHealth() / self.getCrewMaxHealth();
        hull = self.getHullDurability() / self.getHullMaxDurability();
        
        // clear all
        liveEnemies.clear();
        deadTanks.clear();

        // precalculate some stuff
        for (Tank tank : world.getTanks()) {
            if (AbstractStrategy.isAlive(tank)) {
                if (tank.isTeammate()) {
                    // TODO: do we need teammates?
                } else {
                    liveEnemies.add(tank);
                }
            } else {
                deadTanks.add(tank);
            }
        }
    }

    public Line2D potentialAt(double x, double y, ChargeValues charge) {
        Line2D result = new Line2D.Double(x, y, x, y); // center

        for (Bonus bonus : world.getBonuses()) {
            result = sum(result, potentialAt(x, y, bonus.getX(), bonus.getY(), charge.value(bonus)));
        }

        for (Obstacle obstacle : world.getObstacles()) {
            result = sum(result, potentialAt(x, y, obstacle.getX(), obstacle.getY(), charge.value(obstacle)));
        }

        for (Tank tank : world.getTanks()) {
            if (tank.getId() == self.getId()) {
                continue;
            }
            result = sum(result, potentialAt(x, y, tank.getX(), tank.getY(), charge.value(tank)));
        }

        if (MyStrategy.debug) {
            MyStrategy.debugView.drawWithColor(result, Color.GREEN);
        }

        return result;
    }

    private Line2D potentialAt(double x, double y, double chargeX, double chargeY, double charge) {
        double distance = AbstractStrategy.getDistance(x, y, chargeX, chargeY);
        double value = 100000 * charge / (distance * distance);
        double angle = Utils2D.getAngleTo(x, y, chargeX, chargeY);

        double resultX = x + value * Math.cos(angle);
        double resultY = y + value * Math.sin(angle);

        Line2D.Double result = new Line2D.Double(x, y, resultX, resultY);

        if (MyStrategy.debug) {
            MyStrategy.debugView.drawWithColor(result, Color.BLUE);
        }

        return result;
    }

    private Line2D sum(Line2D line1, Line2D line2) {
        if (line1.getX1() != line2.getX1() || line1.getY1() != line2.getY1()) { // TODO be sure and make an assertion
            throw new RuntimeException("FATAL");
        }
        return new Line2D.Double(line1.getX1(), line1.getY1(), line1.getX2() + line2.getX2() - line1.getX1(), line1.getY2() + line2.getY2()
                - line1.getY1());
    }
}

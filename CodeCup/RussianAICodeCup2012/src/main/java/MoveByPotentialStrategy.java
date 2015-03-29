import java.awt.geom.Line2D;

import model.Move;
import model.Tank;

class MoveByPotentialStrategy extends AbstractStrategy {

    static final double ANGLE_THRESHOLD = MIN_ANGLE * 15;
    static final double ANGLE_90 = MIN_ANGLE * 90;
    static final double COEFF = 1.3;

    static final int HEALTH_THRESHOLD = 85;
    static final int DURABILITY_THRESHOLD = 160;
    static final int PREMIUM_SHELL_THRESHOLD = 1;

    ChargeValues charge = new ChargeValues();

    @Override
    void move(StrategyContext context) {
        Tank self = context.self;
        Move move = context.move;

        updateCharge(context);

        Line2D vector = context.potentialAt(self.getX(), self.getY(), charge);

        double angle = self.getAngleTo(vector.getX2(), vector.getY2());
        double absAngle = Math.abs(angle);

        if (absAngle <= ANGLE_90) {
            moveByFront(move, angle, absAngle);
        } else {
            angle = inverseAngle(angle);
            absAngle = Math.abs(angle);
            moveByRear(move, angle, absAngle);
        }
    }

    private void updateCharge(StrategyContext context) {
        Tank self = context.self;

        // reset
        charge.ammoCharge = 0;
        charge.medkitCharge = 0;
        charge.repairCharge = 0;

        if (self.getPremiumShellCount() < PREMIUM_SHELL_THRESHOLD) {
            charge.ammoCharge = 20;
        }

        if (self.getCrewHealth() < HEALTH_THRESHOLD) {
            charge.medkitCharge = (100 - context.health);
        }

        if (self.getHullDurability() < DURABILITY_THRESHOLD) {
            charge.repairCharge = (100 - context.hull);
        }

        // reset
        charge.enemyTankCharge = 0;

        if (context.liveEnemies.size() == 1) {
            charge.enemyTankCharge = 0;
        }

        if (MyStrategy.debug) {
            MyStrategy.debugView.addInfoLine(charge.toString());
        }
    }

    private void moveByFront(Move move, double angle, double absAngle) {
        if (angle > ANGLE_THRESHOLD) {
            move.setLeftTrackPower(1.0);
            move.setRightTrackPower(1 - absAngle * COEFF);
        } else if (angle < -ANGLE_THRESHOLD) {
            move.setLeftTrackPower(1 - absAngle * COEFF);
            move.setRightTrackPower(1.0);
        } else {
            moveForward(move);
        }
    }

    private void moveByRear(Move move, double angle, double absAngle) {
        if (angle > ANGLE_THRESHOLD) {
            move.setLeftTrackPower(-1 + absAngle * COEFF);
            move.setRightTrackPower(-1.0);
        } else if (angle < -ANGLE_THRESHOLD) {
            move.setLeftTrackPower(-1.0);
            move.setRightTrackPower(-1 + absAngle * COEFF);
        } else {
            moveBackward(move);
        }
    }

}

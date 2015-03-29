import java.util.List;

import model.Bonus;
import model.BonusType;
import model.Move;
import model.Tank;
import model.World;

class MoveToClosestBonusStrategy extends AbstractStrategy {

    static final double ANGLE_TO_BONUS = MIN_ANGLE * 15;
    static final double COEFF = 1.3;

    final List<BonusType> needBonus;
    final Strategy noBonusStrategy;

    public MoveToClosestBonusStrategy(Strategy noBonusStrategy, List<BonusType> need) {
        this.needBonus = need;
        this.noBonusStrategy = noBonusStrategy;
    }

    @Override
    void move(StrategyContext context) {
        Tank self = context.self;
        World world = context.world;
        Move move = context.move;

        Bonus targetBonus = null;
        double minTiks = Double.MAX_VALUE;
        boolean takeByFront = true;
        for (Bonus bonus : world.getBonuses()) {
            // ignore not need bonuses
            if (!needBonus.contains(bonus.getType())) {
                continue;
            }

            double distance = self.getDistanceTo(bonus);

            // lets ignore too far bonuses
            if (distance > 800) {
                continue;
            }

            // no we need to decide how to take bonus - by rear speed or front speed
            double angle = self.getAngleTo(bonus);
            double tiksForFront = (Math.abs(angle) / TURN_SPEED_PER_TIK) + (distance / SPEED_PER_TICK);
            double tiksForRear = 1.15 * ((Math.abs(inverseAngle(angle)) / TURN_SPEED_PER_TIK) + (distance / REAR_SPEED_PER_TICK));

            // System.out.println(tiksForFront + " " + tiksForRear);

            if (Math.min(tiksForFront, tiksForRear) < minTiks) {
                if (tiksForFront < tiksForRear) {
                    minTiks = tiksForFront;
                    takeByFront = true;
                } else {
                    minTiks = tiksForRear;
                    takeByFront = false;
                }
                targetBonus = bonus;
            }
        }

        if (targetBonus != null) {
            double angle = self.getAngleTo(targetBonus);
            double absAngle = Math.abs(angle);
            
            if (takeByFront) { // move by front
                if (angle > ANGLE_TO_BONUS) {
                    move.setLeftTrackPower(1.0);
                    move.setRightTrackPower(1 - absAngle * COEFF);
                } else if (angle < -ANGLE_TO_BONUS) {
                    move.setLeftTrackPower(1 - absAngle * COEFF);
                    move.setRightTrackPower(1.0);
                } else {
                    moveForward(move);
                }
            } else { // move by rear
                angle = inverseAngle(angle);
                if (angle > ANGLE_TO_BONUS) {
                    move.setLeftTrackPower(0.75);
                    move.setRightTrackPower(1 - absAngle * COEFF);
                } else if (angle < -ANGLE_TO_BONUS) {
                    move.setLeftTrackPower(1 - absAngle * COEFF);
                    move.setRightTrackPower(0.75);
                } else {
                    moveBackward(move);
                }
            }
        } else {
            noBonusStrategy.move(self, world, move);
        }
    }

}

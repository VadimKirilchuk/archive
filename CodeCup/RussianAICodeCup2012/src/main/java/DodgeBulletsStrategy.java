import model.Move;
import model.Shell;
import model.ShellType;
import model.Tank;
import model.World;

class DodgeBulletsStrategy extends AbstractStrategy {

    @Override
    void move(StrategyContext context) {
        Tank self = context.self;
        World world = context.world;
        Move move = context.move;

        // avoid bullets which are flying to our tank and destroy closest one
        // it would be better to avoid premium ones first
        Shell targetShell = null;
        double targetScore = Double.MAX_VALUE;
        for (Shell shell : world.getShells()) {
            double bulletAngle = Math.abs(shell.getAngleTo(self));
            // filter out bullets not targetet to us
            if (bulletAngle > MIN_ANGLE * 25) {
                continue;
            }

            double distance = shell.getDistanceTo(self);
            double speed = (shell.getType() == ShellType.PREMIUM ? PREMIUM_BULLET_SPEED_PER_TIK : REGULAR_BULLET_SPEED_PER_TIK);
            double timeToHit = distance / speed;

            double score = (timeToHit) - (shell.getType() == ShellType.PREMIUM ? 1 : 0);
            if (score < targetScore) {
                targetScore = score;
                targetShell = shell;
            }
        }

     // switch directions if we've stopped //TODO: !!!!
//        if (getVelocity() == 0)
//            moveDirection *= -1;
     // strafe by changing direction every 20 ticks
//        if (getTime() % 20 == 0) {
//            moveDirection *= -1;
//            setAhead(150 * moveDirection);
//        }
        
        
        if (targetShell != null) {
            double selfAngle = self.getAngleTo(targetShell);
            if (Math.abs(selfAngle) > MIN_ANGLE * 10) {// if not front hit
                if (Math.abs(selfAngle) < MIN_ANGLE * 100) { // move backward
                    moveBackward(move);
                } else { // move forward
                    moveForward(move);
                }
            }
        } 
//        else { // turn by 90 to worst enemy
//            Tank targetEnemy = findEnemy(self, world, move);
//            if (targetEnemy != null) {
//                double angleToEnemy = self.getAngleTo(targetEnemy);
//                double diff = Math.abs(angleToEnemy) - MIN_ANGLE * 30; // how much angle to turn
//                double degreesThreshold = MIN_ANGLE * 5;
//                if (angleToEnemy >= 0) {
//                    if (diff < degreesThreshold) {
//                        turnLeft(move);
//                    } else if (diff > degreesThreshold) {
//                        turnRight(move);
//                    }
//                } else {
//                    if (diff < degreesThreshold) {
//                        turnRight(move);
//                    } else if (diff > degreesThreshold) {
//                        turnLeft(move);
//                    }
//                }
//            }
//        }
    }

    private Tank findEnemy(Tank self, World world, Move move) {
        Tank targetEnemy = null;

        double minScore = Double.MAX_VALUE;
        for (Tank enemy : world.getTanks()) {
            if (isLiveEnemy(enemy)) {
                double enemyTiksToTurnTurret = tiksToTurnTurret(enemy, self);
                double distance = AbstractStrategy.getDistance(self, enemy);
                double enemyTimeToHitUs = tiksToShift(distance, REGULAR_BULLET_SPEED_PER_TIK);

                double score = enemyTiksToTurnTurret + enemyTimeToHitUs;

                if (score < minScore) {
                    minScore = score;
                    targetEnemy = enemy;
                }
            }
        }

        return targetEnemy;
    }
}

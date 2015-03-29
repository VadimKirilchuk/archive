import model.Move;
import model.Shell;
import model.ShellType;
import model.Tank;
import model.World;

/**
 * Strategy which tries to destroy incomming bullets. Use when it is a last chance to survive.
 */
class AttackBulletStrategy extends AbstractStrategy {

    @Override
    void move(StrategyContext context) {
        Tank self = context.self;
        World world = context.world;
        Move move = context.move;

        // destroy incoming bullets
        bulletProof(self, world, move);
    }

    private void bulletProof(Tank self, World world, Move move) {
        // find bullets which are flying to our tank and destroy closest one
        // it would be better to destroy premium ones first
        Shell targetShell = null;
        double targetScore = Double.MAX_VALUE;
        for (Shell shell : world.getShells()) {
            double bulletAngle = Math.abs(shell.getAngleTo(self));
            // filter out bullets not targeted to us
            if (bulletAngle > MIN_ANGLE * 10) {
                continue;
            }

            // find closest bullet which we can destroy
            // we must take in account number of tiks to turn our turret
            double tiksToTurnTurret = tiksToTurnTurret(self, shell);
            double timeToHit = tiksToHit(self, shell);

            // System.out.println("toTurn:" + tiksToTurnTurret + " dist:" + "toHit:" + timeToHit);

            if (tiksToTurnTurret + 4 > timeToHit) {// if tiks > then tiks to hit then we cant destroy it
                continue;
            }

            double score = (timeToHit + tiksToTurnTurret) - (shell.getType() == ShellType.PREMIUM ? 1 : 0);

            if (score < targetScore) {
                targetShell = shell;
                targetScore = score;
            }
        }

        if (targetShell != null) {
            double distance = self.getDistanceTo(targetShell);
            // lets predict next shell position
            double shellX = predictUnitX(targetShell, 20);
            double shellY = predictUnitY(targetShell, 20);
            double turretAngle = self.getTurretAngleTo(shellX, shellY);
            if (turretAngle > MIN_ANGLE * 2) {
                turnTurretRight(move);
            } else if (turretAngle < -MIN_ANGLE * 2) {
                turnTurretLeft(move);
            } else if (noFireObstacle(self, targetShell, world)) {
                fireRegular(move);
            }
        } else { // find most dangerous enemy (gun angle, distance)
            Tank enemy = findEnemy(self, world, move);
            if (enemy != null) {
                double turretAngle = self.getTurretAngleTo(enemy);
                if (turretAngle > MIN_ANGLE) {
                    turnTurretRight(move);
                } else if (turretAngle < -MIN_ANGLE) {
                    turnTurretLeft(move);
                }
            }
        }
    }

    private Tank findEnemy(Tank self, World world, Move move) {
        Tank targetEnemy = null;

        double minScore = Double.MAX_VALUE;
        for (Tank enemy : world.getTanks()) {
            if (isLiveEnemy(enemy)) {
                double enemyTiksToTurnTurret = tiksToTurnTurret(enemy, self);
                double tiksToTurnTurret = tiksToTurnTurret(self, enemy);
                double distance = AbstractStrategy.getDistance(self, enemy);
                double enemyTimeToHitUs = tiksToShift(distance, REGULAR_BULLET_SPEED_PER_TIK);

                double score = enemyTiksToTurnTurret + enemyTimeToHitUs + tiksToTurnTurret / 4;

                if (score < minScore) {
                    minScore = score;
                    targetEnemy = enemy;
                }
            }
        }

        return targetEnemy;
    }
}

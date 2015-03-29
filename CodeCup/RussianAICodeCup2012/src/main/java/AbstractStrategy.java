import java.util.ArrayList;
import java.util.List;

import model.Bonus;
import model.FireType;
import model.Move;
import model.Obstacle;
import model.Shell;
import model.ShellType;
import model.Tank;
import model.TankType;
import model.Unit;
import model.World;

/**
 * This class contains tank selection, useful constants and methods for implementations. Also this class is responsible for game mechanics -
 * calculating engine speed and so on...
 */
abstract class AbstractStrategy implements Strategy {
    static final String playerName = "vkirilchuk";

    static final double MIN_ANGLE = StrictMath.PI / 180.0; // 1 grad in radians

    static final double TANK_WEIGHT = 10;
    static final double ENGINE_POWER = 7500;

    static final double SPEED_PER_TICK = 3.0;
    static final double REAR_SPEED_PER_TICK = SPEED_PER_TICK * 0.75;

    static final double TURN_SPEED_PER_TIK = 0.027;

    static final double REGULAR_BULLET_SPEED_PER_TIK = 16.7;
    static final double PREMIUM_BULLET_SPEED_PER_TIK = 13.3;

    static final double TURRET_TURN_SPEED_PER_TIK = MIN_ANGLE * 1;

    static boolean isLiveEnemy(Tank target) {
        boolean result = !target.isTeammate() && isAlive(target);
        return result;
    }

    static boolean isAlive(Tank target) {
        return (target.getCrewHealth() * target.getHullDurability() > 0);
    }

    static double inverseAngle(double angle) {
        if (angle >= 0) { // 1rad from front is -Pi + 1rad for back
            return -(Math.PI - angle);
        } else { // -1rad from front is Pi - 1rad for back
            return Math.PI + angle;
        }
    }

    static void turnTurretRight(Move move) {
        move.setTurretTurn(2.0);
    }

    static void turnTurretLeft(Move move) {
        move.setTurretTurn(-2.0);
    }

    static void moveForward(Move move) {
        move.setLeftTrackPower(1);
        move.setRightTrackPower(1);
    }

    static void turnRight(Move move) {
        move.setLeftTrackPower(0.75);
        move.setRightTrackPower(-1);
    }

    static void turnLeft(Move move) {
        move.setLeftTrackPower(-1);
        move.setRightTrackPower(0.75);
    }

    static void moveBackward(Move move) {
        move.setLeftTrackPower(-1);
        move.setRightTrackPower(-1);
    }

    /**
     * Движение танка затормаживается по жизни экипажа. Берется коэф: double koef = 0.5 + 0.5 * context.health; где health =
     * crew_health/max_health - число от 0 до 1 этот коэф умножается на силу действующую на танк force = (0.5*left_track_power +
     * 0.5*right_track_power)*engine_power*koef Т.е. при максимальном здоровье и left_track_power = right_track_power = 1, получим силу
     * равную engine_power = 7500 для нормального танка Потом считаем ускорение равное force/m где m = 10 масса получаем новую скорость v1 =
     * old_v + F*dt/m, где dt = 1/60 и делаем поправку на сопротивление воздуха result_v = v1 - v1*damping/m где damping = 0.5 И при
     * передачи новых скоростей и координат в нашу стратегию, все это делится еще на 60 Например если тпнк покоится, и мы придаем ему макс
     * ускорение v1 = 0 + 7500*(1/60)/10 = 12.5 result_v = 12.5 - 12.5*0.5/10 = 11.875 И наша стратегия получает значение новой скорости
     * равное 11.875/60 = 0.19791666666666666667
     **/
    // TODO:
    // @Deprecated // not tested + only for self + only for 1;1 power on tracks
    // static double newSpeed(StrategyContext context) {
    // double healthCoeff = 0.5 + 0.5 * context.health;
    // double leftTrackPower = 1;
    // double rightTrackPower = 1;
    // double force = (0.5 * leftTrackPower + 0.5 * rightTrackPower) * ENGINE_POWER * healthCoeff;
    // double a = force / TANK_WEIGHT;
    // double newV = /* currentSpeed */ + force * (1.0 / 600);
    // double damping = 0.5;
    // newV = newV - newV * 0.5 / 10;
    // return (newV / 60);
    // }

    /**
     * С угловой скоростью тоже довольно просто. Допустим прикладываем на левый трак 1, на правый 0. Вращательный момент сил тогда Torque =
     * 2 * dF = 2 * (7500 - 0) = 15000 Момент инерции танка: I = m*(wid^2 + heig^2)/12 = 10*(90^2 + 60^2)/12 = 9750 Изменение угловой
     * скорости: w1 = w_old + Torque*dT/I = 0 + 15000*(1/60)/9750 = 0.02564102564102564103 Учет силы сопротивления воздуха с rotDamping =
     * 20*m = 200: result_w = w1 - w1*rotDamping/I = 0.02564102564102564103 - 0.02564102564102564103*200/9750 = 0.02511505588428665352 При
     * возврате в стратегию это число делится на 60, и получается 0.02511505588428665352/60 = 0.00041858426473811089 Все эти деления на 60
     * нужны из-за того, что в физ двиге dT = 1/60 секунды, в стратегиях же время измеряется в тиках. Поэтому чтобы формула например
     * изменения координат dPos = v * dT в стратегии перешла в просто dPos = v * 1 Скорости, ускорения и угловые ускорения умножаются на
     * этот dT = 1/60
     **/
    // TODO:

    static double tiksToTurnTankFront(Tank self, Unit unit) {
        return tiksToTurnTankFront(self, unit.getX(), unit.getY());
    }

    static double tiksToTurnTankFront(Tank self, double x, double y) {// TODO: depends on hp actually...
        double angle = self.getAngleTo(x, y);

        return Math.abs(angle / TURN_SPEED_PER_TIK);
    }

    static double tiksToTurnTankBack(Tank self, Unit unit) {
        return tiksToTurnTankBack(self, unit.getX(), unit.getY());
    }

    static double tiksToTurnTankBack(Tank self, double x, double y) {// TODO: depends on hp actually...
        double angle = inverseAngle(self.getAngleTo(x, y));

        return Math.abs(angle / TURN_SPEED_PER_TIK);
    }

    static double tiksToTurnTurret(Tank self, Unit unit) {
        return tiksToTurnTurret(self, unit.getX(), unit.getY());
    }

    static double tiksToTurnTurret(Tank self, double x, double y) {// TODO: depends on hp actually...
        double angle = self.getTurretAngleTo(x, y);

        return Math.abs(angle / TURRET_TURN_SPEED_PER_TIK);
    }

    /**
     * rusk Замедляется простая на полпроцента за тик, премиумная на процент за тик. Пройденное расстояние считается через сумму
     * геометрической прогрессии, в одну формулу. Время рассчитывается решив уравнение - приравняв сумму прогрессии к пути, так-же в одну
     * формулу. Вот формулы на время долета до точки из моего кода (расстояние до точки DistP) 
     * timeP:=LN(1-DistP*0.005/16.666666)/LN(0.995); //Для обычного
     * timeP:=LN(1-DistP*0.01/13.333333)/LN(0.99); //Для премиумного.
     * OR
     * Да, для пули формулы такие же как и для танка, только сопротивление воздуха damping = 0.005, v2 = v1*(1 - 0.005/m) Для обычной пули
     * масса 1, для премиумной 0.5 Начальная скорость обычной пули 1000/60, премиумной 800/60 Еще надо заметить, что центр пули во время
     * выстрела сдвинут на длину орудия от центра танка с учетом направления. Если у нас танк имеет координату x = 320, то координата пули
     * будет 320 + 67,5 = 387.5 в следующем тике скорость пули будет 1000/60*(1-0,005) = 16.583333333333267 и координата 387.5 +
     * 16.583333333333267 = 404.083333333333267
     */
    // TODO:
    static int tiksToHit(Tank target, Shell shell) {
        double distance = getDistance(target, shell);
        return tiksToShift(distance, getSpeed(shell));
    }

    static int tiksToHit(Tank source, ShellType shellType, double targetX, double targetY) {
        //TODO: maybe subtract virtual gun length?
        double distance = getDistance(source.getX(), source.getY(), targetX, targetY);
        if (shellType == ShellType.REGULAR) {
            return tiksToShift(distance, REGULAR_BULLET_SPEED_PER_TIK);
        } else {
            return tiksToShift(distance, PREMIUM_BULLET_SPEED_PER_TIK);
        }
    }

    static int tiksToShift(double distance, double speed) {
        return (int) (distance / speed);
    }

    static double getSpeed(Unit unit) {
        return Math.sqrt(unit.getSpeedX() * unit.getSpeedX() + unit.getSpeedY() * unit.getSpeedY());
    }

    static double predictUnitX(Unit unit, int tiksNum) {
        return unit.getX() + unit.getSpeedX() * tiksNum;
    }

    static double predictUnitY(Unit unit, int tiksNum) {
        return unit.getY() + unit.getSpeedY() * tiksNum;
    }

    /**
     * Чтоб нормально проверить попадает ли пуля в бонус - Нужно провести две прямые параллельные выстрелу с двух сторон от него на
     * расстоянии половины ширины пули. И проверить пересекаются ли они с бонусом. То есть либо проверить пересечение с двумя его
     * диагоналями, либо что то-же самое - с четыремя сторонами. Или проверить что все четыре его вершины лежат по одну сторону от обеих
     * проверяемых прямых. Чтоб узнать по одну ли сторону лежат точки - достаточно посмотреть положение точки относительно прямой. Это можно
     * узнать из формулы площади треугольника через его координаты со знаком, состоящего из двух любых точек на прямой и проверяемой точки -
     * знак как раз покажет положение точки. То есть полная проверка - проверяем что бонус лежит в стороне куда стреляем (угол от -Pi/2 до
     * Pi/2), что расстояние до него меньше чем расстояние до цели, и что пуля его не пересекает.
     * 
     * я написал, нужно увеличить радиус итема на размер снаряда, тогда снаряд выродится в линию, что упростит проверки.
     */
    static boolean noFireObstacle(Tank self, Unit target, World world) {
        double selfX = self.getX();
        double selfY = self.getY();
        double distanceToTarget = self.getDistanceTo(target);

        for (Unit obstacle : getAllWorldObstacles(world, self.getId(), target.getId())) {
            double turretAngleToObstacle = Math.abs(self.getTurretAngleTo(obstacle));
            if (turretAngleToObstacle > MIN_ANGLE * 45) { // obstacle is outside
                continue;
            }

            // ignore alive enemies
            if (obstacle instanceof Tank) {
                Tank tank = (Tank) obstacle;
                if (isLiveEnemy(tank)) {
                    continue;
                }
            }

            double obstacleX = obstacle.getX();
            double obstacleY = obstacle.getY();
            double distance = getDistance(selfX, selfY, obstacleX, obstacleY);

            if (distance > distanceToTarget) { // it can be on fire line but too far...
                continue;
            }

            // old way of obstacle check
            // double normal = distance * Math.sin(turretAngleToObstacle);
            // double obstacleRadius = Math.max(obstacle.getWidth(), obstacle.getHeight()) * 0.5 + 10;
            //
            // if (normal <= obstacleRadius) {
            // return false;
            // }

            // new way
            // true rectangle ABCD coords
            // A---B
            // |-C-|->
            // C---D
            double bulletWidth = 10;
            double obstacleHalfWidth = (obstacle.getWidth() + bulletWidth) * 0.5;
            double obstacleHalfHeight = (obstacle.getHeight() + bulletWidth) * 0.5;

            double aX = -obstacleHalfWidth;
            double aY = -obstacleHalfHeight;
            double bX = obstacleHalfWidth;
            double bY = aY;
            double cX = bX;
            double cY = obstacleHalfHeight;
            double dX = aX;
            double dY = cY;

            // rotating by rotation matrix
            // (x') = ( cos sin ) (x) //note that our system has inversed y!
            // (y') = ( -sin cos ) (y)
            double angle = obstacle.getAngle();
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double aXNew = cos * aX + sin * aY + obstacleX;
            double aYNew = -sin * aX + cos * aY + obstacleY;
            double bXNew = cos * bX + sin * bY + obstacleX;
            double bYNew = -sin * bX + cos * bY + obstacleY;
            double cXNew = cos * cX + sin * cY + obstacleX;
            double cYNew = -sin * cX + cos * cY + obstacleY;
            double dXNew = cos * dX + sin * dY + obstacleX;
            double dYNew = -sin * dX + cos * dY + obstacleY;

            // check that all points at the same angle side
            double angleA = self.getTurretAngleTo(aXNew, aYNew);
            double angleB = self.getTurretAngleTo(bXNew, bYNew);
            double angleC = self.getTurretAngleTo(cXNew, cYNew);
            double angleD = self.getTurretAngleTo(dXNew, dYNew);

            double[] angles = { angleA, angleB, angleC, angleD };
            int oneSide = 0;
            int secondSide = 0;
            for (int i = 0; i < angles.length; i++) {
                double curAngle = angles[i];
                if (curAngle > 0 && curAngle < MIN_ANGLE * 180) {
                    ++oneSide;
                } else if (curAngle < 0 && curAngle > -MIN_ANGLE * 180) {
                    ++secondSide;
                }
            }

            if (!(oneSide == 4 || secondSide == 4)) {
                return false;
            }
        }

        return true;
    }

    static boolean noDirectObstacle(double x, double y, double targetX, double targetY, List<Unit> obstacles) {// TODO unify the code!
        double distanceToTarget = getDistance(x, y, targetX, targetY);
        double angleToTarget = Utils2D.getAngleTo(x, y, targetX, targetY);

        for (Unit obstacle : obstacles) {
            // ignore alive enemies
            if (obstacle instanceof Tank) {
                Tank tank = (Tank) obstacle;
                if (isLiveEnemy(tank)) {
                    continue;
                }
            }

            double obstacleX = obstacle.getX();
            double obstacleY = obstacle.getY();

            double angleToObstacle = Utils2D.getAngleTo(x, y, obstacleX, obstacleY);
            double absAngleDiff = Math.abs(angleToTarget - angleToObstacle);
            if (absAngleDiff > MIN_ANGLE * 45) { // obstacle is outside
                continue;
            }

            double distance = getDistance(x, y, obstacleX, obstacleY);
            if (distance > distanceToTarget) { // it can be on fire line but too far...
                continue;
            }

            // true rectangle ABCD coords
            // A---B
            // |-C-|->
            // C---D
            double bulletWidth = 10;
            double obstacleHalfWidth = (obstacle.getWidth() + bulletWidth) * 0.5;
            double obstacleHalfHeight = (obstacle.getHeight() + bulletWidth) * 0.5;

            double aX = -obstacleHalfWidth;
            double aY = -obstacleHalfHeight;
            double bX = obstacleHalfWidth;
            double bY = aY;
            double cX = bX;
            double cY = obstacleHalfHeight;
            double dX = aX;
            double dY = cY;

            // rotating by rotation matrix
            // (x') = ( cos sin ) (x) //note that our system has inversed y!
            // (y') = ( -sin cos ) (y)
            double angle = obstacle.getAngle();
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double aXNew = cos * aX + sin * aY + obstacleX;
            double aYNew = -sin * aX + cos * aY + obstacleY;
            double bXNew = cos * bX + sin * bY + obstacleX;
            double bYNew = -sin * bX + cos * bY + obstacleY;
            double cXNew = cos * cX + sin * cY + obstacleX;
            double cYNew = -sin * cX + cos * cY + obstacleY;
            double dXNew = cos * dX + sin * dY + obstacleX;
            double dYNew = -sin * dX + cos * dY + obstacleY;

            // check that all points at the same angle side
            double angleA = Utils2D.getAngleTo(x, y, aXNew, aYNew);
            double angleB = Utils2D.getAngleTo(x, y, bXNew, bYNew);
            double angleC = Utils2D.getAngleTo(x, y, cXNew, cYNew);
            double angleD = Utils2D.getAngleTo(x, y, dXNew, dYNew);

            double[] angles = { angleA, angleB, angleC, angleD };
            int oneSide = 0;
            int secondSide = 0;
            for (int i = 0; i < angles.length; i++) {
                double curAngle = angleToTarget - angles[i];
                if (curAngle > 0 && curAngle < MIN_ANGLE * 180) {
                    ++oneSide;
                } else if (curAngle < 0 && curAngle > -MIN_ANGLE * 180) {
                    ++secondSide;
                }
            }

            if (!(oneSide == 4 || secondSide == 4)) {
                return false;
            }
        }

        return true;
    }

    static double getDistance(Unit one, Unit second) {
        return getDistance(one.getX(), one.getY(), second.getX(), second.getY());
    }

    /**
     * @return distance between two points 
     */
    static double getDistance(double x, double y, double x2, double y2) {
        double xDiff = x - x2;
        double yDiff = y - y2;

        return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    static void firePremium(Move move) {
        move.setFireType(FireType.PREMIUM_PREFERRED);
    }

    static void fireRegular(Move move) {
        move.setFireType(FireType.REGULAR);
    }

    static List<Unit> getAllWorldObstacles(World world, long... ignoreIds) {
        List<Unit> result = new ArrayList<Unit>(world.getTanks().length + world.getBonuses().length + world.getObstacles().length);

        for (Tank tank : world.getTanks()) {
            if (!arrayContains(tank.getId(), ignoreIds)) {
                result.add(tank);
            }
        }

        for (Bonus bonus : world.getBonuses()) {
            result.add(bonus);
        }

        for (Obstacle obstacle : world.getObstacles()) {
            result.add(obstacle);
        }

        return result;
    }

    static boolean arrayContains(long value, long... array) {
        if (array == null) {
            return false;
        }

        for (long t : array) {
            if (value == t) {
                return true;
            }
        }

        return false;
    }

    // to not override in every class
    @Override
    public TankType selectTank(int tankIndex, int teamSize) {
        throw new UnsupportedOperationException("Must never be called!");
    }

    /**
     * all strategies depends not only on self, world and move, but on precalculated context too
     */
    abstract void move(StrategyContext context);

    @Override
    public void move(Tank self, World world, Move move) {
    }
}

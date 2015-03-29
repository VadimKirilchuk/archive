import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.atan2;

public final class Utils2D {

    /**
     * Returns absolute angle form point to target
     */
    public static double getAngleTo(double x, double y, double targetX, double targetY) {
        double absoluteAngleTo = atan2(targetY - y, targetX - x);

        while (absoluteAngleTo > PI) {
            absoluteAngleTo -= 2.0D * PI;
        }

        while (absoluteAngleTo < -PI) {
            absoluteAngleTo += 2.0D * PI;
        }

        return absoluteAngleTo;
    }

    private Utils2D() {
    }
}

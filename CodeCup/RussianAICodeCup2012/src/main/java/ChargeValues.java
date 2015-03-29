import model.Bonus;
import model.Obstacle;
import model.Shell;
import model.Tank;


public class ChargeValues {
    
    double ammoCharge = 0;
    double medkitCharge = 0;
    double repairCharge = 0;
    double enemyTankCharge = 0;
    
    public double value(Bonus bonus) {
        switch (bonus.getType()) {
            case AMMO_CRATE: {
                return ammoCharge;
            }
            case MEDIKIT: {
                return medkitCharge;
            }
            case REPAIR_KIT: {
                return repairCharge;
            }
        }
        return 0;
    }

    public double value(Tank tank) {
        if (tank.isTeammate()) {
            return -10;
        } else if (!AbstractStrategy.isAlive(tank)){
            return -10;
        } else {
            return enemyTankCharge;
        }
    }

    public double value(Shell shell) {
        return -0;
    }

    public double value(Obstacle obstacle) {
        return -10;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ChargeValues [ammoCharge=").append(ammoCharge).append(", medkitCharge=").append(medkitCharge)
                .append(", repairCharge=").append(repairCharge).append(", enemyTankCharge=").append(enemyTankCharge).append("]");
        return builder.toString();
    }
    
    
}
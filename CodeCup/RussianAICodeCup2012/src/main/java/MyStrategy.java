import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import model.Move;
import model.Tank;
import model.TankType;
import model.World;

public final class MyStrategy implements Strategy {

    public static boolean debug = true;
    public static volatile VisualDebugFrame debugView;
    {
        if (debug) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        debugView = new VisualDebugFrame();
                    }
                });
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    final StrategyContext context = new StrategyContext();
    AbstractStrategy moveStrategy = new MoveByPotentialStrategy();
    AbstractStrategy attackStrategy = new DefaultAttackStrategy();

    @Override
    public void move(Tank self, World world, Move move) {
        context.update(self, world, move);

        // move somewhere
        moveStrategy.move(context);

        // rotate gun or atack something
        attackStrategy.move(context);

        if (debug) {
            debugView.updateView(this, context);
        }
    }

    @Override
    public TankType selectTank(int tankIndex, int teamSize) {
        return TankType.MEDIUM;
    }
}

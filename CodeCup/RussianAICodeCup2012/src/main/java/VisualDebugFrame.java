import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferStrategy;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import model.Bonus;
import model.Obstacle;
import model.Shell;
import model.Tank;
import model.Unit;
import model.World;

public class VisualDebugFrame extends JFrame {

    private double scaleX = 0.75;
    private double scaleY = 0.75;
    private int originWidth = 1280;
    private int originHeight = 800;
    private int width = (int) (originWidth * scaleX);
    private int height = (int) (originHeight * scaleY);

    private BufferStrategy canvasBufferStrategy;
    private Graphics2D canvasGraphics;

    private Canvas drawingPanel;
    private JTextArea infoTextArea;

    // dot-dashed line
    private Stroke thindashed = new BasicStroke(0.1f, // line width
            BasicStroke.CAP_BUTT,/* cap style */
            BasicStroke.JOIN_BEVEL, 1.0f,/* join style, miter limit */
            new float[] { 8.0f, 3.0f, 2.0f, 3.0f },/* the dash pattern *//* on 8, off 3, on 2, off 3 */
            0.0f/* the dash phase */);

    public VisualDebugFrame() {
        super("Debug");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        createLayout();
        pack();
        setMinimumSize(getSize());

        setLocationRelativeTo(null);
        setResizable(true);
        setVisible(true);
        
        createCanvasBufferStrategy();
    }

    public void updateView(final MyStrategy myStrategy, final StrategyContext context) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                if (!isShowing()) {
                    return;
                }

                World world = context.world;

                for (Tank tank : world.getTanks()) {
                    drawTank(tank);
                }

                for (Shell shell : world.getShells()) {
                    Rectangle2D r = createRectangle(shell);
                    Shape result = rotate(shell.getAngle(), r);
                    canvasGraphics.draw(result);
                }

                for (Bonus bonus : world.getBonuses()) {
                    drawBonus(bonus);
                }

                for (Obstacle obstacle : world.getObstacles()) {
                    Rectangle2D shape = createRectangle(obstacle);
                    canvasGraphics.draw(shape);
                }
                
                canvasBufferStrategy.show();
                canvasGraphics.dispose();
                initCanvas();
                infoTextArea.setText("");
            }
        };
        invokeInDispatchThreadIfNeeded(runnable);
    }

    public void drawBonus(final Bonus bonus) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Rectangle2D shape = createRectangle(bonus);
                canvasGraphics.draw(shape);
                switch (bonus.getType()) {
                    case AMMO_CRATE: {
                        canvasGraphics.drawString("A", (int) bonus.getX(), (int) bonus.getY());
                        break;
                    }
                    case MEDIKIT: {
                        canvasGraphics.drawString("+", (int) bonus.getX(), (int) bonus.getY());
                        break;
                    }
                    case REPAIR_KIT: {
                        canvasGraphics.drawString("R", (int) bonus.getX(), (int) bonus.getY());
                        break;
                    }
                }
            }
        };
        invokeInDispatchThreadIfNeeded(runnable);
    }
    
    public void addInfoLine(final String line) {
        Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                infoTextArea.append(line);
                infoTextArea.append("\n");  
            }
        };
        invokeInDispatchThreadIfNeeded(runnable);
    }

    public void drawTank(final Tank tank) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Rectangle2D tankShape = createRectangle(tank);
                Shape rotatedTank = rotate(tank.getAngle(), tankShape);
                Line2D gunShape = new Line2D.Double(tank.getX(), tank.getY(), tank.getX() + tank.getVirtualGunLength(), tank.getY());
                Shape rotatedGun = rotate(tank.getAngle() + tank.getTurretRelativeAngle(), gunShape);
                canvasGraphics.draw(rotatedTank);
                canvasGraphics.draw(rotatedGun);
                // i also want a dashed line from gun
                Line2D gunShape2 = new Line2D.Double(tank.getX(), tank.getY(), tank.getX() + originWidth, tank.getY());
                rotatedGun = rotate(tank.getAngle() + tank.getTurretRelativeAngle(), gunShape2);
                drawWithStroke(rotatedGun, thindashed);
            }
        };
        invokeInDispatchThreadIfNeeded(runnable);
    }

    public void drawWithStroke(final Shape shape, final Stroke stroke) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Stroke original = canvasGraphics.getStroke();
                canvasGraphics.setStroke(stroke);
                canvasGraphics.draw(shape);
                canvasGraphics.setStroke(original);
            }
        };
        invokeInDispatchThreadIfNeeded(runnable);
    }

    public void drawWithColor(final Shape shape, final Color color) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Color original = canvasGraphics.getColor();
                canvasGraphics.setColor(color);
                canvasGraphics.draw(shape);
                canvasGraphics.setColor(original);
            }
        };
        invokeInDispatchThreadIfNeeded(runnable);
    }

    public Double createRectangle(Unit unit) {
        double unitWidth = unit.getWidth();
        double unitHeight = unit.getHeight();
        return new Rectangle2D.Double(unit.getX() - unitWidth * 0.5, unit.getY() - unitHeight * 0.5, unitWidth, unitHeight);
    }

    public Shape rotate(double angle, Rectangle2D sourceShape) {
        AffineTransform transform = getTransform(angle, sourceShape.getCenterX(), sourceShape.getCenterY());
        Shape result = transform.createTransformedShape(sourceShape);
        return result;
    }

    public Shape rotate(double angle, Line2D sourceShape) {
        AffineTransform transform = getTransform(angle, sourceShape.getX1(), sourceShape.getY1());
        Shape result = transform.createTransformedShape(sourceShape);
        return result;
    }

    public AffineTransform getTransform(double angle, double anchorX, double anchorY) {
        return AffineTransform.getRotateInstance(angle, anchorX, anchorY);
    }

    private void invokeInDispatchThreadIfNeeded(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    private void createLayout() {
        setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));

        drawingPanel = new Canvas();
        drawingPanel.setIgnoreRepaint(true);
        
        Dimension preferredSize = new Dimension(width, height);
        drawingPanel.setPreferredSize(preferredSize);
        add(drawingPanel);

        infoTextArea = new JTextArea();
        infoTextArea.setEditable(false);
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setPreferredSize(new Dimension(200, 200));
        infoTextArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        add(infoTextArea);
    }

    private void createCanvasBufferStrategy() {
        drawingPanel.createBufferStrategy(2);
        canvasBufferStrategy = drawingPanel.getBufferStrategy();
        initCanvas();
    }
    
    private void initCanvas() {
        canvasGraphics = (Graphics2D) canvasBufferStrategy.getDrawGraphics();
        canvasGraphics.scale(scaleX, scaleY);
        canvasGraphics.clearRect(0, 0, originWidth, originHeight);//clear
        canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
}

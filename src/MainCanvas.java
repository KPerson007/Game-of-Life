import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.GraphicAttribute;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.lang.Math;

/**
 * Created by Kevin on 9/14/2016.
 */
public class MainCanvas extends Canvas implements Runnable, KeyListener, MouseListener {
    private final int TEXT_X = 10;
    private final int TEXT_Y_OFFSET = 15;
    private final int NUM_TEXT = 5;
    private final int MAX_GRID_SIZE = 100;

    private int y1 = TEXT_Y_OFFSET * (NUM_TEXT + 1); //get starting y coordinate of the grid
    private int gridSize = 25;
    private int xOffset = 0;
    private int yOffset = 0;
    private int maxX = gridSize;
    private int maxY = gridSize;
    private int minX = 1;
    private int minY = 1;
    private Thread runThread = null;
    private int simulationSpeed = 1;
    private boolean started = false;
    private boolean oneGenOnly = false;
    private ArrayList<Point> organismLocations = null; //the points of organisms

    public void update(Graphics g)
    {
        //set up double buffering
        Graphics doubleBufferGraphics;
        BufferedImage doubleBuffer;
        Dimension d = this.getSize();
        doubleBuffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        doubleBufferGraphics = doubleBuffer.getGraphics();
        doubleBufferGraphics.setColor(this.getBackground());
        doubleBufferGraphics.fillRect(0, 0, d.width, d.height);
        doubleBufferGraphics.setColor(this.getForeground());
        paint(doubleBufferGraphics);

        //flip
        g.drawImage(doubleBuffer, 0, 0, this);
    }

    public void paint(Graphics g)
    {
        Dimension d = this.getSize();
        //initialize the main loop thread if it is not
        if (runThread == null)
        {
            this.addKeyListener(this);
            this.addMouseListener(this);
            runThread = new Thread(this);
            runThread.start();
        }

        //initialize a default location set for the organisms
        if (organismLocations == null)
        {
            organismLocations = new ArrayList<Point>();
        }

        //paint the main screen on the BufferedImage
        //draw text telling the user the application's controls
        g.drawString("CONTROLS: SPACE to do 1 generation, 1-9 to load presets,", TEXT_X, TEXT_Y_OFFSET);
        g.drawString("LEFT CLICK on grid spots to add an organism, C to clear gird,", TEXT_X, TEXT_Y_OFFSET * 2);
        g.drawString("RIGHT CLICK on grid spots to remove an organism, ENTER to start/stop,", TEXT_X, TEXT_Y_OFFSET * 3);
        g.drawString("RIGHT ARROW to increase simulation speed, LEFT ARROW to decrease simulation speed,", TEXT_X, TEXT_Y_OFFSET * (NUM_TEXT - 1));
        g.drawString("UP ARROW to increase grid size, DOWN ARROW to decrease grid size", TEXT_X, TEXT_Y_OFFSET * NUM_TEXT);
        String statsString = "Grid Size: " + gridSize + "x" + gridSize + " Simulation Speed: " + simulationSpeed + "x";
        g.drawString(statsString, d.width - TEXT_X - g.getFontMetrics().stringWidth(statsString), TEXT_Y_OFFSET);
        //draw the grid
        //draw insides
        boolean drawNewXBoundary = false;
        boolean drawNewYBoundary = false;
        //determine how to center the grid and draw boundaries
        if (d.width - getX(gridSize - 1, d) != getX(1, d))
            xOffset = (d.width - (getX(gridSize - 1, d) + getX(1, d))) / 2;
        if ((d.height - y1) - getY(gridSize - 1, d, y1) != getY(1, d, y1))
            yOffset = (d.height - (getY(gridSize - 1, d, y1) + getY(1, d, y1) - y1)) / 2;
        //System.out.println(xOffset);
        //System.out.println(yOffset);
        for (int i = 1; i < gridSize; i++)
        {
            int x = getX(i, d);
            if (i == gridSize - 1)
                if (d.width - x != getX(1, d))
                    drawNewXBoundary = false;
            g.drawLine(x + xOffset, y1 + yOffset, x + xOffset, d.height - yOffset); //column
            int y = getY(i, d, y1);
            if (i == gridSize - 1)
                if ((d.height - y1) - y != getY(1, d, y1))
                    drawNewYBoundary = false;
            g.drawLine(xOffset, y + yOffset, d.width - xOffset, y + yOffset); //row
        }
        if (drawNewXBoundary)
        {
            g.setColor(Color.magenta);
            int x = getX(gridSize - 1, d) + getX(1, d);
            System.out.println(x);
            g.fillRect(x, y1, d.width - x, d.height - y1);
            g.setColor(this.getForeground());
        }
        if (drawNewYBoundary)
        {
            g.setColor(Color.magenta);
            int y = getY(gridSize - 1, d, y1) + getY(1, d, y1) - y1;
            g.fillRect(0, y, d.width, d.height - y);
            g.setColor(this.getForeground());
        }

        //draw borders
        g.drawLine(xOffset, y1 + yOffset, d.width - xOffset, y1 + yOffset); //top
        g.drawLine(xOffset, y1 + yOffset, xOffset, d.height - yOffset); //left
        g.drawLine(xOffset, d.height - yOffset, d.width - xOffset, d.height - yOffset); //bottom
        g.drawLine(d.width - xOffset, y1 + yOffset, d.width - xOffset, d.height - yOffset); //right

        //draw organisms
        for (Point p : organismLocations)
            if (!(p.x > gridSize || p.y > gridSize))
                g.fillRect(getX(p.x, d) - getX(1, d) + xOffset, getY(p.y, d, y1) - getY(1, d, y1) + y1 + yOffset, getX(1, d), getY(1, d, y1) - y1);  //the coordinates for the organisms are 1 based not 0 based, so everything must be shifted to reflect that
    }

    public int getX(int n, Dimension d)
    {
        return (d.width / gridSize) * n;
    }

    public int getY(int n, Dimension d, int offset)
    {
        return (((d.height - offset) / gridSize) * n) + offset;
    }

    @Override
    public void run()
    {
        while (true)
        {
            if (started)
            {
                if (oneGenOnly == true)
                {
                    started = false;
                    oneGenOnly = false;
                }

                //System.out.println("Started simulation");
                ArrayList<Point> newPoints = new ArrayList<Point>();
                maxX = 0;
                maxY = 0;
                minX = MAX_GRID_SIZE;
                minY = MAX_GRID_SIZE;
                for (Point p : organismLocations) //apply rules 1, 2, and 3 to the live cells and check for max and min x and y
                {
                    if (p.x > maxX)
                        maxX = p.x;
                    if (p.y > maxY)
                        maxY = p.y;
                    if (p.x < minX)
                        minX = p.x;
                    if (p.y < minY)
                        minY = p.y;
                    //System.out.println("rule 1,2,3 point p: " + p.x + ", " + p.y);
                    ArrayList<Point> removedPoints = new ArrayList<Point>();
                    int surroundingOrganisms = 0;
                    if (organismLocations.contains(new Point(p.x - 1, p.y - 1)))
                        surroundingOrganisms++;
                    if (organismLocations.contains(new Point(p.x, p.y - 1)))
                        surroundingOrganisms++;
                    if (organismLocations.contains(new Point(p.x + 1, p.y - 1)))
                        surroundingOrganisms++;
                    if (organismLocations.contains(new Point(p.x - 1, p.y)))
                        surroundingOrganisms++;
                    if (organismLocations.contains(new Point(p.x + 1, p.y)))
                        surroundingOrganisms++;
                    if (organismLocations.contains(new Point(p.x - 1, p.y + 1)))
                        surroundingOrganisms++;
                    if (organismLocations.contains(new Point(p.x, p.y + 1)))
                        surroundingOrganisms++;
                    if (organismLocations.contains(new Point(p.x + 1, p.y + 1)))
                        surroundingOrganisms++;
                    if (surroundingOrganisms < 2)
                    {
                        removedPoints.add(p);
                        //System.out.println("removed " + p.toString());
                    }
                    if (surroundingOrganisms == 2 || surroundingOrganisms == 3)
                    {
                        newPoints.add(p);
                        //System.out.println("added " + p.toString());
                    }
                    if (surroundingOrganisms > 3)
                    {
                        removedPoints.add(p);
                        //System.out.println("removed " + p.toString());
                    }
                }

                //apply rule 4 to all dead cells
                for (int x = Math.max(1, minX - 1); x <= Math.min(MAX_GRID_SIZE, maxX + 1); x++) //initial value of x can't be lower than 1, final value of x can't be higher than gridSize
                {
                    for (int y = Math.max(1, minY - 1); y <= Math.min(MAX_GRID_SIZE, maxY + 1); y++) //initial value of y can't be lower than 1, final value of y can't be higher than gridSize
                    {
                        //System.out.println("NEW LOOP: " + x + ", " + y);
                        int surroundingOrganisms = 0;
                        if (!(organismLocations.contains(new Point(x, y))))
                        {
                            for (Point t : organismLocations) {
                                //System.out.println("rule 4 point t: " + t.x + ", " + t.y);
                                //System.out.println("rule 4 point x,y: " + x + ", " + y);
                                if ((t.x == x || t.x == x - 1 || t.x == x + 1) && (t.y == y || t.y == y - 1 || t.y == y + 1))
                                    //System.out.println("rule 4 found point: " + t.x + ", " + t.y);
                                    surroundingOrganisms++;
                            }
                        }
                        //System.out.println("rule 4 organisms: " + surroundingOrganisms);
                        if (surroundingOrganisms == 3) {
                            newPoints.add(new Point(x, y));
                            //System.out.println("added " + x + ", " + y);
                        }
                    }
                }

                organismLocations.removeAll(organismLocations);
                organismLocations = new ArrayList<Point>();
                organismLocations.addAll(newPoints);
            }
            repaint();
            try
            {
                Thread.currentThread();
                Thread.sleep(1000 / simulationSpeed);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        switch (e.getKeyCode())
        {
            case KeyEvent.VK_SPACE:
                started = true;
                oneGenOnly = true;
                break;
            case KeyEvent.VK_ENTER:
                if (started == true)
                    started = false;
                else
                    started = true;
                break;
            case KeyEvent.VK_C:
                //clear the grid
                organismLocations = new ArrayList<Point>();
                break;
            case KeyEvent.VK_LEFT:
                if (simulationSpeed > 1)
                    simulationSpeed--;
                break;
            case KeyEvent.VK_RIGHT:
                simulationSpeed++;
                break;
            case KeyEvent.VK_UP:
                if (gridSize < MAX_GRID_SIZE)
                    gridSize++;
                break;
            case KeyEvent.VK_DOWN:
                if (gridSize > 10)
                    gridSize--;
                break;
            case KeyEvent.VK_1:
                //load preset "exploder"
                organismLocations = new ArrayList<Point>();
                organismLocations.add(new Point(10, 10));
                organismLocations.add(new Point(10, 11));
                organismLocations.add(new Point(10, 12));
                organismLocations.add(new Point(10, 13));
                organismLocations.add(new Point(10, 14));
                organismLocations.add(new Point(12, 10));
                organismLocations.add(new Point(12, 14));
                organismLocations.add(new Point(14, 10));
                organismLocations.add(new Point(14, 11));
                organismLocations.add(new Point(14, 12));
                organismLocations.add(new Point(14, 13));
                organismLocations.add(new Point(14, 14));
                break;
            case KeyEvent.VK_2:
                //load preset "10 cell row"
                organismLocations = new ArrayList<Point>();
                organismLocations.add(new Point(8, 12));
                organismLocations.add(new Point(9, 12));
                organismLocations.add(new Point(10, 12));
                organismLocations.add(new Point(11, 12));
                organismLocations.add(new Point(12, 12));
                organismLocations.add(new Point(13, 12));
                organismLocations.add(new Point(14, 12));
                organismLocations.add(new Point(15, 12));
                organismLocations.add(new Point(16, 12));
                organismLocations.add(new Point(17, 12));
                break;
            case KeyEvent.VK_3:
                //load preset "glider"
                organismLocations = new ArrayList<Point>();
                organismLocations.add(new Point(8, 12));
                organismLocations.add(new Point(9, 12));
                organismLocations.add(new Point(10, 12));
                organismLocations.add(new Point(10, 11));
                organismLocations.add(new Point(9, 10));
                break;
            case KeyEvent.VK_4:
                //load preset "pinwheel"
                organismLocations = new ArrayList<Point>();
                organismLocations.add(new Point(10, 10));
                organismLocations.add(new Point(10, 11));
                organismLocations.add(new Point(10, 12));
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void mouseClicked (MouseEvent e)
    {
        if (e.getButton() == 1)
        {
            //System.out.println("Click " + e.getX() + " " + e.getY());
            Dimension d = this.getSize();
            int foundX = 0;
            int foundY = 0;
            for (int x = 1; x <= gridSize; x++) //loop through every cell to
                for (int y = 1; y <= gridSize; y++)
                    if ((e.getX() >= getX(x - 1, d) + xOffset && e.getX() <= getX(x, d) + xOffset) && (e.getY() >= getY(y - 1, d, y1) + yOffset && e.getY() <= getY(y, d, y1) + yOffset)) {
                        foundX = x;
                        foundY = y;
                    }
            if (foundX != 0 && foundY != 0)
                organismLocations.add(new Point(foundX, foundY));
        }
        else if (e.getButton() == 3)
        {
            //System.out.println("right click");
            Dimension d = this.getSize();
            int foundX = 0;
            int foundY = 0;
            for (int x = 1; x <= gridSize; x++) //loop through every cell to
                for (int y = 1; y <= gridSize; y++)
                    if ((e.getX() >= getX(x - 1, d) + xOffset && e.getX() <= getX(x, d) + xOffset) && (e.getY() >= getY(y - 1, d, y1) + yOffset && e.getY() <= getY(y, d, y1) + yOffset)) {
                        foundX = x;
                        foundY = y;
                    }
            if (foundX != 0 && foundY != 0)
                organismLocations.remove(new Point(foundX, foundY));
        }
    }

    @Override
    public void mouseEntered (MouseEvent e)
    {
    }

    @Override
    public void mouseExited (MouseEvent e)
    {
    }

    @Override
    public void mousePressed (MouseEvent e)
    {
    }

    @Override
    public void mouseReleased (MouseEvent e)
    {
    }
}
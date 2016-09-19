import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.GraphicAttribute;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Kevin on 9/14/2016.
 */
public class MainCanvas extends Canvas implements Runnable, KeyListener {
    private final int TEXT_X = 10;
    private final int TEXT_Y_OFFSET = 15;
    private final int NUM_TEXT = 4;

    private Thread runThread = null;
    private int gridSize = 25;
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
            runThread = new Thread(this);
            runThread.start();
        }

        //initialize a default location set for the organisms
        if (organismLocations == null)
        {
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
        }

        //paint the main screen on the BufferedImage
        //draw text telling the user the application's controls
        g.drawString("CONTROLS: SPACE to do 1 generation,", TEXT_X, TEXT_Y_OFFSET);
        g.drawString("CLICK on grid spots to add an organism, ENTER to start/stop,", TEXT_X, TEXT_Y_OFFSET * (NUM_TEXT / 2));
        g.drawString("RIGHT ARROW to increase simulation speed, LEFT ARROW to decrease simulation speed,", TEXT_X, TEXT_Y_OFFSET * (NUM_TEXT - 1));
        g.drawString("UP ARROW to increase grid size, DOWN ARROW to decrease grid size", TEXT_X, TEXT_Y_OFFSET * NUM_TEXT);
        String statsString = "Grid Size: " + gridSize + "x" + gridSize + " Simulation Speed: " + simulationSpeed + "x";
        g.drawString(statsString, d.width - TEXT_X - g.getFontMetrics().stringWidth(statsString), TEXT_Y_OFFSET);
        //draw the grid
        //draw borders
        int y1 = TEXT_Y_OFFSET * (NUM_TEXT + 1);
        g.drawLine(0, y1, d.width, y1); //top
        g.drawLine(0, y1, 0, d.height); //left
        g.drawLine(0, d.height - 1, d.width, d.height - 1); //bottom
        g.drawLine(d.width - 1, y1, d.width - 1, d.height); //right
        //draw insides
        for (int i = 1; i < gridSize; i++)
        {
            int x = getX(i, d);
            g.drawLine(x, y1, x, d.height);
            int y = getY(i, d, y1);
            g.drawLine(0, y, d.width, y);
        }
        //draw organisms
        for (Point p : organismLocations)
        {
            g.fillRect(getX(p.x, d) - getX(1, d), getY(p.y, d, y1) - getY(1, d, y1) + y1, getX(1, d), getY(1, d, y1) - y1);  //the coordinates for the organisms are 1 based not 0 based, so everything must be shifted to reflect that
        }
    }

    public int getX(int n, Dimension d)
    {
        return (d.width / gridSize) * n;
    }

    public int getY(int n, Dimension d, int y1)
    {
        return (((d.height - y1) / gridSize) * n) + y1;
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
                for (Point p : organismLocations) //apply rules 1, 2, and 3 to the live cells
                {
                    //System.out.println("rule 1,2,3 point p: " + p.x + ", " + p.y);
                    ArrayList<Point> removedPoints = new ArrayList<Point>();
                    int surroundingOrganisms = 0;
//                    for (Point t : organismLocations)
//                    {
//                        System.out.println("rule 1,2,3 point t: " + t.x + ", " + t.y);
//                        if (t.x == p.x || t.x == p.x - 1 || t.x == p.x + 1)
//                            if (t.y == p.y || t.y == p.y - 1 || t.y == p.y + 1)
//                                if (!(t.x == p.x && t.y == p.y))
//                                    surroundingOrganisms++;
//                    }
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
                for (int x = 1; x <= gridSize; x++)
                {
                    for (int y = 1; y <= gridSize; y++) {
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
            case KeyEvent.VK_LEFT:
                if (simulationSpeed > 1)
                    simulationSpeed--;
                break;
            case KeyEvent.VK_RIGHT:
                simulationSpeed++;
                break;
            case KeyEvent.VK_UP:
                gridSize++;
                break;
            case KeyEvent.VK_DOWN:
                if (gridSize > 10)
                    gridSize--;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }
}

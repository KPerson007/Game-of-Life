import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by Kevin on 9/14/2016.
 */
public class MainApplet extends Applet {
    private MainCanvas c;

    public void init()
    {
        c = new MainCanvas();
        c.setPreferredSize(new Dimension(640, 480));
        c.setVisible(true);
        c.setFocusable(true);
        this.add(c);
        this.setVisible(true);
        this.setSize(new Dimension(640, 480));
    }

    public void paint(Graphics g)
    {
        this.setSize(new Dimension(640, 480));
    }
}

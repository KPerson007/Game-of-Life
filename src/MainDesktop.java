import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Kevin on 9/14/2016.
 */
public class MainDesktop {

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Game of Life");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setPreferredSize(new Dimension(640, 480));
        frame.setSize(new Dimension(640, 480));
        MainCanvas c = new MainCanvas();
        frame.add(c);
        frame.setVisible(true);
    }

}

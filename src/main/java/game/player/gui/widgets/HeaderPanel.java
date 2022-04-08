package game.player.gui.widgets;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    private static JLabel label;

    public HeaderPanel() {
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);

        label = new JLabel();
        setLabelText("Not logged in");

        label.setPreferredSize(new Dimension(label.getWidth(), 50));

        label.setForeground(Color.white);
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 25 ));
        label.setHorizontalAlignment(JLabel.CENTER);

        add(label, BorderLayout.CENTER);
    }

    public static void setLabelText(String text) {
        label.setText("Header Zone : " + text);
    }
}


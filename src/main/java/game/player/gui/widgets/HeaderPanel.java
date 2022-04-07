package game.player.gui.widgets;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel {

    private static JLabel label;

    public HeaderPanel() {
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);

        this.label = new JLabel("Header Zone : (0, 0)");
        this.label.setPreferredSize(new Dimension(this.label.getWidth(), 50));

        this.label.setForeground(Color.white);
        this.label.setFont(new Font(this.label.getFont().getName(), Font.PLAIN, 25 ));
        this.label.setHorizontalAlignment(JLabel.CENTER);

        add(label, BorderLayout.CENTER);
    }

    public static void setLabelText(String text) {
        label.setText(text);
    }
}


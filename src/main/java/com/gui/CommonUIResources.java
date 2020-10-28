package com.gui;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.util.Properties;

public class CommonUIResources {
    static final Properties UI_TEXTS = new Properties();
    static final Properties PROGRAM_META = new Properties();
    static final Properties LANG_PACK_TEMPLATE = new Properties();

    public void initializeUIResources() {
        try {
            UI_TEXTS.load(getClass().getResourceAsStream("/UITexts.properties"));
            PROGRAM_META.load(getClass().getResourceAsStream("/WREXMeta.properties"));
            LANG_PACK_TEMPLATE.load(getClass().getResourceAsStream("/langPackTemplate.properties"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "An unknown problem has occurred.",
                    "Problem",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
}

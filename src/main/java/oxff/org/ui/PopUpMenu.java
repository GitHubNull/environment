package oxff.org.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PopUpMenu implements ContextMenuItemsProvider {

    MontoyaApi montoyaApi;
    Logging logger;

    private Color[] buildin_colors = {
            Color.BLUE,
            Color.CYAN,
            Color.GRAY,
            Color.GREEN,
            Color.MAGENTA,
            Color.ORANGE,
            Color.PINK,
            Color.RED,
            Color.YELLOW
    };

    private HighlightColor[] highlightColors = {
            HighlightColor.BLUE,
            HighlightColor.CYAN,
            HighlightColor.GRAY,
            HighlightColor.GREEN,
            HighlightColor.MAGENTA,
            HighlightColor.ORANGE,
            HighlightColor.PINK,
            HighlightColor.RED,
            HighlightColor.YELLOW
    };

    private final HashMap<String, Color> nameColors = new HashMap<>();
    private final HashMap<Color, HighlightColor> colorHighlightColorHashMap = new HashMap<>();

    private final String[] colorNames = {"BLUE", "CYAN", "GRAY", "GREEN", "MAGENTA", "ORANGE", "PINK", "RED", "YELLOW"};

    public PopUpMenu(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        logger = montoyaApi.logging();

        for (int i = 0; i < buildin_colors.length; i++) {
            nameColors.put(colorNames[i], buildin_colors[i]);
            colorHighlightColorHashMap.put(buildin_colors[i], highlightColors[i]);
        }
    }
    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        ArrayList<Component> menus = new ArrayList<>();

        for (String colorName : colorNames) {
            JMenuItem item = new JMenuItem(colorName);
            item.setOpaque(true);
            item.setBackground(nameColors.get(colorName));
            item.setForeground(Color.WHITE);
            menus.add(item);
        }

        return menus;
    }
}

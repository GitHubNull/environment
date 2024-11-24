package oxff.org.ui;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.HighlightColor;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PopUpMenu implements ContextMenuItemsProvider {

    MontoyaApi montoyaApi;
    Logging logger;

    private final static Color[] build_in_colors = {
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

    private final static HighlightColor[] highlightColors = {
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

    private final static HashMap<String, Color> nameColors = new HashMap<>();
    private final static HashMap<Color, HighlightColor> colorHighlightColorHashMap = new HashMap<>();

    private final String[] colorNames = {"BLUE", "CYAN", "GRAY", "GREEN", "MAGENTA", "ORANGE", "PINK", "RED", "YELLOW"};

    public PopUpMenu(MontoyaApi montoyaApi) {
        this.montoyaApi = montoyaApi;
        logger = montoyaApi.logging();

        for (int i = 0; i < build_in_colors.length; i++) {
            nameColors.put(colorNames[i], build_in_colors[i]);
            colorHighlightColorHashMap.put(build_in_colors[i], highlightColors[i]);
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

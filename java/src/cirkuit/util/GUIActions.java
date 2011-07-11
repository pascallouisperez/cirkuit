package cirkuit.util;

/**
 * This class stores all the actions happening in CirKuit 2D's GUIs. These are separated
 * into ... sections:<br />
 * - CB : Combo boxes actions<br />
 * - MENU : Menu actions<br />
 * These variables can then be used when registering action commands (eg.
 * <code>button.setActionCommand(GUIActions.BUTTON_CLOSE+"");</code>) and then used in the
 * action listener:
<pre>
try {
    int action = Integer.parseInt(e.getActionCommand());
    switch (action) {
        case GUIActions.BUTTON_CLOSE:
            // your code here
            break;
        
        ...
    }
} catch(Exception err) {};
</pre>
 * @author Sven Gowal (sven.gowal@lha.ch)
 * @author Pascal Perez (pascal.perez@lha.ch)
 */
public class GUIActions {
    // the actions are in alphabetical order of the variable name, please keep it clean! Pascal Perez.
    // A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
    
    // [20'000-29'999]
    public static final int BUTTON_CANCEL      =  20000;
    public static final int BUTTON_CLOSE       =  20100;
    public static final int BUTTON_CREATEGAME  =  20200;
    public static final int BUTTON_DESTROYGAME =  20250;
    public static final int BUTTON_JOIN        =  20300;
    public static final int BUTTON_MODIFYGAME  =  20400;
    public static final int BUTTON_OK          =  20500;
    public static final int BUTTON_QUIT        =  20600;
    public static final int BUTTON_REFRESH     =  20700;
    public static final int BUTTON_STARTGAME   =  20800;
    
    // [10'000-19'999]
    public static final int CB_AVAILGAMES      = 10000;
    
    // [0-9'999]
    public static final int MENU_ABOUT         =     0;
    public static final int MENU_CREATE        =   100;
    public static final int MENU_DISCONNECT    =   200;
    public static final int MENU_EDITCIRCUIT   =   300;
    public static final int MENU_EXIT          =   400;
    public static final int MENU_FILE          =   500;
    public static final int MENU_HELP          =   600;
    public static final int MENU_LAUNCHSERVER  =   700;
    public static final int MENU_LOAD          =   800;
    public static final int MENU_OPTIONS       =   900;
    public static final int MENU_PLAYONLINE    =  1000;
    public static final int MENU_PRINT         =  1100;
    public static final int MENU_QUITGAME      =  1200;
    public static final int MENU_REFRESH       =  1300;
    public static final int MENU_REPLAY        =  1400;
    public static final int MENU_SAVE          =  1500;
    public static final int MENU_SPEED025      =  1600;
    public static final int MENU_SPEED050      =  1700;
    public static final int MENU_SPEED100      =  1800;
    public static final int MENU_SPEED200      =  1900;
    public static final int MENU_STARTGAME     =  2000;
}

package keyboard.tablet;

import utilities.MyUtilities;
import utilities.Point;
import keyboard.KeyboardAttribute;
import keyboard.KeyboardAttributes;
import enums.Attribute;
import enums.Key;

public class TabletAttributes extends KeyboardAttributes {
    private static final Point KEYBOARD_SIZE = new Point(730, 360);
    private static final Integer BORDER_SIZE = 10;
    private static final Float CAMERA_DISTANCE = MyUtilities.MATH_UTILITILES.findDimensionalAlignment(KEYBOARD_SIZE.y, BORDER_SIZE); // Align 2D with 3D
    private static final Point GAP_SIZE = new Point(10, 10);
    private static final Point KEY_SIZE = new Point(64, 64);
    private static final Point SPACE_KEY_SIZE = new Point(286, 64);
    private static final Point BACK_SPACE_KEY_SIZE = new Point(101, 64);
    private static final Point ENTER_KEY_SIZE = new Point(143, 64);
    private static final Point SHIFT_KEY_SIZE = new Point(101, 64);
    private static final int FIRST_ROW_OFFSET = 0;
    private static final int SECOND_ROW_OFFSET = 0;
    private static final int THIRD_ROW_OFFSET = 37;
    private static final int FOURTH_ROW_OFFSET = 0;
    private static final int FIFTH_ROW_OFFSET = 143;
    private static final Key [] FIRST_ROW = {Key.VK_1, Key.VK_2, Key.VK_3, Key.VK_4, Key.VK_5, Key.VK_6, Key.VK_7, Key.VK_8, Key.VK_9, Key.VK_0};
    private static final Key [] SECOND_ROW = {Key.VK_Q, Key.VK_W, Key.VK_E, Key.VK_R, Key.VK_T, Key.VK_Y, Key.VK_U, Key.VK_I, Key.VK_O, Key.VK_P};
    private static final Key [] THIRD_ROW = {Key.VK_A, Key.VK_S, Key.VK_D, Key.VK_F, Key.VK_G, Key.VK_H, Key.VK_J, Key.VK_K, Key.VK_L};
    private static final Key [] FOURTH_ROW = {Key.VK_SHIFT, Key.VK_Z, Key.VK_X, Key.VK_C, Key.VK_V, Key.VK_B, Key.VK_N, Key.VK_M, Key.VK_BACK_SPACE};
    private static final Key [] FIFTH_ROW = {Key.VK_COMMA, Key.VK_SPACE, Key.VK_PERIOD, Key.VK_ENTER};
    
    TabletAttributes(TabletKeyboard keyboard) {
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.KEYBOARD_SIZE, KEYBOARD_SIZE));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.BORDER_SIZE, BORDER_SIZE));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.CAMERA_DISTANCE, CAMERA_DISTANCE));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.GAP_SIZE, GAP_SIZE));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.KEY_SIZE, KEY_SIZE));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.SPACE_KEY_SIZE, SPACE_KEY_SIZE));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.BACK_SPACE_KEY_SIZE, BACK_SPACE_KEY_SIZE));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.ENTER_KEY_SIZE, ENTER_KEY_SIZE));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.SHIFT_KEY_SIZE, SHIFT_KEY_SIZE));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.NUMBER_OF_KEYS, FIRST_ROW.length + SECOND_ROW.length + THIRD_ROW.length + FOURTH_ROW.length));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.ROW_OFFSETS, (new Integer[] {FIRST_ROW_OFFSET,SECOND_ROW_OFFSET,THIRD_ROW_OFFSET,FOURTH_ROW_OFFSET,FIFTH_ROW_OFFSET})));
        this.addAttribute(new KeyboardAttribute(keyboard, Attribute.KEY_ROWS, new Key[][] {FIRST_ROW, SECOND_ROW, THIRD_ROW, FOURTH_ROW, FIFTH_ROW}));
    }
}

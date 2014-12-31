package keyboard.renderables;

import java.util.TreeMap;

import javax.media.opengl.GL2;

import com.leapmotion.leap.Vector;

import enums.Attribute;
import enums.Key;
import enums.Renderable;
import keyboard.KeyboardAttributes;
import keyboard.KeyboardRenderable;

//import static javax.media.opengl.GL.*;  // GL constants
//import static javax.media.opengl.GL2.*; // GL2 constants

public class VirtualKeyboard extends KeyboardRenderable {
    private static final String RENDER_NAME = Renderable.VIRTUAL_KEYS.toString();
    private TreeMap<Key, VirtualKey> keys = new TreeMap<Key, VirtualKey>();
    //private VirtualKey [] keys;
    
    public VirtualKeyboard(KeyboardAttributes keyboardAttributes) {
        super(RENDER_NAME);
        createKeys(keyboardAttributes);
    }
    
    private void createKeys(KeyboardAttributes keyboardAttributes) {
        int keyboardHeight = (int) keyboardAttributes.getValueByName(Attribute.KEYBOARD_HEIGHT.toString());
        int gapSize = (int) keyboardAttributes.getValueByName(Attribute.GAP_SIZE.toString());
        int keyWidth = (int) keyboardAttributes.getValueByName(Attribute.KEY_WIDTH.toString());
        int keyHeight = (int) keyboardAttributes.getValueByName(Attribute.KEY_HEIGHT.toString());
        int spaceKeyWidth = (int) keyboardAttributes.getValueByName(Attribute.SPACE_KEY_WIDTH.toString());
        int backSpaceKeyWidth = (int) keyboardAttributes.getValueByName(Attribute.BACK_SPACE_KEY_WIDTH.toString());
        int shiftKeyWidth = (int) keyboardAttributes.getValueByName(Attribute.SHIFT_KEY_WIDTH.toString());
        int enterWidth = (int) keyboardAttributes.getValueByName(Attribute.ENTER_KEY_WIDTH.toString());
        //int numKeys = (int) keyboardAttributes.getValueByName(AttributeName.NUMBER_OF_KEYS.toString());
        int[] rowOffsets = (int[]) keyboardAttributes.getValueByName(Attribute.ROW_OFFSETS.toString());
        Key[][] keyRows = (Key[][]) keyboardAttributes.getValueByName(Attribute.KEY_ROWS.toString());
        
        for(int rowIndex = 0, x = 0, y = keyboardHeight + gapSize; rowIndex < keyRows.length; rowIndex++) {
            x = rowOffsets[rowIndex];
            y -= (keyHeight + gapSize);
            for(int colIndex = 0; colIndex < keyRows[rowIndex].length; colIndex++) {
                Key key = keyRows[rowIndex][colIndex];
                if(key == Key.VK_SPACE) {
                    keys.put(key, new VirtualKey(x, y, spaceKeyWidth, keyHeight, key));
                    x += spaceKeyWidth + gapSize;
                } else if (key == Key.VK_BACK_SPACE) {
                    keys.put(key, new VirtualKey(x, y, backSpaceKeyWidth, keyHeight, key));
                    x += backSpaceKeyWidth + gapSize;
                } else if (key == Key.VK_SHIFT) {
                    keys.put(key, new VirtualKey(x, y, shiftKeyWidth, keyHeight, key));
                    x += shiftKeyWidth + gapSize;
                } else if (key == Key.VK_ENTER) {
                    keys.put(key, new VirtualKey(x, y, enterWidth, keyHeight, key));
                    x += enterWidth + gapSize;
                } else if (key == Key.VK_NULL) {
                    x += keyWidth + gapSize;
                } else {
                    keys.put(key, new VirtualKey(x, y, keyWidth, keyHeight, key));
                    x += keyWidth + gapSize;
                }
            }
        }
        
    }
    
    public void pressed(Key key) { // possibly generate key pressed event here with time/key/etc saved. Return the key pressed object.
        VirtualKey virtualKey = keys.get(key);
        if(virtualKey != null) {
            virtualKey.pressed();
        }
    }
    
    public VirtualKey isHoveringAny(Vector point) {
        VirtualKey vKey = null;
        for(VirtualKey virtualKey: keys.values()) {
            if(virtualKey.isHovering(point)) {
                vKey = virtualKey;
            }
        }
        return vKey;
    }
    
    public boolean isHovering(Key key, Vector point) {
        VirtualKey virtualKey = keys.get(key);
        if(virtualKey != null) {
            return virtualKey.isHovering(point);
        }
        return false;
    }

    @Override
    public void render(GL2 gl) {
        if(isEnabled()) {
            for(VirtualKey virtualKey: keys.values()) {
                virtualKey.render(gl);
            }
        }
    }

    public void clearAll() {
        for(VirtualKey virtualKey: keys.values()) {
            virtualKey.clear();
        }
    }
}

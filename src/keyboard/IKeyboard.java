package keyboard;

import java.util.ArrayList;
import java.util.Map;

import javax.media.opengl.GL2;

public abstract class IKeyboard {
    // TODO: list of things we need from the keyboard as an outside party
    // 1 - render the keyboard
    // 2 - get the attributes of the keyboard (size, etc)
    // 3 - get/set the settings of the keyboard (settings I can change)
    // 4 - get/set the renderables of the keyboard (image, key colors, leap plane etc)
    // 5 - give it key pressed events (to render them properly) --- and record specialized data for the specific type of keyboard
    // 6 - must be capable of creating it's own keyboard events using the robot if not the default (This will require a self sustaining update function possibly)
    private ArrayList<KeyboardObserver> observers = new ArrayList<KeyboardObserver>();
    protected ArrayList<KeyboardSetting> keyboardSettings = new ArrayList<KeyboardSetting>();
    protected ArrayList<KeyboardAttribute> keyboardAttributes = new ArrayList<KeyboardAttribute>();
    protected ArrayList<KeyboardRenderable> keyboardRenderables = new ArrayList<KeyboardRenderable>();
    protected int width;
    protected int height;
    protected char key;
    
    public abstract void render(GL2 gl);
    public abstract void update();
    
    public int getHeight() {
        return height;
    }
    public int getWidth() {
        return width;
    }
    
    public ArrayList<KeyboardSetting> getAllSettings() {
        return keyboardSettings;
    }
    public ArrayList<KeyboardAttribute> getAllAttributes() {
        return keyboardAttributes;
    }
    public ArrayList<KeyboardRenderable> getAllRenderables() {
        return keyboardRenderables;
    }
    
    public void registerObserver(KeyboardObserver observer) {
        if(observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }
    
    public void removeObserver(KeyboardObserver observer) {
        observers.remove(observer);
    }

    protected void notifyListeners() {
        for(KeyboardObserver observer : observers) {
            observer.keyboardEventObserved(key);
        }
    }
}

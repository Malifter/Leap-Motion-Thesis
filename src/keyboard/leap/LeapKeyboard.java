package keyboard.leap;

import swipe.SwipeKeyboard;
import utilities.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JPanel;

import utilities.MyUtilities;

import com.leapmotion.leap.InteractionBox;
import com.leapmotion.leap.Vector;

import enums.Attribute;
import enums.FileExt;
import enums.FileName;
import enums.FilePath;
import enums.Gesture;
import enums.Key;
import enums.Renderable;
import enums.Setting;
import experiment.WordManager;
import experiment.data.DataManager;
import experiment.data.LeapDataObserver;
import experiment.playback.LeapPlaybackObserver;
import experiment.playback.PlaybackManager;
import keyboard.CalibrationObserver;
import keyboard.IKeyboard;
import keyboard.KeyboardGesture;
import keyboard.KeyboardSetting;
import keyboard.renderables.KeyboardGestures;
import keyboard.renderables.LeapPlane;
import keyboard.renderables.SwipePoint;
import keyboard.renderables.LeapTool;
import keyboard.renderables.SwipeTrail;
import keyboard.renderables.VirtualKeyboard;
import leap.LeapData;
import leap.LeapListener;
import leap.LeapObserver;

public class LeapKeyboard extends IKeyboard implements LeapObserver, CalibrationObserver, LeapPlaybackObserver {
    public final int KEYBOARD_ID;
    private final String KEYBOARD_NAME;
    private final String KEYBOARD_FILE_NAME;
    private final ReentrantLock LEAP_LOCK = new ReentrantLock();
    private final float CAMERA_DISTANCE;
    private ArrayList<LeapDataObserver> observers = new ArrayList<LeapDataObserver>();
    private SwipePoint leapPoint;
    private SwipeTrail swipeTrail;
    private LeapData leapData;
    private LeapTool leapTool;
    private LeapPlane leapPlane;
    private LeapGestures leapGestures;
    private KeyboardGestures keyboardGestures;
    private SwipeKeyboard swipeKeyboard;
    private VirtualKeyboard virtualKeyboard;
    private boolean isCalibrated = false;
    private boolean shiftOnce = false;
    private boolean shiftTwice = false;
    
    public LeapKeyboard(boolean air) {
        super(air ? 2 : 1, air ? "Leap Air Keyboard" : "Leap Surface Keyboard", air ?  FileName.LEAP_AIR.getName() : FileName.LEAP_SURFACE.getName());
        KEYBOARD_ID = air ? 2 : 1;
        KEYBOARD_NAME = air ? "Leap Air Keyboard" : "Leap Surface Keyboard";
        KEYBOARD_FILE_NAME = air ?  FileName.LEAP_AIR.getName() : FileName.LEAP_SURFACE.getName();
        keyboardAttributes = new LeapAttributes(this);
        keyboardSettings = new LeapSettings(this);
        System.out.println(KEYBOARD_NAME + " - Loading Settings from " + FilePath.CONFIG.getPath() + KEYBOARD_FILE_NAME + FileExt.INI.getExt());
        try {
            MyUtilities.FILE_IO_UTILITIES.readSettingsAndAttributesFromFile(FilePath.CONFIG.getPath(), KEYBOARD_FILE_NAME + FileExt.INI.getExt(), this);
        } catch (IOException e) {
            System.out.println("Error occured while reading settings from file. Using default values on unreached settings.");
            e.printStackTrace();
        }
        System.out.println("-------------------------------------------------------");
        keyboardRenderables = new LeapRenderables(this, air);
        keyboardSize = keyboardAttributes.getAttributeAsPoint(Attribute.KEYBOARD_SIZE);
        int borderSize = keyboardAttributes.getAttributeAsInteger(Attribute.BORDER_SIZE) * 2;
        imageSize = new Point(keyboardSize.x + borderSize, keyboardSize.y + borderSize);
        CAMERA_DISTANCE = keyboardAttributes.getAttributeAsFloat(Attribute.CAMERA_DISTANCE);
        virtualKeyboard = (VirtualKeyboard) keyboardRenderables.getRenderable(Renderable.VIRTUAL_KEYBOARD);
        leapPoint = (SwipePoint) keyboardRenderables.getRenderable(Renderable.SWIPE_POINT);
        leapTool = (LeapTool) keyboardRenderables.getRenderable(Renderable.LEAP_TOOL);
        if(Gesture.ENABLED) {
            keyboardGestures = (KeyboardGestures) keyboardRenderables.getRenderable(Renderable.KEYBOARD_GESTURES);
        }
        swipeTrail = (SwipeTrail) keyboardRenderables.getRenderable(Renderable.SWIPE_TRAIL);
        leapPlane = (LeapPlane) keyboardRenderables.getRenderable(Renderable.LEAP_PLANE);
        leapPlane.registerObserver(this);
        if(!leapPlane.isCalibrated()) {
            leapPoint.blockAccess(true);
            leapTool.blockAccess(true);
            if(Gesture.ENABLED) {
                keyboardGestures.blockAccess(true);
            }
        } else {
            isCalibrated = true;
        }
        if(Gesture.ENABLED) {
            leapGestures = new LeapGestures();
        }
        swipeKeyboard = new SwipeKeyboard(this);
    }
    
    @Override
    public void render(GL2 gl) {
        MyUtilities.OPEN_GL_UTILITIES.switchToPerspective(gl, this, true);
        gl.glPushMatrix();
        gl.glTranslatef(-imageSize.x/2f, -imageSize.y/2f, -CAMERA_DISTANCE);
        keyboardRenderables.render(gl);
        gl.glPopMatrix();
    }
    
    @Override
    public void update() {
        LEAP_LOCK.lock();
        try {
            if(leapData != null) {
                leapData.populateData(leapPoint, leapTool);
            }
        } finally {
            LEAP_LOCK.unlock();
        }
        

        if(isPlayingBack()) {
            playbackManager.update();
            boolean isTouching = leapPlane.isNormalizedTouching(leapPoint.getNormalizedPoint().getZ()); //<= 40; // FIGURE OUT APPROPRIATE THRESHOLD HERE
            // Set tool point, scale it, rotate and position it.
            leapTool.update(leapPoint.getNormalizedPoint());
            
            if(Gesture.ENABLED) {
                // Set new gesture destination location.
                for(KeyboardGesture gesture: keyboardGestures.getGestures()) {
                    if(gesture.isValid()) {
                        gesture.update(leapPoint.getNormalizedPoint());
                    } else {
                        gesture.update();
                    }
                }
            }
            
            // Set add to trail and set location.
            if(isTouching) {
                swipeTrail.update(leapPoint.getNormalizedPoint());
            } else {
                swipeTrail.update();
            }
            
            // Update gestures after plane, we need both normalized and non normalized points.
            if(Gesture.ENABLED) leapGestures.update();
            
            swipeKeyboard.update(isTouching);
        } else {
            // Allow leap plane to take over the updates of specific objects that require the plane
            leapPlane.update(leapPoint, leapTool, Gesture.ENABLED ? keyboardGestures : null, swipeTrail);
            
            // Update gestures after plane, we need both normalized and non normalized points.
            if(Gesture.ENABLED) leapGestures.update();
            
            if(leapTool.isValid()) swipeKeyboard.update(leapPlane.isTouching());
        }
        
        //if(leapTool.isValid()) {
            if(leapTool.isValid()) notifyListenersLeapEvent(leapPoint.getNormalizedPoint(), leapTool.getDirection());
            
            Key key;
            if((key = swipeKeyboard.isPressed()) != Key.VK_NULL) {
                if(key != Key.VK_SHIFT) {
                    if(shiftOnce) {
                        keyPressed = key.toUpper();
                        shiftOnce = shiftTwice;
                        if(!shiftTwice) {
                            keyboardRenderables.swapToLowerCaseKeyboard();
                        }
                    } else {
                        keyPressed = key.getValue();   
                    }
                    if(!isPlayingBack()) {
                        notifyListenersKeyEvent();
                    }
                } else if(!shiftOnce && !shiftTwice) {
                    shiftOnce = true;
                    keyboardRenderables.swapToUpperCaseKeyboard();
                } else if(shiftOnce && !shiftTwice) {
                    shiftTwice = true;
                } else {
                    shiftTwice = false;
                    shiftOnce = false;
                    keyboardRenderables.swapToLowerCaseKeyboard();
                }
            }
            if(shiftTwice) {
                virtualKeyboard.locked(Key.VK_SHIFT);
            } else if(shiftOnce) {
                virtualKeyboard.pressed(Key.VK_SHIFT);
            }
        //}
    }
    
    @Override
    public void addToUI(JPanel panel, GLCanvas canvas) {
        LeapListener.registerObserver(this);
        LeapListener.startListening();
        WordManager.registerObserver(swipeKeyboard);
    }

    @Override
    public void removeFromUI(JPanel panel, GLCanvas canvas) {
        LeapListener.stopListening();
        LeapListener.removeObserver(this);
        leapTool.deleteQuadric();
        if(Gesture.ENABLED) {
            keyboardGestures.deleteQuadric();
        }
        WordManager.removeObserver(swipeKeyboard);
    }
    
    public void registerObserver(LeapDataObserver observer) {
        if(observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }
    
    public void removeObserver(LeapDataObserver observer) {
        observers.remove(observer);
    }

    protected void notifyListenersLeapEvent(Vector leapPoint, Vector toolDirection) {
        for(LeapDataObserver observer : observers) {
            observer.leapDataEventObserved(leapPoint, toolDirection);
        }
    }
    
    @Override
    protected boolean isPlayingBack() {
        LEAP_LOCK.lock();
        try {
            return isPlayback;
        } finally {
            LEAP_LOCK.unlock();
        }
    }
    
    @Override
    public void beginPlayback(PlaybackManager playbackManager) {
        LEAP_LOCK.lock();
        try {
            System.out.println(KEYBOARD_NAME + " - Loading playback settings from " + playbackManager.getFilePath() + KEYBOARD_FILE_NAME + FileExt.INI.getExt());
            try {
                MyUtilities.FILE_IO_UTILITIES.readSettingsAndAttributesFromFile(playbackManager.getFilePath(), KEYBOARD_FILE_NAME + FileExt.INI.getExt(), this);
            } catch (IOException e) {
                System.out.println("Error occured while reading settings from file. Using default values on unreached settings.");
                e.printStackTrace();
            }
            System.out.println("-------------------------------------------------------");
            LeapListener.stopListening();
            LeapListener.removeObserver(this);
        	leapPoint.setNormalizedPoint(Vector.zero());
            isPlayback = true;
            playbackManager.registerObserver(this);
            this.playbackManager = playbackManager;
        } finally {
            LEAP_LOCK.unlock();
        }
    }
    
	@Override
	public void pressedEventObserved(Key key) {
        keyPressed = key.getValue();
        notifyListenersKeyEvent();
	}
	
    @Override
    public void upperEventObserved(boolean upper) {
        // Ignoring SHIFT for now
    }

	@Override
	public void positionEventObserved(Vector pointPosition) {
		// Add point from the leap
	    leapPoint.setNormalizedPoint(pointPosition);
	}
	
    @Override
    public void directionEventObserved(Vector toolDirection) {
        leapTool.setTool(toolDirection);
    }
    
    @Override
    public void finishPlayback(PlaybackManager playbackManager) {
        LEAP_LOCK.lock();
        try {
            System.out.println(KEYBOARD_NAME + " - Loading default settings from " + FilePath.CONFIG.getPath() + KEYBOARD_FILE_NAME + FileExt.INI.getExt());
            try {
                MyUtilities.FILE_IO_UTILITIES.readSettingsAndAttributesFromFile(FilePath.CONFIG.getPath(), KEYBOARD_FILE_NAME + FileExt.INI.getExt(), this);
            } catch (IOException e) {
                System.out.println("Error occured while reading settings from file. Using default values on unreached settings.");
                e.printStackTrace();
            }
            System.out.println("-------------------------------------------------------");
            playbackManager.removeObserver(this);
            isPlayback = false;
            this.playbackManager = null;
            LeapListener.registerObserver(this);
            LeapListener.startListening();
        } finally {
            LEAP_LOCK.unlock();
        }
    }
    
    @Override
    public void beginExperiment(DataManager dataManager) {
        LEAP_LOCK.lock();
        try {
            this.registerObserver(dataManager);
        } finally {
            LEAP_LOCK.unlock();
        }
    }
    
    @Override
    public void finishExperiment(DataManager dataManager) {
        LEAP_LOCK.lock();
        try {
            this.removeObserver(dataManager);
        } finally {
            LEAP_LOCK.unlock();
        }
    }

    @Override
    public void beginCalibration(JPanel textPanel) {
        leapPoint.blockAccess(true);
        leapTool.blockAccess(true);
        if(Gesture.ENABLED) {
            keyboardGestures.blockAccess(true);
        }
        virtualKeyboard.blockAccess(true);
        keyboardRenderables.getRenderable(Renderable.KEYBOARD_IMAGE).blockAccess(true);
        leapPlane.beginCalibration(textPanel);
    }

    @Override
    protected void finishCalibration() {
        leapPoint.grantAccess(true);
        leapTool.grantAccess(true);
        if(Gesture.ENABLED) {
            keyboardGestures.grantAccess(true);
        }
        virtualKeyboard.grantAccess(true);
        keyboardRenderables.getRenderable(Renderable.KEYBOARD_IMAGE).grantAccess(true);
        isCalibrated = true;
        notifyListenersCalibrationFinished();
    }
    
    @Override
    public boolean isCalibrated() {
        return isCalibrated;
    }
    
    @Override
    public void leapEventObserved(LeapData leapData) {
        LEAP_LOCK.lock();
        try {
            this.leapData = leapData;
        } finally {
            LEAP_LOCK.unlock();
        }
    }

    @Override
    public void leapInteractionBoxSet(InteractionBox iBox) {
        leapPoint.setInteractionBox(iBox);
        leapPlane.setInteractionBox(iBox);
    }

    @Override
    public void keyboardCalibrationFinishedEventObserved() {
        finishCalibration();
    }
    
    private class LeapGestures {
        private final KeyboardSetting GESTURE_SWIPE_MIN_LENGTH;
        private final KeyboardSetting GESTURE_SWIPE_MIN_VELOCITY;
        private boolean detectingSwipeGesture = false;
        private KeyboardGesture gesture;
        
        public LeapGestures() {
            if(Gesture.ENABLED) {
                GESTURE_SWIPE_MIN_LENGTH = keyboardSettings.getSetting(Setting.GESTURE_SWIPE_MIN_LENGTH);
                GESTURE_SWIPE_MIN_VELOCITY = keyboardSettings.getSetting(Setting.GESTURE_SWIPE_MIN_VELOCITY);
            } else {
                GESTURE_SWIPE_MIN_LENGTH = null;
                GESTURE_SWIPE_MIN_VELOCITY = null;
            }
        }
        
        public void update() {
            if(Gesture.ENABLED) {
                // Remove completed gestures.
                keyboardGestures.removeFinishedGestures();
                
                // We have already detected a swipe that was long enough.
                if(detectingSwipeGesture && gesture != null && keyboardGestures.containsGesture(gesture)) {
                    if(leapTool.getVelocity().magnitude() < GESTURE_SWIPE_MIN_VELOCITY.getValue()) {
                        gesture.gestureFinshed();
                        gesture = null;
                    } // else continue detecting it
                }
                // We have already detected a swipe but it's not long enough yet.
                else if (detectingSwipeGesture && gesture != null){
                    // Update the gesture since it's not added to the renderables yet.
                    gesture.update(leapPoint.getNormalizedPoint());
                    // Check that we're still maintaining velocity.
                    if(leapTool.getVelocity().magnitude() >= GESTURE_SWIPE_MIN_VELOCITY.getValue()) {
                        // Check if we've met minimum length requirement.
                        if(gesture.getLength() >= GESTURE_SWIPE_MIN_LENGTH.getValue()) {
                            // Gesture meets all minimum requirements, so add to renderable gestures.
                            keyboardGestures.addGesture(gesture);
                        } // else continue detecting it
                    } else if(gesture.getLength() >= GESTURE_SWIPE_MIN_LENGTH.getValue()) {
                        // We were long enough at the time we lost minimum velocity.
                        keyboardGestures.addGesture(gesture);
                    } else {
                        // We lost velocity before reaching minimum length.
                        detectingSwipeGesture = false;
                    }
                }
                // We aren't currently detecting a swipe.
                else {
                    if(leapTool.getVelocity().magnitude() >= GESTURE_SWIPE_MIN_VELOCITY.getValue()) {
                        detectingSwipeGesture = true;
                        gesture = new KeyboardGesture(leapPoint.getNormalizedPoint(), Gesture.SWIPE);
                    }
                }
            }
        }
    }
}

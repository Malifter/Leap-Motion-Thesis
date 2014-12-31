package keyboard.leap;

import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;

import utilities.MyUtilities;

import com.leapmotion.leap.InteractionBox;

import enums.Attribute;
import enums.FileName;
import enums.Renderable;
import keyboard.IKeyboard;
import keyboard.renderables.LeapGestures;
import keyboard.renderables.LeapPlane;
import keyboard.renderables.LeapPoint;
import keyboard.renderables.LeapTool;
import keyboard.renderables.VirtualKey;
import keyboard.renderables.VirtualKeyboard;
import leap.LeapData;
import leap.LeapObserver;


public class LeapKeyboard extends IKeyboard implements LeapObserver {
    public static final int KEYBOARD_ID = 1;
    private static final String KEYBOARD_FILE_NAME = FileName.LEAP_NAME.getName();
    private static final ReentrantLock LEAP_LOCK = new ReentrantLock();
    private final int DIST_TO_CAMERA;
    private LeapData leapData;
    private LeapTool leapTool;
    private LeapPoint leapPoint;
    private LeapGestures leapGestures;
    private LeapPlane leapPlane;
    private VirtualKeyboard virtualKeyboard;
    
    public LeapKeyboard() {
        super(KEYBOARD_ID, KEYBOARD_FILE_NAME);
        keyboardAttributes = new LeapAttributes(this);
        keyboardSettings = new LeapSettings(this);
        keyboardRenderables = new LeapRenderables(this);
        keyboardWidth = keyboardAttributes.getAttributeByName(Attribute.KEYBOARD_WIDTH.toString());
        keyboardHeight = keyboardAttributes.getAttributeByName(Attribute.KEYBOARD_HEIGHT.toString());
        DIST_TO_CAMERA = keyboardAttributes.getAttributeByName(Attribute.DIST_TO_CAMERA.toString()).getValueAsInteger();
        virtualKeyboard = (VirtualKeyboard) keyboardRenderables.getRenderableByName(Renderable.VIRTUAL_KEYS.toString());
        leapPoint = (LeapPoint) keyboardRenderables.getRenderableByName(Renderable.LEAP_POINT.toString());
        leapTool = (LeapTool) keyboardRenderables.getRenderableByName(Renderable.LEAP_TOOL.toString());
        leapGestures = (LeapGestures) keyboardRenderables.getRenderableByName(Renderable.LEAP_GESTURES.toString());
        leapPlane = (LeapPlane) keyboardRenderables.getRenderableByName(Renderable.LEAP_PLANE.toString());
        if(!leapPlane.isCalibrated()) {
            leapPoint.blockAccess(true);
            leapTool.blockAccess(true);
            leapGestures.blockAccess(true);
        }
    }
    
    public void calibrateLeapPlane() {
        leapPlane.calibrate();
        leapPoint.grantAccess(true);
        leapTool.grantAccess(true);
        leapGestures.grantAccess(true);
    }
    
    @Override
    public void render(GL2 gl) {
        MyUtilities.OPEN_GL_UTILITIES.switchToPerspective(gl, this);
        gl.glPushMatrix();
        gl.glTranslatef(-keyboardWidth.getValueAsInteger()/2f, -keyboardHeight.getValueAsInteger()/2f, -DIST_TO_CAMERA); // 465 is the magic number that somehow centers the 2D with the 3D
        keyboardRenderables.render(gl);
        gl.glPopMatrix();
    }
    
    @Override
    public void update() {
        LEAP_LOCK.lock();
        try {
            if(leapTool.isValid()) {
                //keyPressed = 'l';
                //notifyListeners();
                // Allow leap plane to take over the updates of specific objects that require the plane
                leapPlane.update(leapPoint, leapTool);
                VirtualKey vKey;
                if((vKey = virtualKeyboard.isHoveringAny(leapPoint.getNormalizedPoint())) != null && leapPlane.isTouching()) {
                    vKey.pressed();
                }
            } else {
                virtualKeyboard.clearAll();
            }
        } finally {
            LEAP_LOCK.unlock();
        }
    }
    
    @Override
    public void leapEventObserved(LeapData leapData) {
        LEAP_LOCK.lock();
        try {
            this.leapData = leapData;
            this.leapData.populateData(leapPoint, leapTool);
        } finally {
            LEAP_LOCK.unlock();
        }
    }

    @Override
    public void leapInteractionBoxSet(InteractionBox iBox) {
        leapPoint.setInteractionBox(iBox);
        leapPlane.setInteractionBox(iBox);
        leapGestures.setInteractionBox(iBox);
        leapTool.createCylinder();
    }
}

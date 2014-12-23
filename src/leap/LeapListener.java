package leap;
/******************************************************************************\
* Copyright (C) 2012-2013 Leap Motion, Inc. All rights reserved.               *
* Leap Motion proprietary and confidential. Not for distribution.              *
* Use subject to the terms of the Leap Motion SDK Agreement available at       *
* https://developer.leapmotion.com/sdk_agreement, or another agreement         *
* between Leap Motion and you, your company or other organization.             *
\******************************************************************************/

import java.util.ArrayList;

import keyboard.IKeyboard;
import keyboard.KeyboardSetting;
import ui.SaveSettingsObserver;

import com.leapmotion.leap.*;

import enums.KeyboardType;

public class LeapListener extends Listener implements SaveSettingsObserver {
    private static Controller controller;
    private static LeapListener listener;
    private ArrayList<LeapObserver> observers = new ArrayList<LeapObserver>();
    private LeapPointData leapPointData = new LeapPointData();
    //private LeapPlaneData leapPlaneData = new LeapPlaneData();
    private LeapToolData leapToolData = new LeapToolData();
    private LeapGestureData leapGestureData = new LeapGestureData();
    private LeapData leapData = new LeapData(leapPointData,/* leapPlaneData,*/ leapToolData, leapGestureData);
    //private ReentrantLock leapLock = new ReentrantLock();
    private Tool testTool = new Tool();
    
    public LeapListener(Controller controller) {
        super();
        LeapListener.listener = this;
        LeapListener.controller = controller;
    }
    
    public void onInit(Controller controller) {
        System.out.println("Initialized");
    }

    public void onConnect(Controller controller) {
        System.out.println("Connected");
        controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        controller.enableGesture(Gesture.Type.TYPE_CIRCLE);
        controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP);
        controller.enableGesture(Gesture.Type.TYPE_KEY_TAP);
    }

    public void onDisconnect(Controller controller) {
        //Note: not dispatched when running in a debugger.
        System.out.println("Disconnected");
    }

    public void onExit(Controller controller) {
        System.out.println("Exited");
    }
    
    /*public Vector getTrackedPosition() {
        leapLock.lock();
        try {
            return trackedPosition;
        } finally {
            leapLock.unlock();
        }
    }
    
    public Vector getTrackedDirection() {
        leapLock.lock();
        try {
            return trackedDirection;
        } finally {
            leapLock.unlock();
        }
    }*/

    public void onFrame(Controller controller) {
        // Get the most recent frame and report some basic information
        Frame frame = controller.frame();
        System.out.println("Frame id: " + frame.id());
        
        /*System.out.println("Frame id: " + frame.id()
                         + ", timestamp: " + frame.timestamp()
                         + ", hands: " + frame.hands().count()
                         + ", fingers: " + frame.fingers().count()
                         + ", tools: " + frame.tools().count()
                         + ", gestures " + frame.gestures().count());

        //Get hands
        for(Hand hand : frame.hands()) {
            String handType = hand.isLeft() ? "Left hand" : "Right hand";
            System.out.println("  " + handType + ", id: " + hand.id()
                             + ", palm position: " + hand.palmPosition());

            // Get the hand's normal vector and direction
            Vector normal = hand.palmNormal();
            Vector direction = hand.direction();

            // Calculate the hand's pitch, roll, and yaw angles
            System.out.println("  pitch: " + Math.toDegrees(direction.pitch()) + " degrees, "
                             + "roll: " + Math.toDegrees(normal.roll()) + " degrees, "
                             + "yaw: " + Math.toDegrees(direction.yaw()) + " degrees");

            // Get arm bone
            Arm arm = hand.arm();
            System.out.println("  Arm direction: " + arm.direction()
                             + ", wrist position: " + arm.wristPosition()
                             + ", elbow position: " + arm.elbowPosition());

            // Get fingers
            for (Finger finger : hand.fingers()) {
                System.out.println("    " + finger.type() + ", id: " + finger.id()
                                 + ", length: " + finger.length()
                                 + "mm, width: " + finger.width() + "mm");

                //Get Bones
                for(Bone.Type boneType : Bone.Type.values()) {
                    Bone bone = finger.bone(boneType);
                    System.out.println("      " + bone.type()
                                     + " bone, start: " + bone.prevJoint()
                                     + ", end: " + bone.nextJoint()
                                     + ", direction: " + bone.direction());
                }
            }
        }*/

        // Get tools
        for(Tool tool : frame.tools()) {
            /*System.out.println("  Tool id: " + tool.id()
                             + ", position: " + tool.tipPosition()
                             + ", direction: " + tool.direction());*/
            /*leapLock.lock();
            try {
                trackedPosition = tool.tipPosition();
                trackedDirection = tool.direction();
            } finally {
                leapLock.unlock();
            }*/
            System.out.println("leap: " + "Tip Position: " + tool.tipPosition() + " Stabilized Tip Position: " + tool.stabilizedTipPosition());
            testTool = tool;
        }

       /* GestureList gestures = frame.gestures();
        for (int i = 0; i < gestures.count(); i++) {
            Gesture gesture = gestures.get(i);

            switch (gesture.type()) {
                case TYPE_CIRCLE:
                    CircleGesture circle = new CircleGesture(gesture);

                    // Calculate clock direction using the angle between circle normal and pointable
                    String clockwiseness;
                    if (circle.pointable().direction().angleTo(circle.normal()) <= Math.PI/2) {
                        // Clockwise if angle is less than 90 degrees
                        clockwiseness = "clockwise";
                    } else {
                        clockwiseness = "counterclockwise";
                    }

                    // Calculate angle swept since last frame
                    double sweptAngle = 0;
                    if (circle.state() != State.STATE_START) {
                        CircleGesture previousUpdate = new CircleGesture(controller.frame(1).gesture(circle.id()));
                        sweptAngle = (circle.progress() - previousUpdate.progress()) * 2 * Math.PI;
                    }

                    System.out.println("  Circle id: " + circle.id()
                               + ", " + circle.state()
                               + ", progress: " + circle.progress()
                               + ", radius: " + circle.radius()
                               + ", angle: " + Math.toDegrees(sweptAngle)
                               + ", " + clockwiseness);
                    break;
                case TYPE_SWIPE:
                    SwipeGesture swipe = new SwipeGesture(gesture);
                    System.out.println("  Swipe id: " + swipe.id()
                               + ", " + swipe.state()
                               + ", position: " + swipe.position()
                               + ", direction: " + swipe.direction()
                               + ", speed: " + swipe.speed());
                    break;
                case TYPE_SCREEN_TAP:
                    ScreenTapGesture screenTap = new ScreenTapGesture(gesture);
                    System.out.println("  Screen Tap id: " + screenTap.id()
                               + ", " + screenTap.state()
                               + ", position: " + screenTap.position()
                               + ", direction: " + screenTap.direction());
                    break;
                case TYPE_KEY_TAP:
                    KeyTapGesture keyTap = new KeyTapGesture(gesture);
                    System.out.println("  Key Tap id: " + keyTap.id()
                               + ", " + keyTap.state()
                               + ", position: " + keyTap.position()
                               + ", direction: " + keyTap.direction());
                    break;
                default:
                    System.out.println("Unknown gesture type.");
                    break;
            }
        }

        if (!frame.hands().isEmpty() || !gestures.isEmpty()) {
            System.out.println();
        }*/
        if(testTool.isValid()) {
            notifyListeners();
        }
    }
    
    public static void stopListening() {
        controller.removeListener(listener);
    }
    
    public static void startListening() {
        controller.addListener(listener);
    }
    
    public void registerObserver(LeapObserver observer) {
        if(observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }
    
    public void removeObserver(LeapObserver observer) {
        observers.remove(observer);
    }

    protected void notifyListeners() {
        for(LeapObserver observer : observers) {
            observer.leapEventObserved(testTool);
        }
    }

    @Override
    public void saveSettingsEventObserved(IKeyboard keyboard) {
        if(keyboard == KeyboardType.LEAP.getKeyboard()) {
            Config config = controller.config();
            for(KeyboardSetting ks: keyboard.getSettings().getAllSettings()) {
                config.setFloat(ks.getName(), (float) ks.getValue());
            }
            System.out.println("Saving Settings to Leap Config: " + (config.save() ? "success" : "failed"));
        }
    }
}

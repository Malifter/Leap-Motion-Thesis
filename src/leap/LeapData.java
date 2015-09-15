package leap;

import keyboard.renderables.SwipePoint;
import keyboard.renderables.LeapTool;

import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Tool;
import com.leapmotion.leap.Vector;

public class LeapData {
    private Tool toolData;
    private Hand handData;
    public int frameCount = 0;
    
    public LeapData() {
        // On creation, be safe and create non-null invalid data
        toolData = new Tool();
    }

    public void setToolData(Tool toolData) {
        this.toolData = toolData;
    }
    
    public void setHandData(Hand handData) {
        this.handData = handData;
    }
    
    public Tool getToolData() {
        return toolData;
    }
    
    public Hand getHandData() {
        return handData;
    }
    
    public void populateToolData(SwipePoint leapPoint, LeapTool leapTool) {
        populatePointData(leapPoint);
        populateToolData(leapTool);
    }
    
    public void populateHandData(SwipePoint leapPoint) {
        if(leapPoint != null) {
            if(handData.isValid()) {
                leapPoint.setPoint(handData.stabilizedPalmPosition());
            } else {
                leapPoint.setPoint(Vector.zero());
            }
        }
    }
    
    private void populateToolData(LeapTool leapTool) {
        if(leapTool != null) {
            if(!toolData.isValid() && handData.isValid()) {
                leapTool.setTool(handData.fingers().get(1));
            } else {
                leapTool.setTool(toolData);
            }
        }
    }
    
    private void populatePointData(SwipePoint leapPoint) {
        if(leapPoint != null) {
            if(toolData.isValid()) {
                leapPoint.setPoint(toolData.stabilizedTipPosition());
                leapPoint.setTouchData(toolData);
            } else if(handData.isValid()) {
                // NOTE: Using the stabilizedTipPosition here causes some odd lag along the z-axis.
                leapPoint.setPoint(handData.fingers().get(1).tipPosition());
                leapPoint.setTouchData(handData.fingers().get(1));
            } else {
                leapPoint.setPoint(Vector.zero());
                leapPoint.setTouchData(null);
            }
        }
    }
}

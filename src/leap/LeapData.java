package leap;

import keyboard.renderables.SwipePoint;
import keyboard.renderables.LeapTool;

import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Tool;


public class LeapData {
    private Tool toolData;
    private Hand handData;
    
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
            leapPoint.setPoint(handData.palmPosition());
        }
    }
    
    private void populateToolData(LeapTool leapTool) {
        if(leapTool != null) {
            leapTool.setTool(toolData);
        }
    }
    
    private void populatePointData(SwipePoint leapPoint) {
        if(leapPoint != null) {
            leapPoint.setPoint(toolData.stabilizedTipPosition());
        }
    }
}

package keyboard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import enums.DecimalPrecision;

public class KeyboardSetting {
    private String name;
    private double value;
    private double min;
    private double max;
    private JPanel settingsPanel;
    private DecimalPrecision decimalPrecision;
    private boolean spinnerEvent = false; // Not a good programming practice. For some reason the lock I added didn't solve the problem
    private boolean sliderEvent = false;  // but this pair of booleans did. Removing the redundant event would require additional classes.
    
    public KeyboardSetting(String name, Number value, Number min, Number max, DecimalPrecision decimalPrecision) {
        this.name = name;
        this.value = value.doubleValue();
        this.min = min.doubleValue();
        this.max = max.doubleValue();
        this.decimalPrecision = decimalPrecision;
        createSettingsPanel();
    }

    public String getName() {
        return name;
    }
    
    public int getValueAsInteger() {
        return (int) value;
    }
    
    public double getValue() {
        return value;
    }
    
    public JPanel getSettingsPanel() {
        return settingsPanel;
    }
    
    private void createSettingsPanel() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel modifiersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel nameLabel = new JLabel(name+":");
        JSlider settingsSlider = new JSlider(JSlider.HORIZONTAL, (int)(min/decimalPrecision.getPrecision()), (int)(max/decimalPrecision.getPrecision()), (int)(value/decimalPrecision.getPrecision()));
        SpinnerNumberModel settingsSpinnerNumberModel = new SpinnerNumberModel(value, min, max, decimalPrecision.getPrecision());
        JSpinner settingsSpinner = new JSpinner(settingsSpinnerNumberModel);
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor)settingsSpinner.getEditor();  
        DecimalFormat format = editor.getFormat();  
        format.setMinimumFractionDigits(decimalPrecision.getPlaces());  
        editor.getTextField().setHorizontalAlignment(SwingConstants.CENTER);  
        Dimension d = settingsSpinner.getPreferredSize();  
        d.width = 85;  
        settingsSpinner.setPreferredSize(d); 
        
        settingsSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sliderEvent = true;
                if(!spinnerEvent) {
                    JSlider slider = (JSlider) e.getSource();
                    value = slider.getValue()*decimalPrecision.getPrecision();
                    settingsSpinner.setValue(value);
                }
                sliderEvent = false;
            }
        });
        
        settingsSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                spinnerEvent = true;
                if(!sliderEvent) {
                    JSpinner spinner = (JSpinner) e.getSource();
                    value = (double) spinner.getValue();
                    settingsSlider.setValue((int)(value/decimalPrecision.getPrecision()));
                }
                spinnerEvent = false;
            }
        });
        
        namePanel.add(nameLabel);
        modifiersPanel.add(settingsSpinner);
        modifiersPanel.add(settingsSlider);
        settingsPanel.add(namePanel);
        settingsPanel.add(modifiersPanel);
        settingsPanel.setMaximumSize(settingsPanel.getPreferredSize());
        settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}

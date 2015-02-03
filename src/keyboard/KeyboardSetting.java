package keyboard;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;

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
import enums.Setting;

public class KeyboardSetting {
    private final ReentrantLock VALUE_LOCK = new ReentrantLock();
    private final Setting SETTING;
    private double value;
    private JPanel settingsPanel;
    private DecimalPrecision decimalPrecision;
    private JSpinner settingsSpinner;
    private JSlider settingsSlider;
    //private boolean spinnerEvent = false; // Not a good programming practice. For some reason the lock I added didn't solve the problem
    //private boolean sliderEvent = false;  // but this pair of booleans did. Removing the redundant event would require additional classes.
    
    public KeyboardSetting(IKeyboard keyboard, Setting setting) {
        SETTING = setting;
        /*try {
            this.value = MyUtilities.FILE_IO_UTILITIES.readSettingFromFile(FilePath.CONFIG.getPath(),
                    keyboard.getFileName() + FileExt.INI.getExt(), setting.name(), setting.getDef());
        } catch(IOException e) {
            System.err.println("Error occured while trying to read "  + setting + " from file. Using default value.");
            this.value = setting.getDefault();
        }*/
        value = setting.getDefault();
        
        this.decimalPrecision = setting.getDecimalPrecision();
        createSettingsPanel();
    }

    public Setting getType() {
        return SETTING;
    }
    
    public int getValueAsInteger() {
        return (int) value;
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = value;
        settingsSpinner.setValue(value);
        settingsSlider.setValue((int) (value / decimalPrecision.getPrecision()));
    }
    
    public JPanel getSettingsPanel() {
        return settingsPanel;
    }
    
    private void createSettingsPanel() {
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel modifiersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel nameLabel = new JLabel(SETTING.name() + ":");
        settingsSlider = new JSlider(JSlider.HORIZONTAL,
                (int)(SETTING.getMin()/decimalPrecision.getPrecision()),
                (int)(SETTING.getMax()/decimalPrecision.getPrecision()),
                (int)(value/decimalPrecision.getPrecision()));
        SpinnerNumberModel settingsSpinnerNumberModel = new SpinnerNumberModel(value, SETTING.getMin(), SETTING.getMax(), decimalPrecision.getPrecision());
        settingsSpinner = new JSpinner(settingsSpinnerNumberModel);
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
                if(!VALUE_LOCK.isLocked()) {
                    VALUE_LOCK.lock();
                    try {
                        value = settingsSlider.getValue() * decimalPrecision.getPrecision();
                        settingsSpinner.setValue(value);
                    } finally {
                        VALUE_LOCK.unlock();
                    }
                }
            }
        });
        
        settingsSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(!VALUE_LOCK.isLocked()) {
                    VALUE_LOCK.lock();
                    try {
                        double precision = decimalPrecision.getPrecision();
                        value = Math.round((double) settingsSpinner.getValue() / precision) * precision;
                        settingsSlider.setValue((int) (value / precision));
                    } finally {
                        VALUE_LOCK.unlock();
                    }
                }
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

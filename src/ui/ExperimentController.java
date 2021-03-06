/*
 * Copyright (c) 2015, Garrett Benoit. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package ui;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;
import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL2;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.text.html.HTMLDocument;

import utilities.MyUtilities;
import keyboard.KeyboardAttributes;
import keyboard.KeyboardObserver;
import keyboard.KeyboardSetting;
import enums.Attribute;
import enums.FileName;
import enums.Keyboard;
import enums.KeyboardType;
import experiment.TutorialManager;
import experiment.WordManager;
import experiment.data.DataManager;
import experiment.playback.PlaybackManager;

public class ExperimentController extends GraphicsController implements KeyboardObserver {
    private static final String DEFAULT_INFO = "<font><b>CALIBRATE:</b><br>Calibrate the keyboard (if available).<br><br></font>"
            + "<font><b>TUTORIAL:</b><br>A brief example to familiarize yourself with the keyboard.<br><br></font>"
            + "<font><b>PRACTICE:</b><br>A small sample of what you should expect from the experiment.<br><br></font>"
            + "<font><b>EXPERIMENT:</b><br>The actual experiment with recorded data.</font>";
    public static final int EXPERIMENT_SIZE = 15;
    private final int PRACTICE_SIZE = EXPERIMENT_SIZE;
    private final String TUTORIAL = FileName.TUTORIAL.getName();
    private final ReentrantLock EXPERIMENT_LOCK = new ReentrantLock();
    private final int ONE_SECOND = 1000;
    private final int COUNTDOWN_TIME = 5;
    private final Color LIGHT_GREEN = new Color(204, 255, 204);
    private final Color LIGHT_RED = new Color(255, 204, 204);
    private final int FADE_DURATION = 500;
    private int practiceWordCount = 0;
    private Color currentColor = Color.WHITE;
    private long previousFadeTime;
    private long fadeTimeElapsed = 0;
    private boolean isFading = false;
    private JSplitPane splitPane;
    private Component rightComponent;
    private JPanel canvasPanel;
    private JButton calibrateButton;
    private JButton tutorialButton;
    private JButton practiceButton;
    private JButton experimentButton;
    private JEditorPane infoPane;
    private JPanel infoPanel;
    private JPanel settingsPanel;
    private JPanel wordPanel;
    private JPanel answerPanel;
    private JLabel wordLabel;
    private JLabel answerLabel;
    private boolean runningCalibration = false;
    private boolean runningTutorial = false;
    private boolean runningPractice = false;
    private boolean runningExperiment = false;
    private boolean experimentCompletedSuccessfully = false;
    private boolean ranPractice = false;
    private String subjectID;
    private WordManager wordManager = new WordManager();
    private PlaybackManager playbackManager;
    private DataManager dataManager;
    private TutorialManager tutorialManager;
    private Timer delayedStart;
    
    public ExperimentController() {
        canvasPanel = new JPanel();
        canvas = new GLCanvas(CAPABILITIES);
        canvasPanel.add(canvas);
        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        infoPane = new JEditorPane("text/html", "");
        ((HTMLDocument)infoPane.getDocument()).getStyleSheet().addRule("body { font-size: 15pt; }");
        infoPane.setText(DEFAULT_INFO);
        infoPanel = new JPanel();
        settingsPanel = new JPanel();
        wordPanel = new JPanel();
        answerPanel = new JPanel();
        wordLabel = new JLabel();
        answerLabel = new JLabel();
        calibrateButton = new JButton("Calibrate");
        tutorialButton = new JButton("Tutorial");
        practiceButton = new JButton("Practice");
        experimentButton = new JButton("Run Experiment");
        
        JButton buttons[] = {calibrateButton, tutorialButton, practiceButton, experimentButton};
        JLabel labels[] = {wordLabel, answerLabel};
        JPanel panels[] = {wordPanel, answerPanel, settingsPanel, infoPanel};

        // Window builder builds window using important fields here. It adds unimportant fields that we use for aesthetics only.
        WindowBuilder.buildExperimentWindow(frame, canvasPanel, infoPane, panels, labels, buttons, splitPane);
        rightComponent = splitPane.getRightComponent();
        infoPane.setFocusable(false);
        canvas.setFocusable(false);
        
        calibrateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(isLeapKeyboard()) {
                    if(keyboard.isCalibrated()) {
                        Object[] options = {"Recalibrate", "Cancel"};
                        int selection =
                                JOptionPane.showOptionDialog(frame,
                                "The Leap Motion Interaction Plane has already been calibrated.\nDo you want to recalibrate it?",
                                "Warning!",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null,
                                options,
                                options[0]);
                        if(selection == JOptionPane.YES_OPTION) {
                            beginCalibration();
                            keyboard.beginCalibration(answerPanel);
                        }
                    } else {
                        beginCalibration();
                        keyboard.beginCalibration(answerPanel);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame,
                    "This keyboard cannot be calibrated.",
                    "Error!",
                    JOptionPane.ERROR_MESSAGE);
                }
                frame.requestFocusInWindow();
            }
        });
        
        tutorialButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EXPERIMENT_LOCK.lock();
                try {
                    beginTutorial();
                    keyboard.beginPlayback(playbackManager);
                } finally {
                    EXPERIMENT_LOCK.unlock();
                }
                frame.requestFocusInWindow();
            }
        });
        
        practiceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EXPERIMENT_LOCK.lock();
                try {
                    if(runningPractice) {
                        finishPractice();
                    } else {
                        runningPractice = true;
                        wordManager.loadPracticeWords(PRACTICE_SIZE);
                        disableUI();
                        delayedStart();
                    }
                } finally {
                    EXPERIMENT_LOCK.unlock();
                }
                frame.requestFocusInWindow();
            }
        });
        
        experimentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EXPERIMENT_LOCK.lock();
                try {
                    runningExperiment = true;
                    if(TUTORIAL.equals(subjectID)) {
                        wordManager.loadTutorialWords();
                    } else {
                        wordManager.loadExperimentWords(keyboard.getFileName());
                    }
                    disableUI();
                    delayedStart();
                } finally {
                    EXPERIMENT_LOCK.unlock();
                }
                frame.requestFocusInWindow();
            }
        });
        
        // by default, an AWT Frame doesn't do anything when you click
        // the close button; this bit of code will terminate the program when
        // the window is asked to close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                EXPERIMENT_LOCK.lock();
                try {
                    if(!runningCalibration && !runningTutorial && !runningPractice && !runningExperiment) {
                        disable();
                    }
                } finally {
                    EXPERIMENT_LOCK.unlock();
                }
            }
        });
        
        frame.pack();
    }
    
    @Override
    public void keyboardCalibrationFinishedEventObserved() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                finishCalibration();
            }
        });
    }
    
    private void beginCalibration() {
        runningCalibration = true;
        disableUI();
        wordLabel.setVisible(false);
        answerLabel.setVisible(false);
        wordPanel.removeAll();
        infoPane.setText("Calibration in progress...\n\n"
                + "Please follow the Calibration instructions.");
        settingsPanel.removeAll();
        if(isLeapKeyboard()) {
            KeyboardAttributes ka = keyboard.getAttributes();
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_A).getAttributePanel());
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_B).getAttributePanel());
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_C).getAttributePanel());
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_D).getAttributePanel());
        }
    }
    
    private void finishCalibration() {
        runningCalibration = false;
        wordManager.setDefault();
        enableUI();
        wordPanel.add(wordLabel);
        wordLabel.setVisible(true);
        answerPanel.add(answerLabel);
        answerLabel.setVisible(true);
        settingsPanel.removeAll();
        KeyboardAttributes ka = keyboard.getAttributes();
        settingsPanel.add(ka.getAttribute(Attribute.KEYBOARD_SIZE).getAttributePanel());
        if(isLeapKeyboard()) {
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_A).getAttributePanel());
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_B).getAttributePanel());
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_C).getAttributePanel());
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_D).getAttributePanel());
        }
        
        for(KeyboardSetting ks: keyboard.getSettings().getAllSettings()) {
            settingsPanel.add(ks.getSettingsPanel());
        }
        frame.pack();
    }
    
    private void beginTutorial() {
        runningTutorial = true;
        playbackManager = new PlaybackManager(true, TUTORIAL, keyboard);
        tutorialManager = new TutorialManager();
        infoPanel.add(tutorialManager.getComponent());
        infoPane.setText(tutorialManager.getText());
        wordManager.loadTutorialWords();
        disableUI();
        wordManager.paintLetters(wordLabel, answerLabel);
        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.currentWord(), wordLabel, wordPanel);
        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.getAnswer(), answerLabel, answerPanel);
    }
    
    private void finishTutorial() {
        runningTutorial = false;
        infoPanel.remove(tutorialManager.getComponent());
        tutorialManager = null;
        playbackManager = null;
        answerLabel.setText("");
        wordManager.setAnswer("");
        wordManager.setDefault();
        enableUI();
    }
    
    private void beginPractice() {
        delayedStart = null;
        //enableUI();
        //splitPane.setRightComponent(null);
        //disableUI();
        practiceButton.setEnabled(true);
        practiceButton.setText("Finish Practice");
        wordManager.paintLetters(wordLabel, answerLabel);
        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.currentWord(), wordLabel, wordPanel);
        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.getAnswer(), answerLabel, answerPanel);
    }
    
    private void finishPractice() {
        runningPractice = false;
        splitPane.setRightComponent(rightComponent);
        ranPractice = true;
        wordManager.setDefault();
        enableUI();
        practiceButton.setText("Practice");
    }
    
    private void beginExperiment() {
        delayedStart = null;
        String timeStarted = "_";
        timeStarted += LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        timeStarted += "_" + LocalTime.now().format(DateTimeFormatter.ofPattern("kkmm"));
        dataManager = new DataManager(keyboard, subjectID, timeStarted, practiceWordCount);
        enableUI();
        splitPane.setRightComponent(null);
        disableUI();
        dataManager.startRecording();
        wordManager.paintLetters(wordLabel, answerLabel);
        dataManager.startWord(wordManager.currentWord());
        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.currentWord(), wordLabel, wordPanel);
        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.getAnswer(), answerLabel, answerPanel);
    }
    
    private void finishExperiment() {
        runningExperiment = false;
        experimentCompletedSuccessfully = true;
        splitPane.setRightComponent(rightComponent);
        dataManager.stopRecording();
        dataManager.save(keyboard);
        dataManager = null;
        //wordManager.setDefault();
        /*enableUI();
        currentColor = Color.WHITE;
        wordPanel.setBackground(currentColor);
        answerPanel.setBackground(currentColor);
        isFading = false;
        fadeTimeElapsed = 0;*/
        disable();
    }
    
    public boolean experimentCompletedSuccessfully() {
        try {
            return experimentCompletedSuccessfully;
        } finally {
            // consume boolean
            experimentCompletedSuccessfully = false;
        }
    }
    
    public void disableUI() {
        wordLabel.setText("");
        answerLabel.setText("");
        wordManager.setAnswer("");
        calibrateButton.setEnabled(false);
        tutorialButton.setEnabled(false);
        practiceButton.setEnabled(false);
        experimentButton.setEnabled(false);
        settingsPanel.setEnabled(false);
        if(isEnabled) {
            frame.pack();
        }
    }
    
    public void enableUI() {
        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.currentWord(), wordLabel, wordPanel);
        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.getAnswer(), answerLabel, answerPanel);
        wordManager.paintLetters(wordLabel, answerLabel);
        currentColor = Color.WHITE;
        wordPanel.setBackground(currentColor);
        answerPanel.setBackground(currentColor);
        calibrateButton.setEnabled(true);
        tutorialButton.setEnabled(true);
        practiceButton.setEnabled(true);
        if(ranPractice) {
            experimentButton.setEnabled(true);
        }
        ((HTMLDocument)infoPane.getDocument()).getStyleSheet().addRule("body { font-size: 15pt; }");
        infoPane.setText(DEFAULT_INFO);
        settingsPanel.setEnabled(true);
        frame.pack();
    }
    
    @Override
    protected void disable() {
        removeKeyboardFromUI();
        frame.setVisible(false);
        canvas.removeGLEventListener(this);
        keyboard.removeObserver(this);
        ranPractice = false;
        isEnabled = false;
        disableUI();
        practiceWordCount = 0;
        frame.dispose();
    }
    
    @Override
    public void enable() {
        addKeyboardToUI();
        frame.setVisible(true);
        frame.requestFocusInWindow();
        canvas.addGLEventListener(this);
        keyboard.registerObserver(this);
        wordManager.setDefault();
        enableUI();
        // Moved here from finish Exp
        currentColor = Color.WHITE;
        wordPanel.setBackground(currentColor);
        answerPanel.setBackground(currentColor);
        isFading = false;
        fadeTimeElapsed = 0;
        
        isEnabled = true;
    }
    
    public void enable(String subjectID, KeyboardType keyboardType) {
        if(frame != null) {
            frame.setTitle("Experiment - Subject ID: " + subjectID + " Test: " + keyboardType.getName());
        }
        this.subjectID = subjectID;
        keyboard = Keyboard.getByType(keyboardType).getKeyboard();
        if(TUTORIAL.equals(subjectID)) {
            ranPractice = true;
        }
        enable();
    }
    
    private void removeKeyboardFromUI() {
        wordLabel.setText("");
        answerLabel.setText("");
        wordManager.setAnswer("");
        settingsPanel.removeAll();
        keyboard.removeFromUI(canvasPanel, canvas);
        //keyboard = null;
    }
    
    private void addKeyboardToUI() {
        keyboard.addToUI(canvasPanel, canvas);
        
        KeyboardAttributes ka = keyboard.getAttributes();
        settingsPanel.add(ka.getAttribute(Attribute.KEYBOARD_SIZE).getAttributePanel());
        
        if(isLeapKeyboard()) {
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_A).getAttributePanel());
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_B).getAttributePanel());
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_C).getAttributePanel());
            settingsPanel.add(ka.getAttribute(Attribute.LEAP_PLANE_POINT_D).getAttributePanel());
        }
        
        for(KeyboardSetting ks: keyboard.getSettings().getAllSettings()) {
            settingsPanel.add(ks.getSettingsPanel());
        }
        
        canvas.setPreferredSize(new Dimension(keyboard.getImageWidth(), keyboard.getImageHeight()));
        canvas.setSize(keyboard.getImageWidth(), keyboard.getImageHeight());
        //canvasPanel.setPreferredSize(canvas.getPreferredSize());
        //canvasPanel.setSize(canvas.getPreferredSize());
        wordPanel.setPreferredSize(new Dimension(keyboard.getImageWidth(), wordPanel.getHeight()));
        answerPanel.setPreferredSize(new Dimension(keyboard.getImageWidth(), answerPanel.getHeight()));
        wordPanel.setSize(keyboard.getImageWidth(), wordPanel.getHeight());
        answerPanel.setSize(keyboard.getImageWidth(), answerPanel.getHeight());
        frame.revalidate();
        frame.repaint();
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = frame.getSize();
        frame.setLocation((int)(screenSize.getWidth()/2 - windowSize.getWidth()/2),
                          (int)(screenSize.getHeight()/2 - windowSize.getHeight()/2));
    }
    
    @Override
    public void update() {        
        EXPERIMENT_LOCK.lock();
        keyboard.update();
        
        try {
            if(runningTutorial) {
                if(tutorialManager.isValid() && wordManager.isDefault()) {
                    wordManager.loadTutorialWords();
                    wordManager.paintLetters(wordLabel, answerLabel);
                    MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.currentWord(), wordLabel, wordPanel);
                    MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.getAnswer(), answerLabel, answerPanel);
                }
                if(tutorialManager.hasNext() && tutorialManager.isValid()) {
                    infoPane.setText(tutorialManager.getText());
                } else if(!tutorialManager.isValid()) {
                    keyboard.finishPlayback(playbackManager);
                    finishTutorial();
                }
            } else if(runningPractice && !wordManager.isValid() && delayedStart == null) {
                finishPractice();
            } else if(runningExperiment && !wordManager.isValid() && delayedStart == null) {
                keyboard.finishExperiment(dataManager);
                finishExperiment();
            }
        } finally {
            EXPERIMENT_LOCK.unlock();
        }
        
        // Manage the color flash here when an answer is correct/incorrect.
        if(isFading) {
            long now = System.currentTimeMillis();
            fadeTimeElapsed += now - previousFadeTime;
            previousFadeTime = now;

            if(fadeTimeElapsed <= FADE_DURATION) {
                float fraction = fadeTimeElapsed/(float)FADE_DURATION;
                Color color = wordPanel.getBackground();
                int red = (int)(fraction * currentColor.getRed() + 
                        (1 - fraction) * color.getRed());
                int green = (int)(fraction * currentColor.getGreen() + 
                        (1 - fraction) * color.getGreen());
                int blue = (int)(fraction * currentColor.getBlue() + 
                        (1 - fraction) * color.getBlue());
                color = new Color(red, green, blue);
                wordPanel.setBackground(color);
                answerPanel.setBackground(color);
            } else {
                isFading = false;
                fadeTimeElapsed = 0;
            }
        }
    }
    
    @Override
    protected void render(GL2 gl) {
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        keyboard.render(gl);
    }

    @Override
    public void keyboardKeyEventObserved(char key) {
        if(delayedStart == null) {
            if(runningExperiment) {
                dataManager.keyPressedEvent(key, wordManager.currentLetter());
            }
            if(key == '\b') {
                if(0 < wordManager.getAnswer().length() && wordManager.currentIndex() < wordManager.getAnswer().length()) {
                    wordManager.setAnswer(wordManager.getAnswer().substring(0, wordManager.getAnswer().length()-1));
                    MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.getAnswer(), answerLabel, answerPanel);
                }
                if(wordManager.isMatch(true)) {
                    currentColor = LIGHT_GREEN;
                    wordPanel.setBackground(currentColor);
                    answerPanel.setBackground(currentColor);
                } else {
                    if(!wordPanel.getBackground().equals(Color.WHITE)) {
                        currentColor = Color.WHITE;
                        wordPanel.setBackground(currentColor);
                        answerPanel.setBackground(currentColor);
                    }
                }
            } else if(key == '\n') {
                currentColor = Color.WHITE;
                previousFadeTime = System.currentTimeMillis();
                fadeTimeElapsed = 0;
                isFading = true;
                if(wordManager.isMatch(false)) {
                    wordPanel.setBackground(Color.GREEN);
                    answerPanel.setBackground(Color.GREEN);
                    if(runningExperiment) {
                        dataManager.stopWord();
                    }
                    wordManager.nextWord();
                    wordManager.setAnswer("");
                    wordLabel.setText(wordManager.currentWord());
                    if(runningPractice) {
                        practiceWordCount++;
                    }
                    if(runningExperiment && wordManager.isValid()) {
                        dataManager.startWord(wordManager.currentWord());
                    }
                    if(wordManager.isValid()) {
                        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.currentWord(), wordLabel, wordPanel);
                        MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.getAnswer(), answerLabel, answerPanel);
                    }
                } else {
                    wordPanel.setBackground(LIGHT_RED);
                    answerPanel.setBackground(LIGHT_RED);
                }
            } else {
                wordManager.setAnswer(wordManager.getAnswer()+Character.toString(key));
                MyUtilities.SWING_UTILITIES.calculateFontSize(wordManager.getAnswer(), answerLabel, answerPanel);
                if(wordManager.isMatch(true)) {
                    currentColor = LIGHT_GREEN;
                    wordPanel.setBackground(currentColor);
                    answerPanel.setBackground(currentColor);
                } else {
                    if(!wordPanel.getBackground().equals(Color.WHITE)) {
                        currentColor = Color.WHITE;
                        wordPanel.setBackground(currentColor);
                        answerPanel.setBackground(currentColor);
                    }
                }
            }
            wordManager.paintLetters(wordLabel, answerLabel);
        }
    }
    
    private boolean isLeapKeyboard() {
        return keyboard.getType().isLeap();
    }
    
    private void delayedStart() {
        delayedStart = new Timer(ONE_SECOND, new CountDownListener());
        ((HTMLDocument)infoPane.getDocument()).getStyleSheet().addRule("body { font-size: 200pt; }");
        infoPane.setText("<center><b>" + COUNTDOWN_TIME + "</b></center>");
        delayedStart.start();
    }
    
    private class CountDownListener implements ActionListener {
        private int countDown = COUNTDOWN_TIME;
        public void actionPerformed(ActionEvent e){
            countDown--;
            infoPane.setText("<center><b>" + countDown + "</b></center>");
            if(countDown == 0) {
                EXPERIMENT_LOCK.lock();
                try {
                    delayedStart.stop();
                    if(runningPractice) {
                        beginPractice();
                    } else if(runningExperiment) {
                        beginExperiment();
                        keyboard.beginExperiment(dataManager);
                    } else {
                        System.err.println("Something went wrong with the counter.");
                    }
                } finally {
                    EXPERIMENT_LOCK.unlock();
                }
            }
        }
    }
}

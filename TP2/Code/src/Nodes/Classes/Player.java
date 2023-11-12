package Nodes.Classes;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

public class Player {
    JFrame player = new JFrame("Player");
    JButton setupButton = new JButton("Setup");
    JButton playButton = new JButton("Play");
    JButton pauseButton = new JButton("Pause");
    JButton tearButton = new JButton("Teardown");
    JPanel mainPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JLabel iconLabel = new JLabel();
    private Clip clip;

    public Player() {
        player.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.setLayout(new GridLayout(1,0));
        // buttonPanel.add(setupButton);
        buttonPanel.add(playButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(tearButton);

        playButton.addActionListener(new playButtonListener());
        tearButton.addActionListener(new tearButtonListener());
        pauseButton.addActionListener(new pauseButtonListener());

        /*
        setupButton.addActionListener(new setupButtonListener());



         */
        iconLabel.setIcon(null);
        mainPanel.setLayout(null);
        mainPanel.add(iconLabel);
        mainPanel.add(buttonPanel);
        iconLabel.setBounds(0,0,680,400);
        buttonPanel.setBounds(0,280,600,50);

        player.getContentPane().add(mainPanel, BorderLayout.CENTER);
        player.setSize(new Dimension(600,400));
        player.setVisible(true);
    }
    class playButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            System.out.println("Play Button pressed !");
        }
    }

    class tearButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            System.out.println("Teardown Button pressed !");
            System.exit(0);
        }
    }
    class pauseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("Pause Button pressed !");
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
        }
    }
    public void updateFrame(byte[] frame) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.createImage(frame, 0, frame.length);
        ImageIcon icon = new ImageIcon(image);
        iconLabel.setIcon(icon);
    }

    public void updateAudio(byte[] audio) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        InputStream inputStream = new ByteArrayInputStream(audio);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        clip = AudioSystem.getClip();
        clip.open(audioInputStream);
        clip.start();
    }
}
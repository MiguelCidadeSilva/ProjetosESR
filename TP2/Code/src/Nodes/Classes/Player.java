package Nodes.Classes;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Player {
    JFrame player = new JFrame("Player");
    JButton setupButton = new JButton("Setup");
    JButton pauseButton = new JButton("Stop Stream");
    JPanel mainPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JLabel iconLabel = new JLabel();
    private Client client;
    private Clip clip;

    public Player(Client c) {
	this.client = c;
        player.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.setLayout(new GridLayout(1,0));
        // buttonPanel.add(setupButton);

        buttonPanel.add(pauseButton);

        pauseButton.addActionListener(new pauseButtonListener());
        player.addWindowListener(new windowListener());

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
    class windowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            client.endStreaming();
        }
    }
    class playButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            System.out.println("Botão de Play pressionado!");
        }
    }

    class tearButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){

            System.out.println("Botão Teardown pressionado!");
            System.exit(0);
        }
    }
    class pauseButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            System.out.println("Butão de pausa pressionado!");
            if (clip != null && clip.isRunning()) {
                clip.stop();
            }
            client.endStreaming();
        }
    }
    public void updateFrame(byte[] frame) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.createImage(frame, 0, frame.length);
        ImageIcon icon = new ImageIcon(image);
        iconLabel.setIcon(icon);
    }

    public void updateAudio(byte[] audio) {
        try {
            InputStream inputStream = new ByteArrayInputStream(audio);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }
}

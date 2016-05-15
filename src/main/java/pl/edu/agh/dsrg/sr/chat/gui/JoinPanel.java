package pl.edu.agh.dsrg.sr.chat.gui;

import pl.edu.agh.dsrg.sr.chat.service.ChatService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by P on 12.05.2016.
 */
public class JoinPanel extends JPanel implements ActionListener{
    private JComboBox comboBox;
    private JButton joinButton;
    private JLabel comboBoxLabel;
    private JPanel mainPanel;
    private ChatService chatService;
    private DefaultComboBoxModel<String> channelsModel = new DefaultComboBoxModel<>();

    public JoinPanel(ChatService chatService) {
        this.chatService = chatService;
        createGUI();
    }

    private void createGUI() {
        comboBox.setEditable(true);
        comboBox.setSelectedItem("");
        comboBox.setModel(channelsModel);

        comboBox.addActionListener(this);
        joinButton.addActionListener(this);
        add(mainPanel);
    }

    public void addChannel(String channelName) {
        channelsModel.addElement(channelName);
    }

    public void removeChannel(String channelName) {
        channelsModel.removeElement(channelName);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (!"Join".equals(actionCommand)) {
            return;
        }

        final String channelName = (String) comboBox.getSelectedItem();

        if (channelName == null || channelName.isEmpty()) {
            return;
        }

        new Thread(() -> {
            try {
                chatService.joinChannel(channelName);
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(JoinPanel.this,
                        String.format("%s: %s", e1.getClass().getName(), e1.getMessage()),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            comboBox.setSelectedItem("");
        }).start();
    }
}

package pl.edu.agh.dsrg.sr.chat.gui;

import pl.edu.agh.dsrg.sr.chat.service.ChatService;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

/**
 * Created by P on 12.05.2016.
 */
public class ChannelPanel extends JPanel implements ActionListener {
    private JButton leaveButton;
    private JPanel chatPanel;
    private JPanel sendPanel;
    private JTextField messageTextField;
    private JButton sendButton;
    private JTextArea transcriptTextArea;
    private JList list;
    private JScrollPane scrollTranscript;
    private JScrollPane scrollUsers;
    public JPanel mainPanel;

    private final String channelName;
    private final ChatService chatService;
    private DefaultListModel<String> usersModel = new DefaultListModel<>();

    public ChannelPanel(ChatService chatService, String channelName) {
        this.chatService = chatService;
        this.channelName = channelName;

        createGUI();
    }

    private void createGUI() {
        add(mainPanel);

        JList<String> users = new JList<>(usersModel);

        DefaultCaret caret = (DefaultCaret) transcriptTextArea.getCaret();
        caret.setUpdatePolicy(Rectangle2D.OUT_BOTTOM);

        scrollTranscript.setViewportView(transcriptTextArea);
        scrollUsers.setViewportView(users);

        sendButton.addActionListener(this);
        messageTextField.addActionListener(this);

        leaveButton.addActionListener(this);
    }

    public void clear() {
        transcriptTextArea.setText("");
    }

    public void append(String line) {
        transcriptTextArea.append(line);
    }

    public void addUser(String nick) {
        usersModel.addElement(nick);
    }

    public void removeUser(String nick) {
        usersModel.removeElement(nick);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == sendButton) {
            String message = messageTextField.getText();

            if (message == null || message.isEmpty()) {
                return;
            }

            try {
                chatService.sendMessage(channelName, message);
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(ChannelPanel.this,
                        String.format("%s: %s", e1.getClass().getName(),
                                e1.getMessage()),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
            messageTextField.setText("");
        }

        if (source == leaveButton) {
            try {
                chatService.leaveChannel(channelName);
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(ChannelPanel.this,
                        String.format("%s: %s", e1.getClass().getName(),
                                e1.getMessage()),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}

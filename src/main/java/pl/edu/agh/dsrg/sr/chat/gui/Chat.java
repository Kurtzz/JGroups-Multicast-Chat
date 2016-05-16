package pl.edu.agh.dsrg.sr.chat.gui;

import pl.edu.agh.dsrg.sr.chat.service.ChatService;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by P on 12.05.2016.
 */
public class Chat extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel panel;

    private Map<String, ChannelPanel> tabs;
    private JoinPanel joinPanel;
    private ChatService chatService;

    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;

    public Chat() throws Exception {
        super("Chat");

        chatService = new ChatService();
        chatService.setGui(this);
        chatService.setNick(promptForNick());

        tabs = new HashMap<>();

        createGUI();

        chatService.joinManagementChannel();
    }

    private String promptForNick() throws Exception {
        String nick = "";

        while (nick.length() <= 0) {
            nick = JOptionPane.showInputDialog(this, "Enter nick", "Nick", JOptionPane.QUESTION_MESSAGE);

            if (nick == null) {
                System.exit(1);
            }
        }

        return nick;
    }

    private void createGUI() {
        tabbedPane = new JTabbedPane();
        add(tabbedPane);

        joinPanel = new JoinPanel(chatService);
        tabbedPane.add("Join", joinPanel);

        pack();
        setSize(WIDTH, HEIGHT);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                chatService.leaveManagementChannel();
                try {
                    Thread.sleep(500);
                    System.exit(0);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

    public void addChannel(String channelName) {
        ChannelPanel panel = new ChannelPanel(chatService, channelName);
        tabs.put(channelName, panel);
        joinPanel.addChannel(channelName);
    }

    public void joinChannel(String channelName) {
        ChannelPanel panel = tabs.get(channelName);
        tabbedPane.add(panel, channelName);
        tabbedPane.setSelectedComponent(panel);
    }

    public void leaveChannel(String channelName) {
        ChannelPanel panel = tabs.get(channelName);
        panel.clear();
        tabbedPane.setSelectedIndex(0);
        tabbedPane.remove(panel);
    }

    public void putMessage(String channelName, String nick, String text) {
        ChannelPanel panel = tabs.get(channelName);

        if (panel.isDisplayable()) {
            panel.append(String.format("<%s> %s\n", nick, text));
        }
    }

    public void addUser(String channelName, String nick) {
        ChannelPanel panel = tabs.get(channelName);
        panel.addUser(nick);

        if (panel.isDisplayable()) {
            panel.append(String.format("* %s joined channel.\n", nick));
        }
    }

    public void removeUser(String channelName, String nick) {
        ChannelPanel panel = tabs.get(channelName);
        panel.removeUser(nick);

        if (panel.isDisplayable()) {
            panel.append(String.format("* %s left channel.\n", nick));
        }
    }

    public void removeChannel(String channelName) {
        tabs.remove(channelName);
        joinPanel.removeChannel(channelName);
    }
}

package pl.edu.agh.dsrg.sr.chat.service.receivers;

import com.google.common.base.Function;
import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.*;
import pl.edu.agh.dsrg.sr.chat.gui.Chat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Lists.transform;
import static pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.*;

/**
 * Created by P on 09.05.2016.
 */
public class ManagementReceiver extends ReceiverAdapter {
    private final Map<String, List<String>> channelUsers;
    private final Chat gui;
    private final JChannel managementChannel;

    public ManagementReceiver(Map<String, List<String>> channelUsers, Chat gui, JChannel managementChannel) {
        this.channelUsers = channelUsers;
        this.gui = gui;
        this.managementChannel = managementChannel;
    }

    @Override
    public void receive(Message msg) {
        synchronized (channelUsers) {
            ChatAction action;

            try {
                action = ChatAction.parseFrom(msg.getBuffer());

                ChatAction.ActionType actionType = action.getAction();
                String channelName = action.getChannel();
                String nick = action.getNickname();

                switch (actionType) {
                    case JOIN:
                        if (!channelUsers.containsKey(channelName)) {
                            channelUsers.put(channelName, new LinkedList<>());
                            gui.addChannel(channelName);
                        }

                        channelUsers.get(channelName).add(nick);
                        gui.addUser(channelName, nick);
                        break;

                    case LEAVE:
                        channelUsers.get(channelName).remove(nick);
                        gui.removeUser(channelName, nick);

                        if (channelUsers.get(channelName).isEmpty()) {
                            channelUsers.remove(channelName);
                            gui.removeChannel(channelName);
                        }
                        break;
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (channelUsers) {
            ChatState.Builder builder = ChatState.newBuilder();

            for (Map.Entry<String, List<String>> entry : channelUsers.entrySet()) {
                String channelName = entry.getKey();
                List<String> users = entry.getValue();

                for (String user : users) {
                    builder.addStateBuilder()
                            .setAction(ChatAction.ActionType.JOIN)
                            .setChannel(channelName)
                            .setNickname(user);
                }
            }

            ChatState state = builder.build();

            state.writeTo(output);
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        synchronized (channelUsers) {
            ChatState state = ChatState.parseFrom(input);
            channelUsers.clear();

            for (ChatAction chatAction : state.getStateList()) {
                String channelName = chatAction.getChannel();
                String nick = chatAction.getNickname();

                if (!channelUsers.containsKey(channelName)) {
                    channelUsers.put(channelName, new LinkedList<>());
                    gui.addChannel(channelName);
                }

                channelUsers.get(channelName).add(nick);
                gui.addUser(channelName, nick);
            }
        }
    }

    @Override
    public void viewAccepted(View view) {
        synchronized (channelUsers) {
            List<String> currentUsers = transform(view.getMembers(), new Function<Address, String>() {
                @Override
                public String apply(Address address) {
                    return managementChannel.getName(address);
                }
            });

            for (Map.Entry<String, List<String>> entry : channelUsers.entrySet()) {
                String channelName = entry.getKey();

                List<String> lostUsers = newLinkedList(entry.getValue());
                lostUsers.removeAll(currentUsers);

                for (String lostUser : lostUsers) {
                    gui.removeUser(channelName, lostUser);
                }

                entry.getValue().retainAll(currentUsers);
            }
        }
    }
}

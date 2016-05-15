package pl.edu.agh.dsrg.sr.chat.service.receivers;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import pl.edu.agh.dsrg.sr.chat.gui.Chat;

import static pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos.*;

/**
 * Created by P on 09.05.2016.
 */
public class ChannelReceiver extends ReceiverAdapter {
    private final JChannel channel;
    private final Chat gui;

    public ChannelReceiver(JChannel channel, Chat gui) {
        this.channel = channel;
        this.gui = gui;
    }

    @Override
    public void receive(Message msg) {
        try {
            Address address = msg.getSrc();

            ChatMessage message = ChatMessage.parseFrom(msg.getBuffer());

            String channelName = channel.getClusterName();
            String nick = channel.getName(address);
            String text = message.getMessage();

            gui.putMessage(channelName, nick, text);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}

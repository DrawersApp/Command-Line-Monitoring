package dictionary.bot;

import com.uber.sdk.rides.client.internal.RetrofitAdapter;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.TLSUtils;
import org.jivesoftware.smackx.json.packet.JsonPacketExtension;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by harshit on 20/1/16.
 */
public class Bot {

    private static Bot bot;

    private Bot() throws XmppStringprepException {
        initializeConnection();
    }

    public static synchronized Bot getBot() throws XmppStringprepException {
        if (bot == null) {
            bot = new Bot();
        }
        return bot;
    }

    private XMPPTCPConnection xmppConnection;

    public void initializeConnection() throws XmppStringprepException {
        // Listen for messages
        SmackConfiguration.setDefaultPacketReplyTimeout(30 * 1000);
        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
        config.setUsernameAndPassword("0a4da160-a41e-42e6-a1c4-7642470db68f", "command");
        config.setResource("smack");
        config.setXmppDomain(JidCreate.domainBareFrom("ejabberd.sandwitch.in"));
        config.setDebuggerEnabled(true);
        config.setCompressionEnabled(true);
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible);
        config.setKeystorePath("src/main/java/res/ser.cert");
        try {
            TLSUtils.acceptAllCertificates(config);
            TLSUtils.disableHostnameVerificationForTlsCertificicates(config);
        } catch (Exception e) {
            e.printStackTrace();
        }

        xmppConnection = new XMPPTCPConnection(config.build());

        xmppConnection.setUseStreamManagement(true);
        xmppConnection.setUseStreamManagementResumption(true);
        xmppConnection.setPreferredResumptionTime(5 * 60);
        // Listen for messages
        StanzaFilter chatFilter = MessageTypeFilter.CHAT;
        xmppConnection.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException, InterruptedException {
                Message message = (Message) packet;
                // Handle inbound message.TODO - Check for message types.
                Observable.just(message)
                        .subscribeOn(Schedulers.computation())
                        .map(message1 -> message1.getBody())
                        .filter(body -> body != null && body.length() > 0)
                        .map(command -> ShellCommands.getShellCommands().executeCommand(command))
                        .filter(output -> output != null && output.length()>0)
                        .subscribe(filteredOutput -> {
                            try {
                                xmppConnection.sendStanza(generateMessage(message.getFrom(),
                                        Message.Type.chat, filteredOutput));
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
            }
        }, chatFilter);
        try {
            xmppConnection.connect();
        } catch (InterruptedException | XMPPException | SmackException | IOException e) {
            e.printStackTrace();
        }
        /*
          Third parameter is resource. Used to support chat across different entities like Laptop/Mobile.
          Hard code resource to the client/libray - help us identify the client.
         */

        try {
            xmppConnection.login();
        } catch (InterruptedException | IOException | SmackException | XMPPException e) {
            e.printStackTrace();
        }
    }


    private Message generateMessage(Jid from, Message.Type chat, String s) {
        Message message = new Message();
        message.setStanzaId(UUID.randomUUID().toString());
        message.setType(chat);
        message.setTo(from);
        message.setBody(s);
        message.addExtension(generateJsonContainer(ChatType.TEXT));
        return message;
    }



    public JsonPacketExtension generateJsonContainer(ChatType chatType) {
        ChatMetaData  chatMetaData = new ChatMetaData(chatType.toString(), System.currentTimeMillis());
        JsonPacketExtension jsonPacketExtension = new JsonPacketExtension(chatMetaData.toJsonString());
        return jsonPacketExtension;
    }

}

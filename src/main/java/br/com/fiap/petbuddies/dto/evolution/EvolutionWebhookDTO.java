package br.com.fiap.petbuddies.dto.evolution;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EvolutionWebhookDTO {

    private String event;
    private String instance;
    private Data data;

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getInstance() { return instance; }
    public void setInstance(String instance) { this.instance = instance; }

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private Key key;
        private Message message;

        public Key getKey() { return key; }
        public void setKey(Key key) { this.key = key; }

        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Key {
        private String remoteJid;
        private boolean fromMe;

        public String getRemoteJid() { return remoteJid; }
        public void setRemoteJid(String remoteJid) { this.remoteJid = remoteJid; }

        public boolean isFromMe() { return fromMe; }
        public void setFromMe(boolean fromMe) { this.fromMe = fromMe; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String conversation;
        private ExtendedTextMessage extendedTextMessage;

        public String getConversation() { return conversation; }
        public void setConversation(String conversation) { this.conversation = conversation; }

        public ExtendedTextMessage getExtendedTextMessage() { return extendedTextMessage; }
        public void setExtendedTextMessage(ExtendedTextMessage extendedTextMessage) {
            this.extendedTextMessage = extendedTextMessage;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtendedTextMessage {
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}

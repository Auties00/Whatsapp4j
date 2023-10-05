package it.auties.whatsapp.model.request;

import it.auties.whatsapp.model.jid.Jid;
import it.auties.whatsapp.model.info.MessageInfo;

import java.util.List;
import java.util.Map;

public record MessageSendRequest(MessageInfo info, List<Jid> recipients, boolean force, boolean peer, Map<String, ?> additionalAttributes) {
    public MessageSendRequest(MessageInfo info) {
        this(info, null, false, false, null);
    }

    public boolean hasRecipientOverride() {
        return recipients != null && !recipients.isEmpty();
    }
}
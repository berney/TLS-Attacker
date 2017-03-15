/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.tls.protocol.serializer.extension;

import de.rub.nds.tlsattacker.tls.protocol.extension.ExtensionMessage;
import de.rub.nds.tlsattacker.tls.protocol.parser.extension.*;
import de.rub.nds.tlsattacker.tls.protocol.extension.HeartbeatExtensionMessage;

/**
 *
 * @author Robert Merget - robert.merget@rub.de
 */
public class HeartbeatExtensionSerializer extends ExtensionSerializer<HeartbeatExtensionMessage>{

    private final HeartbeatExtensionMessage message;
    
    public HeartbeatExtensionSerializer(HeartbeatExtensionMessage message) {
        super(message);
        this.message = message;
    }

    @Override
    public byte[] serializeExtensionContent() {
        appendBytes(message.getHeartbeatMode().getValue());
        return getAlreadySerialized();
    }   
}
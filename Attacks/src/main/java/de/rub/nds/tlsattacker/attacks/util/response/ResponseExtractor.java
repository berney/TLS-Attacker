/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.attacks.util.response;

import static de.rub.nds.tlsattacker.attacks.impl.Attacker.LOGGER;
import de.rub.nds.tlsattacker.core.constants.ProtocolMessageType;
import de.rub.nds.tlsattacker.core.protocol.message.ProtocolMessage;
import de.rub.nds.tlsattacker.core.record.AbstractRecord;
import de.rub.nds.tlsattacker.core.record.Record;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import de.rub.nds.tlsattacker.core.workflow.action.ReceivingAction;
import de.rub.nds.tlsattacker.transport.exception.InvalidTransportHandlerStateException;
import de.rub.nds.tlsattacker.transport.socket.SocketState;
import de.rub.nds.tlsattacker.transport.tcp.ClientTcpTransportHandler;
import java.util.LinkedList;
import java.util.List;

public class ResponseExtractor {

    private ResponseExtractor() {
    }

    public static ResponseFingerprint getFingerprint(State state) {
        TlsContext context = state.getTlsContext();
        ReceivingAction action = state.getWorkflowTrace().getLastReceivingAction();
        boolean receivedTransportHandlerException = context.isReceivedTransportHandlerException();
        boolean receivedAnEncryptedAlert = didReceiveEncryptedAlert(action);
        int numberRecordsReceived = action.getReceivedRecords().size();
        int numberOfMessageReceived = action.getReceivedMessages().size();
        List<Class<AbstractRecord>> recordClasses = extractRecordClasses(action);
        List<Class<ProtocolMessage>> messageClasses = extractMessageClasses(action);
        List<ProtocolMessage> messageList = action.getReceivedMessages();
        List<AbstractRecord> recordList = action.getReceivedRecords();
        SocketState socketState = extractSocketState(state);
        return new ResponseFingerprint(receivedTransportHandlerException, receivedAnEncryptedAlert,
                numberRecordsReceived, numberOfMessageReceived, recordClasses, messageClasses, messageList, recordList,
                socketState);
    }

    private static SocketState extractSocketState(State state) {
        try {
            if (state.getTlsContext().getTransportHandler() instanceof ClientTcpTransportHandler) {
                SocketState socketState = (((ClientTcpTransportHandler) (state.getTlsContext().getTransportHandler()))
                        .getSocketState());
                return socketState;
            } else {
                return null;
            }
        } catch (InvalidTransportHandlerStateException ex) {
            LOGGER.warn(ex);
            return null;
        }
    }

    private static List<Class<AbstractRecord>> extractRecordClasses(ReceivingAction action) {
        List<Class<AbstractRecord>> classList = new LinkedList<>();
        for (AbstractRecord record : action.getReceivedRecords()) {
            classList.add((Class<AbstractRecord>) record.getClass());
        }
        return classList;
    }

    private static List<Class<ProtocolMessage>> extractMessageClasses(ReceivingAction action) {
        List<Class<ProtocolMessage>> classList = new LinkedList<>();
        for (ProtocolMessage message : action.getReceivedMessages()) {
            classList.add((Class<ProtocolMessage>) message.getClass());
        }
        return classList;
    }

    private static boolean didReceiveEncryptedAlert(ReceivingAction action) {
        for (AbstractRecord abstractRecord : action.getReceivedRecords()) {
            if (abstractRecord instanceof Record) {
                Record record = (Record) abstractRecord;
                if (record.getContentMessageType() == ProtocolMessageType.ALERT) {
                    if (record.getLength().getValue() > 6) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.core.protocol.message;

import de.rub.nds.modifiablevariable.ModifiableVariableFactory;
import de.rub.nds.modifiablevariable.ModifiableVariableProperty;
import de.rub.nds.modifiablevariable.bytearray.ModifiableByteArray;
import de.rub.nds.modifiablevariable.integer.ModifiableInteger;
import de.rub.nds.modifiablevariable.singlebyte.ModifiableByte;
import de.rub.nds.modifiablevariable.util.ArrayConverter;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.HandshakeMessageType;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.protocol.handler.ClientHelloHandler;
import de.rub.nds.tlsattacker.core.protocol.handler.ProtocolMessageHandler;
import de.rub.nds.tlsattacker.core.protocol.message.extension.AlpnExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.CachedInfoExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.CertificateStatusRequestExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.CertificateStatusRequestV2ExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.CertificateTypeExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ClientAuthzExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ClientCertificateTypeExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ClientCertificateUrlExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ECPointFormatExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.EarlyDataExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.EllipticCurvesExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.EncryptThenMacExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ExtendedMasterSecretExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.HeartbeatExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.KS.KeySharePair;
import de.rub.nds.tlsattacker.core.protocol.message.extension.KeyShareExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.MaxFragmentLengthExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.PSKKeyExchangeModesExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.PaddingExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.PreSharedKeyExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.RenegotiationInfoExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.SNI.ServerNamePair;
import de.rub.nds.tlsattacker.core.protocol.message.extension.SRPExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ServerAuthzExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ServerCertificateTypeExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ServerNameIndicationExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.SessionTicketTLSExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.SignatureAndHashAlgorithmsExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.SignedCertificateTimestampExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.SrtpExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.SupportedVersionsExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.TokenBindingExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.TruncatedHmacExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.TrustedCaIndicationExtensionMessage;
import de.rub.nds.tlsattacker.core.state.TlsContext;
import java.nio.charset.Charset;
import java.util.Date;
import javax.xml.bind.annotation.XmlRootElement;

// @XmlType(propOrder = {"compressionLength", "cipherSuiteLength"})
@XmlRootElement
public class ClientHelloMessage extends HelloMessage {

    /**
     * compression length
     */
    @ModifiableVariableProperty(type = ModifiableVariableProperty.Type.LENGTH)
    private ModifiableInteger compressionLength;
    /**
     * cipher suite byte length
     */
    @ModifiableVariableProperty(type = ModifiableVariableProperty.Type.LENGTH)
    private ModifiableInteger cipherSuiteLength;
    /**
     * array of supported CipherSuites
     */
    @ModifiableVariableProperty(type = ModifiableVariableProperty.Type.TLS_CONSTANT)
    private ModifiableByteArray cipherSuites;
    /**
     * array of supported compressions
     */
    @ModifiableVariableProperty(type = ModifiableVariableProperty.Type.TLS_CONSTANT)
    private ModifiableByteArray compressions;

    @ModifiableVariableProperty(type = ModifiableVariableProperty.Type.COOKIE)
    private ModifiableByteArray cookie = null;

    @ModifiableVariableProperty(type = ModifiableVariableProperty.Type.LENGTH)
    private ModifiableByte cookieLength = null;

    public ClientHelloMessage() {
        super(HandshakeMessageType.CLIENT_HELLO);
    }

    public ClientHelloMessage(Config tlsConfig) {
        super(tlsConfig, HandshakeMessageType.CLIENT_HELLO);
        if (tlsConfig.isAddHeartbeatExtension()) {
            addExtension(new HeartbeatExtensionMessage());
        }
        if (tlsConfig.isAddECPointFormatExtension()) {
            addExtension(new ECPointFormatExtensionMessage());
        }
        if (tlsConfig.isAddEllipticCurveExtension()) {
            addExtension(new EllipticCurvesExtensionMessage());
        }
        if (tlsConfig.isAddMaxFragmentLengthExtenstion()) {
            addExtension(new MaxFragmentLengthExtensionMessage());
        }
        if (tlsConfig.isAddServerNameIndicationExtension()) {
            ServerNameIndicationExtensionMessage extension = new ServerNameIndicationExtensionMessage();
            ServerNamePair pair = new ServerNamePair();
            pair.setServerNameConfig(tlsConfig.getSniHostname().getBytes(Charset.forName("ASCII")));
            pair.setServerNameTypeConfig(tlsConfig.getSniType().getValue());
            extension.getServerNameList().add(pair);
            addExtension(extension);
        }
        if (tlsConfig.isAddSignatureAndHashAlgrorithmsExtension()) {
            addExtension(new SignatureAndHashAlgorithmsExtensionMessage());
        }
        if (tlsConfig.isAddSupportedVersionsExtension()) {
            addExtension(new SupportedVersionsExtensionMessage());
        }
        if (tlsConfig.isAddKeyShareExtension()) {
            addExtension(new KeyShareExtensionMessage(tlsConfig));
        }
        if (tlsConfig.isAddEarlyDataExtension()) {
            addExtension(new EarlyDataExtensionMessage());
        }
        if (tlsConfig.isAddPSKKeyExchangeModesExtension()) {
            addExtension(new PSKKeyExchangeModesExtensionMessage(tlsConfig));
        }
        if (tlsConfig.isAddExtendedMasterSecretExtension()) {
            addExtension(new ExtendedMasterSecretExtensionMessage());
        }
        if (tlsConfig.isAddSessionTicketTLSExtension()) {
            addExtension(new SessionTicketTLSExtensionMessage());
        }
        if (tlsConfig.isAddSignedCertificateTimestampExtension()) {
            addExtension(new SignedCertificateTimestampExtensionMessage());
        }
        if (tlsConfig.isAddPaddingExtension()) {
            addExtension(new PaddingExtensionMessage());
        }
        if (tlsConfig.isAddRenegotiationInfoExtension()) {
            addExtension(new RenegotiationInfoExtensionMessage());
        }
        if (tlsConfig.isAddTokenBindingExtension()) {
            addExtension(new TokenBindingExtensionMessage());
        }
        if (tlsConfig.isAddCertificateStatusRequestExtension()) {
            addExtension(new CertificateStatusRequestExtensionMessage());
        }
        if (tlsConfig.isAddAlpnExtension()) {
            addExtension(new AlpnExtensionMessage(tlsConfig));
        }
        if (tlsConfig.isAddSRPExtension()) {
            addExtension(new SRPExtensionMessage());
        }
        if (tlsConfig.isAddSRTPExtension()) {
            addExtension(new SrtpExtensionMessage());
        }
        if (tlsConfig.isAddTruncatedHmacExtension()) {
            addExtension(new TruncatedHmacExtensionMessage());
        }
        if (tlsConfig.isAddUserMappingExtension()) {
            addExtension(new UserMappingExtensionMessage());
        }
        if (tlsConfig.isAddCertificateTypeExtension()) {
            addExtension(new CertificateTypeExtensionMessage());
        }
        if (tlsConfig.isAddClientAuthzExtension()) {
            addExtension(new ClientAuthzExtensionMessage());
        }
        if (tlsConfig.isAddServerAuthzExtension()) {
            addExtension(new ServerAuthzExtensionMessage());
        }
        if (tlsConfig.isAddClientCertificateTypeExtension()) {
            addExtension(new ClientCertificateTypeExtensionMessage());
        }
        if (tlsConfig.isAddServerCertificateTypeExtension()) {
            addExtension(new ServerCertificateTypeExtensionMessage());
        }
        if (tlsConfig.isAddEncryptThenMacExtension()) {
            addExtension(new EncryptThenMacExtensionMessage());
        }
        if (tlsConfig.isAddCachedInfoExtension()) {
            addExtension(new CachedInfoExtensionMessage());
        }
        if (tlsConfig.isAddClientCertificateUrlExtension()) {
            addExtension(new ClientCertificateUrlExtensionMessage());
        }
        if (tlsConfig.isAddTrustedCaIndicationExtension()) {
            addExtension(new TrustedCaIndicationExtensionMessage());
        }
        if (tlsConfig.isAddCertificateStatusRequestV2Extension()) {
            addExtension(new CertificateStatusRequestV2ExtensionMessage());
        }
        if (tlsConfig.isAddPreSharedKeyExtension()) {
            addExtension(new PreSharedKeyExtensionMessage(tlsConfig));
        } 
        //In TLS 1.3, the PSK ext has to be the last ClientHello extension
    }

    public ModifiableInteger getCompressionLength() {
        return compressionLength;
    }

    public ModifiableInteger getCipherSuiteLength() {
        return cipherSuiteLength;
    }

    public ModifiableByteArray getCipherSuites() {
        return cipherSuites;
    }

    public ModifiableByteArray getCompressions() {
        return compressions;
    }

    public void setCompressionLength(ModifiableInteger compressionLength) {
        this.compressionLength = compressionLength;
    }

    public void setCipherSuiteLength(ModifiableInteger cipherSuiteLength) {
        this.cipherSuiteLength = cipherSuiteLength;
    }

    public void setCipherSuites(ModifiableByteArray cipherSuites) {
        this.cipherSuites = cipherSuites;
    }

    public void setCompressions(ModifiableByteArray compressions) {
        this.compressions = compressions;
    }

    public void setCompressionLength(int compressionLength) {
        this.compressionLength = ModifiableVariableFactory.safelySetValue(this.compressionLength, compressionLength);
    }

    public void setCipherSuiteLength(int cipherSuiteLength) {
        this.cipherSuiteLength = ModifiableVariableFactory.safelySetValue(this.cipherSuiteLength, cipherSuiteLength);
    }

    public void setCipherSuites(byte[] array) {
        this.cipherSuites = ModifiableVariableFactory.safelySetValue(cipherSuites, array);
    }

    public void setCompressions(byte[] array) {
        this.compressions = ModifiableVariableFactory.safelySetValue(compressions, array);
    }

    public ModifiableByteArray getCookie() {
        return cookie;
    }

    public ModifiableByte getCookieLength() {
        return cookieLength;
    }

    public void setCookie(byte[] cookie) {
        this.cookie = ModifiableVariableFactory.safelySetValue(this.cookie, cookie);
    }

    public void setCookie(ModifiableByteArray cookie) {
        this.cookie = cookie;
    }

    public void setCookieLength(byte cookieLength) {
        this.cookieLength = ModifiableVariableFactory.safelySetValue(this.cookieLength, cookieLength);
    }

    public void setCookieLength(ModifiableByte cookieLength) {
        this.cookieLength = cookieLength;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (getProtocolVersion() != null && getProtocolVersion().getValue() != null) {
            sb.append("\n  Protocol Version: ");
            sb.append(ProtocolVersion.getProtocolVersion(getProtocolVersion().getValue()));
        }
        if (getUnixTime() != null && getUnixTime().getValue() != null) {
            sb.append("\n  Client Unix Time: ");
            sb.append(new Date(ArrayConverter.bytesToLong(getUnixTime().getValue()) * 1000));
        }
        if (getRandom() != null && getRandom().getValue() != null) {
            sb.append("\n  Client Random: ").append(ArrayConverter.bytesToHexString(getRandom().getValue()));
        }
        if (getSessionId() != null && getSessionId().getValue() != null) {
            sb.append("\n  Session ID: ").append(ArrayConverter.bytesToHexString(getSessionId().getValue()));
        }
        if (getCipherSuites() != null && getCipherSuites().getValue() != null) {
            sb.append("\n  Supported Cipher Suites: ").append(
                    ArrayConverter.bytesToHexString(getCipherSuites().getValue()));
        }
        if (getCompressions() != null && getCompressions().getValue() != null) {
            sb.append("\n  Supported Compression Methods: ")
                    .append(ArrayConverter.bytesToHexString(getCompressions().getValue())).append("\n  Extensions: ");
        }
        if (getExtensions() != null) {
            for (ExtensionMessage extension : getExtensions()) {
                sb.append(extension.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    @Override
    public ProtocolMessageHandler getHandler(TlsContext context) {
        return new ClientHelloHandler(context);
    }

}

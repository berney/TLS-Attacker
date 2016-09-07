/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2016 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0 http://www.apache.org/licenses/LICENSE-2.0
 */
package tlsattacker.fuzzer.mutator.certificate;

import tlsattacker.fuzzer.analyzer.Rule;
import tlsattacker.fuzzer.certificate.ClientCertificateStructure;
import tlsattacker.fuzzer.certificate.ServerCertificateStructure;
import tlsattacker.fuzzer.config.analyzer.RuleConfig;
import tlsattacker.fuzzer.config.ConfigManager;
import tlsattacker.fuzzer.config.EvolutionaryFuzzerConfig;
import tlsattacker.fuzzer.config.mutator.certificate.FixedCertificateMutatorConfig;
import de.rub.nds.tlsattacker.tls.constants.CompressionMethod;
import de.rub.nds.tlsattacker.tls.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.util.KeystoreHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXB;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.crypto.tls.TlsUtils;
import org.bouncycastle.jce.provider.X509CertificateObject;

/**
 * 
 * @author Robert Merget - robert.merget@rub.de
 */
public class FixedCertificateMutator extends CertificateMutator {
    private FixedCertificateMutatorConfig config;
    private List<ClientCertificateStructure> clientCertList;
    private List<ServerCertificateStructure> serverPairList;
    private Random r;

    public FixedCertificateMutator() {
	EvolutionaryFuzzerConfig evoConfig = ConfigManager.getInstance().getConfig();
	String configFileName = "fixed_cert.config";
	File f = new File(evoConfig.getCertificateMutatorConfigFolder() + configFileName);
	if (f.exists()) {
	    config = JAXB.unmarshal(f, FixedCertificateMutatorConfig.class);
	} else {
	    LOG.log(Level.FINE, "No ConfigFile found:" + configFileName);
	}
	if (config == null) {
	    config = new FixedCertificateMutatorConfig();
	    serialize(f);
	}
	this.clientCertList = config.getClientCertificates();
	this.serverPairList = config.getServerCertificates();
	if (clientCertList.isEmpty() || serverPairList.isEmpty()) {
	    LOG.log(Level.INFO,
		    "The CertificateMutator is not properly configured. Make sure that the FixedCertificateMutator knows atleast one Client and one Server CertificatePair");
	    throw new ConfigurationException("CertificateMutator has not enough Certificates");
	}
	r = new Random();
    }

    public List<ClientCertificateStructure> getClientCertList() {
	return clientCertList;
    }

    public List<ServerCertificateStructure> getServerPairList() {
	return serverPairList;
    }

    @Override
    public ClientCertificateStructure getClientCertificateStructure() {
	return clientCertList.get(r.nextInt(clientCertList.size()));
    }

    @Override
    public ServerCertificateStructure getServerCertificateStructure() {
	return serverPairList.get(r.nextInt(serverPairList.size()));
    }

    public void serialize(File file) {
	if (!file.exists()) {
	    try {
		file.createNewFile();
	    } catch (IOException ex) {
		Logger.getLogger(FixedCertificateMutator.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
	JAXB.marshal(config, file);
    }

    private static final Logger LOG = Logger.getLogger(FixedCertificateMutator.class.getName());
}

/**
 * TLS-Attacker - A Modular Penetration Testing Framework for TLS
 *
 * Copyright 2014-2017 Ruhr University Bochum / Hackmanit GmbH
 *
 * Licensed under Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package de.rub.nds.tlsattacker.attacks.impl;

import de.rub.nds.tls.subject.TlsImplementationType;
import de.rub.nds.tls.subject.TlsServer;
import de.rub.nds.tls.subject.docker.DockerSpotifyTlsServerManager;
import de.rub.nds.tls.subject.docker.DockerTlsServerManagerFactory;
import de.rub.nds.tlsattacker.attacks.config.EarlyCCSCommandConfig;
import de.rub.nds.tlsattacker.attacks.config.delegate.GeneralAttackDelegate;
import de.rub.nds.tlsattacker.core.config.delegate.ClientDelegate;
import de.rub.nds.tlsattacker.util.UnlimitedStrengthEnabler;
import de.rub.nds.tlsattacker.util.tests.DockerTests;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.experimental.categories.Category;

@Category(DockerTests.class)
public class EarlyCCSAttackerTest {

    public EarlyCCSAttackerTest() {
    }

    private DockerSpotifyTlsServerManager serverManager;
    private TlsServer server = null;

    @BeforeClass
    public static void setUpClass() {
        UnlimitedStrengthEnabler.enable();
        Security.addProvider(new BouncyCastleProvider());
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.kill();
        }
    }

    @Test
    public void testIsVulnerableFalse() {
        System.out.println("Starting CVE-20162107 tests vs Openssl 1.0.1h (expected false)");
        DockerTlsServerManagerFactory factory = new DockerTlsServerManagerFactory();
        server = factory.get(TlsImplementationType.OPENSSL, "1.0.1h");
        EarlyCCSCommandConfig config = new EarlyCCSCommandConfig(new GeneralAttackDelegate());
        ClientDelegate delegate = (ClientDelegate) config.getDelegate(ClientDelegate.class);
        delegate.setHost(server.getHost() + ":" + server.getPort());
        EarlyCCSAttacker attacker = new EarlyCCSAttacker(config);
        assertFalse(attacker.isVulnerable());
    }

    @Test
    public void testIsVulnerableTrue() {
        System.out.println("Starting CVE-20162107 tests vs Openssl 1.0.1g (expected true)");
        DockerTlsServerManagerFactory factory = new DockerTlsServerManagerFactory();
        server = factory.get(TlsImplementationType.OPENSSL, "1.0.1g");
        EarlyCCSCommandConfig config = new EarlyCCSCommandConfig(new GeneralAttackDelegate());
        ClientDelegate delegate = (ClientDelegate) config.getDelegate(ClientDelegate.class);
        delegate.setHost(server.getHost() + ":" + server.getPort());
        EarlyCCSAttacker attacker = new EarlyCCSAttacker(config);
        assertTrue(attacker.isVulnerable());
    }

}

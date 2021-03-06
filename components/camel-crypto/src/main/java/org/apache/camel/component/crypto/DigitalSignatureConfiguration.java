/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.crypto;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.RuntimeCamelException;


public class DigitalSignatureConfiguration implements Cloneable, CamelContextAware {

    private PrivateKey privateKey;
    private KeyStore keystore;
    private SecureRandom secureRandom;
    private String algorithm = "SHA1WithDSA";
    private Integer bufferSize = Integer.valueOf(2048);
    private String provider;
    private String signatureHeaderName;
    private String alias;
    private char[] password;
    private PublicKey publicKey;
    private Certificate certificate;
    private CamelContext context;

    /** references that should be resolved when the context changes */
    private String publicKeyName;
    private String certificateName;
    private String privateKeyName;
    private String keystoreName;
    private String randomName;
    private boolean clearHeaders = true;
    private String operation;

    public DigitalSignatureConfiguration copy() {
        try {
            return (DigitalSignatureConfiguration)clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeCamelException(e);
        }
    }

    public CamelContext getCamelContext() {
        return context;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.context = camelContext;
        // try to retrieve the references once the context is available.
        setKeystore(keystoreName);
        setPublicKey(publicKeyName);
        setPrivateKey(privateKeyName);
        setCertificate(certificateName);
        setSecureRandom(randomName);
    }

    /**
     * Gets the JCE name of the Algorithm that should be used for the signer.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the JCE name of the Algorithm that should be used for the signer.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Gets the alias used to query the KeyStore for keys and {@link java.security.cert.Certificate Certificates}
     * to be used in signing and verifying exchanges. This value can be provided at runtime via the message header
     * {@link org.apache.camel.component.crypto.DigitalSignatureConstants#KEYSTORE_ALIAS}
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias used to query the KeyStore for keys and {@link java.security.cert.Certificate Certificates}
     * to be used in signing and verifying exchanges. This value can be provided at runtime via the message header
     * {@link org.apache.camel.component.crypto.DigitalSignatureConstants#KEYSTORE_ALIAS}
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Get the PrivateKey that should be used to sign the exchange
     */
    public PrivateKey getPrivateKey() throws Exception {
        return getPrivateKey(alias, password);
    }

    /**
     * Get the PrivateKey that should be used to sign the signature in the
     * exchange using the supplied alias.
     *
     * @param alias the alias used to retrieve the Certificate from the keystore.
     */
    public PrivateKey getPrivateKey(String alias) throws Exception {
        return getPrivateKey(alias, password);
    }

    /**
     * Get the PrivateKey that should be used to sign the signature in the
     * exchange using the supplied alias.
     *
     * @param alias the alias used to retrieve the Certificate from the keystore.
     */
    public PrivateKey getPrivateKey(String alias, char[] password) throws Exception {
        PrivateKey pk = null;
        if (alias != null && keystore != null) {
            pk = (PrivateKey)keystore.getKey(alias, password);
        }
        if (pk == null) {
            pk = privateKey;
        }
        return pk;
    }

    /**
     * Set the PrivateKey that should be used to sign the exchange
     *
     * @param privateKey the key with with to sign the exchange.
     */
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Sets the reference name for a PrivateKey that can be fond in the registry.
     */
    public void setPrivateKey(String privateKeyName) {
        if (context != null && privateKeyName != null) {
            PrivateKey pk = context.getRegistry().lookupByNameAndType(privateKeyName, PrivateKey.class);
            if (pk != null) {
                setPrivateKey(pk);
            }
        }
        if (privateKeyName != null) {
            this.privateKeyName = privateKeyName;
        }
    }

    /**
     * Set the PublicKey that should be used to verify the signature in the exchange.
     */
    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Sets the reference name for a publicKey that can be fond in the registry.
     */
    public void setPublicKey(String publicKeyName) {
        if (context != null && publicKeyName != null) {
            PublicKey pk = context.getRegistry().lookupByNameAndType(publicKeyName, PublicKey.class);
            if (pk != null) {
                setPublicKey(pk);
            }
        }
        if (publicKeyName != null) {
            this.publicKeyName = publicKeyName;
        }
    }

    /**
     * get the PublicKey that should be used to verify the signature in the exchange.
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Set the Certificate that should be used to verify the signature in the
     * exchange. If a {@link KeyStore} has been configured then this will
     * attempt to retrieve the {@link Certificate}from it using hte supplied
     * alias. If either the alias or the Keystore is invalid then the configured
     * certificate will be returned
     *
     * @param alias the alias used to retrieve the Certificate from the keystore.
     */
    public Certificate getCertificate(String alias) throws Exception {
        Certificate cert = null;
        if (alias != null && keystore != null) {
            cert = keystore.getCertificate(alias);
        }
        if (cert == null) {
            cert = certificate;
        }
        return cert;
    }

    /**
     * Get the explicitly configured {@link Certificate} that should be used to
     * verify the signature in the exchange.
     */
    public Certificate getCertificate() throws Exception {
        return certificate;
    }

    /**
     * Set the Certificate that should be used to verify the signature in the
     * exchange based on its payload.
     */
    public void setCertificate(Certificate certificate) {

        this.certificate = certificate;
    }

    /**
     * Sets the reference name for a PrivateKey that can be fond in the registry.
     */
    public void setCertificate(String certificateName) {
        if (context != null && certificateName != null) {
            Certificate certificate = context.getRegistry().lookupByNameAndType(certificateName, Certificate.class);
            if (certificate != null) {
                setCertificate(certificate);
            }
        }
        if (certificateName != null) {
            this.certificateName = certificateName;
        }
    }

    /**
     * Gets the KeyStore that can contain keys and Certficates for use in
     * signing and verifying exchanges. A {@link KeyStore} is typically used
     * with an alias, either one supplied in the Route definition or dynamically
     * via the message header "CamelSignatureKeyStoreAlias". If no alias is
     * supplied and there is only a single entry in the Keystore, then this
     * single entry will be used.
     */
    public KeyStore getKeystore() {
        return keystore;
    }

    /**
     * Sets the KeyStore that can contain keys and Certficates for use in
     * signing and verifying exchanges. A {@link KeyStore} is typically used
     * with an alias, either one supplied in the Route definition or dynamically
     * via the message header "CamelSignatureKeyStoreAlias". If no alias is
     * supplied and there is only a single entry in the Keystore, then this
     * single entry will be used.
     */
    public void setKeystore(KeyStore keystore) {
        this.keystore = keystore;
    }

    /**
     * Sets the reference name for a Keystore that can be fond in the registry.
     */
    public void setKeystore(String keystoreName) {
        if (context != null && keystoreName != null) {
            KeyStore keystore = context.getRegistry().lookupByNameAndType(keystoreName, KeyStore.class);
            if (keystore != null) {
                setKeystore(keystore);
            }
        }
        if (keystoreName != null) {
            this.keystoreName = keystoreName;
        }
    }

    /**
     * Gets the password used to access an aliased {@link PrivateKey} in the KeyStore.
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * Sets the password used to access an aliased {@link PrivateKey} in the KeyStore.
     */
    public void setPassword(char[] password) {
        this.password = password;
    }

    /**
     * Get the SecureRandom used to initialize the Signature service
     */
    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    /**
     * Sets the reference name for a SecureRandom that can be fond in the registry.
     */
    public void setSecureRandom(String randomName) {
        if (context != null && randomName != null) {
            SecureRandom random = context.getRegistry().lookupByNameAndType(randomName, SecureRandom.class);
            if (keystore != null) {
                setSecureRandom(random);
            }
        }
        if (randomName != null) {
            this.randomName = randomName;
        }
    }

    /**
     * Set the SecureRandom used to initialize the Signature service
     *
     * @param secureRandom the random used to init the Signature service
     */
    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * Get the size of the buffer used to read in the Exchange payload data.
     */
    public Integer getBufferSize() {
        return bufferSize;
    }

    /**
     * Set the size of the buffer used to read in the Exchange payload data.
     */
    public void setBufferSize(Integer bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Get the id of the security provider that provides the configured
     * {@link Signature} algorithm.
     */
    public String getProvider() {
        return provider;
    }

    /**
     * Set the id of the security provider that provides the configured
     * {@link Signature} algorithm.
     *
     * @param provider the id of the security provider
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Get the name of the message header that should be used to store the
     * base64 encoded signature. This defaults to 'CamelDigitalSignature'
     */
    public String getSignatureHeader() {
        return signatureHeaderName != null ? signatureHeaderName : DigitalSignatureConstants.SIGNATURE;
    }

    /**
     * Set the name of the message header that should be used to store the
     * base64 encoded signature. This defaults to 'CamelDigitalSignature'
     */
    public void setSignatureHeader(String signatureHeaderName) {
        this.signatureHeaderName = signatureHeaderName;
    }

    /**
     * Determines if the Signature specific headers be cleared after signing and
     * verification. Defaults to true, and should only be made otherwise at your
     * extreme peril as vital private information such as Keys and passwords may
     * escape if unset.
     *
     * @return true if the Signature headers should be unset, false otherwise
     */
    public boolean getClearHeaders() {
        return clearHeaders;
    }

    /**
     * Determines if the Signature specific headers be cleared after signing and
     * verification. Defaults to true, and should only be made otherwise at your
     * extreme peril as vital private information such as Keys and passwords may
     * escape if unset.
     */
    public void setClearHeaders(boolean clearHeaders) {
        this.clearHeaders = clearHeaders;
    }

    /**
     * Set the Crypto operation from that supplied after the crypto scheme in the
     * endpoint uri e.g. crypto:sign sets sign as the operation.
     *
     * @param operation the operation supplied after the crypto scheme
     */
    public void setCryptoOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Gets the Crypto operation that was supplied in the the crypto scheme in the endpoint uri
     */
    public String getCryptoOperation() {
        return operation;
    }
}

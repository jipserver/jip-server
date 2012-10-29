package jip.utils;

import org.bouncycastle.openssl.PEMWriter;
import org.springframework.uaa.client.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

public class KeyUtils {

    private static KeyPair generateRsaKeyPair(KeyPairGenerator generator, SecureRandom rand) {
        generator.initialize(2048, rand);
        return generator.genKeyPair();
    }

    /**
     * return a "public" -> rsa public key, "private" -> its corresponding private key
     */
    public static Map<String, String> generate() {
        try {
            return generate(KeyPairGenerator.getInstance("RSA"), new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such algorithm", e);
        }
    }

    private static Map<String, String> generate(KeyPairGenerator generator, SecureRandom rand) {
        try {
            KeyPair pair = generateRsaKeyPair(generator, rand);
            Map<String, String> map = new HashMap<String, String>();
            map.put("public", encodeAsOpenSSH(RSAPublicKey.class.cast(pair.getPublic())));
            map.put("private", encodeAsPem(RSAPrivateKey.class.cast(pair.getPrivate())));
            return map;
        } catch (IOException e) {
            // ignore
        }
        return null;
    }

    private static String encodeAsOpenSSH(RSAPublicKey key) {
        byte[] keyBlob = keyBlob(key.getPublicExponent(), key.getModulus());
        return "ssh-rsa " + Base64.encodeBytes(keyBlob);
    }

    private static String encodeAsPem(RSAPrivateKey key) throws IOException {
        StringWriter stringWriter = new StringWriter();
        PEMWriter pemFormatWriter = new PEMWriter(stringWriter);
        pemFormatWriter.writeObject(key);
        pemFormatWriter.close();
        return stringWriter.toString();
    }

    private static byte[] keyBlob(BigInteger publicExponent, BigInteger modulus) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            writeLengthFirst("ssh-rsa".getBytes(), out);
            writeLengthFirst(publicExponent.toByteArray(), out);
            writeLengthFirst(modulus.toByteArray(), out);
            return out.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    // http://www.ietf.org/rfc/rfc4253.txt
    private static void writeLengthFirst(byte[] array, ByteArrayOutputStream out) throws IOException {
        out.write((array.length >>> 24) & 0xFF);
        out.write((array.length >>> 16) & 0xFF);
        out.write((array.length >>> 8) & 0xFF);
        out.write((array.length >>> 0) & 0xFF);
        if (array.length == 1 && array[0] == (byte) 0x00)
            out.write(new byte[0]);
        else
            out.write(array);
    }
}

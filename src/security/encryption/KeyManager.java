package security.encryption;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;

public class KeyManager {
    private static KeyStore createKeyStore(String fileName, String pw, String algorithm) throws Exception {
        File file = new File(fileName);
        final KeyStore keyStore = KeyStore.getInstance("JCEKS");
        if (file.exists()) {
            // .keystore file already exists => load it
            keyStore.load(new FileInputStream(file), pw.toCharArray());
        } else {
            // .keystore file not created yet => create it and save the key + the IV
            keyStore.load(null, null);
            //keyStore.store(new FileOutputStream(fileName), pw.toCharArray());

            // generate a secret key for encryption - the same for all the blocks
            String[] fields= algorithm.split("/");
            SecretKey secretKey = KeyGenerator.getInstance(fields[0]).generateKey();

            //generate a secret key for mac
            SecretKey hMacKey =new SecretKeySpec(secretKey.getEncoded(), "HmacSHA512");

            // store the secret key
            KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(secretKey);
            KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection("pw-secret".toCharArray());
            keyStore.setEntry("mySecretKey", keyStoreEntry, keyPassword);
            keyStore.store(new FileOutputStream(fileName), pw.toCharArray());

            //store the hmac key
            KeyStore.SecretKeyEntry keyStoreHMacEntry = new KeyStore.SecretKeyEntry(hMacKey);
            keyStore.setEntry("hMacSecretKey",keyStoreHMacEntry,keyPassword);
            keyStore.store(new FileOutputStream(fileName),pw.toCharArray());
        }
        return keyStore;
    }
    public static SecretKey[] getKeys(String fileName, String pw, String algorithm) throws Exception{
        SecretKey secretKeys[] = new SecretKey[2];

        // retrieve the stored keys back
//        File file = new File(fileName);
//        final KeyStore keyStore = KeyStore.getInstance("JCEKS");
//        keyStore.load(new FileInputStream(file), pw.toCharArray());

        //Retrieve/create KeyStore file
        KeyStore keyStore = createKeyStore(fileName,pw,algorithm);

        //Retrieve keys
        KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection("pw-secret".toCharArray());

        KeyStore.Entry keyEntry = keyStore.getEntry("mySecretKey", keyPassword);
        secretKeys[0] = ((KeyStore.SecretKeyEntry) keyEntry).getSecretKey();

        KeyStore.Entry hMacKeyEntry = keyStore.getEntry("hMacSecretKey", keyPassword);
        secretKeys[1] = ((KeyStore.SecretKeyEntry) hMacKeyEntry).getSecretKey();
        return secretKeys;
    }
}

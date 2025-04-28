package org.example.api;

import org.apache.commons.codec.binary.Base64;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * 模拟前端 Q 类的 RSA 公钥加密（对应 vendor-jsencrypt-Ve0Y4aUG.js）
 */
public class RsaEncryptor {
    private PublicKey publicKey;

    /**
     * 设置 Base64 编码的公钥
     * @param base64PublicKey 从 checkup 接口获取的 encryptionKey
     */
    public void setPublicKey(String base64PublicKey) {
        try {
            byte[] keyBytes = Base64.decodeBase64(base64PublicKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("公钥解析失败", e);
        }
    }

    /**
     * 加密数据（匹配前端 jsencrypt 的 RSA/ECB/PKCS1Padding 模式）
     * @param data 待加密的 JSON 字符串（如 {"identifier":"xxx","password":"xxx"}）
     * @return Base64 编码的加密结果（cryptogram）
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.encodeBase64String(encryptedBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("加密失败", e);
        }
    }
}
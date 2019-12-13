package com.leyou.common;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;


public class AuthTest {

    private String privateFilePath = "D:/heima/rsa/id_rsa";
    private String publicFilePath = "D:/heima/rsa/id_rsa.pub";

    @Test
    public void testRSA() throws Exception {
        // 生成密钥对
        RsaUtils.generateKey(publicFilePath, privateFilePath, "hello", 2048);

        // 获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        System.out.println("privateKey = " + privateKey);
        // 获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        System.out.println("publicKey = " + publicKey);
    }

    @Test
    public void testJWT() throws Exception {
        // 获取私钥
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        // 生成token
        String token = JwtUtils.generateTokenExpireInMinutes(new UserInfo(1L, "Jack", "guest"), privateKey, 5);
        System.out.println("token = " + token);

        String newToken= "eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjoie1wiaWRcIjozMyxcInVzZXJuYW1lXCI6XCJ0ZXN0MVwiLFwicm9sZVwiOlwi5rWL6K-Vcm9sZVwifSIsImp0aSI6Ik1ESXhZMlpqTnpZdFlqbG1aUzAwTnpVekxUazROVFl0TkRjNE9UTmlPVEUyWkRrMyIsImV4cCI6MTU3NjI0MzM4OH0.fsB1zuH9o-BnnOl49tQdJpVNP1kZficQJ14AVpRubfy3N_QOxrTSFykD8nS5FiCQCWJD9uAp0Xwj-8a6t9NiJQknyja6TLYR2PoDLDWsQVyvU4boJCYPDUd9U6lJs8c_CqxdPMYMXbMYemk50x4bg7D_oQaJzBISRMXKrlegMaNQKm84w6vFO2WNWLLaKTUXNH2VQP2w7ZiX0paZEmixo5THsFSHzg-07H1_80EEy2V3SaJbOVwJe9SxlLbUPuzeRU4S2-KIE6CLcjCcFnGgzODK0THBCXvAVyBC3JARayHws2GAbOEL7bhFQyvDyEeiJ88V8yYN6xx4xShnP3wGoA";

        // 获取公钥
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        // 解析token
        Payload<UserInfo> info = JwtUtils.getInfoFromToken(newToken, publicKey, UserInfo.class);

        System.out.println("info.getExpiration() = " + info.getExpiration());
        System.out.println("info.getInfo() = " + info.getInfo());
        System.out.println("info.getId() = " + info.getId());
    }
}
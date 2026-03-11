package com.example.ymal;

import com.example.config.AppConfig;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.ByteArrayResource;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EncryptedYamlConfig{

    public EncryptedYamlConfig(AnnotationConfigApplicationContext context) {
        try {
            // 1. Spring Environment 가져오기
            ConfigurableEnvironment env = context.getEnvironment();

            // 2. AES 키 읽기
            String AES_KEY = System.getProperty("ENCRYPTION_KEY");
            byte[] keyBytes = Base64.getDecoder().decode(AES_KEY);
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

            // 3. 암호화된 YAML 읽기
            InputStream is = getClass().getClassLoader().getResourceAsStream("config.enc");
            if (is == null) throw new IllegalStateException("config.enc not found");

            String encryptedBase64 = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);

            // 4. AES 복호화
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            String yamlContent = new String(decryptedBytes, StandardCharsets.UTF_8);

            // 5. YAML 파싱
            Map<String, Object> yamlMap = new org.yaml.snakeyaml.Yaml().load(yamlContent);

            // 6. flatten
            Map<String, Object> flattenedMap = new HashMap<>();
            flattenMap("", yamlMap, flattenedMap);

            // 7. PropertySource 등록 (최우선)
            MapPropertySource ps = new MapPropertySource("encryptedYaml", flattenedMap);
            env.getPropertySources().addFirst(ps);

            context.register(AppConfig.class);
            context.refresh();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//    public EncryptedYamlConfig(ConfigurableApplicationContext applicationContext) {
//        try {
//            ConfigurableEnvironment env = applicationContext.getEnvironment();
//            System.out.println("??????????????????");
//            String AES_KEY = System.getProperty("ENCRYPTION_KEY");
//            System.out.println(">>>>"+AES_KEY);
//            byte[] keyBytes = Base64.getDecoder().decode(AES_KEY);
//            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
//
//            // 1. 암호화된 파일 읽기
//            InputStream is = getClass().getClassLoader().getResourceAsStream("config.enc");
//            if (is == null) {
//                throw new IllegalStateException("Encrypted file not found in classpath: config.enc");
//            }
//            String encryptedBase64 = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);  // << 반드시 디코딩
//
//            System.out.println("encryptedBytes >>>> "+encryptedBytes.length);
//            // 2. AES 복호화
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//            cipher.init(Cipher.DECRYPT_MODE, key);
//            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
//
//            String yamlContent = new String(decryptedBytes, StandardCharsets.UTF_8);
//            System.out.println("yamlContent>>>> "+yamlContent +"|");
//
//            // 3. SnakeYAML로 파싱
//            Yaml yaml = new Yaml();
//            Map<String, Object> yamlMap = yaml.load(yamlContent);
//
//            // 4. 중첩 맵을 flat map으로 변환
//            Map<String, Object> flattenedMap = new HashMap<>();
//            flattenMap("", yamlMap, flattenedMap);
//
//            // 5. PropertySource 생성
//            MapPropertySource propertySource = new MapPropertySource("encryptedYaml", flattenedMap);
//
//            // 6. 환경에 추가
//            env.getPropertySources().addFirst(propertySource);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to load encrypted YAML", e);
//        }
//    }

    private static final String ENC_FILE_PATH = "src/main/resources/config.enc";
//    private static final String AES_KEY = "1234567890123456";

//    @Bean
//    public MapPropertySource encryptedYamlPropertySource(ConfigurableEnvironment env) throws Exception {
//        System.out.println("??????????????????");
//        String AES_KEY = System.getProperty("ENCRYPTION_KEY");
//        System.out.println(">>>>"+AES_KEY);
//        byte[] keyBytes = Base64.getDecoder().decode(AES_KEY);
//        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
//
//        // 1. 암호화된 파일 읽기
//        InputStream is = getClass().getClassLoader().getResourceAsStream("config.enc");
//        if (is == null) {
//            throw new IllegalStateException("Encrypted file not found in classpath: config.enc");
//        }
//        String encryptedBase64 = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);  // << 반드시 디코딩
//
//        System.out.println("encryptedBytes >>>> "+encryptedBytes.length);
//        // 2. AES 복호화
//        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//        cipher.init(Cipher.DECRYPT_MODE, key);
//        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
//
//        String yamlContent = new String(decryptedBytes, StandardCharsets.UTF_8);
//        System.out.println("yamlContent>>>> "+yamlContent +"|");
//
//        // 3. SnakeYAML로 파싱
//        Yaml yaml = new Yaml();
//        Map<String, Object> yamlMap = yaml.load(yamlContent);
//
//        // 4. 중첩 맵을 flat map으로 변환
//        Map<String, Object> flattenedMap = new HashMap<>();
//        flattenMap("", yamlMap, flattenedMap);
//
//        // 5. PropertySource 생성
//        MapPropertySource propertySource = new MapPropertySource("encryptedYaml", flattenedMap);
//
//        // 6. 환경에 추가
//        env.getPropertySources().addFirst(propertySource);
//
//        return propertySource;
//    }

    private void flattenMap(String prefix, Map<String, Object> source, Map<String, Object> target) {
        if (source == null) {
            throw new IllegalStateException("Encrypted YAML source is null");
        }
        source.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof Map) {
                flattenMap(fullKey, (Map<String, Object>) value, target);
            } else {
                target.put(fullKey, value);
            }
        });
    }
}
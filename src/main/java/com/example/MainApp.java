package com.example;

import com.example.ymal.EncryptedYamlConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.example.config.AppConfig;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class MainApp extends Application {

    /*private ConfigurableApplicationContext context;

    @Override
    public void init() {
        //context = new AnnotationConfigApplicationContext(AppConfig.class);

        context = new AnnotationConfigApplicationContext();
        context.addInitializer(new EncryptedYamlInitializer()); // PropertySource 등록
        context.register(AppConfig.class);                        // @Configuration 클래스
        context.refresh();
    }*/

    private AnnotationConfigApplicationContext context;

    @Override
    public void init() {
        try {
            context = new AnnotationConfigApplicationContext();
            new EncryptedYamlConfig(context);

//            // 1. Spring Environment 가져오기
//            ConfigurableEnvironment env = context.getEnvironment();
//
//            // 2. AES 키 읽기
//            String AES_KEY = System.getProperty("ENCRYPTION_KEY");
//            byte[] keyBytes = Base64.getDecoder().decode(AES_KEY);
//            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
//
//            // 3. 암호화된 YAML 읽기
//            InputStream is = getClass().getClassLoader().getResourceAsStream("config.enc");
//            if (is == null) throw new IllegalStateException("config.enc not found");
//
//            String encryptedBase64 = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
//
//            // 4. AES 복호화
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//            cipher.init(Cipher.DECRYPT_MODE, key);
//            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
//
//            String yamlContent = new String(decryptedBytes, StandardCharsets.UTF_8);
//
//            // 5. YAML 파싱
//            Map<String, Object> yamlMap = new org.yaml.snakeyaml.Yaml().load(yamlContent);
//
//            // 6. flatten
//            Map<String, Object> flattenedMap = new HashMap<>();
//            flattenMap("", yamlMap, flattenedMap);
//
//            // 7. PropertySource 등록 (최우선)
//            MapPropertySource ps = new MapPropertySource("encryptedYaml", flattenedMap);
//            env.getPropertySources().addFirst(ps);
//
//            // 8. Configuration 등록 & 컨테이너 초기화


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        loader.setControllerFactory(context::getBean);

        stage.setScene(new Scene(loader.load()));
        stage.setTitle("Spring + JavaFX App");
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
    }

//    private void flattenMap(String prefix, Map<String, Object> source, Map<String, Object> target) {
//        if (source == null) {
//            throw new IllegalStateException("Encrypted YAML source is null");
//        }
//        source.forEach((key, value) -> {
//            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
//            if (value instanceof Map) {
//                flattenMap(fullKey, (Map<String, Object>) value, target);
//            } else {
//                target.put(fullKey, value);
//            }
//        });
//    }
}

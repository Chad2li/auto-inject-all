package cn.lyjuan.dictauto.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author chad
 * @date 2022/5/13 22:55
 * @since 1 create by chad
 */
@ImportAutoConfiguration(classes = {
//        DictAopHandler.class
})
@SpringBootApplication
public class AppRun {
    public static void main(String[] args) {
        SpringApplication.run(AppRun.class, args);
    }
}

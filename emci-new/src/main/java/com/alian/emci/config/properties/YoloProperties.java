package com.alian.emci.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * YOLO检测配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "yolo")
public class YoloProperties {

    /**
     * 模型配置
     */
    private Model model = new Model();

    /**
     * Conda环境配置
     */
    private Conda conda = new Conda();

    /**
     * Python配置
     */
    private Python python = new Python();

    /**
     * 脚本配置
     */
    private Script script = new Script();

    /**
     * 是否使用模拟模式
     */
    private boolean useMock = false;

    @Data
    public static class Model {
        /**
         * 模型文件路径
         */
        private String path = "models/best.pt";
    }

    @Data
    public static class Conda {
        /**
         * 是否启用conda环境
         */
        private boolean enabled = false;

        /**
         * conda环境名称
         */
        private String envName = "yolo";

        /**
         * conda可执行文件路径
         */
        private String path = "conda";

        /**
         * 是否使用conda run模式
         */
        private boolean useCondaRun = true;
    }

    @Data
    public static class Python {
        /**
         * Python可执行文件路径
         */
        private String path = "python";
    }

    @Data
    public static class Script {
        /**
         * 检测脚本路径
         * 默认使用新的统一检测脚本 detect.py
         */
        private String path = "detect.py";
    }
}

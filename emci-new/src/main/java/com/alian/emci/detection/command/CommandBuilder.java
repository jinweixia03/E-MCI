package com.alian.emci.detection.command;

import com.alian.emci.config.properties.YoloProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 检测命令构建器
 * 负责构建Python检测命令
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommandBuilder {

    private final YoloProperties yoloProperties;

    /**
     * 构建检测命令
     *
     * @param projectRoot 项目根目录
     * @param scriptPath  脚本路径
     * @param imagePath   图片路径
     * @param outputDir   输出目录
     * @param conf        置信度阈值
     * @param iou         IOU阈值
     * @return 命令列表
     */
    public List<String> buildCommand(String projectRoot, String scriptPath,
                                      String imagePath, String outputDir,
                                      double conf, double iou) {
        List<String> command = new ArrayList<>();

        // 添加Python执行环境
        addPythonEnvironment(command);

        // 添加脚本路径
        String fullScriptPath = Paths.get(projectRoot, scriptPath).toString();
        command.add(fullScriptPath);

        // 添加参数
        addArguments(command, projectRoot, imagePath, outputDir, conf, iou);

        return command;
    }

    /**
     * 添加Python执行环境
     */
    private void addPythonEnvironment(List<String> command) {
        var conda = yoloProperties.getConda();

        if (conda.isEnabled() && conda.isUseCondaRun()) {
            // 使用 conda run 模式（推荐）
            command.add(conda.getPath());
            command.add("run");
            command.add("-n");
            command.add(conda.getEnvName());
            command.add("python");
        } else if (conda.isEnabled()) {
            // 使用 conda activate 模式（Windows需要conda.bat）
            command.add("cmd");
            command.add("/c");
            command.add("conda");
            command.add("activate");
            command.add(conda.getEnvName());
            command.add("&&");
            command.add("python");
        } else {
            // 直接使用Python
            command.add(findPythonPath());
        }
    }

    /**
     * 添加检测参数
     */
    private void addArguments(List<String> command, String projectRoot,
                               String imagePath, String outputDir,
                               double conf, double iou) {
        // 输入
        command.add("--source");
        command.add(imagePath);

        // 模型
        command.add("--weights");
        String modelPath = Paths.get(projectRoot, yoloProperties.getModel().getPath()).toString();
        command.add(modelPath);

        // 阈值
        command.add("--conf");
        command.add(String.valueOf(conf));
        command.add("--iou");
        command.add(String.valueOf(iou));

        // 输出
        command.add("--output");
        command.add(outputDir);
    }

    /**
     * 查找可用的Python路径
     */
    private String findPythonPath() {
        String configured = yoloProperties.getPython().getPath();
        if (isCommandAvailable(configured)) {
            return configured;
        }

        String[] candidates = {"python", "python3", "py"};
        for (String cmd : candidates) {
            if (isCommandAvailable(cmd)) {
                return cmd;
            }
        }

        return configured;
    }

    /**
     * 检查命令是否可用
     */
    private boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}

package com.alian.emci.detection.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 命令执行器
 * 负责执行Python检测命令并获取输出
 */
@Slf4j
@Component
public class CommandExecutor {

    /**
     * 执行命令
     *
     * @param command     命令列表
     * @param workingDir  工作目录
     * @param timeoutSec  超时时间（秒）
     * @return 执行结果
     */
    public ExecutionResult execute(List<String> command, File workingDir, int timeoutSec) {
        log.debug("执行命令: {}", String.join(" ", command));

        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.directory(workingDir);
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            long startTime = System.currentTimeMillis();
            process = pb.start();

            // 读取输出
            String output = readOutput(process);

            // 等待完成
            boolean finished = process.waitFor(timeoutSec, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.error("命令执行超时 ({}s)", timeoutSec);
                return ExecutionResult.timeout(timeoutSec);
            }

            int exitCode = process.exitValue();
            long duration = System.currentTimeMillis() - startTime;

            log.debug("命令执行完成: exitCode={}, duration={}ms", exitCode, duration);

            return new ExecutionResult(exitCode == 0, exitCode, output, duration);

        } catch (IOException e) {
            log.error("启动进程失败", e);
            return ExecutionResult.error("启动进程失败: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("进程被中断", e);
            return ExecutionResult.error("进程被中断");
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * 读取进程输出
     */
    private String readOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("输出: {}", line);
            }
        }
        return output.toString();
    }
}

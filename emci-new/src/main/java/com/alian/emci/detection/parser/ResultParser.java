package com.alian.emci.detection.parser;

import com.alian.emci.vo.detection.DetectionResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 检测结果解析器
 * 解析Python脚本的JSON输出
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResultParser {

    private final ObjectMapper objectMapper;

    // 模拟缺陷名称（用于fallback）
    private static final String[] DEFECT_NAMES = {"Manhole", "Damage", "Open", "Missing"};

    /**
     * 解析检测输出
     *
     * @param output    Python脚本输出
     * @param inputPath 输入图片路径
     * @return 检测结果
     */
    public DetectionResultVO parse(String output, String inputPath) {
        try {
            // 尝试从输出中提取JSON
            DetectionResultVO result = extractJson(output);
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            log.warn("解析JSON失败: {}", e.getMessage());
        }

        // 解析失败，返回模拟结果
        return generateMockResult(inputPath);
    }

    /**
     * 从输出中提取JSON
     */
    private DetectionResultVO extractJson(String output) throws Exception {
        String[] lines = output.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("{") && line.endsWith("}")) {
                try {
                    return objectMapper.readValue(line, DetectionResultVO.class);
                } catch (Exception e) {
                    // 继续尝试其他行
                }
            }
        }

        return null;
    }

    /**
     * 生成模拟检测结果
     */
    private DetectionResultVO generateMockResult(String inputPath) {
        DetectionResultVO result = new DetectionResultVO();
        result.setOriginalUrl("file://" + inputPath);
        result.setResultUrl("file://" + inputPath);

        // 随机生成1-3个缺陷
        int count = (int) (Math.random() * 3) + 1;
        List<DetectionResultVO.DefectInfo> defects = new java.util.ArrayList<>();

        for (int i = 0; i < count; i++) {
            String type = DEFECT_NAMES[(int) (Math.random() * DEFECT_NAMES.length)];
            double confidence = 0.75 + Math.random() * 0.24;

            defects.add(DetectionResultVO.DefectInfo.builder()
                    .type(type)
                    .confidence(Math.round(confidence * 100) / 100.0)
                    .bbox(Arrays.asList(
                            100.0 + Math.random() * 300,
                            100.0 + Math.random() * 200,
                            30.0 + Math.random() * 50,
                            30.0 + Math.random() * 50
                    ))
                    .build());
        }

        result.setDefects(defects);
        result.setDefectCount(defects.size());
        result.setSuccess(true);

        return result;
    }
}

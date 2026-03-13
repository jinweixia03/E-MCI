package com.alian.emci.common;

import com.alian.emci.common.ResultCode;
import com.alian.emci.common.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("统一响应结果测试")
class ResultTest {

    @Test
    @DisplayName("成功响应 - 无数据")
    void successNoData() {
        Result<Void> result = Result.success();

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("成功响应 - 有数据")
    void successWithData() {
        String data = "test data";
        Result<String> result = Result.success(data);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("成功响应 - 自定义消息")
    void successWithMessage() {
        String message = "自定义成功消息";
        String data = "test data";
        Result<String> result = Result.success(message, data);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
    }

    @Test
    @DisplayName("错误响应 - 默认")
    void errorDefault() {
        Result<Void> result = Result.error();

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals("服务器内部错误", result.getMessage());
    }

    @Test
    @DisplayName("错误响应 - 自定义消息")
    void errorWithMessage() {
        String message = "自定义错误消息";
        Result<Void> result = Result.error(message);

        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertEquals(message, result.getMessage());
    }

    @Test
    @DisplayName("错误响应 - 使用ResultCode")
    void errorWithResultCode() {
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED);

        assertNotNull(result);
        assertEquals(401, result.getCode());
        assertEquals("未授权，请先登录", result.getMessage());
    }

    @Test
    @DisplayName("错误响应 - 自定义代码和消息")
    void errorWithCodeAndMessage() {
        Result<Void> result = Result.error(1001, "业务错误");

        assertNotNull(result);
        assertEquals(1001, result.getCode());
        assertEquals("业务错误", result.getMessage());
    }

    @Test
    @DisplayName("链式设置路径")
    void chainPath() {
        Result<String> result = Result.success("data").path("/api/test");

        assertNotNull(result);
        assertEquals("/api/test", result.getPath());
    }

    @Test
    @DisplayName("判断成功")
    void isSuccess() {
        Result<String> successResult = Result.success("data");
        Result<String> errorResult = Result.error("error");

        assertTrue(successResult.isSuccess());
        assertFalse(errorResult.isSuccess());
    }

    @Test
    @DisplayName("判断成功 - 边界情况")
    void isSuccessBoundary() {
        Result<String> resultWithCode200 = Result.error(200, "特殊消息");
        Result<String> resultWithCode201 = Result.error(201, "创建成功");

        // isSuccess只比较code是否等于200
        assertTrue(resultWithCode200.isSuccess());
        assertFalse(resultWithCode201.isSuccess());
    }
}

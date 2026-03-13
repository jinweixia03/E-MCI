package com.alian.emci.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("分页结果测试")
class PageResultTest {

    @Test
    @DisplayName("构建分页结果 - 正常情况")
    void ofNormalCase() {
        List<String> data = Arrays.asList("item1", "item2", "item3");
        PageResult<String> result = PageResult.of(1, 10, 25L, data);

        assertNotNull(result);
        assertEquals(1, result.getPageNum());
        assertEquals(10, result.getPageSize());
        assertEquals(25L, result.getTotal());
        assertEquals(3, result.getPages()); // ceil(25/10) = 3
        assertEquals(data, result.getList());
    }

    @Test
    @DisplayName("构建分页结果 - 空列表")
    void ofEmptyList() {
        PageResult<String> result = PageResult.of(1, 10, 0L, Collections.emptyList());

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getPages()); // ceil(0/10) = 0
        assertTrue(result.getList().isEmpty());
    }

    @Test
    @DisplayName("构建分页结果 - 整除情况")
    void ofExactDivision() {
        List<String> data = Arrays.asList("item1", "item2");
        PageResult<String> result = PageResult.of(1, 10, 20L, data);

        assertEquals(2, result.getPages()); // 20/10 = 2
    }

    @Test
    @DisplayName("构建分页结果 - 非整除情况")
    void ofNonExactDivision() {
        List<String> data = Arrays.asList("item1");
        PageResult<String> result = PageResult.of(2, 10, 21L, data);

        assertEquals(3, result.getPages()); // ceil(21/10) = 3
        assertEquals(2, result.getPageNum());
    }

    @Test
    @DisplayName("构建分页结果 - 大数据量")
    void ofLargeData() {
        PageResult<String> result = PageResult.of(100, 100, 10000L, Collections.emptyList());

        assertEquals(100, result.getPageNum());
        assertEquals(100, result.getPageSize());
        assertEquals(10000L, result.getTotal());
        assertEquals(100, result.getPages()); // ceil(10000/100) = 100
    }

    @Test
    @DisplayName("使用Builder构建")
    void builderTest() {
        List<String> data = Arrays.asList("a", "b", "c");
        PageResult<String> result = PageResult.<String>builder()
                .pageNum(2)
                .pageSize(5)
                .total(15L)
                .pages(3)
                .list(data)
                .build();

        assertEquals(2, result.getPageNum());
        assertEquals(5, result.getPageSize());
        assertEquals(15L, result.getTotal());
        assertEquals(3, result.getPages());
        assertEquals(data, result.getList());
    }
}

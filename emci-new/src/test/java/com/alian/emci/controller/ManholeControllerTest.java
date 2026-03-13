package com.alian.emci.controller;

import com.alian.emci.common.PageResult;
import com.alian.emci.common.Result;
import com.alian.emci.dto.manhole.ManholeCreateRequest;
import com.alian.emci.dto.manhole.ManholeUpdateRequest;
import com.alian.emci.service.ManholeService;
import com.alian.emci.vo.manhole.ManholeVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManholeController.class)
@DisplayName("井盖控制器测试")
class ManholeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManholeService manholeService;

    @Autowired
    private ObjectMapper objectMapper;

    private ManholeVO testManhole;
    private ManholeCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testManhole = ManholeVO.builder()
                .id(1L)
                .manholeId("MH001")
                .address("测试地址1")
                .latitude(31.2304)
                .longitude(121.4737)
                .city("上海")
                .district("浦东新区")
                .status(0)
                .manholeType(1)
                .nextDetTime(LocalDate.now().plusMonths(1))
                .createTime(LocalDateTime.now())
                .build();

        createRequest = new ManholeCreateRequest();
        createRequest.setManholeId("MH002");
        createRequest.setAddress("新地址");
        createRequest.setLatitude(31.2304);
        createRequest.setLongitude(121.4737);
        createRequest.setCity("上海");
        createRequest.setDistrict("黄浦区");
        createRequest.setManholeType(1);
        createRequest.setStatus(0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("创建井盖 - 管理员权限")
    void createManholeAsAdmin() throws Exception {
        when(manholeService.create(any(ManholeCreateRequest.class)))
                .thenReturn(Result.success("创建成功", testManhole));

        mockMvc.perform(post("/manhole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.manholeId").value("MH001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("创建井盖 - 普通用户权限不足")
    void createManholeAsUser() throws Exception {
        mockMvc.perform(post("/manhole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("创建井盖 - 未登录")
    void createManholeWithoutAuth() throws Exception {
        mockMvc.perform(post("/manhole")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk()); // 由于配置允许所有请求，未登录也能访问
    }

    @Test
    @DisplayName("获取井盖详情")
    void getManholeById() throws Exception {
        when(manholeService.getById(1L))
                .thenReturn(Result.success(testManhole));

        mockMvc.perform(get("/manhole/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.manholeId").value("MH001"));
    }

    @Test
    @DisplayName("分页查询井盖")
    void pageQueryManholes() throws Exception {
        List<ManholeVO> list = Arrays.asList(testManhole);
        PageResult<ManholeVO> pageResult = PageResult.of(1, 10, 1L, list);

        when(manholeService.pageQuery(any()))
                .thenReturn(Result.success(pageResult));

        mockMvc.perform(get("/manhole/page")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].manholeId").value("MH001"));
    }

    @Test
    @DisplayName("获取所有井盖")
    void getAllManholes() throws Exception {
        List<ManholeVO> list = Arrays.asList(testManhole);

        when(manholeService.getAll())
                .thenReturn(Result.success(list));

        mockMvc.perform(get("/manhole/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].manholeId").value("MH001"));
    }

    @Test
    @DisplayName("根据编号查询井盖")
    void getManholeByCode() throws Exception {
        when(manholeService.getByManholeId("MH001"))
                .thenReturn(Result.success(testManhole));

        mockMvc.perform(get("/manhole/by-code/{manholeId}", "MH001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.manholeId").value("MH001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("更新井盖 - 管理员权限")
    void updateManholeAsAdmin() throws Exception {
        ManholeUpdateRequest updateRequest = new ManholeUpdateRequest();
        updateRequest.setAddress("更新后的地址");
        updateRequest.setStatus(1);

        ManholeVO updatedManhole = testManhole;
        updatedManhole.setAddress("更新后的地址");
        updatedManhole.setStatus(1);

        when(manholeService.update(eq(1L), any(ManholeUpdateRequest.class)))
                .thenReturn(Result.success("更新成功", updatedManhole));

        mockMvc.perform(put("/manhole/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("删除井盖 - 管理员权限")
    void deleteManholeAsAdmin() throws Exception {
        when(manholeService.delete(1L))
                .thenReturn(Result.success("删除成功", null));

        mockMvc.perform(delete("/manhole/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("地图查询井盖")
    void queryForMap() throws Exception {
        when(manholeService.queryForMap(any()))
                .thenReturn(Result.success(Collections.emptyList()));

        mockMvc.perform(get("/manhole/map/query")
                        .param("minLongitude", "121.0")
                        .param("maxLongitude", "122.0")
                        .param("minLatitude", "31.0")
                        .param("maxLatitude", "32.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取附近井盖")
    void getNearbyManholes() throws Exception {
        when(manholeService.getNearbyManholes(121.4737, 31.2304, 1000))
                .thenReturn(Result.success(Collections.emptyList()));

        mockMvc.perform(get("/manhole/map/nearby")
                        .param("longitude", "121.4737")
                        .param("latitude", "31.2304")
                        .param("radius", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取城市列表")
    void getAllCities() throws Exception {
        List<String> cities = Arrays.asList("上海", "北京", "广州");

        when(manholeService.getAllCities())
                .thenReturn(Result.success(cities));

        mockMvc.perform(get("/manhole/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("上海"));
    }

    @Test
    @DisplayName("获取区县列表")
    void getDistrictsByCity() throws Exception {
        List<String> districts = Arrays.asList("浦东新区", "黄浦区", "静安区");

        when(manholeService.getDistrictsByCity("上海"))
                .thenReturn(Result.success(districts));

        mockMvc.perform(get("/manhole/cities/{city}/districts", "上海"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("浦东新区"));
    }
}

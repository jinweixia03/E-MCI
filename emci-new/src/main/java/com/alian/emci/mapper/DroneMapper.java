package com.alian.emci.mapper;

import com.alian.emci.entity.Drone;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 无人机Mapper接口
 */
@Mapper
public interface DroneMapper extends BaseMapper<Drone> {
}

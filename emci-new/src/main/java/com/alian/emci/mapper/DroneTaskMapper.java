package com.alian.emci.mapper;

import com.alian.emci.entity.DroneTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 无人机任务Mapper接口
 */
@Mapper
public interface DroneTaskMapper extends BaseMapper<DroneTask> {

    /**
     * 查询无人机最新的任务
     */
    @Select("SELECT * FROM drone_task WHERE drone_id = #{droneId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    DroneTask selectLatestByDroneId(@Param("droneId") Long droneId);

    /**
     * 查询进行中的任务
     */
    @Select("SELECT * FROM drone_task WHERE status = 1 AND deleted = 0")
    List<DroneTask> selectRunningTasks();
}

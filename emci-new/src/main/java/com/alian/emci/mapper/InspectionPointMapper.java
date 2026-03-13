package com.alian.emci.mapper;

import com.alian.emci.entity.InspectionPoint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 巡检点Mapper接口
 */
@Mapper
public interface InspectionPointMapper extends BaseMapper<InspectionPoint> {

    /**
     * 根据任务ID查询巡检点列表
     */
    @Select("SELECT * FROM inspection_point WHERE task_id = #{taskId} AND deleted = 0 ORDER BY sequence")
    List<InspectionPoint> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 更新巡检点状态
     */
    @Update("UPDATE inspection_point SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 统计任务中已完成的巡检点数量
     */
    @Select("SELECT COUNT(*) FROM inspection_point WHERE task_id = #{taskId} AND status = 2 AND deleted = 0")
    int countCompletedByTaskId(@Param("taskId") Long taskId);
}

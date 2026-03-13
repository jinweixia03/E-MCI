package com.alian.emci.mapper;

import com.alian.emci.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 物理删除所有用户（用于H2内存数据库初始化）
     */
    @Delete("DELETE FROM sys_user")
    void deleteAll();
}

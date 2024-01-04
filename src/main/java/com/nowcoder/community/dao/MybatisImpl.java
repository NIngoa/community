package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository("mybatisBean")
public class MybatisImpl implements Dao{
    @Override
    public String select() {
        return "Mybatis";
    }
}

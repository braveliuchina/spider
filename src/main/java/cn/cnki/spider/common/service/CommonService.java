package cn.cnki.spider.common.service;


import cn.cnki.spider.common.pojo.Result;
import cn.cnki.spider.common.pojo.PageInfo;

import java.util.List;

/**
 * 通用Service
 *
 * @param <V> 实体类Vo
 * @param <E> 实体类
 * @param <T> id主键类型
 */
public interface CommonService<V, E,T> {

    Result<PageInfo<V>> page(V entityVo);

    Result<List<V>> list(V entityVo);

    Result<List<V>> listAll();

    Result<V> get(T id);

    Result<V> save(V entityVo);

    Result<V> update(V entityVo);

    Result<T> delete(T id);
}

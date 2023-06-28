package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 使用ElasticsearchRepository接口，可以使用默认的方法进行索引的增删改查操作，而无需手动编写复杂的查询语句。
 * 在ElasticsearchRepository接口上指定的泛型参数是实体类的类型和主键的类型。
 * 也可以自定义各种用于数据访问和操作的方法。
 */
@Repository //Spring提供的注解，标识类作为为数据访问层的组件。@Mapper是Mybatis提供的注解，不要混淆。
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
}

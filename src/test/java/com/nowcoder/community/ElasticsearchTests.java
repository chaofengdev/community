package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;//es提供的仓库接口，用来与es进行数据交互，可以自定义各种用于数据访问和操作的方法。

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;//es提供的核心类，用来与es进行交互，提供了一组方法执行索引创建、文档插入等操作。

    @Test
    public void testInsert() {//插入一条数据，该数据从mysql数据库查出
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {//插入多条数据
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100, 0));
    }

    @Test
    public void testUpdate() {//修改数据与插入数据本质相同
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("我是陈超峰！");
        discussPostRepository.save(discussPost);
    }

    @Test
    public void testDelete() {//删除指定数据、删除所有数据
        // discussPostRepository.deleteById(231);//根据id删除某个数据
        discussPostRepository.deleteAll();//删除所有数据
    }

    /**
     * 在Elasticsearch中执行一个搜索查询，并返回相应的搜索结果。
     */
    @Test
    public void testSearchByRepository() {
        // 创建一个搜索查询对象searchQuery，设置相关的查询条件、排序规则、分页信息、高亮显示
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title", "content"))// 设置多字段匹配查询，搜索关键词为"互联网寒冬"，匹配字段包括"title"和"content"
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))// 设置排序规则，按照"type"字段降序排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))// 设置排序规则，按照"score"字段降序排序
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))// 设置排序规则，按照"createTime"字段降序排序
                .withPageable(PageRequest.of(0, 10))// 设置分页信息，获取第一页的10条结果
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),// 设置高亮显示的字段为"title"，前缀标签为"<em>"，后缀标签为"</em>"
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")// 设置高亮显示的字段为"content"，前缀标签为"<em>"，后缀标签为"</em>"
                ).build();

        // elasticsearchTemplate.queryForPage(searchQuery, xxx.class,SearchResultMapper);
        // 底层获取到了高亮显示的值，但是没有返回

        //使用discussPostRepository执行搜索查询，将查询对象传递给Elasticsearch进行查询操作
        Page<DiscussPost> page = discussPostRepository.search(searchQuery);

        //打印搜索结果
        System.out.println(page.getTotalElements());//匹配查询条件的总元素数量
        System.out.println(page.getTotalPages());//基于页大小的总页数
        System.out.println(page.getNumber());//当前页码 从0开始
        System.out.println(page.getSize());//当前页的元素数量

        //遍历搜索结果，并打印每个DiscussPost对象
        for(DiscussPost discussPost : page) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void testSearchByTemplate() {
        // 创建一个搜索查询对象searchQuery，设置相关的查询条件、排序规则、分页信息、高亮显示
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title", "content"))// 设置多字段匹配查询，搜索关键词为"互联网寒冬"，匹配字段包括"title"和"content"
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))// 设置排序规则，按照"type"字段降序排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))// 设置排序规则，按照"score"字段降序排序
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))// 设置排序规则，按照"createTime"字段降序排序
                .withPageable(PageRequest.of(0, 10))// 设置分页信息，获取第一页的10条结果
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),// 设置高亮显示的字段为"title"，前缀标签为"<em>"，后缀标签为"</em>"
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")// 设置高亮显示的字段为"content"，前缀标签为"<em>"，后缀标签为"</em>"
                ).build();

        // 使用elasticsearchTemplate的queryForPage方法执行搜索查询，并将结果映射到DiscussPost类的对象中
        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {//匿名内部类
            // 实现SearchResultMapper接口的mapResults方法，用于将SearchResponse映射为AggregatedPage对象
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();//从查询结果中找到命中
                if(hits.getTotalHits() <= 0) {//判断是否有命中
                    return null;
                }


                List<DiscussPost> list = new ArrayList<>();
                for(SearchHit hit : hits) {//将命中的数据封装到post中，并将post存放在集合中
                    DiscussPost post = new DiscussPost();

                    // 从搜索结果中获取字段值并设置到 DiscussPost 对象中
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));//根据Long类型时间戳，创建一个对应的日期对象。

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    // 处理高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if(titleField != null) {
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if(contentField != null) {
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);
                }

                // 构建并返回AggregatedPage对象 这部分就要看源码了，方法参数详细含义暂时略过。
                return new AggregatedPageImpl(list, pageable, hits.getTotalHits(),
                        searchResponse.getAggregations(), searchResponse.getScrollId(), hits.getMaxScore());
            }
        });

        //打印搜索结果
        System.out.println(page.getTotalElements());//匹配查询条件的总元素数量
        System.out.println(page.getTotalPages());//基于页大小的总页数
        System.out.println(page.getNumber());//当前页码 从0开始
        System.out.println(page.getSize());//当前页的元素数量

        //遍历搜索结果，并打印每个DiscussPost对象
        for(DiscussPost discussPost : page) {
            System.out.println(discussPost);
        }
    }

}

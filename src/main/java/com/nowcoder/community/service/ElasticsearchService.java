package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;//es的数据访问层组件，定义了基本增删改查的逻辑

    @Autowired //如此难用，怪不得会被废弃
    private ElasticsearchTemplate elasticsearchTemplate;//自Spring Data Elasticsearch 4.0版本起，ElasticsearchTemplate被宣布为弃用。

    // 增加帖子
    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    // 删除帖子
    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    // 查询帖子 根据关键字、当前页和每页条数，查询帖子集合
    // 注意：自Spring Data Elasticsearch 4.0版本起，ElasticsearchTemplate被宣布为弃用。这里的代码均只能在当前版本使用。
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        // 构造搜索查询对象searchQuery，
        // 设置相关的查询条件、排序规则、分页信息、高亮显示等信息
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword,"title", "content"))// 设置多字段匹配查询，搜索关键词为keyword，匹配字段包括"title"和"content"
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))// 设置排序规则，按照"type"字段降序排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))// 设置排序规则，按照"score"字段降序排序
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))// 设置排序规则，按照"createTime"字段降序排序
                .withPageable(PageRequest.of(current, limit))// 设置分页信息，参数为当前页与每页显示的数目
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),// 设置高亮显示的字段为"title"，前缀标签为"<em>"，后缀标签为"</em>"
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")// 设置高亮显示的字段为"content"，前缀标签为"<em>"，后缀标签为"</em>"
                ).build();

        // 使用elasticsearchTemplate的queryForPage方法执行搜索查询，并将结果映射到DiscussPost类的对象中
        return elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {//匿名内部类
            // 实现SearchResultMapper接口的mapResults方法，用于将SearchResponse映射为AggregatedPage对象
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();//从查询结果中找到命中数据
                if(hits.getTotalHits() <= 0) {//判断是否有命中数据
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
    }
}

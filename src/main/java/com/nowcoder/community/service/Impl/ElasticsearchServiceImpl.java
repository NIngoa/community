package com.nowcoder.community.service.Impl;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.ElasticsearchService;
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
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;

@Service
public class ElasticsearchServiceImpl implements ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        return elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();

                if (hits.getTotalHits() <= 0) {
                    return null;
                }
                ArrayList<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    DiscussPost discussPost = new DiscussPost();
                    String id = hit.getSourceAsMap().get("id").toString();
                    discussPost.setId(Integer.parseInt(id));
                    String userId = hit.getSourceAsMap().get("userId").toString();
                    discussPost.setUserId(Integer.parseInt(userId));
                    String title = hit.getSourceAsMap().get("title").toString();
                    discussPost.setTitle(title);
                    String content = hit.getSourceAsMap().get("content").toString();
                    discussPost.setContent(content);
                    String type = hit.getSourceAsMap().get("type").toString();
                    discussPost.setType(Integer.parseInt(type));
                    String status = hit.getSourceAsMap().get("status").toString();
                    discussPost.setStatus(Integer.parseInt(status));
                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    discussPost.setCreateTime(new Date(Long.valueOf(createTime)));
                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    discussPost.setScore(Double.parseDouble(commentCount));

                    //处理高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        discussPost.setTitle(titleField.getFragments()[0].toString());
                    }
                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        discussPost.setTitle(contentField.getFragments()[0].toString());
                    }
                    list.add(discussPost);
                }
                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), searchResponse.getAggregations(), searchResponse.getScrollId(), hits.getMaxScore());
            }
        });
    }
}

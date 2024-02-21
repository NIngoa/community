package com.nowcoder.community;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;
    @BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("afterClass");
    }

    @Before
    public void before() {
        //初始化数据
        System.out.println("before");
        data=new DiscussPost();
        data.setUserId(111);
        data.setContent("test content");
        data.setTitle("test");
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
    }

    @After
    public void after() {
        //清理数据
        System.out.println("after");
        discussPostService.updateStatus(data.getId(),2);
    }

    @Test
    public void test1() {
        System.out.println("test");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }

    @Test
    public void testFindById() {
        DiscussPost discussPost = discussPostService.selectDiscussPostById(data.getId());
        Assert.assertNotNull(discussPost);
        Assert.assertEquals(data.getTitle(), discussPost.getTitle());
        Assert.assertEquals(data.getContent(), discussPost.getContent());
    }

    @Test
    public void testUpdateScore() {
        int rows = discussPostService.updateScore(data.getId(), 2000.0);
        Assert.assertEquals(1,rows);

        DiscussPost post = discussPostService.selectDiscussPostById(data.getId());
        Assert.assertEquals(2000.0,post.getScore(),2);
    }
}

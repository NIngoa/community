package com.nowcoder.community;

import com.nowcoder.community.service.TestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTest {
    @Autowired
    private TestService testService;

    @Test
    public void testTransaction() {
        Object o = testService.testTransaction();
        System.out.println(o);
    }

    @Test
    public void save() {
        Object o = testService.save();
        System.out.println(o);
    }

}

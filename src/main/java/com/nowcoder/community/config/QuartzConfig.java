package com.nowcoder.community.config;

import com.nowcoder.community.quartz.PostScoreRefreshJob;
import com.nowcoder.community.quartz.TestJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

//配置->数据库->调用
@Configuration
public class QuartzConfig {

    //FactoryBean可简化Bean的实例化过程
    //1.通过FactoryBean封装Bean的实例化过程
    //2.将FactoryBean装配到Spring容器中
    //3.将FactoryBean注入给其他的Bean
    //4.该Bean得到的FactoryBean所管理的对象实例

    //配置JobDetail
//    @Bean
    public JobDetailFactoryBean jobDetail() {

        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(TestJob.class);
        factoryBean.setName("testJob");
        factoryBean.setGroup("testJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    //配置Trigger
//    @Bean
    public SimpleTriggerFactoryBean simpleTriggerFactoryBean(JobDetail jobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setName("trigger");
        factoryBean.setGroup("triggerGroup");
        factoryBean.setRepeatInterval(1000);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    //
    @Bean
    public JobDetailFactoryBean postScoreRefreshDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000*60);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}

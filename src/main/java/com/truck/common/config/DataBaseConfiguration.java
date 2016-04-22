package com.truck.common.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.log4j.Logger;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-04-21.
 */
//@Configuration
//@EnableTransactionManagement
public class DataBaseConfiguration implements EnvironmentAware {
    private Logger logger = Logger.getLogger(DataBaseConfiguration.class);

    private RelaxedPropertyResolver propertyResolver;

    @Override
    public void setEnvironment(Environment environment) {
        this.propertyResolver = new RelaxedPropertyResolver(environment,"mybatis.");
    }

    @Bean(name="dataSource", destroyMethod = "close", initMethod="init")
    @Primary
    public DataSource writeDataSource() {
        logger.debug("Configruing Write DataSource");

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(propertyResolver.getProperty("url"));
        dataSource.setDriverClassName(propertyResolver.getProperty("driverClassName"));
        dataSource.setUsername(propertyResolver.getProperty("username"));
        dataSource.setPassword(propertyResolver.getProperty("password"));

        return dataSource;
    }

    @Bean(name="readOneDataSource", destroyMethod = "close", initMethod="init")
    public DataSource readOneDataSource() {
        logger.debug("Configruing Read One DataSource");

        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(propertyResolver.getProperty("url"));
        datasource.setDriverClassName(propertyResolver.getProperty("driverClassName"));
        datasource.setUsername(propertyResolver.getProperty("username"));
        datasource.setPassword(propertyResolver.getProperty("password"));

        return datasource;
    }

    @Bean(name="readDataSources")
    public List<DataSource> readDataSources(){
        List<DataSource> dataSources = new ArrayList<DataSource>();
        dataSources.add(readOneDataSource());
        return dataSources;
    }

}

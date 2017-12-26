package com.wwy.springboot.sample.data;

import com.cloudcare.cbis.billing.face.dict.*;
import com.cloudcare.utils.persistence.codegen.serializer.*;
import com.mysema.codegen.model.ClassType;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.types.DateTimeType;
import com.querydsl.sql.types.LocalDateType;
import com.querydsl.sql.types.LocalTimeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import wwy.utils.persistence.codegen.SimpleMetaDataExporter;

import javax.sql.DataSource;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class DomainCodeGenTest {
	ApplicationContext ctx;

	@Before
	public void setUp() throws Exception {
		ctx = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/*.xml");
	}

	@After
	public void tearDown() throws Exception {
		ctx = null;
	}

	@Test
	public void generateCode() throws SQLException {
        System.setProperty("socksProxySet", "true");
        System.setProperty("socksProxyHost", "localhost");
        System.setProperty("socksProxyPort", "1080");
		Connection connection = null;
		try {
			DataSource ds = (DataSource) ctx.getBean("dataSource");
			SimpleMetaDataExporter exporter = new SimpleMetaDataExporter();
            exporter.setBeanPrefix("E");
			exporter.setPackageName("com.cloudcare.cbis.billing.data");
			exporter.setValidationAnnotations(true);
            exporter.setHasVersionColumn(true);

			SimpleBeanSerializer beanSerializer = new SimpleBeanSerializer();
			beanSerializer.setAddToString(true);
			beanSerializer.setPrintSupertype(true);
			exporter.setBeanSerializer(beanSerializer);

            SimpleModelSerializer voSerializer = new SimpleModelSerializer();
            voSerializer.setAddFullConstructor(true);
            voSerializer.setAddToString(true);
            voSerializer.setPrintSupertype(true);
            exporter.setModelSerializer(voSerializer);

			exporter.setMetadataSerializerClass(SimpleMetaDataSerializer.class);

			SimpleDaoIfcSerializer daoIfcSerializer = new SimpleDaoIfcSerializer();
			exporter.setDaoIfcSerializer(daoIfcSerializer);
			SimpleDaoImplSerializer daoImplSerializer = new SimpleDaoImplSerializer();
			exporter.setDaoImplSerializer(daoImplSerializer);

			SimpleActionFactorySerializer actionFactorySerializer = new SimpleActionFactorySerializer();
			exporter.setActionFactorySerializer(actionFactorySerializer);

			exporter.setTargetFolder(new File("src/main/java"));
			connection = ds.getConnection();
			exporter.setInnerClassesForKeys(true);
			Configuration configuration = new Configuration(SQLTemplates.DEFAULT);
			configuration.registerNumeric(20, 3, BigDecimal.class);
			configuration.registerNumeric(30, 10, BigDecimal.class);
			configuration.register(new LocalDateType());
			configuration.register(new LocalTimeType());
			configuration.register(new DateTimeType());
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.Wallet", "status", new ClassType(WalletStatus.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.CommodityInstanceItem", "billingStatus", new ClassType(BillingStatus.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.CommodityInstanceItem", "billingMode", new ClassType(BillingMode.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.CommodityInstanceItem", "status", new ClassType(CommodityItemStatus.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.CommodityInstance", "status", new ClassType(CommodityStatus.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.CommodityInstance", "billingMode", new ClassType(BillingMode.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.BillingRule", "periodTimeUnit", new ClassType(TimeUnit.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.BillingRule", "meteringDataRoundingMode", new ClassType(RoundingMode.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.BillingRule", "billingMode", new ClassType(BillingMode.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.CommodityItemPrice", "billingMode", new ClassType(BillingMode.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.WalletCheck", "checkType", new ClassType(CheckType.class));
            exporter.registerVoDataType("com.cloudcare.cbis.billing.data.model.CommodityBillingResult", "billingMode", new ClassType(BillingMode.class));
            exporter.setConfiguration(configuration);

//            exporter.setTableNamePattern("billing_.*,wallet.*,check_.*");
			exporter.export(connection.getMetaData(), connection.createStatement());

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

}

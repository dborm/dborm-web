<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd"
	default-autowire="byName">
	<#list tables as table>
	<bean id="${table.entityName?uncap_first}" class="com.tbc.paas.mdl.cfg.domain.EntityDefine">
		<property name="tableName" value="${table.tableName}" />
		<property name="entityClassName" value="${table.package}.${table.entityName?cap_first}" />
		<property name="pkPropery" value="${table.pkProperty}" />
		<property name="extPropery" value="extMap" />
		<#--<property name="shared" value="true" />-->
		<property name="properties">
			<value>
				<#list table.columns as column>
				${column.propertyName}=${column.columnName},${column.sqlType}
				</#list>
			</value>
		</property>
	</bean>
	</#list>
</beans>
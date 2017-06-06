package ${package};

<#list importClasses as imprtClass>
import ${imprtClass};
</#list>

<#if annotation>
import com.tbc.paas.mdl.cfg.annotation.Column;
import com.tbc.paas.mdl.cfg.annotation.Dynamic;
import com.tbc.paas.mdl.cfg.annotation.Id;
import com.tbc.paas.mdl.cfg.annotation.Table;
</#if>
/**
 * 
 * @author tz
 * @version 1.0
 */
<#if annotation>
@Dynamic
@Table(tableName = "${tableName}")
</#if>
public class ${entityName?cap_first} {
	
	<#list columns as column>
	<#if annotation>
	<#if column.pkColumn>
	@Id
	</#if>
	@Column(columnName = "${column.columnName}")
	</#if>
	private ${column.propertyType} ${column.propertyName?uncap_first};
	</#list>
	
	private Map<String, Object> extMap;
	
	<#list columns as column>
	public ${column.propertyType} get${column.propertyName?cap_first}(){
		return this.${column.propertyName?uncap_first};
	}
	
	public void set${column.propertyName?cap_first}(${column.propertyType} ${column.propertyName?uncap_first}){
		this.${column.propertyName?uncap_first} = ${column.propertyName?uncap_first};
	}
	</#list>
	
	
	
	public void setExtMap(Map<String, Object> extMap) {
		this.extMap = extMap;
	}

	public Map<String, Object> getExtMap() {
		return extMap;
	}
}

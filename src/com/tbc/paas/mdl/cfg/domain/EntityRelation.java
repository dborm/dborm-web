package com.tbc.paas.mdl.cfg.domain;

import java.lang.reflect.Field;

import com.tbc.paas.mql.metadata.domain.TableRelation;

public class EntityRelation extends TableRelation {
	private Field entityField;

	public EntityRelation() {
		super();
	}

	public Field getEntityField() {
		return entityField;
	}

	public void setEntityField(Field entityField) {
		this.entityField = entityField;
	}
}

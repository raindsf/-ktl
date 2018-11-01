package com.iotimc.plugins.igreport.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.iotimc.core.bean.code.UUIDEntity;
import com.iotimc.core.bean.entity.BaseEntityMC;

@Entity
@Table(name = "IGREPORT")
public class Igreport extends BaseEntityMC<String> implements UUIDEntity {
	
	private static final long serialVersionUID = 7286107294543669169L;

	private String name;
	
	private String img;
	
	private String code;
	
	private Integer idx;
	
	private String notes;

	private String dataname;
	
	private String jsonval;
	
	@Column(name = "NAME")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "CODE")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name = "IDX")
	public Integer getIdx() {
		return idx;
	}

	public void setIdx(Integer idx) {
		this.idx = idx;
	}

	@Column(name = "NOTES")
	public String getNotes() {
		return notes;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Column(name = "JSONVAL")
	public String getJsonval() {
		return jsonval;
	}

	public void setJsonval(String jsonval) {
		this.jsonval = jsonval;
	}

	@Column(name = "IMG")
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	@Column(name = "DATANAME")
	public String getDataname() {
		return dataname;
	}

	public void setDataname(String dataname) {
		this.dataname = dataname;
	}
	
	
}

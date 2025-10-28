package com.erdal.model;

import com.erdal.role.RoleType;

public class Role {
	
	private int id;
	
	private RoleType roleType;

	public Role() {};

	public Role(int id,RoleType roleType) {
		super();
		this.id = id;
		this.roleType=roleType;
	}

	public RoleType getRoleType() {
		return roleType;
	}

	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	@Override
	public String toString() {
		return "Role [id=" + id + ", getId()=" + getId() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

}

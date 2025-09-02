package com.erdal.model;

public class User {
    private int id;
    private String fullName;
    private String phone;
    private String gender;
    private String adaress;
    private int taskId;
    
	public User() {}

	public User(int id, String fullName, String phone, String gender, String adaress, int taskId) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.phone = phone;
		this.gender = gender;
		this.adaress = adaress;
		this.taskId = taskId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAdaress() {
		return adaress;
	}

	public void setAdaress(String adaress) {
		this.adaress = adaress;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	@Override
	public String toString() {
		return "Kullanici [id=" + id + ", fullName=" + fullName + ", phone=" + phone + ", gender=" + gender
				+ ", adaress=" + adaress + ", taskId=" + taskId + ", getId()=" + getId() + ", getFullName()="
				+ getFullName() + ", getPhone()=" + getPhone() + ", getGender()=" + getGender() + ", getAdaress()="
				+ getAdaress() + ", getTaskId()=" + getTaskId() + ", getClass()=" + getClass() + ", hashCode()="
				+ hashCode() + ", toString()=" + super.toString() + "]";
	}
	
	

}
package com.erdal.model;

public class User {
    private String id;
    private String fullName;
    private String phone;
    private String gender;
    private String address;
    private String email;     
    private String password; 
    private boolean isAdmin;
    

	public User() {}


	public User(String id, String fullName, String phone, String gender, String address, String email, String password,
			boolean isAdmin) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.phone = phone;
		this.gender = gender;
		this.address = address;
		this.email = email;
		this.password = password;
		this.isAdmin = isAdmin;
	}


	public User(String id, String fullName, String phone, String gender, String address, String email,
			String password) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.phone = phone;
		this.gender = gender;
		this.address = address;
		this.email = email;
		this.password = password;
	}
	public User(String fullName, String phone, String gender, String address, String email,
			String password) {
		super();
		this.fullName = fullName;
		this.phone = phone;
		this.gender = gender;
		this.address = address;
		this.email = email;
		this.password = password;
	}
	

	public boolean isAdmin() {
		return isAdmin;
	}




	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	


	public String getId() {
		return id;
	}


	public void setId(String id) {
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


	public String getAddress() {
		return address;
	}


	public void setAddress(String address) {
		this.address = address;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	@Override
	public String toString() {
		return "User [id=" + id + ", fullName=" + fullName + ", phone=" + phone + ", gender=" + gender + ", address="
				+ address + ", email=" + email + ", password=" + password + ", isAdmin=" + isAdmin + ", isAdmin()="
				+ isAdmin() + ", getId()=" + getId() + ", getFullName()=" + getFullName() + ", getPhone()=" + getPhone()
				+ ", getGender()=" + getGender() + ", getAddress()=" + getAddress() + ", getEmail()=" + getEmail()
				+ ", getPassword()=" + getPassword() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}


	


	
	

}
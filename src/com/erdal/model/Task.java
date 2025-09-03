package com.erdal.model;

public class Task {
    private int id;                 // Not: DB’deki id kolonu
    private String title;           // Not: Görev başlığı
    private String description;
    private String userId;
    
    // Not: Görev açıklaması

    //  Boş constructor (JDBC için gerekli)
    public Task() {}

    // Parametreli constructor (tam nesne)
    public Task(int id, String title, String description,String userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.userId = userId;
    }

    //  Parametreli constructor (ekleme için, id DB tarafından oluşturulacak)
    public Task(String title, String description,String userId) {
        this.title = title;
        this.description = description;
        this.userId = userId;
        
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "Task [id=" + id + ", title=" + title + ", description=" + description + ", userId=" + userId
				+ ", getId()=" + getId() + ", getTitle()=" + getTitle() + ", getDescription()=" + getDescription()
				+ ", getUserId()=" + getUserId() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

	

	
  
}

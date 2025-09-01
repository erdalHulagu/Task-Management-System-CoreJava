package com.erdal.model;

public class Task {
    private int id;                 // Not: DB’deki id kolonu
    private String title;           // Not: Görev başlığı
    private String description;     // Not: Görev açıklaması

    // ✅ Boş constructor (JDBC için gerekli)
    public Task() {}

    // ✅ Parametreli constructor (tam nesne)
    public Task(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    // ✅ Parametreli constructor (ekleme için, id DB tarafından oluşturulacak)
    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    // ✅ Getter ve Setter’lar
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "[" + id + "] " + title + " - " + description;
    }
}

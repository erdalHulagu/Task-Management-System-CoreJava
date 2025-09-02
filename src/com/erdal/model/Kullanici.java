package com.erdal.model;

public class Kullanici {
    private int id;
    private String isim;
    private String soyisim;
    private String tell;
    private String cinsiyet;
    private String adres;
    
    public Kullanici () {};
	public Kullanici(int id, String isim, String soyisim, String tell, String cinsiyet, String adres) {
		super();
		this.id = id;
		this.isim = isim;
		this.soyisim = soyisim;
		this.tell = tell;
		this.cinsiyet = cinsiyet;
		this.adres = adres;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIsim() {
		return isim;
	}
	public void setIsim(String isim) {
		this.isim = isim;
	}
	public String getSoyisim() {
		return soyisim;
	}
	public void setSoyisim(String soyisim) {
		this.soyisim = soyisim;
	}
	public String getTell() {
		return tell;
	}
	public void setTell(String tell) {
		this.tell = tell;
	}
	public String getCinsiyet() {
		return cinsiyet;
	}
	public void setCinsiyet(String cinsiyet) {
		this.cinsiyet = cinsiyet;
	}
	public String getAdres() {
		return adres;
	}
	public void setAdres(String adres) {
		this.adres = adres;
	}
	@Override
	public String toString() {
		return "Kullanici [id=" + id + ", isim=" + isim + ", soyisim=" + soyisim + ", tell=" + tell + ", cinsiyet="
				+ cinsiyet + ", adres=" + adres + ", getId()=" + getId() + ", getIsim()=" + getIsim()
				+ ", getSoyisim()=" + getSoyisim() + ", getTell()=" + getTell() + ", getCinsiyet()=" + getCinsiyet()
				+ ", getAdres()=" + getAdres() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode()
				+ ", toString()=" + super.toString() + "]";
	}

    
}


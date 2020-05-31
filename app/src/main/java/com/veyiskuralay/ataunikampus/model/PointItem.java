package com.veyiskuralay.ataunikampus.model;


// Veritabanında konuma ait tutuğumuz verilerin çekilmesi sırasında kullanmak için oluşturduk.
public class PointItem {

    private int id;
    private String p_name;
    private double p_lat;
    private double p_lng;
    private String p_image;


    public  PointItem(int id, String p_name, double p_lat, double p_lng, String p_image){

        this.id      = id;
        this.p_name  = p_name;
        this.p_lat   = p_lat;
        this.p_lng   = p_lng;
        this.p_image = p_image;

    }

    public int getId() {
        return id;
    }

    public String getP_name() {
        return p_name;
    }

    public double getP_lat() {
        return p_lat;
    }

    public double getP_lng() {
        return p_lng;
    }

    public String getP_image() {
        return p_image;
    }



}

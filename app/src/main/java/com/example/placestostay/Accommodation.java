package com.example.placestostay;

public class Accommodation {
    private String name,type;
    private double price, lon,lat;

    public Accommodation(String name, String type, double price, double lon, double lat) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.lon = lon;
        this.lat = lat;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }

    @Override
    public String toString() {
        return "Accommodation{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", price=" + price +
                ", lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}

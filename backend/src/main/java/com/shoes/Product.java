package com.shoes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Product {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private double price;
    private String image;

    public Product(){}
    public Product(String name, String description, double price, String image){
        this.name=name; this.description=description; this.price=price; this.image=image;
    }
    public Long getId(){return id;}
    public String getName(){return name;}
    public String getDescription(){return description;}
    public double getPrice(){return price;}
    public String getImage(){return image;}
    public void setId(Long id){this.id=id;}
    public void setName(String n){this.name=n;}
    public void setDescription(String d){this.description=d;}
    public void setPrice(double p){this.price=p;}
    public void setImage(String i){this.image=i;}
}

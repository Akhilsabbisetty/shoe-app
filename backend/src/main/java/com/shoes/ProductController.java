package com.shoes;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductRepository repo;

    @GetMapping("/products")
    public List<Product> list(){ return repo.findAll(); }

    @EventListener(ApplicationReadyEvent.class)
    public void seed(){
        if(repo.count()==0){
            repo.save(new Product("Classic Sneakers","Comfortable everyday sneakers",49.99,"https://via.placeholder.com/80"));
            repo.save(new Product("Trail Runner","Durable trail shoes",79.99,"https://via.placeholder.com/80"));
            repo.save(new Product("Leather Loafers","Stylish leather loafers",69.99,"https://via.placeholder.com/80"));
        }
    }
}

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
    public List<Product> list(){
        return repo.findAll();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed(){
        if(repo.count() == 0){
            // Sneakers & Casual
            repo.save(new Product("Nike Air Zoom Pegasus", "Lightweight running shoes with great cushioning.", 119.99,
                "https://images.unsplash.com/photo-1606813902789-0f33d9a3d2d2?w=600"));
            repo.save(new Product("Adidas Ultraboost", "Premium comfort and performance for everyday wear.", 149.99,
                "https://images.unsplash.com/photo-1618354691373-4b52e7f8b7aa?w=600"));
            repo.save(new Product("Puma Smash Sneakers", "Classic Puma design with soft comfort.", 89.99,
                "https://images.unsplash.com/photo-1589187155473-1e31d99d7c15?w=600"));
            repo.save(new Product("Woodland Outdoor Shoes", "Rugged design perfect for adventures.", 99.99,
                "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600"));
            repo.save(new Product("Skechers Go Walk", "Super comfortable walking shoes for daily use.", 79.99,
                "https://images.unsplash.com/photo-1600181952470-8300b9e3fddf?w=600"));

            // Formal & Leather
            repo.save(new Product("Clarks Leather Derby", "Elegant leather shoes ideal for formal occasions.", 129.99,
                "https://images.unsplash.com/photo-1528701800489-20be3c2a1e55?w=600"));
            repo.save(new Product("Hush Puppies Classic Loafers", "Timeless comfort and style.", 109.99,
                "https://images.unsplash.com/photo-1606812095761-5c527d29b3dc?w=600"));
            repo.save(new Product("Bata Formal Slip-Ons", "Budget-friendly formal wear with premium looks.", 69.99,
                "https://images.unsplash.com/photo-1544441893-675973e31985?w=600"));

            // Sandals
            repo.save(new Product("Crocs Sandals", "Light, comfy, and water-friendly casual sandals.", 49.99,
                "https://images.unsplash.com/photo-1620916566398-39c9a98b03eb?w=600"));
            repo.save(new Product("Woodland Leather Sandals", "Tough leather sandals for daily use.", 59.99,
                "https://images.unsplash.com/photo-1621373763531-44e719c3e5e7?w=600"));
            repo.save(new Product("Nike Air Max Slides", "Sporty slides with Air cushioning.", 39.99,
                "https://images.unsplash.com/photo-1631631990682-ec8cda2ff8a5?w=600"));
            repo.save(new Product("Adidas Adilette Slides", "Iconic 3-stripe slides for leisure and sport.", 34.99,
                "https://images.unsplash.com/photo-1595465618493-40cfaa2d5187?w=600"));

            // Women’s collection
            repo.save(new Product("Puma Women’s Trainers", "Lightweight trainers for everyday comfort.", 89.99,
                "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=600"));
            repo.save(new Product("Clarks Heeled Sandals", "Elegant heeled sandals for occasions.", 99.99,
                "https://images.unsplash.com/photo-1604335399105-3e4fcb0a9747?w=600"));
            repo.save(new Product("Bata Women’s Flats", "Comfortable flat sandals with stylish design.", 44.99,
                "https://images.unsplash.com/photo-1593032465174-cd58f9f3765b?w=600"));
        }
    }
}

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
    public List<Product> list() {
        return repo.findAll();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (repo.count() == 0) {
            // Sneakers & Casual
            repo.save(new Product("Nike Air Zoom Pegasus", "Lightweight running shoes with great cushioning.", 119.99,
					"https://tse3.mm.bing.net/th/id/OIP.nPN5cjF2rpCIEDxm9mJMbwHaHa?cb=12&pid=Api"));
			repo.save(new Product("Adidas Ultraboost", "Premium comfort and performance for everyday wear.", 149.99,
					"https://tse3.mm.bing.net/th/id/OIP.t3O-1KDg9vlEavqzGsh_VAHaHa?cb=12&pid=Api"));
			repo.save(new Product("Puma Smash Sneakers", "Classic Puma design with soft comfort.", 89.99,
					"https://tse4.mm.bing.net/th/id/OIP.BekZXqU4gPOKC5ZfzEqIngHaHa?cb=12&pid=Api&ucfimg=1"));
			repo.save(new Product("Woodland Outdoor Shoes", "Rugged design perfect for adventures.", 99.99,
					"https://tse2.mm.bing.net/th/id/OIP.4TuIt2E9UKS_8wwTakhoKQHaHa?cb=12&pid=Api"));
			repo.save(new Product("Skechers Go Walk", "Super comfortable walking shoes for daily use.", 79.99,
					"https://tse4.mm.bing.net/th/id/OIP.PcV81frJg7yHq8v0uA3fFAHaHa?pid=Api"));

			repo.save(new Product("Clarks Leather Derby", "Elegant leather shoes ideal for formal occasions.", 129.99,
					"https://tse3.mm.bing.net/th/id/OIP.Xx4N8lh-0D0rAUP9hZ2mjQHaHa?pid=Api"));
			repo.save(new Product("Hush Puppies Classic Loafers", "Timeless comfort and style.", 109.99,
					"https://tse2.mm.bing.net/th/id/OIP.AZ8Rk0D1xK6SL4Hzg0rVgwHaHa?pid=Api"));
			repo.save(new Product("Bata Formal Slip-Ons", "Budget-friendly formal wear with premium looks.", 69.99,
					"https://tse4.mm.bing.net/th/id/OIP.7cT3_ZMLJqIuZQ9nXU8z_gHaHa?pid=Api"));

			repo.save(new Product("Crocs Sandals", "Light, comfy, and water-friendly casual sandals.", 49.99,
					"https://tse3.mm.bing.net/th/id/OIP.yW3Q1vLx91vH0RyrG7XpVQHaHa?pid=Api"));
			repo.save(new Product("Woodland Leather Sandals", "Tough leather sandals for daily use.", 59.99,
					"https://tse2.mm.bing.net/th/id/OIP.KzPuqT9Gk4uL7jvM7vZgDAHaHa?pid=Api"));
			repo.save(new Product("Nike Air Max Slides", "Sporty slides with Air cushioning.", 39.99,
					"https://tse3.mm.bing.net/th/id/OIP.7N4dIh3VjIMunM3jIhzkgwHaHa?pid=Api"));
			repo.save(new Product("Adidas Adilette Slides", "Iconic 3-stripe slides for leisure and sport.", 34.99,
					"https://tse4.mm.bing.net/th/id/OIP.r4v8FMSW2aD6-0w9-nj4VQHaHa?pid=Api"));

			repo.save(new Product("Puma Women’s Trainers", "Lightweight trainers for everyday comfort.", 89.99,
					"https://tse4.mm.bing.net/th/id/OIP.2M4aJZzjE2q5gQJ5zT0y3QHaHa?pid=Api"));
			repo.save(new Product("Clarks Heeled Sandals", "Elegant heeled sandals for occasions.", 99.99,
					"https://tse2.mm.bing.net/th/id/OIP.P3Mbd2oq_0X4RkU3y0C-1AHaHa?pid=Api"));
			repo.save(new Product("Bata Women’s Flats", "Comfortable flat sandals with stylish design.", 44.99,
					"https://tse4.mm.bing.net/th/id/OIP.mV1x3xZ8lS4T8uLxK7Kf2gHaHa?pid=Api"));
	

            // Additional placeholder products (optional)
            repo.save(new Product("Trail Runner", "Durable trail shoes", 79.99,
                    "https://via.placeholder.com/80"));
            repo.save(new Product("Leather Loafers", "Stylish leather loafers", 69.99,
                    "https://via.placeholder.com/80"));
        }
    }
}

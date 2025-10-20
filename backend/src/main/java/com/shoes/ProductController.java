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
		    repo.save(new Product("Nike Air Zoom Pegasus", "Lightweight running shoes with great cushioning.", 119.99,
					"https://tse3.mm.bing.net/th/id/OIP.nPN5cjF2rpCIEDxm9mJMbwHaHa?cb=12&pid=Api"));
			repo.save(new Product("Adidas Ultraboost", "Premium comfort and performance for everyday wear.", 149.99,
					"https://tse3.mm.bing.net/th/id/OIP.t3O-1KDg9vlEavqzGsh_VAHaHa?cb=12&pid=Api"));
			repo.save(new Product("Puma Smash Sneakers", "Classic Puma design with soft comfort.", 89.99,
					"https://tse4.mm.bing.net/th/id/OIP.BekZXqU4gPOKC5ZfzEqIngHaHa?cb=12&pid=Api&ucfimg=1"));
			repo.save(new Product("Woodland Outdoor Shoes", "Rugged design perfect for adventures.", 99.99,
					"https://tse2.mm.bing.net/th/id/OIP.4TuIt2E9UKS_8wwTakhoKQHaHa?cb=12&pid=Api"));

        }
    }
}

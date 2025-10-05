package com.shoes;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/shoes")
public class ShoeController {
    @GetMapping
    public List<Map<String,String>> getShoes() {
        List<Map<String,String>> shoes = new ArrayList<>();
        shoes.add(Map.of("name","Running Shoes","price","$50"));
        shoes.add(Map.of("name","Leather Sandals","price","$30"));
        return shoes;
    }
}

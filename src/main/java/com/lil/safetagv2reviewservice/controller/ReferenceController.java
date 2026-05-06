package com.lil.safetagv2reviewservice.controller;

import com.lil.safetagv2reviewservice.domain.PathologyFamily;
import com.lil.safetagv2reviewservice.domain.TagCategory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/reviews/references") // Le préfixe /reviews permet à la Gateway de bien router la requête
public class ReferenceController {

    @GetMapping("/pathology-families")
    public PathologyFamily[] getPathologyFamilies() {
        return PathologyFamily.values();
    }

    @GetMapping("/tag-categories")
    public TagCategory[] getTagCategories() {
        return TagCategory.values();
    }
}

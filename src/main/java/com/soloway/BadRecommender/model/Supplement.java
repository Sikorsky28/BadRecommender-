package com.soloway.BadRecommender.model;

import java.util.Set;
import java.util.List;

public class Supplement {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Category category;
    private Set<String> tags;
    private boolean active;
    private List<AnswerEffect> effects;
    private Double rating;
    private String productUrl;
    private String imageUrl;
    private String price;
    private String type; // "основные" или "дополнительные"

    public Supplement(Long id, String code, String name, Category category, Set<String> tags, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = "";
        this.category = category;
        this.tags = tags;
        this.active = active;
    }

    public Supplement(Long id, String name, String category, Set<String> tags, boolean active) {
        this.id = id;
        this.name = name;
        this.description = "";
        this.category = new Category(category);
        this.tags = tags;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Set<String> getTags() { return tags; }
    public boolean isActive() { return active; }
    public List<AnswerEffect> getEffects() { return effects; }
    public Double getRating() { return rating; }
    public String getProductUrl() { return productUrl; }
    public String getImageUrl() { return imageUrl; }
    public String getPrice() { return price; }
    public String getType() { return type; }

    public void setId(Long id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(Category category) { this.category = category; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public void setActive(boolean active) { this.active = active; }
    public void setEffects(List<AnswerEffect> effects) { this.effects = effects; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setProductUrl(String productUrl) { this.productUrl = productUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPrice(String price) { this.price = price; }
    public void setType(String type) { this.type = type; }
}

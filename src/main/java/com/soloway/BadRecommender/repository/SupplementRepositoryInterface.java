package com.soloway.BadRecommender.repository;

import com.soloway.BadRecommender.model.Supplement;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SupplementRepositoryInterface {
    
    List<Supplement> getAll();
    
    List<Supplement> getAllIncludingInactive();
    
    Optional<Supplement> getById(Long id);
    
    List<Supplement> getByCategory(String category);
    
    List<Supplement> getByTag(String tag);
    
    List<Supplement> getByTags(Set<String> tags);
    
    List<Supplement> searchByName(String name);
    
    Supplement save(Supplement supplement);
    
    void deleteById(Long id);
    
    void deactivateById(Long id);
    
    void activateById(Long id);
    
    List<String> getAllCategories();
    
    Set<String> getAllTags();
}








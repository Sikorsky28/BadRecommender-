package com.soloway.BadRecommender.service;

import com.soloway.BadRecommender.model.Supplement;
import com.soloway.BadRecommender.model.Category;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FallbackDataService {

    public List<Supplement> getFallbackSupplements() {
        List<Supplement> supplements = new ArrayList<>();
        
        // Бодрость и энергия
        Supplement energy = new Supplement(1L, "ENERGY-001", "Energy", new Category("Энергия"), 
                Set.of("energy", "vitality"), true);
        energy.setDescription("Комплекс витаминов группы B и аминокислот для повышения энергии и выносливости. Помогает бороться с усталостью и улучшает концентрацию внимания.");
        supplements.add(energy);
        
        Supplement coq10 = new Supplement(2L, "COQ10-001", "Coenzyme Q10", new Category("Энергия"), 
                Set.of("energy", "heart", "antioxidant"), true);
        coq10.setDescription("Мощный антиоксидант, который поддерживает здоровье сердца и повышает энергетический уровень клеток.");
        supplements.add(coq10);
        
        // Сон
        Supplement melatonin = new Supplement(3L, "SLEEP-001", "Melatonin", new Category("Сон"), 
                Set.of("sleep", "relaxation"), true);
        melatonin.setDescription("Гормон сна, который помогает нормализовать циркадные ритмы и улучшить качество сна.");
        supplements.add(melatonin);
        
        // Иммунитет
        Supplement vitaminC = new Supplement(4L, "IMMUNITY-001", "Vitamin C", new Category("Иммунитет"), 
                Set.of("immunity", "antioxidant"), true);
        vitaminC.setDescription("Мощная защита от простуд, усталости и стресса. Укрепляет иммунитет и ускоряет восстановление.");
        supplements.add(vitaminC);
        
        // Мужское здоровье
        Supplement zinc = new Supplement(5L, "MALE-001", "Zinc 25 mg", new Category("Мужское здоровье"), 
                Set.of("male", "immunity"), true);
        zinc.setDescription("Добавка с цинком пиколинатом компенсирует дефицит цинка, улучшая иммунитет, состояние кожи, волос и ногтей.");
        supplements.add(zinc);
        
        Supplement mensFormula = new Supplement(6L, "MALE-002", "Men's Formula", new Category("Мужское здоровье"), 
                Set.of("male", "antioxidant"), true);
        mensFormula.setDescription("Менс Формула оказывает мощное антиоксидантное и противовоспалительное действие для долговременной профилактики мужского здоровья.");
        supplements.add(mensFormula);
        
        // Сердце и сосуды
        Supplement omega3 = new Supplement(7L, "HEART-001", "Extra Omega-3", new Category("Сердце"), 
                Set.of("heart", "omega3"), true);
        omega3.setDescription("Высокоочищенный рыбный жир премиум-класса без запаха и вкуса с высокой концентрацией Омега-3 в одной капсуле.");
        supplements.add(omega3);
        
        Supplement resveratrol = new Supplement(8L, "HEART-002", "Resveratrol", new Category("Сердце"), 
                Set.of("heart", "antioxidant"), true);
        resveratrol.setDescription("Мощный природный антиоксидант, который защищает клетки от окислительного стресса и замедляет старение.");
        supplements.add(resveratrol);
        
        // Дополнительные
        Supplement ginkgo = new Supplement(9L, "BRAIN-001", "Gingko Biloba + Vitamin C", new Category("Мозг"), 
                Set.of("brain", "memory"), true);
        ginkgo.setDescription("Гингко Билоба улучшает кровоснабжение мозга, убирает «туман в голове» и поддерживает концентрацию.");
        supplements.add(ginkgo);
        
        Supplement prebio = new Supplement(10L, "DIGESTION-001", "Prebio Complex", new Category("Пищеварение"), 
                Set.of("digestion", "probiotic"), true);
        prebio.setDescription("Пребио Комплекс помогает нормализовать работу желудка и кишечника. Является хорошей профилактикой хронических заболеваний пищеварительного тракта.");
        supplements.add(prebio);
        
        return supplements;
    }
    
    public List<Category> getFallbackCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("Энергия"));
        categories.add(new Category("Сон"));
        categories.add(new Category("Иммунитет"));
        categories.add(new Category("Мужское здоровье"));
        categories.add(new Category("Сердце"));
        categories.add(new Category("Мозг"));
        categories.add(new Category("Пищеварение"));
        return categories;
    }
}

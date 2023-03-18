package com.kenzie.capstone.service.converter;

import com.kenzie.capstone.service.converter.dao.RecipeDao;

import javax.inject.Inject;

public class RecipeService {

    private RecipeDao recipeDao;

    @Inject
    public RecipeService(RecipeDao recipeDao) {
        this.recipeDao = recipeDao;
    }

    public String getAllRecipes(String query) {
        //will finish implementing
        String result = recipeDao.getAllRecipes(query);

        return result;
    }

    public String getRandomRecipe(){
        return recipeDao.getRandomRecipe();
    }
}

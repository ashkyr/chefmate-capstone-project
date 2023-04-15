package com.kenzie.appserver.service;

import com.kenzie.appserver.Service.ChefMateUserService;
import com.kenzie.appserver.config.CacheStore;
import com.kenzie.appserver.controller.model.ChefMateUserResponse;
import com.kenzie.appserver.controller.model.CreateChefMateUserRequest;
import com.kenzie.appserver.repositories.ChefMateUserRepository;
import com.kenzie.appserver.repositories.model.ChefMateUserRecord;
import com.kenzie.capstone.service.client.RecipeServiceLambdaJavaClient.RecipeLambdaServiceClient;
import com.kenzie.capstone.service.client.ReviewServiceLambdaJavaClient.ReviewLambdaServiceClient;
import com.kenzie.capstone.service.model.RecipeServiceLambdaModel.Recipe;
import com.kenzie.capstone.service.model.RecipeServiceLambdaModel.RecipeResponse;
import com.kenzie.capstone.service.model.ReviewServiceLambdaModel.Review;
import com.kenzie.capstone.service.model.ReviewServiceLambdaModel.ReviewCreateRequest;
import com.kenzie.capstone.service.model.ReviewServiceLambdaModel.ReviewResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;

public class ChefMateUserServiceTest {

    private ChefMateUserRepository chefMateUserRepository;
    private ChefMateUserService chefMateUserService;
    private ReviewLambdaServiceClient reviewLambdaServiceClient;
    private RecipeLambdaServiceClient recipeLambdaServiceClient;
    private CacheStore cache;

    @BeforeEach
    void setup() {
        chefMateUserRepository = mock(ChefMateUserRepository.class);
        reviewLambdaServiceClient = mock(ReviewLambdaServiceClient.class);
        recipeLambdaServiceClient = mock(RecipeLambdaServiceClient.class);
        cache = mock(CacheStore.class);
        chefMateUserService = new ChefMateUserService(chefMateUserRepository, reviewLambdaServiceClient, recipeLambdaServiceClient, cache);
    }

    /** ------------------------------------------------------------------------
     *  chefMateUserService.addNewUser
     *  ------------------------------------------------------------------------ **/
    @Test
    void addNewUser() {
        // GIVEN
        String userId = randomUUID().toString();

        CreateChefMateUserRequest request = new CreateChefMateUserRequest();
        request.setUserId(userId);
        request.setUserPreferences(Optional.empty());
        request.setRecipesTried(Optional.empty());
        request.setIngredients(Optional.empty());

        ArgumentCaptor<ChefMateUserRecord> userRecordCaptor = ArgumentCaptor.forClass(ChefMateUserRecord.class);

        // WHEN
        ChefMateUserResponse returnedUser = chefMateUserService.addNewUser(request);

        // THEN
        Assertions.assertNotNull(returnedUser);

        verify(chefMateUserRepository).save(userRecordCaptor.capture());

        ChefMateUserRecord record = userRecordCaptor.getValue();

        Assertions.assertNotNull(record, "The user record is returned");
        Assertions.assertNotNull(record.getUserId(), "The user id exists");
    }

    /** ------------------------------------------------------------------------
     *  chefMateUserService.findByUserId
     *  ------------------------------------------------------------------------ **/

    @Test
    void findByUserId_valid_user() {
        // GIVEN
        String userId = randomUUID().toString();

        ChefMateUserRecord record = new ChefMateUserRecord();
        record.setUserId(userId);

        when(chefMateUserRepository.findById(userId)).thenReturn(Optional.of(record));
        // WHEN
        ChefMateUserResponse user = chefMateUserService.getUserById(userId);

        // THEN
        Assertions.assertNotNull(user, "The user is returned");
        Assertions.assertEquals(record.getUserId(), user.getUserId(), "The user id matches");
    }

    @Test
    void findByUserId_invalid_user() {
        // GIVEN
        String userId = randomUUID().toString();

        // WHEN
        when(chefMateUserRepository.findById(userId)).thenReturn(Optional.empty());
        ChefMateUserResponse user = chefMateUserService.getUserById(userId);

        // THEN
        Assertions.assertNull(user, "The user is null when not found");
    }

    /** ------------------------------------------------------------------------
     *  chefMateUserService.updateUserPreferences
     *  ------------------------------------------------------------------------ **/

    @Test
    void updateUserPreferences_valid_user() {
        // GIVEN
        String userId = randomUUID().toString();

        ChefMateUserRecord oldUserRecord = new ChefMateUserRecord();
        oldUserRecord.setUserId(userId);
        List<String> newUserPreferences = Arrays.asList("Gluten Free", "Vegetarian", "Dairy Free");

        when(chefMateUserRepository.findById(userId)).thenReturn(Optional.of(oldUserRecord));

        ArgumentCaptor<ChefMateUserRecord> userRecordCaptor = ArgumentCaptor.forClass(ChefMateUserRecord.class);

        // WHEN
        chefMateUserService.updateUserPreferences(userId, newUserPreferences);

        // THEN
        verify(chefMateUserRepository).save(userRecordCaptor.capture());

        ChefMateUserRecord record = userRecordCaptor.getValue();

        Assertions.assertNotNull(record, "The user record is returned");
        Assertions.assertEquals(record.getUserId(), userId, "The user id matches");
    }

    @Test
    void updateUserPreferences_invalid_user() {
        // GIVEN
        String userId = randomUUID().toString();
        List<String> newUserPreferences = Arrays.asList("Gluten Free", "Vegetarian", "Dairy Free");


        when(chefMateUserRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN
        Assertions.assertThrows(ResponseStatusException.class, () -> chefMateUserService.updateUserPreferences(userId, newUserPreferences));

        // THEN
        try {
            verify(chefMateUserRepository, never()).save(any());
        } catch(MockitoAssertionError error) {
            throw new MockitoAssertionError("There should not be a call to .save() if the user is not found in the database. - " + error);
        }
    }

    /** ------------------------------------------------------------------------
     *  chefMateUserService.updateUserPreferences
     *  ------------------------------------------------------------------------ **/

    @Test
    void updateRecipesTried_valid_user() {
        // GIVEN
        String userId = randomUUID().toString();

        ChefMateUserRecord oldUserRecord = new ChefMateUserRecord();
        oldUserRecord.setUserId(userId);
        Set<String> newRecipesTried = new HashSet<>();
        newRecipesTried.add("Italian Recipe");
        newRecipesTried.add("Chinese Recipe");

        when(chefMateUserRepository.findById(userId)).thenReturn(Optional.of(oldUserRecord));

        ArgumentCaptor<ChefMateUserRecord> userRecordCaptor = ArgumentCaptor.forClass(ChefMateUserRecord.class);

        // WHEN
        chefMateUserService.updateRecipesTried(userId, newRecipesTried);

        // THEN
        verify(chefMateUserRepository).save(userRecordCaptor.capture());

        ChefMateUserRecord record = userRecordCaptor.getValue();

        Assertions.assertNotNull(record, "The user record is returned");
        Assertions.assertEquals(record.getUserId(), userId, "The user id matches");
    }

    @Test
    void updateRecipesTried_invalid_user() {
        // GIVEN
        String userId = randomUUID().toString();
        Set<String> newRecipesTried = new HashSet<>();
        newRecipesTried.add("Italian Recipe");
        newRecipesTried.add("Chinese Recipe");

        when(chefMateUserRepository.findById(userId)).thenReturn(Optional.empty());

        // WHEN
        Assertions.assertThrows(ResponseStatusException.class, () -> chefMateUserService.updateRecipesTried(userId, newRecipesTried));

        // THEN
        try {
            verify(chefMateUserRepository, never()).save(any());
        } catch(MockitoAssertionError error) {
            throw new MockitoAssertionError("There should not be a call to .save() if the user is not found in the database. - " + error);
        }
    }


    /** ------------------------------------------------------------------------
     *  chefMateUserService.deleteUser
     *  ------------------------------------------------------------------------ **/
    @Test
    void deleteUser() {
        // GIVEN
        String userId = randomUUID().toString();

        ChefMateUserRecord oldCustomerRecord = new ChefMateUserRecord();
        oldCustomerRecord.setUserId(userId);

        doNothing().when(chefMateUserRepository).deleteById(userId);

        // WHEN
        chefMateUserService.deleteUser(userId);

        // THEN
        verify(chefMateUserRepository).deleteById(userId);
    }

    @Test
    void addReview_properUser_addsReview() {
        ReviewCreateRequest request = new ReviewCreateRequest();
        ReviewResponse response = new ReviewResponse();

        String userId = randomUUID().toString();
        String recipeId = randomUUID().toString();

        request.setReviewerId(userId);
        request.setRating(3.0);
        request.setComment("test Comment");
        request.setRecipeId(recipeId);

        response.setRating(3.0);
        response.setComment("test Comment");
        response.setReviewerId(userId);
        response.setRecipeId(recipeId);

        Set<String> recipesTried = new HashSet<>();
        recipesTried.add(recipeId);

        ChefMateUserRecord userRecord = new ChefMateUserRecord();
        userRecord.setUserId(userId);
        userRecord.setRecipesTried(recipesTried);

        when(chefMateUserRepository.findById(userId)).thenReturn(Optional.of(userRecord));
        when(reviewLambdaServiceClient.addReview(request)).thenReturn(response);

        ReviewResponse actualResponse = chefMateUserService.addReview(request);
        Assertions.assertEquals(actualResponse.getComment(), request.getComment());
        Assertions.assertEquals(actualResponse.getReviewerId(), request.getReviewerId());
        Assertions.assertEquals(actualResponse.getRecipeId(), request.getRecipeId());
        Assertions.assertEquals(actualResponse.getRating(), request.getRating());
    }

//    @Test
//    public void addReview_userIsNotValid_throwsException() {
//        ReviewCreateRequest request = new ReviewCreateRequest();
//
//        String userId = randomUUID().toString();
//        String recipeId = randomUUID().toString();
//
//        request.setReviewerId(userId);
//        request.setRating(3.0);
//        request.setComment("test Comment");
//        request.setRecipeId(recipeId);
//
//        when(chefMateUserRepository.findById(userId)).thenReturn(Optional.ofNullable(null));
//
//        Assertions.assertThrows(ResponseStatusException.class, () -> chefMateUserService.addReview(request));
//    }

//    @Test
//    public void addReview_recipeIsNotTried_throwsException() {
//        ReviewCreateRequest request = new ReviewCreateRequest();
//        ReviewResponse response = new ReviewResponse();
//
//        String userId = randomUUID().toString();
//        String notTriedRecipeId = randomUUID().toString();
//
//        request.setReviewerId(userId);
//        request.setRating(3.0);
//        request.setComment("test Comment");
//        request.setRecipeId(notTriedRecipeId);
//
//
//        Set<String> recipesTried = new HashSet<>();
//        recipesTried.add(UUID.randomUUID().toString());
//
//        ChefMateUserRecord userRecord = new ChefMateUserRecord();
//        userRecord.setUserId(userId);
//        userRecord.setRecipesTried(recipesTried);
//
//        when(chefMateUserRepository.findById(userId)).thenReturn(Optional.of(userRecord));
//
//        Assertions.assertThrows(ResponseStatusException.class, () -> chefMateUserService.addReview(request));
//    }

    @Test
    public void getReviews_validId_returnsReviews() {
        String recipeId = UUID.randomUUID().toString();
        String reviewerId = UUID.randomUUID().toString();

        Review review1 = new Review(recipeId, reviewerId, 5.0, "good recipe");
        Review review2 = new Review(recipeId, reviewerId, 1.0, "bad recipe");

        List<Review> reviewList = new ArrayList<>();
        reviewList.add(review1);
        reviewList.add(review2);

        when(reviewLambdaServiceClient.getRecipeReviews(recipeId)).thenReturn(reviewList);
        List<ReviewResponse> responseList = chefMateUserService.getRecipeReviews(recipeId);

        Assertions.assertEquals(reviewList.size(), responseList.size());

        Assertions.assertEquals(responseList.get(0).getRecipeId(), recipeId);
        Assertions.assertEquals(responseList.get(1).getRecipeId(), recipeId);

        Assertions.assertEquals(responseList.get(0).getReviewerId(), reviewerId);
        Assertions.assertEquals(responseList.get(1).getReviewerId(), reviewerId);

        Assertions.assertEquals(responseList.get(0).getComment(), review1.getComment());
        Assertions.assertEquals(responseList.get(1).getComment(), review2.getComment());

        Assertions.assertEquals(responseList.get(0).getRating(), review1.getRating());
        Assertions.assertEquals(responseList.get(1).getRating(), review2.getRating());
    }

    @Test
    public void getReviews_invalidReviewIds_throwExceptions() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> chefMateUserService.getRecipeReviews(null), "Null Id expected to throw exception");
        Assertions.assertThrows(IllegalArgumentException.class, () -> chefMateUserService.getRecipeReviews(""), "Empty Id expected to throw exception");
    }

    @Test
    void searchByNutrients() {
        String query = "minCarbs=5";

        List<RecipeResponse> recipeResponses = new ArrayList<>();
        recipeResponses.add(new RecipeResponse());
        recipeResponses.add(new RecipeResponse());

        when(recipeLambdaServiceClient.getSearchByNutrients(query)).thenReturn(recipeResponses);

        List<RecipeResponse> results = chefMateUserService.searchByNutrients(query);

        Assertions.assertNotNull(results, "The results should not be null!");
        Assertions.assertEquals(recipeResponses.size(), results.size());
    }

    @Test
    void getAllRecipes(){
        String query = "Chicken";
        List<RecipeResponse> expected = new ArrayList<>();
        expected.add(new RecipeResponse());
        expected.add(new RecipeResponse());

        when(recipeLambdaServiceClient.getAllRecipes(query)).thenReturn(expected);

        List<RecipeResponse> actual = chefMateUserService.getAllRecipes(query);

        Assertions.assertNotNull(actual, "The recipes should not be null!");
        Assertions.assertEquals(expected.size(), actual.size());
    }

    @Test
    void getRandomRecipe() {
        List<Recipe> expected = new ArrayList<>();
        expected.add(new Recipe());
        expected.add(new Recipe());

        when(recipeLambdaServiceClient.getRandomRecipe()).thenReturn(expected);

        List<Recipe> actual = chefMateUserService.getRandomRecipe();

        Assertions.assertNotNull(actual, "The list of recipes should not be null!");
        Assertions.assertEquals(actual.size(), expected.size());
    }

    @Test
    void searchByIngredients() {
        String query = "apple,flour,sugar";

        List<RecipeResponse> recipeResponses = new ArrayList<>();
        recipeResponses.add(new RecipeResponse());
        recipeResponses.add(new RecipeResponse());

        when(recipeLambdaServiceClient.getSearchByIngredients(query)).thenReturn(recipeResponses);

        List<RecipeResponse> results = chefMateUserService.searchByIngredients(query);

        Assertions.assertNotNull(results, "The results should not be null!");
        Assertions.assertEquals(recipeResponses.size(), results.size());
    }
}

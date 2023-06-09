import BaseClass from "../util/baseClass";
import axios from 'axios'
import DataStore from "../util/DataStore";

/**
 * Client to call the ChefMateUserService.
 *
 * This could be a great place to explore Mixins. Currently the client is being loaded multiple times on each page,
 * which we could avoid using inheritance or Mixins.
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Classes#Mix-ins
 * https://javascript.info/mixins
 */
export default class ChefMateClient extends BaseClass {

    constructor(props = {}){
        super();
        const methodsToBind = ['clientLoaded', 'addNewUser', 'updateUserPreference', 'updateRecipesTried', 'getUserById', 'deleteUser'];
        this.bindClassMethods(methodsToBind, this);
        this.props = props;
        this.clientLoaded(axios);
        this.dataStore = new DataStore();
    }

    /**
     * Run any functions that are supposed to be called once the client has loaded successfully.
     * @param client The client that has been successfully loaded.
     */
    clientLoaded(client) {
        this.client = client;
        if (this.props.hasOwnProperty("onReady")){
            this.props.onReady();
        }
    }

    /**
     * Makes a request to the backend to save the authenticated user to the ChefMateUser database.
     * @param userId Id of the authenticated user to save.
     * @param errorCallback (Optional) A function to execute if the call fails.
     * @returns The response after saving the user
     */
    async addNewUser(userId, errorCallback) {

        // save only the userId to the frontend datastore for easy access
        //const dataStore = new DataStore();
        this.dataStore.set("userId", userId);
        console.log(userId);
        console.log(this.dataStore.get("userId"));

        try{
            const response = await this.client.post(`/user/createUser`, {
                "userId": userId});
            return response;
        } catch (error) {
            this.handleError("addNewUser", error, errorCallback);
        }
    }

    /**
     * Makes a request to the backend to update the list of the users dietary preferences.
     * @param userId Id of the authenticated user to update preferences for.
     * @param userPreferences Dietary preferences of the user
     * @param errorCallback (Optional) A function to execute if the call fails.
     * @returns The list of preferences retrieved
     */
    async updateUserPreference(userPreferences, errorCallback) {
        try{
            const response = await this.client.put(`/user/userPreferences/${userPreferences}`, {
                "userId": this.dataStore.get("userId"),
                "userPreferences": userPreferences});
            return response.data;
        } catch (error) {
            this.handleError("updateUserPreference", error, errorCallback);
        }
    }

    /**
     * Makes a request to the backend to update the set of recipes tried by the authenticated user.
     * @param userId Id of the authenticated user to update the recipes tried.
     * @param recipesTried Dietary preferences of the user
     * @param errorCallback (Optional) A function to execute if the call fails.
     * @returns The set of recipes tried by the user is retrieved
     */
    async updateRecipesTried(recipesTried, errorCallback) {
        try{
            const response = await this.client.put(`/user/recipesTried/${recipesTried}`, {
                "userId": this.dataStore.get("userId"),
                "recipesTried": recipesTried});
            return response.data;
        } catch (error) {
            this.handleError("updateRecipesTried", error, errorCallback);
        }
    }

    /**
     * Makes a request to the backend to delete the user record from the ChefMateUser database.
     * @param userId Id of the authenticated user to update the recipes tried.
     * @param errorCallback (Optional) A function to execute if the call fails.
     * @returns The set of recipes tried by the user is retrieved
     */
    async deleteUser( errorCallback) {
        try{
            const response = await this.client.delete(`/user/deleteUser/${this.dataStore.get("userId")}`, {
                "userId": this.dataStore.get("userId")});
            return response;
        } catch (error) {
            this.handleError("deleteUser", error, errorCallback);
        }
    }

    /**
     * Makes a request to the backend to delete the user record from the ChefMateUser database.
     * @param userId Id of the authenticated user to update the recipes tried.
     * @param errorCallback (Optional) A function to execute if the call fails.
     * @returns The set of recipes tried by the user is retrieved
     */
    async getUserById(userId, errorCallback) {
        // try{
        //     return await this.client.get(`/user/getUserById/${userId}`, {
        //         "userId": userId});
        // } catch (error) {
        //     this.handleError("getUserById", error, errorCallback);
        // }

        return await this.client.get(`/user/getUserById/${userId}`);
    }

    /**
     * Helper method to log the error and run any error functions.
     * @param error The error received from the server.
     * @param errorCallback (Optional) A function to execute if the call fails.
     */
    handleError(method, error, errorCallback) {
        console.error(method + " failed - " + error);
        if (error.response.data.message !== undefined) {
            console.error(error.response.data.message);
        }
        if (errorCallback) {
            errorCallback(method + " failed - " + error);
        }
    }
}
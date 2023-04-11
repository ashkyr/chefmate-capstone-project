import { createAuth0Client } from '@auth0/auth0-spa-js';
import ChefMateClient from "../api/chefMateClient";
import DataStore from "../util/DataStore";
import HomePage from "../pages/homePage";



let auth0Client = null;

const configureClient = async () => {
  auth0Client = await createAuth0Client({
    domain: process.env.CAPSTONE_DOMAIN,
    clientId: process.env.CAPSTONE_CLIENT_ID,
    authorizationParams: {
      redirect_uri: window.location.origin
    }
  });
};


const setup = async () => {
  console.log("About to call configureClient");
  await configureClient();
  // Update the UI state
  await updateUI();

  // Check for the code and state parameters
  const query = window.location.search;
  console.log(query);
  if (query.includes("state=") &&
      (query.includes("code=") ||
          query.includes("error="))) {

    console.log(query);
    // Process the login state
    await auth0Client.handleRedirectCallback();

    await updateUI();

    // Use replaceState to redirect the user and remove the querystring parameters
    window.history.replaceState({}, document.title, "/");
  }
};

//redirect to the Universal Login Page
document.getElementById('login').addEventListener('click', async () => {
  await auth0Client.loginWithRedirect();
});

const redirectSetup = async () => {
  try {
    //logged in. Get the user profile:
    const user = await auth0Client.getUser();
    console.log(user);

  } catch (error) {
    if (error.message === 'There are no query params available for parsing.') {
      console.error('Authentication error: missing query parameters. Please try logging in again.');
    } else {
      console.error('Authentication error:', error);
    }
  }
};

window.addEventListener('load', async () => {
  await setup();
  await redirectSetup();
});

const updateUI = async () => {
  const isAuthenticated = await auth0Client.isAuthenticated();
  const userProfile = await auth0Client.getUser();

  document.getElementById("logout").disabled = !isAuthenticated;
  document.getElementById("login").disabled = isAuthenticated
  // Use the element with id "profile" in the DOM
  const profileElement = document.getElementById("profile");

  if (isAuthenticated) {

    console.log(userProfile);

    // save only the userId to the frontend datastore for easy access
    const dataStore = new DataStore();
    dataStore.set("userId", userProfile.email);
    await addNewUser(userProfile.email);


    console.log(dataStore.get("userId"));

    profileElement.style.display = "block";
    profileElement.innerHTML = `
            <p>${userProfile.name}</p>
            <img src="${userProfile.picture}" alt="" />
          `;
  } else {
    profileElement.style.display = "none";
  }


console.log("UI updated");
};


// Attach logout event listener to HTML element
document.getElementById('logout').addEventListener('click', () => {
  // Call logout method with returnTo option
  auth0Client.logout({
    returnTo: window.location.origin
  });
});
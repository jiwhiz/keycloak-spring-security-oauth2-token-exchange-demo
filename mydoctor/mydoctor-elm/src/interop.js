import Keycloak from 'keycloak-js';

var keycloak = new Keycloak({
    url: 'http://auth.mydoctor:8080',
    realm: 'mydoctor-demo',
    clientId: 'mydoctor-elm',
});



// This is called BEFORE your Elm app starts up
// 
// The value returned here will be passed as flags 
// into your `Shared.init` function.
export const flags = ({ env }) => {
}

// This is called AFTER your Elm app starts up
//
// Here you can work with `app.ports` to send messages
// to your Elm application, or subscribe to incoming
// messages from Elm
export const onReady = ({ app, env }) => {
    console.log("onReady");
    

    // Listen to the Elm 'login' port
    if (app.ports && app.ports.login) {
        app.ports.checkAuth.subscribe( () => {
            console.log("app.ports.login.checkAuth()");
            // Initialize Keycloak
            keycloak
            .init({
                onLoad: 'check-sso',
                silentCheckSsoRedirectUri:
                    window.location.origin + '/.elm-land/server/silent-check-sso.html'
            })
            .then(function (result) {
                console.log("After init: " + result);
                if (keycloak.authenticated) {
                    console.log("Authenticated. Send the access token back to Elm");
                    app.ports.onLoginSuccess.send(keycloak.token);
                }
            });
        });

        app.ports.login.subscribe( () => {
            console.log("app.ports.login.subscribe()");
            keycloak
            .login()
            .then(function () {
                console.log("After login: " + keycloak.authenticated);
                if (keycloak.authenticated) {
                    app.ports.onLoginSuccess.send(keycloak.token);
                }
            })
            .catch(function () {
                console.error('Failed to login Keycloak');
            });
        })
    
        app.ports.logout.subscribe( () => {
            console.log("Call logout()");
            keycloak
            .logout()
            .then(function () {
                console.log("After logout: " + keycloak.authenticated);
            })
            .catch(function (err) {
                console.error('Failed to logout Keycloak');
                console.error(err);
            });
        })
    }
}
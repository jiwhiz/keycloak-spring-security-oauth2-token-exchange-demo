module Pages.Home_ exposing (Model, Msg, page)

import Effect exposing (Effect, loginWithKeycloak, logoutFromKeycloak, onLoginSuccess)
import Html exposing (Html, br, button, div, text)
import Html.Events exposing (onClick)
import Http
import Page exposing (Page)
import Route exposing (Route)
import Shared
import View exposing (View)


page : Shared.Model -> Route () -> Page Model Msg
page _ _ =
    Page.new
        { init = init
        , subscriptions = subscriptions
        , update = update
        , view = view
        }



-- INIT


type alias Model =
    { isLoggedIn : Bool
    , accessToken : Maybe String
    , apiResponse : Maybe String
    }


init : () -> ( Model, Effect Msg )
init _ =
    ( { isLoggedIn = False, accessToken = Nothing, apiResponse = Nothing }
    , Effect.none
    )



-- UPDATE


type Msg
    = Login
    | LoginSuccess String
    | CallApi
    | ReceiveApiResponse (Result Http.Error String)
    | Logout


update : Msg -> Model -> ( Model, Effect Msg )
update msg model =
    case msg of
        Login ->
            ( model, Effect.loginWithKeycloak )

        Logout ->
            ( { model | isLoggedIn = False, accessToken = Nothing }
            , Effect.logoutFromKeycloak
            )

        LoginSuccess token ->
            let
                _ =
                    Debug.log "LoginSuccess" token
            in
            ( { model | isLoggedIn = True, accessToken = Just token }, Effect.none )

        CallApi ->
            case model.accessToken of
                Just token ->
                    let
                        request =
                            Http.request
                                { method = "GET"
                                , headers = [ Http.header "Authorization" ("Bearer " ++ token) ]
                                , url = "http://api.mydoctor:8081/api/records"
                                , body = Http.emptyBody
                                , expect = Http.expectString ReceiveApiResponse
                                , timeout = Nothing
                                , tracker = Nothing
                                }
                    in
                    ( model, Effect.sendCmd request )

                Nothing ->
                    ( model, Effect.none )

        ReceiveApiResponse result ->
            case result of
                Ok response ->
                    ( { model | apiResponse = Just response }, Effect.none )

                Err error ->
                    ( { model | apiResponse = Just ("Error: " ++ Debug.toString error) }, Effect.none )



-- Subscriptions


subscriptions : Model -> Sub Msg
subscriptions _ =
    onLoginSuccess LoginSuccess



-- VIEW


view : Model -> View Msg
view model =
    { title = "My Doctor"
    , body =
        [ if model.isLoggedIn then
            button [ onClick Logout ] [ text "Logout" ]

          else
            button [ onClick Login ] [ text "Login" ]
        , br [] []
        , button [ onClick CallApi ] [ text "Call backend API" ]
        , case model.apiResponse of
            Just response ->
                div [] [ text ("API Response: " ++ response) ]

            Nothing ->
                text ""
        ]
    }

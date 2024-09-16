# mydoctor-elm

This is another SPA Web UI written in [Elm language](https://elm-lang.org/), 
and built with [Elm Land](https://elm.land) 🌈

## Setup
**Prerequisite**: [Node.js](https://nodejs.org/) (v18.16.0 or higher)

Install Elm Land:

```
npm install -g elm-land@latest
```

You also need to add a new client for this Web UI. Open `http://auth.mydoctor:8080`, and login with `admin/admin`.
Under the `mydoctor-demo` realm, create a new client `mydoctor-elm` for MyDoctor's elm frontend.
Use `http://mydoctor:1234` for all URL settings. Don't forget to append `/*` to *Valid redirect URIs* and 
*Valid post logout redirect URIs*.
Add client role `edit-appointment` and `view-appointment`. Turn off Client authentication.

## Local development

```bash
HOST=mydoctor elm-land server
```

Then you can open [http://mydoctor:1234](http://mydoctor:1234), and click `Login` button to login through Keyloak.
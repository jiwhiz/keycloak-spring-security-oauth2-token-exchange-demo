# keycloak-spring-security-oauth2-token-exchange-demo
This is the code repository for my blog 
[OAuth 2 Token Exchange with Spring Security and Keycloak](https://dev.to/jiwhiz/oauth-2-token-exchange-with-spring-security-and-keycloak-1a6i).

It demonstrates how **MyHealth** users can log in to **MyDoctor** using 
their **MyHealth** credentials, and how **MyDoctor**'s backend can 
securely call **MyHealth**'s APIs on behalf of the user using OAuth 2
[Token Exchange](https://datatracker.ietf.org/doc/html/rfc8693).


## How to run demo locally

### Install Docker, Java 21, NodeJS
In order to setup local demo environment, you need to install
[Docker Desktop](https://docs.docker.com/desktop/) if you didn't have it in your computer.

Install Java 21 by [sdkman](https://sdkman.io/), and switch to JDK 21,
like `sdk use java 21.0.4-oracle`.

Install [NodeJS](https://nodejs.org/).
Install [Angular cli](https://angular.dev/tools/cli).

### Add custom domain to /etc/hosts
We have six servers running lcoally, and if we all use `localhost` with different port numbers, it will mess up browser cookies. So we use custom domain for each server, and add following to your `/etc/hosts` file:

```
127.0.0.1 mydoctor
127.0.0.1 api.mydoctor
127.0.0.1 auth.mydoctor
127.0.0.1 myhealth
127.0.0.1 api.myhealth
127.0.0.1 auth.myhealth
```

For windows, add to `%windir%\system32\drivers\etc\hosts` file.

### Start Keycloak servers with Docker
Run
```
docker compose up -d
```

You can login to **MyDoctor** Keycloak server at [http://auth.mydoctor:8080](http://auth.mydoctor:8080) and **MyHealth** Keycloak server at [http://auth.myhealth:8090](http://auth.myhealth:8090) with `admin` as username and password.

### Start MyHealth API server and Web App
Under `./myhealth/myhealth-api`, run
```
./gradlew bootRun
```

Under `./myhealth/myhealth-ui`, run
```
npm install
ng serve
```

You can open [http://myhealth:4210](http://myhealth:4210) and click 
`login` button to redirect to Keycloak login. Use `john` as username
and password to login. Click button `call http://api.myhealth:8082/api/records`, 
and you should see result of three records in console.

### Start MyDoctor API server and Web App
Under `./mydoctor/mydoctor-api`, run
```
./gradlew bootRun
```

Under `./mydoctor/mydoctor-ui`, run
```
npm install
ng serve
```

You can open [http://mydoctor:4200](http://mydoctor:4200) and click 
`login` button to redirect to Keycloak login. 
Select signin with `MyHealth Keycloak`, and enter `john` as username
and password, you actually login with **MyHealth** account to **MyDoctor**.

If you click button `call http://api.mydoctor:8081/api/records`,
you should see result of three records in console.


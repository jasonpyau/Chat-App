# Chat App

## Table of Contents
<ol>
  <li><a href="#about">About</a></li>
  <li><a href="#demo">Demo</a></li>
  <li><a href="#get-started">Get Started</a></li>
</ol>


## About
This is a relatively simple chat application created using Spring Boot in the backend and React/TypeScript in the frontend. 
It uses StompJS and SockJS (WebSocket) for real-time communication between users and MariaDB to persist data. 
Users can create an account via OAuth 2.0, create custom group chats, and send messages. 
This project takes some inspiration from older versions of Discord.

**Link:** <a href="https://chat.jasonpyau.com">https://chat.jasonpyau.com</a>
<br>

This website is hosted on my Raspberry Pi, using Cloudflare's free tunneling services for SSL certificate and DDoS protection and GitHub Actions for continuous deployment on Git push.

## Demo

![image](https://github.com/jasonpyau/Chat-App/assets/113565962/7408d627-3521-4f42-8b14-d30553d97af5)


<br>

## Get Started
**(Ubuntu)**

*All you need to do to get the app running is run the Spring Boot project; it automatically bundles up the React scripts via Webpack.* 

**Create secrets.properties (./backend/src/main/resources/secrets.properties)** -
<a href="https://github.com/jasonpyau/Chat-App/blob/main/backend/src/main/resources/secrets.properties.sample">secrets.properties.sample</a>

```
MARIADB_SERVER_URL=YOUR_MARIADB_SERVER_URL_HERE
MARIADB_DATABASE_NAME=YOUR_MARIADB_DATABASE_NAME_HERE
MARIADB_USERNAME=YOUR_MARIADB_USERNAME_HERE
MARIADB_PASSWORD=YOUR_MARIADB_PASSWORD_HERE

OAUTH_GOOGLE_CLIENT_ID=YOUR_OAUTH_GOOGLE_CLIENT_ID_HERE
OAUTH_GOOGLE_CLIENT_SECRET=YOUR_OAUTH_GOOGLE_CLIENT_SECRET_HERE
OAUTH_GITHUB_CLIENT_ID=YOUR_OAUTH_GITHUB_CLIENT_ID_HERE
OAUTH_GITHUB_CLIENT_SECRET=YOUR_OAUTH_GITHUB_CLIENT_SECRET_HERE
OAUTH_DISCORD_CLIENT_ID=YOUR_OAUTH_DISCORD_CLIENT_SECRET_HERE
OAUTH_DISCORD_CLIENT_SECRET=YOUR_OAUTH_DISCORD_CLIENT_SECRET_HERE
```

**Run Spring Boot Project**
```
sudo apt update

sudo apt install openjdk-17-jdk

cd ./backend

sudo bash ./mvnw spring-boot:run
```

**Update Webpack bundle on save**
```
cd ./frontend

npm run watch
```
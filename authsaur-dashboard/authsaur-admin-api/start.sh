#!/bin/bash
nohup java -jar authsaur-admin-api-0.0.1-SNAPSHOT.jar --spring.config.location=file:./application.properties > api.log &
tail -f api.log
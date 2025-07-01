.PHONY: all build down re

all: build copy up

build:
	@./gradlew clean build -x test

copy:
	@cp ./build/libs/dyslexia-0.0.1-SNAPSHOT.jar ./app.jar

up:
	@docker compose up --build -d

down:
	@docker compose down

re: down up
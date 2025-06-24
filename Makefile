.PHONY: all build down re

COMPOSE_FILES = -f docker-compose.yml -f docker-compose.local.yml
PROJECT_NAME = -p test

all: build copy up

build:
	@./gradlew clean build -x test

copy:
	@cp ./build/libs/dyslexia-0.0.1-SNAPSHOT.jar ./app.jar

up:
	@docker compose $(COMPOSE_FILES) $(PROJECT_NAME) up --build -d

down:
	@docker compose $(COMPOSE_FILES) $(PROJECT_NAME) down

re: down up
clean:
	@echo "Cleaning app binary ..."
	@mvn clean
	@echo "Cleaning app binary [OK]"
	@echo "Cleaning docker data volumes ..."
	@sudo rm -rf data
	@echo "Cleaning docker data volumes [OK]"

up_build:
	@echo "Cleaning and building app binary ..."
	@mvn clean package -DskipTests=true
	@echo "Cleaning and building app binary [OK]"
	@echo "Building and starting app container ..."
	@docker compose up --build
	@echo "Building and starting app container [OK]"

clean_build_up:
	@echo "Cleaning app binary ..."
	@mvn clean
	@echo "Cleaning app binary [OK]"
	@echo "Cleaning docker data volumes ..."
	@sudo rm -rf data
	@echo "Cleaning docker data volumes [OK]"
	@echo "Cleaning and building app binary ..."
	@mvn clean package -DskipTests=true
	@echo "Cleaning and building app binary [OK]"
	@echo "Building and starting app container ..."
	@docker compose up --build
	@echo "Building and starting app container [OK]"
up:
	@docker compose up
	@echo "Building and starting app container [OK]"

down:
	@echo "Removing app container ..."
	@docker compose down
	@echo "Removing app container [OK]"

log:
	@docker compose logs -f

ps:
	@docker compose ps

DOCKER_IMAGE := prambudi/demo-service-backend-develop:1.1.0
OLD_DOCKER_IMAGE :=
DOCKER_LOGGED_IN := $(shell docker info >/dev/null 2>&1 && echo 1 || echo 0)

push: update-image
ifeq ($(DOCKER_LOGGED_IN),1)
	@echo "docker push to docker hub ..."
	@docker push $(DOCKER_IMAGE)
else
	@echo "docker login"
	@docker login
endif

update-image:
ifneq ($(DOCKER_IMAGE),$(OLD_DOCKER_IMAGE))
	@echo "Updating Docker image in Makefile..."
	@sed -i 's|^DOCKER_IMAGE := .*|DOCKER_IMAGE := $(DOCKER_IMAGE)|' Makefile
	$(eval OLD_DOCKER_IMAGE=$(DOCKER_IMAGE))
else
	@echo "No change in DOCKER_IMAGE. Skipping update."
endif
.PHONY: help build start stop down bash logs

help:
	@cat Makefile

build:
	@docker compose up -d --build
	@docker compose exec -e XDEBUG_MODE=off flow composer install
	@docker compose exec -e XDEBUG_MODE=off flow ./flow doctrine:migrate
	@docker compose exec -e XDEBUG_MODE=off flow ./flow cache:warmup

start:
	@docker compose up -d

stop:
	@docker compose stop

down:
	@docker compose down -v --remove-orphans

bash:
	@docker compose exec flow /bin/bash

logs:
	@docker compose logs -f

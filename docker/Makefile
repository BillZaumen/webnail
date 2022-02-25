DIRECTORY = `pwd`

VERSION = 1.0

FILENAME =

PASSWORD =

docker:
	docker build --tag wtzbzdev/webnail:$(VERSION) .

start:
	docker run --publish 80:80 --detach --name webnail \
		-v "$(DIRECTORY)"/:/app/:ro \
		--env PASSWORD="$(PASSWORD)" \
		--env FILENAME="$(FILENAME)" \
		wtzbzdev/webnail:$(VERSION)

stop:
	@docker stop webnail || echo ... webnail not running
	@docker rm webnail  || echo ... no webnail to remove

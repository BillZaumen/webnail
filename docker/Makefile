DIRECTORY = `pwd`

VERSION = 1.1

FILENAME =

PASSWORD =

#
# Using --no-cache=true so apt-get will fetch the latest versions.
#
docker:
	docker build --no-cache=true --tag wtzbzdev/webnail:$(VERSION) .

docker-release:
	docker push wtzbzdev/webnail:$(VERSION)

start:
	docker run --publish 80:80 --detach --name webnail \
		-v "$(DIRECTORY)"/:/app/:ro \
		--env PASSWORD="$(PASSWORD)" \
		--env FILENAME="$(FILENAME)" \
		wtzbzdev/webnail:$(VERSION)

stop:
	@docker stop webnail || echo ... webnail not running
	@docker rm webnail  || echo ... no webnail to remove

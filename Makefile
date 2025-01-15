# 모듈 이름을 변수로 정의
MODULE = mongodb

dockerBuild:
	./gradlew $(MODULE):clean && ./gradlew $(MODULE):assemble && cd $(MODULE) && docker build --platform linux/arm64 -t $(MODULE) .

dockerRun:
    docker run -it -p 8080:8080 $(MODULE)
FROM ghcr.io/ndgndg91/giri-alpine-corretto21:0.0.1

WORKDIR /app
COPY ./build/libs/mongodb.jar /app/mongodb.jar
COPY run-java-options /opt/

# JVM 초기 메모리와 최대 메모리 비율 설정
ENV JAVA_MAX_MEM_RATIO=50
ENV JAVA_INIT_MEM_RATIO=25
ENV MONGO_CONN_URI=""

EXPOSE 8080
ENTRYPOINT ["./run-java.sh"]
FROM ghcr.io/ndgndg91/giri-alpine-corretto21:0.0.1

WORKDIR /app
COPY ./build/libs/mongodb.jar /app/mongodb.jar
COPY run-java-options /opt/
#COPY run-java.sh .
#RUN chmod +x run-java.sh
#RUN sed -i 's/#networkaddress.cache.ttl=.*/networkaddress.cache.ttl=60/' $JAVA_HOME/conf/security/java.security

# JVM 초기 메모리와 최대 메모리 비율 설정
ENV JAVA_MAX_MEM_RATIO=50
ENV JAVA_INIT_MEM_RATIO=25

EXPOSE 8080
ENTRYPOINT ["./run-java.sh"]
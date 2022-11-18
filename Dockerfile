FROM openjdk:8-jdk-alpine as builder
ARG APP_NAME
MAINTAINER wf2311 "wf2311@163.com"
WORKDIR application
COPY arthas-tunnel-web/target/${APP_NAME}.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:8-jdk-alpine
MAINTAINER wangfeng1 "wf2311@163.com"
WORKDIR application
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/wf2311-lib-dependencies/ ./
COPY --from=builder /application/wf2311-app-dependencies/ ./
COPY --from=builder /application/application/ ./
ADD bin/docker-startup.sh bin/startup.sh

ENV JVM_OPTS '-Xmx256m -Xms256m -Xss256k'
ENV JVM_AGENT ''
ENV NACOS_ADDR 'localhost:8848'
ENV NACOS_NAMESPACE 'public'
ENV SERVER_PORT 9999
ENV TUNNEL_SERVER_PORT 9999
EXPOSE ${SERVER_PORT}
EXPOSE ${TUNNEL_SERVER_PORT}

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo 'Asia/Shanghai' >/etc/timezone \
    && mkdir logs \
    && cd logs \
    && touch start.out \
    && ln -sf /dev/stdout start.out \
    && ln -sf /dev/stderr start.out

RUN chmod +x bin/startup.sh
ENTRYPOINT [ "sh", "-c", "sh bin/startup.sh"]


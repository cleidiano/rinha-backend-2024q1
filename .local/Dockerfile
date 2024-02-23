# Builder
FROM amazoncorretto:21-alpine3.15-jdk AS builder
RUN mkdir -p /tmp/app
RUN apk add unzip
COPY build/distributions/rinha-backend-2024-0.0.1.zip  /tmp/app.zip
RUN unzip /tmp/app.zip -d /tmp/release

FROM  amazoncorretto:21.0.2-alpine3.19

COPY --from=builder /tmp/release /opt/app/

WORKDIR /opt/app
RUN rm -rf /var/cache/*

EXPOSE 8080

CMD ["/opt/app/rinha-backend-2024-0.0.1/bin/rinha-backend-2024"]

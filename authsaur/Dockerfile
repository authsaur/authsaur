FROM adoptopenjdk/openjdk11:alpine-slim AS overlay


FROM adoptopenjdk/openjdk11:alpine-jre AS cas

LABEL "Organization"="Apereo"
LABEL "Description"="Apereo CAS"

RUN cd / \
    && mkdir -p /etc/cas/config \
    && mkdir -p /etc/cas/services \
    && mkdir -p /etc/cas/saml \
    && mkdir -p cas-overlay;

# COPY --from=overlay cas-overlay/build/libs/cas.war cas-overlay/
COPY etc/cas/ /etc/cas/
COPY etc/cas/config/ /etc/cas/config/
# COPY etc/cas/services/ /etc/cas/services/
# COPY etc/cas/saml/ /etc/cas/saml/
COPY cas.war cas-overlay/

EXPOSE 8080 8443

ENV PATH $PATH:$JAVA_HOME/bin:.

WORKDIR cas-overlay
ENTRYPOINT ["java", "-server", "-noverify", "-Xmx2048M", "-jar", "cas.war"]

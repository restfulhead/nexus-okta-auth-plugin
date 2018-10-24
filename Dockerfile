FROM sonatype/nexus3

USER root

ARG OKTA_ORG_URL=https://your-org-here.okta.com

# install plugins
COPY target/nexus-okta-auth-plugin-0-SNAPSHOT.jar /opt/sonatype/nexus/system/nexus-okta-auth-plugin.jar
RUN echo "reference\:file\:nexus-okta-auth-plugin.jar = 200" >> /opt/sonatype/nexus/etc/karaf/startup.properties && \
    touch /opt/sonatype/nexus/etc/nexus-okta-auth.properties && \
    echo "okta.org.url=${OKTA_ORG_URL}" >> /opt/sonatype/nexus/etc/nexus-okta-auth.properties && \
    chown nexus:nexus -R /opt/sonatype/nexus

USER nexus
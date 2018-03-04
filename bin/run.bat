chcp 65001
java -jar -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Dspring.profiles.active=dev lib/ShadowSocks-Share-0.0.1-SNAPSHOT.jar --spring.config.location=config/application-dev.yml
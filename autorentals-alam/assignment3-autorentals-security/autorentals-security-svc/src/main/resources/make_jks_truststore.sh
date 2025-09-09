#!/bin/sh

# make the private keystore used by the server


keytool -genkeypair -keyalg RSA -keysize 2048 -validity 3650 -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -dname  "CN=localhost,OU=Unknown,O=Unknown,L=Unknown,ST=Unknown,C=Unknown" -keystore keystore.p12 -alias https-hua -storepass password123

# make the public truststore used by the client 
# use openssl in windows ubuntu wsl environment, 
# share file from windows to Ubuntu wsl - open explorer , write - \\wsl$\Ubuntu in the address bar
# certs only, no private keys , no private keys, not password protected 
# PEM file is the text file with Base64 Encoded certificate information


openssl pkcs12 -in ./src/main/resources/keystore.p12 -passin pass:password123 -out ./target/certs.pem  -clcerts -nokeys -nodes

# package and run jar

# build and package jar by running unit and inbtegration test
mvn package

##  build and package jar without running unit and integration test

mvn package -DskipTests


# run the jar using https profile to enable  https/tls
java -jar  ./target/autorentals-security-svc-1.0-SNAPSHOTbootexec.jar     --spring.profiles.active=authorities,authorization,https


#test
# Curl accepting the server certificate after added to trusted sources
curl -k -X GET https://localhost:8443/api/whoAmI -u "mary:secret" && echo mary
$ curl -k -X DELETE https://localhost:8443/api/autos -u mary:secret 
$ curl -k -X DELETE https://localhost:8443/api/autos -u sueann:betty
{"timestamp":"2022-09-25T00:51:41.200+00:00","status":403,"error":"Forbidden","path":"/api/autos"}


$ curl -k -X DELETE https://localhost:8443/api/autos -u sueann:betty  --cacert  ./target/certs.pem


#Extract the public cert from the keystore:

keytool -exportcert -alias https-hua  -storetype PKCS12  -keystore keystore.p12 -storepass password123  -rfc -file springboot.crt
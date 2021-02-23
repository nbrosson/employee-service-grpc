# Dependencies

- Java 14
- protocol buffer installed (3.2 in this project)
- openssl 


## Setup SSL certificate

For local dev, Common Name should be localhost. 
```
openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 365 -nodes 
```


## Generate source code

I generated the source code with Maven on Intellij. 

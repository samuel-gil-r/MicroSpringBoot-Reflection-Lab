# MicroSpringBoot Framework

A web framework developed in Java using reflection to register and execute REST services via annotations, with an HTTP server capable of serving static files such as HTML and PNG images.

Developed as part of the Digital Transformation and Business Solutions course
Colombian School of Engineering Julio Garavito
Author: Samuel Antonio Gil Romero — 2026

---

## Getting Started

### Prerequisites


java -version (JDK 17+)
mvn -version (Maven 3.8+)
git --version


### Installing


git clone https://github.com/
<your-username>/MicroSpringBoot-Reflection-Lab.git
cd MicroSpringBoot-Reflection-Lab
mvn clean package


### Running


java -cp target/classes org.example.framework.MicroSpringBoot3 org.example.app.HelloController


Expected console output:


Loading rest controllers and their methods...
Registered: GET / -> index
Registered: GET /pi -> getPI
Registered: GET /hello -> hello
Server started on port 8080
Listo para recibir ...


---

# Architecture

```
org.example
├── annotations
│   ├── GetMapping.java        
│   ├── RequestParam.java     
│   └── RestController.java    
├── app
│   ├── HelloController.java   ← endpoints
│   └── GreetingController.java
├── framework
│   ├── MicroSpringBoot3.java  
│   └── InvokeMain.java        
├── http
│   ├── HttpRequest.java
│   ├── HttpResponse.java
│   ├── HttpServer.java
│   └── StaticFileHandler.java
└── resources/webroot
    ├── index.html
    └── img/tierra.png
```


---

## Running the Tests

### End to end tests


java -cp target/classes org.example.framework.MicroSpringBoot3 org.example.app.HelloController


| URL | Expected response |
|-----|-------------------|
| `http://localhost:8080/index.html` | Página HTML con imagen PNG |
| `http://localhost:8080/` | `Greetings from Spring Boot!` |
| `http://localhost:8080/hello` | `Hello World!` |
| `http://localhost:8080/pi` | `PI = 3.141592653589793` |
| `http://localhost:8080/greeting?name=Samuel` | `Hola Samuel` |

### Unit tests


mvn test


### Test evidence    

#### Ejecución en AWS EC2
<img width="822" height="539" alt="image" src="https://github.com/user-attachments/assets/29c73e42-47a9-40a5-8826-59058ed458d9" />

<img width="788" height="234" alt="image" src="https://github.com/user-attachments/assets/9a6ecdda-b88d-4ad6-9c8d-9212dc0c12dc" />

#### index.html con imagen PNG — AWS  

<img width="779" height="599" alt="image" src="https://github.com/user-attachments/assets/18e9a591-2eee-486c-a9a4-243fedf68cc8" />  

#### GET /hello
<img width="669" height="163" alt="image" src="https://github.com/user-attachments/assets/156759d1-995f-40be-815e-78e810bd09bf" />

#### GET /pi

<img width="706" height="143" alt="image" src="https://github.com/user-attachments/assets/73492273-93c4-441b-a2e5-f1a76d0b56e5" />

---

## Deployment on AWS

1. Compilar y comprimir:


mvn clean package
cd target/classes
zip -r classes.zip .


2. Go up to EC2:


sftp -i Tdse.pem ec2-user@<ec2-dns>
put classes.zip
exit


3. Connect and run:


ssh -i Tdse.pem ec2-user@<ec2-dns>
unzip -o classes.zip -d classes
java -cp classes/classes org.example.framework.MicroSpringBoot3 org.example.app.HelloController


4.

# AWS EC2 Configuration

The application was deployed on an AWS EC2 instance using Linux.

Instance details:

- Instance type: t3.micro
- Operating system: Linux (Amazon Linux)
- Public IPv4 address: 34.229.214.12
- Public DNS: ec2-34-229-214-12.compute-1.amazonaws.com

The application runs on port 8080, so it was necessary to allow that port in the EC2 security group.

Inbound rule added:

| Type | Protocol | Port | Source |
|-----|-----|-----|-----|
| Custom TCP | TCP | 8080 | 0.0.0.0/0 |

This allows the application to be accessed from the browser.

<img width="1919" height="835" alt="image" src="https://github.com/user-attachments/assets/63f11e00-668d-4310-9c61-8277617b21f3" />

5. Probar desde el navegador:


http://<ec2-dns>:8080/index.html


---

## Built With

* Java Sockets — servidor HTTP
* Java Reflection — registro y ejecución de endpoints
* Maven — compilación del proyecto

---

## Conclusions

this framework from the ground up has allowed for a greater understanding of the inner workings of current frameworks like Spring Boot. Thanks to the use of Java Reflection, it has been possible to dynamically load classes, inspect annotations, and invoke methods at runtime without needing to know them at compile time.

The project has shown that a functional IoC container can be built with Java Core, using annotations as metadata for REST endpoint descriptions and automatically connecting components.
The distribution of static files, along with dynamic endpoints, has demonstrated how a real web server can handle different types of requests in a unified pipeline.
The implementation on AWS EC2 has strengthened the understanding of how web applications are exposed to the internet, from network configuration and port management to remote execution via SSH.

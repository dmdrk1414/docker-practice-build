# 배포 과정 정리

**practice docker compose build : https://github.com/dmdrk1414/docker-practice-build**

**practice docker compose : https://github.com/dmdrk1414?tab=repositories**

# 🗒️ 목차

## 1. 폴더 구조 (중요하니 꼼꼼하게 보세요.)

## 2. 배포 구상도 (전체적인 그림)

## 3. Docker-compose 명령어

## 4. frontend : conection [ docker-nginx   <-> docker-next.js ] include docker compose

## 5. backend : conection [ docker-nginx   <-> docker-spring boot 3 ] include docker compose

## 6. Database:mysql : conection [ docker-spring boot 3   <-> docker-mysql ] 



# 😱 주의

Docker-compose.yml 파일에 계속추가를 하니 목차대로 진행을 하고 중간에 이름, 변수명이 변경이 되어도 당황하지 말고 오류를 수정하면서 해보세요.

Dockerfile의 위치를 생각하면서 작업하세요



# 🌲 #1. 폴더 구조

배포 과정중 폴더의 구조는 중요하니 잘보고 하세요.

```
├── backend
│   └── spring-boot-3
│       └─── Dockerfile
├── database
│   └── mysql
│       └─── Dockerfile
├── frontend
│   └─── Dockerfile
├── nginx
│   ├── Dockerfile
│   └── nginx.conf
│       └── nginx.conf
└── docker-compose.yml
```



# 🖌️ 2. 배포 구상도 (전체적인 그림)

<img src="/Users/seungchan/Library/Application Support/typora-user-images/image-20230826230246643.png" alt="image-20230826230246643" style="zoom:80%;" />

# 3. Docker compose 명령어

### **여러 컨테이너 시작/정지/재시작(start/stop/restart)**

```
# docker compose을 실행 
# container 생성과 container의 실행을한다.
docker compose up

# docker compose을 실행을 하되 변경된 compose service(container) 만 다시 실행됩니다.
docker compose up --build. 

# docker compose을 실행
docker compose start

# docker compose을 중지
docker compose stop

# docker compose을 삭제
docker compose down
```



| up      | 컨테이너 생성/시작           |
| ------- | ---------------------------- |
| ps      | 컨테이너 목록 표시           |
| logs    | 컨테이너 로그 출력           |
| run     | 컨테이너 실행                |
| start   | 컨테이너 시작                |
| stop    | 컨테이너 정지                |
| restart | 컨테이너 재시작              |
| pause   | 컨테이너 일시 정지           |
| unpause | 컨테이너 재개                |
| port    | 공개 포트 표시               |
| config  | 구성 확인                    |
| kill    | 실행 중인 컨테이너 강제 정지 |
| rm      | 컨테이너 삭제                |
| down    | 리소스 삭제                  |

# 4. Frontend:nextjs : conection [ docker-nginx   <-> docker-next.js ] include docker compose

1. **준비 사항**
2. **frontend Dockerfile 파일 설정**
3. **frontend-nginx Docker-compose.yml 파일 설정**
4. **Nginx의 config 파일 만들기**
5. **Nginx의 Dockerfile 만들기**
6. **docker compose 실행**

### 4.1. frontend 준비 사항

1. 준비 사항은 next.js 에서 빌드를 해야 됩니다.

```
npm run build
```

2. Build을 시행 하면 .next 폴더에 빌드한 내용들이 나옵니다.
3. 준비는 끝났습니다.

### 4.2. frontend Dockerfile 파일 설정

```
# frontend/Dockerfile

# docker container의 바탕이 되는 image의 이름이다.
# alpine은 Alpine Linux는 매우 작고 경량화된 배포판
FROM node:16.18.0-alpine

# 이미지 내에서 명령어를 실행할(현 위치로 잡을) 디렉토리 설정
WORKDIR /home/node/next

# Copy the rest of the application files to the container
# Docker 이미지를 빌드할 때 파일이 컨테이너 내에 복사됩니다.
# 현재 폴더 (docker file)이 있는 파일의 전체를 (frontend)폴더을 컨테이너 WORKDIR의 안에 복사를 한다.
COPY . .

# Build the production version of the app
# 컨테이너가 시작되는 시점에서 실행이 된다.
RUN npm run build


# Copy the rest of the application files to the container
# Docker 이미지를 빌드할 때 파일이 컨테이너 내에 복사됩니다.
# 현재 폴더 (docker file)이 있는 파일의 전체를 (frontend)폴더을 컨테이너 WORKDIR의 안에 복사를 한다.
COPY . .

# 명령은 컨테이너가 시작될 때 실행됩니다.
CMD [ "npm" ,  "start"]
```

### 4.3. frontend-nginx Docker-compose.yml 파일 설정

Docker-compose.yml 파일 수정하기

```
# practice-build/docker-compose.yml
# /docker-compose.yml
# docker-compose의 버전
version: '3'
# 서비스의 정의 서비스의 이름은 각자 정할수 있습니다.
# docker-compose 가 관리해야되는 서비스(container)들을 정의
services:
  # nextjs의 서비스 정의
  nextjs:
    build: ./frontend
    #컨테이너 다운 시 재시작하라는 명령어
    restart: always
    # 불룸 로컬과 도커 마운트 설정
    # 로컬 폴더와 컨터이너안의 임이의 폴더와 마운트
    volumes:
      - ./frontend:/home/node/next
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 3000번 포트와 컨테이너의 3000번 포트를 매핑합니다. 
    ports:
      - "3000:3000"

  nginx:
    # Dockerfile이 있는 위치
    build: ./nginx
    #컨테이너 다운 시 재시작하라는 명령어
    restart: always
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 80번 포트와 컨테이너의 80번 포트를 매핑합니다.
    ports:
      - "80:80"
    # depends_on은 설정 서비스 명이 nextjs, pring-boot-3 이 먼저 실행되고 nginx의 서비스가 먼저 실행된다.
    depends_on:
      - nextjs
      - spring-boot-3
```

## 4.4 Nginx의 config 파일 만들기

```
# /nginx/nginx.conf

upstream frontend {
    # 사용시 변경 향후 컨테이너이름:포트번호
    # max_fails 3;은 3번의 연속 실패 후에 서버를 '망가진' 상태로 표시합니다.
    # fail_timeout 10s;는 서버가 실패한 후 10초 동안 망가진 상태로 유지되며, 이 기간 동안 추가 연결 시도가 거부됩니다.
    server practice-build-frontend-1:3000 max_fails=3 fail_timeout=10s; 
}

server {
    # listen 80;: 이 설정은 Nginx가 IPv4 주소를 사용하여 80번 포트에서 들어오는 
    # HTTP 요청을 수신하도록 지시합니다. 
    # 80번 포트는 일반적으로 HTTP 트래픽을 처리하는 데 사용됩니다.
    listen 80;
    # listen [::]:80;: 이 설정은 Nginx가 IPv6 주소를 지원하며, 
    # IPv6 주소에서 80번 포트에서 들어오는 HTTP 요청도 수신하도록 지시합니다. 
    listen [::]:80;

    location / {        
        # proxy_pass는 클라이언트의 요청을 이 업스트림 서버로 전달합니다.
        proxy_pass http://frontend;

        # HTTP 버전 1.1을 사용하도록 Nginx에 지시합니다.
        proxy_http_version 1.1;

        # 이 설정은 HTTP 업그레이드 헤더를 설정합니다. 
        # $http_upgrade는 클라이언트의 요청에서 업그레이드 헤더의 값을 가져와서 
        # 백엔드 서버로 전달합니다. 
        # 이렇게 함으로써 웹 소켓과 같은 업그레이드 요청을 올바르게 처리할 수 있습니다.
        proxy_set_header Upgrade $http_upgrade; 

        # Connection 헤더를 'upgrade'로 설정합니다. 
        # 이것은 HTTP 연결을 업그레이드하도록 요청하는 데 사용됩니다.
        proxy_set_header Connection 'upgrade'; 

        # Host 헤더를 클라이언트의 원래 Host 헤더 값으로 설정합니다. 
        # 이것은 요청이 백엔드 서버에 도달할 때 올바른 호스트 이름으로 전달되도록 합니다.
        proxy_set_header Host $host;  
    }
}
```



## 4.5 Nginx의 Dockerfile 만들기

```
# docker container의 바탕이 되는 image의 이름이다.
FROM nginx:latest

# 이미지 생성 과정에서 실행할 명령어
# 이미지 생성 과정에서 npm 모듈을 설치가 된다.
# 기본 nginx 설정 파일을 삭제한다. (custom 설정과 충돌 방지)
RUN rm /etc/nginx/conf.d/default.conf

# host pc 의 nginx.conf 를 아래 경로에 복사
# Docker 이미지를 빌드할 때 파일이 컨테이너 내에 복사됩니다.
COPY ./nginx.conf /etc/nginx/conf.d

# 명령은 컨테이너가 시작될 때 실행됩니다.
CMD ["nginx", "-g", "daemon off;"]
```



## 4.6 docker compose 실행

이제 docker-nextjs(front) <-> docker-nginx 와의 연결이 되었습니다.

이제 docker compose 을 실행을 하면

```
# docker compose을 실행 
# container 생성과 container의 실행을한다.
docker compose up

# docker compose을 실행을 하되 변경된 compose service(container) 만 다시 실행됩니다.
docker compose up --build. 

# docker compose을 실행
docker compose start

# docker compose을 중지
docker compose stop

# docker compose을 삭제
docker compose down
```



# 5. backend : conection [ docker-nginx   <-> docker-spring boot 3 ] include docker compose

1. **준비 사항**
2. **bbackend springboot-3 Dockerfile 파일 설정**

3. ##### **backend(springbok)-nginx Docker-compose.yml 파일 설정**

4. **Nginx의 config 파일 만들기**

5. ###### **Nginx의 Dockerfile 만들기**

6. **docker compose 실행**

## 5.1 **준비 사항**

Spring-boot-3 에서 빌드 파일 *.jar 파일을 생성해야한다.

Spring-boot의 시작폴더에서 (docker 파일의 기준, 또는 spring boot에서 ./gradlew 폴더가 있는 기준)

1. 빌드하는 방법
   1. `./gradlew bootJar` 을하면 빌드 완성
2. 빌드한 .jar 파일 실행
   1. `./gradlew bootRun` 하라 basic 폴더에서

.jar파일이 `./build/libs/*.jar`에서 생성이 되었을 것이다.

나중에 `./build/libs` 폴더는 docker-compose 단계에서 container와 연결을 해줄것이다.

## 5.2 backend springboot-3 Dockerfile 파일 설정

```python
# backend/spring-boot-3/Dockerfile
# 지금 사용중인 이미지 이름
FROM amazoncorretto:17

# 이미지 내에서 명령어를 실행할(현 위치로 잡을) 디렉토리 설정
WORKDIR /home/spring-boot-3/build

# java build 파일 jar파일 복사
ADD ./build/libs/*.jar app.jar

# 8080 포트 오픈
EXPOSE 8080

# 컨테이너 실행시 실행할 명령어
# WORKDIR에서 실행된다.
# 실행하자마자 설정이 된다.
CMD ["java", "-jar", "app.jar"]
```

## 5.3 backend(springbok)-nginx Docker-compose.yml 파일 설정

```
# practice-build/docker-compose.yml
# /docker-compose.yml
# docker-compose의 버전
version: '3'
# 서비스의 정의 서비스의 이름은 각자 정할수 있습니다.
# docker-compose 가 관리해야되는 서비스(container)들을 정의
services:
  # nextjs의 서비스 정의
  nextjs:
    build: ./frontend
    #컨테이너 다운 시 재시작하라는 명령어
    restart: always
    # 불룸 로컬과 도커 마운트 설정
    # 로컬 폴더와 컨터이너안의 임이의 폴더와 마운트
    volumes:
      - ./frontend:/home/node/next
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 3000번 포트와 컨테이너의 3000번 포트를 매핑합니다. 
    ports:
      - "3000:3000"

  spring-boot-3:
    # Dockerfile이 있는 위치
    build: ./backend/spring-boot-3
    #컨테이너 다운 시 재시작하라는 명령어
    restart: always
    # 불룸 로컬과 도커 마운트 설정
    # 로컬 폴더와 컨터이너안의 임이의 폴더와 마운트
    volumes:
      - ./backend/spring-boot-3/build/libs:/home/spring-boot-3/build
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 8080번 포트와 컨테이너의 8080번 포트를 매핑합니다. 
    ports:
      - "8080:8080"

  nginx:
    # Dockerfile이 있는 위치
    build: ./nginx
    #컨테이너 다운 시 재시작하라는 명령어
    restart: always
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 80번 포트와 컨테이너의 80번 포트를 매핑합니다.
    ports:
      - "80:80"
    # depends_on은 설정 서비스 명이 nextjs, pring-boot-3 이 먼저 실행되고 nginx의 서비스가 먼저 실행된다.
    depends_on:
      - nextjs
      - spring-boot-3

  mysql:   
    # Dockerfile이 있는 위치
    build: ./database/mysql
    # build: ./database/mysql
    restart: always 
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 3306번 포트와 컨테이너의 33060번 포트를 매핑합니다. 
    ports: 
      - "3306:3306"
    # 테이블 이름 소문자로 맞춘다.
    volumes:
      - ./database/mysql/scripts:/docker-entrypoint-initdb.d
```



## 5.4 Nginx의 config 파일 만들기

```
# /nginx/nginx.conf

upstream frontend {
    # 사용시 변경 향후 컨테이너이름:포트번호
    # max_fails 3;은 3번의 연속 실패 후에 서버를 '망가진' 상태로 표시합니다.
    # fail_timeout 10s;는 서버가 실패한 후 10초 동안 망가진 상태로 유지되며, 이 기간 동안 추가 연결 시도가 거부됩니다.
    server practice-build-frontend-1:3000 max_fails=3 fail_timeout=10s; 
}

upstream backend {
    # 사용시 변경 컨테이너이름:포트번호
    server practice-build-spring-boot-1:8080 max_fails=3 fail_timeout=10s;
}

server {
    # listen 80;: 이 설정은 Nginx가 IPv4 주소를 사용하여 80번 포트에서 들어오는 
    # HTTP 요청을 수신하도록 지시합니다. 
    # 80번 포트는 일반적으로 HTTP 트래픽을 처리하는 데 사용됩니다.
    listen 80;
    # listen [::]:80;: 이 설정은 Nginx가 IPv6 주소를 지원하며, 
    # IPv6 주소에서 80번 포트에서 들어오는 HTTP 요청도 수신하도록 지시합니다. 
    listen [::]:80;

    location / {        
        # proxy_pass는 클라이언트의 요청을 이 업스트림 서버로 전달합니다.
        proxy_pass http://frontend;

        # HTTP 버전 1.1을 사용하도록 Nginx에 지시합니다.
        proxy_http_version 1.1;

        # 이 설정은 HTTP 업그레이드 헤더를 설정합니다. 
        # $http_upgrade는 클라이언트의 요청에서 업그레이드 헤더의 값을 가져와서 
        # 백엔드 서버로 전달합니다. 
        # 이렇게 함으로써 웹 소켓과 같은 업그레이드 요청을 올바르게 처리할 수 있습니다.
        proxy_set_header Upgrade $http_upgrade; 

        # Connection 헤더를 'upgrade'로 설정합니다. 
        # 이것은 HTTP 연결을 업그레이드하도록 요청하는 데 사용됩니다.
        proxy_set_header Connection 'upgrade'; 

        # Host 헤더를 클라이언트의 원래 Host 헤더 값으로 설정합니다. 
        # 이것은 요청이 백엔드 서버에 도달할 때 올바른 호스트 이름으로 전달되도록 합니다.
        proxy_set_header Host $host;  
    }

    location /api {        
        # rewrite /api/(.*) /$1 break; 이 설정은 URL에서 
        # /api/ 다음의 모든 것을 가져와서 /로 재작성합니다. 
        #$ 1은 정규식 그룹을 나타내며, /api/ 다음의 경로를 그룹으로 캡처합니다. 
        # 그런 다음 /와 함께 캡처한 경로를 조합하여 URL을 재작성합니다.
            # 예를 들어, 클라이언트가 /api/some/route로 요청하면 
            #이 설정은 해당 요청을 /some/route로 재작성합니다. 
            # 이렇게 하면 실제 요청이 /some/route로 전달되며, 
            # /api/ 부분은 URL에서 제거됩니다.
        rewrite /api/(.*) /$1 break;

        # proxy_pass는 클라이언트의 요청을 이 업스트림 서버로 전달합니다.
        proxy_pass http://backend;

        # HTTP 버전 1.1을 사용하도록 Nginx에 지시합니다.
        proxy_http_version 1.1; 

          # 이 설정은 HTTP 업그레이드 헤더를 설정합니다. 
        # $http_upgrade는 클라이언트의 요청에서 업그레이드 헤더의 값을 가져와서 
        # 백엔드 서버로 전달합니다. 
        # 이렇게 함으로써 웹 소켓과 같은 업그레이드 요청을 올바르게 처리할 수 있습니다.
        proxy_set_header Upgrade $http_upgrade;
        
        # Connection 헤더를 'upgrade'로 설정합니다. 
        # 이것은 HTTP 연결을 업그레이드하도록 요청하는 데 사용됩니다.
        proxy_set_header Connection 'upgrade';

        # proxy_set_header: 이 지시문은 Nginx에서 요청 헤더를 설정하는 데 사용됩니다.
        # Host: 요청 헤더 중 하나로, 클라이언트가 요청한 호스트(도메인)를 나타냅니다.
        # $host: Nginx 내장 변수 중 하나로, 현재 요청을 받은 서버에 대한 호스트 정보를 나타냅니다. 이 값은 클라이언트가 요청한 호스트와 일치하게 설정됩니다.
        proxy_set_header Host $host; 

        # 이 헤더는 모든 Origin(모든 도메인)에서 리소스에 접근을 허용합니다. *는 모든 도메인을 나타냅니다. 
        # 이를 통해 어떤 Origin에서도 해당 리소스에 접근할 수 있게 됩니다.
        add_header 'Access-Control-Allow-Origin' '*';
        # 요청이 인증 정보(예: 쿠키, 인증 토큰)를 포함하고 있을 때 CORS 요청을 허용하는지 여부를 지정합니다. 
        # true로 설정되어 있으므로 인증 정보가 있는 요청도 허용됩니다.
        add_header 'Access-Control-Allow-Credentials' 'true';
        # 허용되는 HTTP 메서드를 지정합니다. 이 경우, GET, POST 및 OPTIONS 메서드가 허용됩니다.
        add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS';
        # 허용되는 HTTP 헤더를 지정합니다. 이 헤더는 다양한 표준 및 사용자 지정 헤더를 허용하며, 다양한 클라이언트 요청을 처리하기 위해 사용됩니다. 
        # 이 예에서는 DNT, X-CustomHeader, Keep-Alive, User-Agent 등 다양한 헤더를 허용하고 있습니다.
        add_header 'Access-Control-Allow-Headers' 'DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type';
    }
}
```



## 5.5 Nginx의 Dockerfile 만들기

```
# docker container의 바탕이 되는 image의 이름이다.
FROM nginx:latest

# 이미지 생성 과정에서 실행할 명령어
# 이미지 생성 과정에서 npm 모듈을 설치가 된다.
# 기본 nginx 설정 파일을 삭제한다. (custom 설정과 충돌 방지)
RUN rm /etc/nginx/conf.d/default.conf

# host pc 의 nginx.conf 를 아래 경로에 복사
# Docker 이미지를 빌드할 때 파일이 컨테이너 내에 복사됩니다.
COPY ./nginx.conf /etc/nginx/conf.d

# 명령은 컨테이너가 시작될 때 실행됩니다.
CMD ["nginx", "-g", "daemon off;"]
```



## 5.6 docker compose 실행

이제 docker-nextjs(front) <-> docker-nginx 와의 연결이 되었습니다.

이제 docker compose 을 실행을 하면

```
# docker compose을 실행 
# container 생성과 container의 실행을한다.
docker compose up

# docker compose을 실행을 하되 변경된 compose service(container) 만 다시 실행됩니다.
docker compose up --build. 

# docker compose을 실행
docker compose start

# docker compose을 중지
docker compose stop

# docker compose을 삭제
docker compose down
```



# 6. Database:mysql : conection [ docker-spring boot 3   <-> docker-mysql ] 

1. **mysql 준비 사항**

2. **backend springboot-3 Dockerfile 파일 설정**

3. ##### **backend(springbok)-nginx Docker-compose.yml 파일 설정**

4. **docker compose 실행**

5. **mysql 오류 해결 spring과 mysql 컨테이너 실행시**



## 6.1 mysql 준비 사항

mysql에서 준비 사항은 없다.

Spring-boot-3 의 jpa를 이용해 table을 생성하기 때문에



1. 하지만 mysql container을 생성할시 초기 table을 생성하고 싶으면

2. *.sql 파일을 준비해야한다.

3. Dockerfile 을 생성할시 COPY을 하고
4. Docker-compose 파일을 생성시 마운트 (연결)을 한다.

**/database/mysql/scripts/create_table.sql**

```
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phoneNum VARCHAR(255) NOT NULL,
    major VARCHAR(255) NOT NULL,
    gpa VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    specialtySkill VARCHAR(255) NOT NULL,
    hobby VARCHAR(255) NOT NULL,
    mbti VARCHAR(255) NOT NULL,
    studentId VARCHAR(255) NOT NULL,
    birthDate VARCHAR(1000) NOT NULL,
    advantages VARCHAR(1000) NOT NULL,
    disadvantage VARCHAR(1000) NOT NULL,
    selfIntroduction VARCHAR(1000) NOT NULL,
    photo VARCHAR(5000) NOT NULL,
    is_ob BOOLEAN NOT NULL,
    year_registration VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    regular_member BOOLEAN NOT NULL
);
```

**/database/mysql/scripts/insert_data.sql**

```
INSERT INTO user (name, phoneNum, major, gpa, address, specialtySkill, hobby, mbti, studentId, birthDate, advantages, disadvantage, selfIntroduction, photo, is_ob, year_registration, email, password, regular_member)
VALUES ('박승찬', '123-456-7890', 'Computer Science', '4.0', '123 Main St', 'Programming', 'Hiking', 'ENTP', 'A12345678', '1996-01-15', 'Quick Learner', 'Impatient', 'I am a software engineer with a passion for coding.', 'john_doe.jpg', 0, '2022', 'seungchan141414@gmail.com', '$2a$10$S.B8dnxTCaWm9YZ0yD/q5e8S9YU4eUZy1Rb.Vap.fKWzmuMtnqDHe', 1);

INSERT INTO user_utill (cnt_vacation, is_nuri_king, name, user_id, is_general_affairs)
VALUES (5, 1, '박승찬', 1, 0);
```



## 6.2 docker-mysql Dockerfile 파일 설정

```
# database/mysql/Dockerfile
# 기반 이미지로 MySQL 공식 이미지 사용
FROM mysql:8.0.34

# 환경 변수 설정
ENV MYSQL_ROOT_PASSWORD=1234
ENV MYSQL_DATABASE=nct-db
ENV MYSQL_ROOT_HOST=root
ENV TZ=Asia/Seoul

# 초기 디비 구성 
COPY ./scripts/ /docker-entrypoint-initdb.d/

# 테이블 이름 소문자로 설정

CMD ["mysqld", "--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci"]

# 컨테이너 포트 설정
EXPOSE 3306
```



## 6.3 docker-mysql Docker-compose.yml 파일 설정

```
# practice-build/docker-compose.yml
# /docker-compose.yml
# docker-compose의 버전
version: '3'
# 서비스의 정의 서비스의 이름은 각자 정할수 있습니다.
# docker-compose 가 관리해야되는 서비스(container)들을 정의
services:
  # nextjs의 서비스 정의
  nextjs:
    build: ./frontend
    #컨테이너 다운 시 재시작하라는 명령어
    restart: always
    # 불룸 로컬과 도커 마운트 설정
    # 로컬 폴더와 컨터이너안의 임이의 폴더와 마운트
    volumes:
      - ./frontend:/home/node/next
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 3000번 포트와 컨테이너의 3000번 포트를 매핑합니다. 
    ports:
      - "3000:3000"

  spring-boot-3:
    # Dockerfile이 있는 위치
    build: ./backend/spring-boot-3
    #컨테이너 다운 시 재시작하라는 명령어
    restart: always
    # 불룸 로컬과 도커 마운트 설정
    # 로컬 폴더와 컨터이너안의 임이의 폴더와 마운트
    volumes:
      - ./backend/spring-boot-3/build/libs:/home/spring-boot-3/build
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 8080번 포트와 컨테이너의 8080번 포트를 매핑합니다. 
    ports:
      - "8080:8080"
    # mysql서비스가 먼저 시작하고 그 뒤에 spring-boot-3 가 시작된다.
    depends_on:
     - mysql
    # mysql 컨테이너(서비스)와 spring-boot-3 컨테이너(서비스)와의 연결을 하기위한 환경설정
    # spring-boot-3의 db 설정보다 우선순위가 높다 
    environment:
      # 만약 다른 컨테이너(서비스)와의 db을 연결하고 싶으면 원하는 컨테이너의 이름을 적어라
      # SPRING_DATASOURCE_URL: jdbc:mysql://원하는db컨테이너이름:3306/nct-db?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_URL: jdbc:mysql://practice-build-mysql-1:3306/nct-db?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: "root"
      SPRING_DATASOURCE_PASSWORD: "1234"

  nginx:
    # Dockerfile이 있는 위치
    build: ./nginx
    #컨테이너 다운 시 재시작하라는 명령어
    restart: always
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 80번 포트와 컨테이너의 80번 포트를 매핑합니다.
    ports:
      - "80:80"
    # depends_on은 설정 서비스 명이 nextjs, pring-boot-3 이 먼저 실행되고 nginx의 서비스가 먼저 실행된다.
    depends_on:
      - nextjs
      - spring-boot-3

  mysql:   
    # Dockerfile이 있는 위치
    build: ./database/mysql
    # build: ./database/mysql
    restart: always 
    # 연결할 외부 디렉토리 : 컨테이너 내 디렉토리
    #  호스트 머신의 3306번 포트와 컨테이너의 33060번 포트를 매핑합니다. 
    ports: 
      - "3306:3306"
    # 테이블 이름 소문자로 맞춘다.
    volumes:
      - ./database/mysql/scripts:/docker-entrypoint-initdb.d
```

## 6-4 docker compose 실행

이제 docker-nextjs(front) <-> docker-nginx 와의 연결이 되었습니다.

이제 docker compose 을 실행을 하면

```
# docker compose을 실행 
# container 생성과 container의 실행을한다.
docker compose up

# docker compose을 실행을 하되 변경된 compose service(container) 만 다시 실행됩니다.
docker compose up --build. 

# docker compose을 실행
docker compose start

# docker compose을 중지
docker compose stop

# docker compose을 삭제
docker compose down
```



## 6-4 mysql 오류 해결 spring과 mysql 컨테이너 실행시

1. 

```
practice-build-spring-boot-1  | java.sql.SQLException: null,  message from server: "Host '192.168.32.4' is not allowed to connect to this MySQL server" (1)
```

오류 발생 해결

**1- 터미널에서 bash를 사용하여 MySQL 실행 컨테이너에 연결합니다.**

```
docker exec -it <your-container-id-or-name> bash
```

**2- 컨테이너에서 MySQL 데이터베이스에 연결합니다.**

```
mysql -u your_user -p
```

비밀번호를 묻는 메시지가 나타나면 비밀번호를 입력하고 Enter 키를 눌러야 합니다.

**DB 접속 후 현재 설정 확인.**

------

```java
select Host,User,plugin,authentication_string FROM mysql.user;
```

**원격 유저 생성**

```python
**create user 'root'@'%' identified by '비밀번호';
create user 'root'@'%' identified by '1234';**
```

**모든 IP 허용**

------

```java
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%'
```

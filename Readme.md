# 배포 과정 정리

**practice docker compose build : https://github.com/dmdrk1414/docker-practice-build**

**practice docker compose : https://github.com/dmdrk1414?tab=repositories**

# 🗒️ 목차

## [1. 폴더 구조 (중요하니 꼼꼼하게 보세요.)](#1-폴더-구조)

## [2. 배포 구상도 (전체적인 그림)](#2-배포-구상도-(전체적인 그림))

## [3. Docker-compose 명령어](#3-docker-compose-명령어)

## [4. frontend : conection [ docker-nginx   <-> docker-next.js ] include docker compose](#4-Frontend:nextjs-:-conection-[-docker-nginx-<->-docker-next.js-]-include-docker-compose)

## [5. backend : conection [ docker-nginx   <-> docker-spring boot 3 ] include docker compose](#5-backend-:-conection-[-docker-nginx-<->-docker-spring-boot-3-]-include-docker-compose)

## [6. Database:mysql : conection [ docker-spring boot 3   <-> docker-mysql] ](#6-Database:mysql-:-conection-[-docker-spring-boot-3-<->-docker-mysql] )



# 1. 폴더 구조

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



# 2. 배포 구상도 (전체적인 그림)

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

# 4. Frontend:nextjs : conection [ docker-nginx <-> docker-next.js ] include docker compose

1. **준비 사항**
2. **frontend Dockerfile 파일 설정**
3. **Docker-compose.yml 파일 설정**
4. **Nginx의 config 파일 만들기**
5. **Nginx의 Dockerfile 만들기**

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



# 5. backend : conection [ docker-nginx <-> docker-spring boot 3 ] include docker compose

1. **준비 사항**
2. **Dockerfile 파일 설정**
3. **Docker-compose.yml 파일 설정**
4. **환경 설정**



# 6. Database:mysql : conection [ docker-spring boot 3 <-> docker-mysql] 

1. **준비 사항**
2. **Dockerfile 파일 설정**
3. **Docker-compose.yml 파일 설정**
4. **환경 설정**

### 1. 준비 사항

### 2. Dockerfile 파일 설정

### 3. Docker-compose.yml 파일 설정

1. Docker-compose.yml 파일 수정하기

### 4. 환경 설정

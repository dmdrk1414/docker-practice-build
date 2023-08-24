FROM nginx:latest

# 이미지 생성 과정에서 실행할 명령어
# 이미지 생성 과정에서 npm 모듈을 설치가 된다.
# 기본 nginx 설정 파일을 삭제한다. (custom 설정과 충돌 방지)
RUN rm /etc/nginx/conf.d/default.conf

# host pc 의 nginx.conf 를 아래 경로에 복사
COPY ./nginx.conf /etc/nginx/conf.d

CMD ["nginx", "-g", "daemon off;"]
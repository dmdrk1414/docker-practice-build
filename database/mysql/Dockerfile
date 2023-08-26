
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
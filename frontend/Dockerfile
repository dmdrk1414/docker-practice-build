FROM node:16.18.0-alpine

# 이미지 내에서 명령어를 실행할(현 위치로 잡을) 디렉토리 설정
WORKDIR /home/node/next

# Copy the rest of the application files to the container
COPY . .

# Build the production version of the app
RUN npm run build

COPY . .


CMD [ "npm" ,  "start"]
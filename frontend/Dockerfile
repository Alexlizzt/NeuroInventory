FROM node:24-alpine

WORKDIR /app

ENV NG_CLI_ANALYTICS=false

COPY package*.json ./

RUN npm install --legacy-peer-deps

COPY . .

EXPOSE 4200

CMD ["npm", "start", "--", "--host", "0.0.0.0", "--poll", "2000"]

FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# runtime stage
FROM nginx:stable-alpine
# adjust the path below if your framework outputs to 'dist' or another folder
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
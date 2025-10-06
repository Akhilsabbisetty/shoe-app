# Build stage
FROM node:18-alpine3.21 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# This creates the 'dist' folder
RUN npm run build

# Runtime stage
FROM nginx:stable-alpine
# Remove default nginx content
RUN rm -rf /usr/share/nginx/html/*
# Copy the actual build output
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

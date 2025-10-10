# Stage 1: Build the React app
FROM node:18 AS build

WORKDIR /app

# Copy package.json and package-lock.json first for caching
COPY package*.json ./

RUN npm ci

# Copy the rest of the app (including src & public)
COPY . .

# Build production files
RUN npm run build

# Stage 2: Use nginx to serve the static build
FROM nginx:alpine

# Copy build output to nginx html directory
COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]


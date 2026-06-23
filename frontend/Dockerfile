# syntax=docker/dockerfile:1
FROM public.ecr.aws/docker/library/node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci

COPY . .
RUN find . -type f -name "*.sh" -exec sed -i 's/\r$//' {} \;

ARG VITE_API_URL=/srv-gerenciaracademia
ENV VITE_API_URL=$VITE_API_URL

RUN npm run build

FROM public.ecr.aws/docker/library/nginx:alpine

RUN rm -rf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

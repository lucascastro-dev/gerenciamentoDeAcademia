FROM node:20-alpine AS build

WORKDIR /app

# Copia dependências primeiro (melhor cache)
COPY package*.json ./

# Instala dependências
RUN npm ci

# Copia restante do projeto
COPY . .

# Corrige possíveis problemas de quebra de linha
RUN find . -type f -name "*.sh" -exec sed -i 's/\r$//' {} \;

# Variável da API
ARG VITE_API_URL=/srv-gerenciaracademia
ENV VITE_API_URL=$VITE_API_URL

# Mostra variáveis no log
RUN echo "VITE_API_URL=$VITE_API_URL"

# Build detalhado
RUN npm run build -- --debug

# =====================================================

FROM nginx:alpine

# Remove config default
RUN rm -rf /etc/nginx/conf.d/default.conf

# Copia frontend buildado
COPY --from=build /app/dist /usr/share/nginx/html

# Copia nginx customizado
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
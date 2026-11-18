FROM node:22-slim
WORKDIR /srv
COPY app ./app
COPY backend/package*.json ./backend/
RUN cd backend && npm ci --omit=dev
COPY backend ./backend
ENV NODE_ENV=production PORT=3000 DB_FILE=/data/safarna.db
VOLUME /data
USER node
EXPOSE 3000
WORKDIR /srv/backend
CMD ["node","--disable-warning=ExperimentalWarning","src/server.js"]
